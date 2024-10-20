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
	
	package uplx.server.http;
	
	import java.io.IOException;
	import java.io.InterruptedIOException;
	import java.io.OutputStream;
	import java.util.Arrays;
	import java.util.Iterator;
	import java.util.List;
	import javax.net.ssl.SSLSocket;
	import upl.http.HttpMethod;
	import upl.http.HttpStatus;
	import upl.http.HttpVersion;
	import upl.io.BufferedInputStream;
	import upl.net.Method;
	import uplx.server.Server;
	import uplx.server.ServerException;
	import uplx.server.ServerThread;
	
	/**
	 * The {@code HttpServerThread} handles accepted sockets.
	 */
	public class HttpServerThread extends ServerThread<Request, Response> {
		
		protected RewriteRule rule;
		
		public HttpServerThread getInstance () {
			return new HttpServerThread ();
		}
		
		protected boolean openedConnection () throws ServerException {
			
			// RFC7230#6.6: persist connection unless client or server close explicitly (or legacy client)
			
			/*return
				!request.getHeaders ().get ("Connection").equalsIgnoreCase ("close") &&
				!response.getHeaders ().get ("Connection").equalsIgnoreCase ("close")
			;*/
			
			return false;
			
		}
		
		protected boolean serverError (Throwable t, OutputStream out) {
			
			if (request == null) { // error reading request
				
				if (t instanceof IOException && t.getMessage ().contains ("missing request line"))
					return true; // we're not in the middle of a transaction - so just disconnect
				
				response.getHeaders ().add ("Connection", "close"); // about to close connection
				
				if (t instanceof InterruptedIOException) // e.g. SocketTimeoutException
					response.setStatus (HttpStatus.CLIENT_ERROR_REQUEST_TIMEOUT).sendError ("Timeout waiting for client request");
				else
					response.setStatus (HttpStatus.CLIENT_ERROR_BAD_REQUEST).sendError (t + " " + upl.core.Arrays.implode ("<br/>", t.getStackTrace ()));
				
			} else {
				
				response = new Response (); // ignore whatever headers may have already been set
				
				response.server = server;
				response.out = out;
				
				response.getHeaders ().add ("Connection", "close"); // about to close connection
				response.setStatus (HttpStatus.SERVER_ERROR_INTERNAL).sendError (t + " " + upl.core.Arrays.implode ("<br/>", t.getStackTrace ()));
				
			} // Otherwise, just abort the connection since we can't recover
			
			return true;
			
		}
		
		/**
     * Handles a single transaction on a connection performing various validation checks
     * and required special header handling, possibly returning an
     * appropriate response.
     *
     * @throws ServerException if an error occurs
     */
		protected boolean preprocessTransaction () throws IOException {
			
			if (request.getVersion ().equals (HttpVersion.HTTP_11)) {
				
				if (!request.getHeaders ().contains ("Host")) {
					
					response.setStatus (HttpStatus.CLIENT_ERROR_BAD_REQUEST).sendError ("Missing required Host header"); // RFC2616#14.23: missing Host header gets 400
					
					return false;
					
				}
				
				String expect = request.getHeaders ().get ("Expect"); // return continue response before reading body
				
				if (expect != null) {
					
					if (expect.equalsIgnoreCase ("100-continue")) {
						
						Response tempResp = new Response ();
						
						tempResp.server = server;
						tempResp.out = response.getOutputStream ();
						
						tempResp.setStatus (HttpStatus.INFO_CONTINUE).sendHeaders ();
						
						response.getOutputStream ().flush ();
						
					} else {
						
						response.setStatus (HttpStatus.CLIENT_ERROR_EXPECTATION_FAILED).sendError (); // RFC2616#14.20: if unknown expect, send 417
						
						return false;
						
					}
					
				}
				
			} else if (request.getVersion ().equals (HttpVersion.HTTP_10) || request.getVersion ().equals (HttpVersion.HTTP_09)) {
				
				for (String token : Server.split (request.getHeaders ().get ("Connection"), false))
					request.getHeaders ().remove (token); // RFC2616#14.10 - remove connection headers from older versions
				
			} else {
				
				response.setStatus (HttpStatus.SERVER_ERROR_VERSION_NOT_SUPPORTED).sendError ();
				
				return false;
				
			}
			
			return true;
			
		}
		
		/**
     * Handles communications for a single connection over the given streams.
     * Multiple subsequent transactions are handled on the connection,
     * until the streams are closed, an error occurs, or the request
     * contains a "Connection: close" header which explicitly requests
     * the connection be closed after the transaction ends.
     *
     * @param in	the stream from which the incoming requests are read
     * @param out the stream into which the outgoing responses are written
     * @throws ServerException if an error occurs
     */
		@Override
		protected void handleConnection (BufferedInputStream in, OutputStream out) throws ServerException, IOException {
			
			try {
				
				do {
					
					try {
						
						// create request and response and handle transaction
						
						request = request.getInstance ();
						
						request.server = server;
						request.in = in;
						
						request.readHeaders ();
						
						response = response.getInstance ();
						
						response.server = server;
						response.out = out;
						
						response.setClientCapabilities (request);
						
						handleMethod ();
						
					} catch (Throwable t) { // unhandled errors (not normal error responses like 404)
						if (serverError (t, out)) break; // proceed to close connection
					} finally {
						response.close (); // close response and flush output
					}
					
					if (response != null && request != null)
						response.transfer (request.getBody (), null, -1); // consume any leftover body data so next request can be processed
					
					out.flush ();
					
				} while (openedConnection ());
				
			} finally {
				
				// RFC7230#6.6 - close socket gracefully (except SSL socket which doesn't support half-closing)
				if (!(socket instanceof SSLSocket)) {
					
					socket.shutdownOutput (); // half-close socket (only output)
					response.transfer (socket.getInputStream (), null, -1); // consume input
					
				}
				
			}
			
		}
		
		/**
     * Handles a transaction according to the request method.
     */
		protected void handleMethod () throws IOException {
			
			if (preprocessTransaction ()) {
				
				response.setStatus (HttpStatus.SUCCESS_OK); // By default HTTP status is 200 OK, it can be changed later if it needed
				
				server.request = request;
				server.response = response;
				
				rule = (RewriteRule) server.router.getRule ();
				
				if (rule != null) {
					
					List<Method> methods = Arrays.asList (rule.getMethods ());
					List<Method> bMethods = methods; // PARSER var1 = var2 = 0;
					
					if (request.getMethod ().equals (HttpMethod.GET) || methods.contains (request.getMethod ())) // RFC 2616#5.1.1 - GET and HEAD must be supported
						serveMethod (); // method is handled by context handler (or 404)
					else if (request.getMethod ().equals (HttpMethod.HEAD)) { // default HEAD handler
						
						request.setMethod (HttpMethod.GET); // identical to a GET
						
						response.setDiscardBody (true); // process normally but discard body
						
						serveMethod ();
						
					} else if (request.getMethod ().equals (HttpMethod.TRACE)) // default TRACE handler
						handleTrace ();
					else {
						
						// built-in methods
						
						bMethods.add (HttpMethod.GET);
						bMethods.add (HttpMethod.HEAD);
						bMethods.add (HttpMethod.TRACE);
						bMethods.add (HttpMethod.OPTIONS);
						
						// "*" is a special server-wide (no-context) request supported by OPTIONS
						
						boolean isServerOptions = request.getURI ().getPath ().equals ("*") && request.getMethod ().equals (HttpMethod.OPTIONS);
						
						response.getHeaders ().add ("Allow", join (", ", isServerOptions ? methods : bMethods));
						
						if (request.getMethod ().equals (HttpMethod.OPTIONS)) { // default OPTIONS handler
							
							response.getHeaders ().add ("Content-Length", 0); // RFC2616#9.2
							response.sendHeaders ();
							
						} else if (bMethods.contains (request.getMethod ())) // supported by server, but not this context (nor built-in)
							response.setStatus (HttpStatus.CLIENT_ERROR_METHOD_NOT_ALLOWED).sendHeaders ();
						else // unsupported method
							response.setStatus (HttpStatus.SERVER_ERROR_NOT_IMPLEMENTED).sendError ();
						
					}
					
				} else response.setStatus (HttpStatus.CLIENT_ERROR_NOT_FOUND).sendError ();
				
			}
			
		}
		
		/**
		 * Returns a string constructed by joining the string representations of the
		 * iterated objects (in order), with the delimiter inserted between them.
		 *
		 * @param delim the delimiter that is inserted between the joined strings
		 * @param items the items whose string representations are joined
		 * @param <T>	 the item type
		 * @return the joined string
		 */
		public <T> String join (String delim, Iterable<T> items) {
			
			StringBuilder sb = new StringBuilder ();
			
			for (Iterator<T> it = items.iterator (); it.hasNext ();)
				sb.append (it.next ()).append (it.hasNext () ? delim : "");
			
			return sb.toString ();
			
		}
		
		/**
     * Handles a TRACE method request.
     *
     * @throws ServerException if an error occurs
     */
		public void handleTrace () throws IOException {
			
			response.sendHeaders (-1, -1, null, "message/http", null);
			
			OutputStream out = response.getBody ();
			
			out.write (Server.getBytes (HttpMethod.TRACE.name (), request.getURI ().toString (), " ", request.getVersion ().toString ()));
			out.write (Server.CRLF);
			
			request.getHeaders ().writeTo (out);
			
			response.transfer (request.getBody (), out, -1);
			
		}
		
		/**
     * Serves the content for a request by invoking the context
     * handler for the requested context (path) and HTTP method.
     *
     * @throws ServerException if an error occurs
     */
		protected void serveMethod () throws ServerException {
			
			if (rule.handler == null) {
				
				response
					.setStatus (HttpStatus.CLIENT_ERROR_NOT_FOUND)
					.sendError ("Proper handler not found " + rule); // TODO
				
			} else {
				
				//server.getVirtualHost (null).addContext (rule.regex, rule.handler, rule.methods); // TODO
				
				/*String path = request.getURI ().getPath (); // add directory index if necessary
				
				if (path.endsWith ("/")) {
					
					String index = rule.handler.getDirectoryIndex ();
					
					if (!index.equals ("")) {
						
						request.setPath (path + index);
						
						rule.handler.serve (app, request, response);
						
						request.setPath (path);
						
					}
					
				} else */
				
				HttpValidator validator = (HttpValidator) rule.handler.getValidator ();
				
				if (validator != null) {
					
					if (request.getMethod ().equals (HttpMethod.GET))
						validator.get (app, request, response);
					else if (request.getMethod ().equals (HttpMethod.POST))
						validator.post (app, request, response);
					else if (request.getMethod ().equals (HttpMethod.PUT))
						validator.put (app, request, response);
					else if (request.getMethod ().equals (HttpMethod.DELETE))
						validator.delete (app, request, response);
					
					validator.process ();
					
				}
				
				rule.handler.serve (app, request, response);
				
			}
			
		}
		
	}