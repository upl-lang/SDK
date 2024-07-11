  /*
    * Copyright (c) 2020 - 2024 UPL Foundation
    *
    * Licensed under the Apache License, Version 2.0 (the "License");
    * you may not use this file except in compliance with the License.
    * You may obtain a copy of the License at
    *
    *     http://www.apache.org/licenses/LICENSE-2.0
    *
    * Unless required by applicable law or agreed to in writing, software
    * distributed under the License is distributed on an "AS IS" BASIS,
    * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    * See the License for the specific language governing permissions and
    * limitations under the License.
    */
  
  package uplx.server;
  
  import java.io.BufferedInputStream;
  import java.io.BufferedOutputStream;
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.InterruptedIOException;
  import java.io.OutputStream;
  import java.net.Socket;
  import java.util.Arrays;
  import java.util.Iterator;
  import java.util.LinkedHashSet;
  import java.util.Map;
  import java.util.Set;
  import javax.net.ssl.SSLSocket;
  import upl.core.Log;
  import upl.http.HttpMethod;
  import upl.http.HttpStatus;
  import upl.http.HttpVersion;
  import uplx.server.router.RewriteRule;
  
  /**
    * The {@code SocketThread} handles accepted sockets.
    */
  class SocketThread extends Thread {
    
    public Server server;
    
    public SocketThread (Server server) {
      this.server = server;
    }
    
    @Override
    public void run () {
      
      setName (getClass ().getSimpleName () + "-" + server.getPort ());
      
      try {
        
        while (!server.isClosed ()) {
          
          Socket socket = server.accept ();
          
          socket.setTcpNoDelay (true);
          
          server.executor.execute (new Runnable () {
            
            @Override
            public void run () {
              
              try {
                
                try {
                  
                  socket.setSoTimeout (server.socketTimeout);
                  socket.setTcpNoDelay (true); // we buffer anyway, so improve latency
                  
                  OutputStream out = new BufferedOutputStream (socket.getOutputStream ());
                  
                  handleConnection (socket.getInputStream (), out);
                  
                  out.flush ();
                  
                } finally {
                  
                  try (socket) {
                    
                    // RFC7230#6.6 - close socket gracefully
                    // (except SSL socket which doesn't support half-closing)
                    if (!(socket instanceof SSLSocket)) {
                      
                      socket.shutdownOutput (); // half-close socket (only output)
                      Response.transfer (socket.getInputStream (), null, -1); // consume input
                      
                    }
                    
                  }
                  
                  // and finally close socket fully
                  
                }
                
              } catch (IOException ignore) {
              }
              
            }
            
          });
          
        }
        
      } catch (IOException ignore) {
      }
      
    }
    
    protected Request request;
    protected Response response;
    
    /**
      * Handles communications for a single connection over the given streams.
      * Multiple subsequent transactions are handled on the connection,
      * until the streams are closed, an error occurs, or the request
      * contains a "Connection: close" header which explicitly requests
      * the connection be closed after the transaction ends.
      *
      * @param in  the stream from which the incoming requests are read
      * @param out the stream into which the outgoing responses are written
      * @throws ServerException if an error occurs
      */
    protected void handleConnection (InputStream in, OutputStream out) throws ServerException, IOException {
      
      in = new BufferedInputStream (in, 4096);
      out = new BufferedOutputStream (out, 4096);
      
      do {
        
        // create request and response and handle transaction
        
        response = new Response (server, out);
        
        try {
          
          request = new Request (server, in);
          handleTransaction ();
          
        } catch (Throwable t) { // unhandled errors (not normal error responses like 404)
          
          if (request == null) { // error reading request
            
            if (t instanceof IOException && t.getMessage ().contains ("missing request line"))
              break; // we're not in the middle of a transaction - so just disconnect
            
            response.getHeaders ().add ("Connection", "close"); // about to close connection
            
            if (t instanceof InterruptedIOException) // e.g. SocketTimeoutException
              response.setStatus (HttpStatus.CLIENT_ERROR_REQUEST_TIMEOUT).sendError ("Timeout waiting for client request");
            else
              response.setStatus (HttpStatus.CLIENT_ERROR_BAD_REQUEST).sendError (t + " " + upl.core.Arrays.implode ("<br/>", t.getStackTrace ()));
            
          } else if (!response.headersSent ()) { // if headers were not already sent, we can send an error response
            
            response = new Response (server, out); // ignore whatever headers may have already been set
            
            response.getHeaders ().add ("Connection", "close"); // about to close connection
            response.setStatus (HttpStatus.SERVER_ERROR_INTERNAL).sendError (t + " " + upl.core.Arrays.implode ("<br/>", t.getStackTrace ()));
            
          } // Otherwise, just abort the connection since we can't recover
          
          break; // proceed to close connection
          
        } finally {
          response.close (); // close response and flush output
        }
        
        // consume any leftover body data so next request can be processed
        Response.transfer (request.getBody (), null, -1);
        
        // RFC7230#6.6: persist connection unless client or server close explicitly (or legacy client)
        
      } while (
        !"close".equalsIgnoreCase (request.getHeaders ().get ("Connection")) &&
          !"close".equalsIgnoreCase (response.getHeaders ().get ("Connection")) &&
          request.getVersion ().equals (HttpVersion.HTTP_11)
      );
      
    }
    
    /**
      * Handles a single transaction on a connection.
      * <p>
      * Subclasses can override this method to perform filtering on the
      * request or response, apply wrappers to them, or further customize
      * the transaction processing in some other way.
      *
      * @throws ServerException if and error occurs
      */
    protected void handleTransaction () throws IOException, ServerException {
      
      response.setClientCapabilities (request);
      
      if (preprocessTransaction ())
        handleMethod ();
      
    }
    
    /**
      * Preprocesses a transaction, performing various validation checks
      * and required special header handling, possibly returning an
      * appropriate response.
      *
      * @return whether further processing should be performed on the transaction
      * @throws ServerException if an error occurs
      */
    protected boolean preprocessTransaction () throws IOException, ServerException {
      
      // validate request
      
      HttpVersion version = request.getVersion ();
      
      if (version.equals (HttpVersion.HTTP_11)) {
        
        if (!request.getHeaders ().contains ("Host")) {
          
          response.setStatus (HttpStatus.CLIENT_ERROR_BAD_REQUEST).sendError ("Missing required Host header"); // RFC2616#14.23: missing Host header gets 400
          
          return false;
          
        }
        
        // return continue response before reading body
        
        String expect = request.getHeaders ().get ("Expect");
        
        if (expect != null) {
          
          if (expect.equalsIgnoreCase ("100-continue")) {
            
            Response tempResp = new Response (server, response.getOutputStream ());
            
            tempResp.setStatus (HttpStatus.INFO_CONTINUE);
            tempResp.sendHeaders ();
            
            response.getOutputStream ().flush ();
            
          } else {
            
            response.setStatus (HttpStatus.CLIENT_ERROR_EXPECTATION_FAILED).sendError (); // RFC2616#14.20: if unknown expect, send 417
            
            return false;
            
          }
          
        }
        
      } else if (version.equals (HttpVersion.HTTP_10) || version.equals (HttpVersion.HTTP_09)) {
        
        for (String token : server.split (request.getHeaders ().get ("Connection"), false))
          request.getHeaders ().remove (token); // RFC2616#14.10 - remove connection headers from older versions
        
      } else {
        
        response.setStatus (HttpStatus.CLIENT_ERROR_BAD_REQUEST).sendError ("Unknown version: " + version);
        return false;
        
      }
      
      return true;
      
    }
    
    /**
      * Handles a transaction according to the request method.
      *
      * @throws ServerException if and error occurs
      */
    protected void handleMethod () throws IOException, ServerException {
      
      HttpMethod method = request.getMethod ();
      
      Map<HttpMethod, Handler> handlers = request.getContext ().getHandlers ();
      
      if (method.equals (HttpMethod.GET) || handlers.containsKey (method)) // RFC 2616#5.1.1 - GET and HEAD must be supported
        serveMethod (); // method is handled by context handler (or 404)
      else if (method.equals (HttpMethod.HEAD)) { // default HEAD handler
        
        request.setMethod (HttpMethod.GET); // identical to a GET
        
        response.setDiscardBody (true); // process normally but discard body
        
        serveMethod ();
        
      } else if (method.equals (HttpMethod.TRACE)) // default TRACE handler
        handleTrace ();
      else {
        
        Set<HttpMethod> methods = new LinkedHashSet<> (Arrays.asList (HttpMethod.GET, HttpMethod.HEAD, HttpMethod.TRACE, HttpMethod.OPTIONS)); // built-in methods
        
        // "*" is a special server-wide (no-context) request supported by OPTIONS
        
        boolean isServerOptions = request.getPath ().equals ("*") && method.equals (HttpMethod.OPTIONS);
        
        methods.addAll (isServerOptions ? request.getVirtualHost ().getMethods () : handlers.keySet ());
        
        response.getHeaders ().add ("Allow", join (", ", methods));
        
        if (method.equals (HttpMethod.OPTIONS)) { // default OPTIONS handler
          
          response.getHeaders ().add ("Content-Length", 0); // RFC2616#9.2
          response.sendHeaders ();
          
        } else if (request.getVirtualHost ().getMethods ().contains (method)) // supported by server, but not this context (nor built-in)
          response.setStatus (HttpStatus.CLIENT_ERROR_METHOD_NOT_ALLOWED).sendHeaders ();
        else // unsupported method
          response.setStatus (HttpStatus.SERVER_ERROR_NOT_IMPLEMENTED).sendError ();
        
      }
      
    }
    
    /**
      * Returns a string constructed by joining the string representations of the
      * iterated objects (in order), with the delimiter inserted between them.
      *
      * @param delim the delimiter that is inserted between the joined strings
      * @param items the items whose string representations are joined
      * @param <T>   the item type
      * @return the joined string
      */
    public <T> String join (String delim, Iterable<T> items) {
      
      StringBuilder sb = new StringBuilder ();
      
      for (Iterator<T> it = items.iterator (); it.hasNext (); )
        sb.append (it.next ()).append (it.hasNext () ? delim : "");
      
      return sb.toString ();
      
    }
    
    /**
      * Handles a TRACE method request.
      *
      * @throws ServerException if an error occurs
      */
    public void handleTrace () throws IOException, ServerException {
      
      response.sendHeaders (-1, -1, null, "message/http", null);
      
      OutputStream out = response.getBody ();
      
      out.write (server.getBytes (HttpMethod.TRACE.name (), request.getURI ().toString (), " ", request.getVersion ().toString ()));
      out.write (Server.CRLF);
      
      request.getHeaders ().writeTo (out);
      
      Response.transfer (request.getBody (), out, -1);
      
    }
    
    /**
      * Serves the content for a request by invoking the context
      * handler for the requested context (path) and HTTP method.
      *
      * @throws ServerException if an error occurs
      */
    protected void serveMethod () throws ServerException {
      
      RewriteRule rule = server.router.process (request, response);
      
      if (rule != null) {
        
        request.matcher = rule.matcher;
        
        // get context handler to handle request
        //Handler handler = request.getContext ().getHandlers ().get (request.getMethod ());
        
        if (rule.handler == null) {
          
          response
            .setStatus (HttpStatus.CLIENT_ERROR_NOT_FOUND)
            .sendError ("Proper handler not found " + server.getVirtualHost (null).values);
          
          return;
          
        }
        
        //server.getVirtualHost (null).addContext (rule.regex, rule.handler, rule.methods); // TODO
        
        String path = request.getPath (); // add directory index if necessary
        
        if (path.endsWith ("/")) {
          
          String index = rule.handler.getDirectoryIndex ();
          
          if (!index.equals ("")) {
            
            request.setPath (path + index);
            
            rule.handler.serve (request, response);
            
            request.setPath (path);
            
          }
          
        }
        
        if (response.getStatus ().isSuccess ())
          rule.handler.serve (request, response);
        
      }
      
      if (!response.getStatus ().isSuccess ())
        response.sendError ("Page not found");
      
    }
    
  }