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
  
  import java.io.Closeable;
  import java.io.FilterOutputStream;
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.OutputStream;
  import java.net.URI;
  import java.net.URISyntaxException;
  import java.nio.charset.Charset;
  import java.nio.charset.StandardCharsets;
  import java.util.Calendar;
  import java.util.GregorianCalendar;
  import java.util.Locale;
  import java.util.zip.DeflaterOutputStream;
  import java.util.zip.GZIPOutputStream;
  import upl.core.Arrays;
  import upl.http.HttpVersion;
  import upl.json.JSONObject;
  import upl.type.StringTemplate;
  import upl.util.List;
  import upl.util.ArrayList;
  import upl.http.HttpStatus;
  import upl.http.Status;
  import uplx.server.io.ChunkedOutputStream;
  
  /**
   * The {@code Response} class encapsulates a single HTTP response.
   */
  public class Response implements Closeable {
    
    protected OutputStream out; // the underlying output stream
    protected OutputStream[] encoders = new OutputStream[4]; // chained encoder streams
    protected Headers headers;
    protected boolean discardBody;
    protected int state; // nothing sent, headers sent, or closed
    protected Request request; // request used in determining client capabilities
    private Status status = HttpStatus.SUCCESS_OK;
    
    /**
     * The MIME types that can be compressed (prefix/suffix wildcards allowed).
     */
    public List<String> compressibleContentTypes = new ArrayList<> ();
    
    private final Server server;
    
    /**
     * Date format strings.
     */
    protected final char[]
      DAYS = "Sun Mon Tue Wed Thu Fri Sat".toCharArray (),
      MONTHS = "Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec".toCharArray ();
    
    /**
     * Constructs a Response whose output is written to the given stream.
     *
     * @param out the stream to which the response is written
     */
    public Response (Server server, OutputStream out) {
      
      this.server = server;
      
      this.out = out;
      this.headers = new Headers (server);
      
      compressibleContentTypes.put ("text/*");
      compressibleContentTypes.put ("*/javascript");
      compressibleContentTypes.put ("*icon");
      compressibleContentTypes.put ("*+xml");
      compressibleContentTypes.put ("*/json");
      
    }
    
    /**
     * Sets whether this response's body is discarded or sent.
     *
     * @param discardBody specifies whether the body is discarded or not
     */
    public void setDiscardBody (boolean discardBody) {
      this.discardBody = discardBody;
    }
    
    /**
     * Sets the request which is used in determining the capabilities
     * supported by the client (e.g. compression, encoding, etc.)
     *
     * @param request the request
     */
    public void setClientCapabilities (Request request) {
      this.request = request;
    }
    
    /**
     * Returns the request headers collection.
     *
     * @return the request headers collection
     */
    public Headers getHeaders () {
      return headers;
    }
    
    /**
     * Returns the underlying output stream to which the response is written.
     * Except for special cases, you should use {@link #getBody()} instead.
     *
     * @return the underlying output stream to which the response is written
     */
    public OutputStream getOutputStream () {
      return out;
    }
    
    /**
     * Returns whether the response headers were already sent.
     *
     * @return whether the response headers were already sent
     */
    public boolean headersSent () {
      return state == 1;
    }
    
    /**
     * Returns an output stream into which the response body can be written.
     * The stream applies encodings (e.g. compression) according to the sent headers.
     * This method must be called after response headers have been sent
     * that indicate there is a body. Normally, the content should be
     * prepared (not sent) even before the headers are sent, so that any
     * errors during processing can be caught and a proper error response returned -
     * after the headers are sent, it's too late to change the status into an error.
     *
     * @return an output stream into which the response body can be written,
     * or null if the body should not be written (e.g. it is discarded)
     * @throws ServerException if an error occurs
     */
    public OutputStream getBody () throws ServerException {
      
      try {
        
        if (encoders[0] != null || discardBody)
          return encoders[0]; // return the existing stream (or null)
        
        // set up chain of encoding streams according to headers
        List<String> te = server.split (headers.get ("Transfer-Encoding"), true);
        List<String> ce = server.split (headers.get ("Content-Encoding"), true);
        
        int i = encoders.length - 1;
        
        encoders[i] = new FilterOutputStream (out) {
          
          @Override
          public void close () {
          } // keep underlying connection stream open for now
          
          @Override // override the very inefficient default implementation
          public void write (byte[] b, int off, int len) throws IOException {
            out.write (b, off, len);
          }
          
        };
        
        if (te.contains ("chunked"))
          encoders[--i] = new ChunkedOutputStream (server, encoders[i + 1]);
        if (ce.contains ("gzip") || te.contains ("gzip"))
          encoders[--i] = new GZIPOutputStream (encoders[i + 1], 4096);
        else if (ce.contains ("deflate") || te.contains ("deflate"))
          encoders[--i] = new DeflaterOutputStream (encoders[i + 1]);
        
        encoders[0] = encoders[i];
        encoders[i] = null; // prevent duplicate reference
        
        return encoders[0]; // returned stream is always first
        
      } catch (IOException e) {
        throw new ServerException (e);
      }
      
    }
    
    /**
     * Closes this response and flushes all output.
     *
     * @throws IOException if an error occurs
     */
    public void close () throws IOException {
      state = -1; // closed
      if (encoders[0] != null)
        encoders[0].close (); // close all chained streams (except the underlying one)
      out.flush (); // always flush underlying stream (even if getBody was never called)
    }
    
    /**
     * Sends the response headers with the given response status.
     * A Date header is added if it does not already exist.
     * If the response has a body, the Content-Length/Transfer-Encoding
     * and Content-Type headers must be set before sending the headers.
     *
     * @throws ServerException if an error occurs or headers were already sent
     * @see #sendHeaders (long, long, String, String, long[])
     */
    public void sendHeaders () throws ServerException {
      
      try {
        
        if (headersSent ())
          throw new ServerException ("Headers already sent");
        
        if (!headers.contains ("Date"))
          headers.add ("Date", formatDate (System.currentTimeMillis ()));
        
        headers.add ("Server", "UPLServer/" + Server.VERSION);
        
        out.write (server.getBytes (HttpVersion.HTTP_11 + " ", String.valueOf (status.getCode ()), " ", status.getMessage ()));
        
        out.write (Server.CRLF);
        
        headers.writeTo (out);
        
        state = 1; // headers sent
        
      } catch (IOException e) {
        throw new ServerException (e);
      }
      
    }
    
    /**
     * Sends the response headers, including the given response status
     * and description, and all response headers. If they do not already
     * exist, the following headers are added as necessary:
     * Content-Range, Content-Type, Transfer-Encoding, Content-Encoding,
     * Content-Length, Last-Modified, ETag, Connection  and Date. Ranges are
     * properly calculated as well, with a 200 status changed to a 206 status.
     *
     * @param length       the response body length, or zero if there is no body,
     *                     or negative if there is a body but its length is not yet known
     * @param lastModified the last modified date of the response resource,
     *                     or non-positive if unknown. A time in the future will be
     *                     replaced with the current system time.
     * @param etag         the ETag of the response resource, or null if unknown
     *                     (see RFC2616#3.11)
     * @param contentType  the content type of the response resource, or null
     *                     if unknown (in which case "application/octet-stream" will be sent)
     * @param range        the content range that will be sent, or null if the
     *                     entire resource will be sent
     * @throws ServerException if an error occurs
     */
    public void sendHeaders (long length, long lastModified, String etag, String contentType, long[] range) throws ServerException {
      
      if (range != null) {
        
        headers.add ("Content-Range", "bytes " + range[0] + "-" + range[1] + "/" + (length >= 0 ? length : "*"));
        length = range[1] - range[0] + 1;
        
        if (status == HttpStatus.SUCCESS_OK)
          status = HttpStatus.SUCCESS_PARTIAL_CONTENT;
        
      }
      
      String ct = headers.get ("Content-Type");
      
      if (ct == null) {
        
        ct = contentType != null ? contentType : "application/octet-stream";
        headers.add ("Content-Type", ct);
        
      }
      
      if (!headers.contains ("Content-Length") && !headers.contains ("Transfer-Encoding")) {
        
        // RFC2616#3.6: transfer encodings are case-insensitive and must not be sent to an HTTP/1.0 client
        
        boolean modern = request != null && request.getVersion () == HttpVersion.HTTP_11;
        
        String accepted = request == null ? null : request.getHeaders ().get ("Accept-Encoding");
        
        List<String> encodings = server.split (accepted, true);
        
        String compression = null;
        
        if (encodings.contains ("gzip"))
          compression = "gzip";
        else if (encodings.contains ("deflate"))
          compression = "deflate";
        
        if (compression != null && (length < 0 || length > 300) && isCompressible (ct) && modern) {
          
          headers.add ("Transfer-Encoding", "chunked"); // compressed data is always unknown length
          headers.add ("Content-Encoding", compression);
          
        } else if (length < 0 && modern)
          headers.add ("Transfer-Encoding", "chunked"); // unknown length
        else if (length >= 0)
          headers.add ("Content-Length", Long.toString (length)); // known length
        
      }
      
      if (!headers.contains ("Vary")) // RFC7231#7.1.4: Vary field should include headers
        headers.add ("Vary", "Accept-Encoding"); // that are used in selecting representation
      
      if (lastModified > 0 && !headers.contains ("Last-Modified")) // RFC2616#14.29
        headers.add ("Last-Modified", formatDate (Math.min (lastModified, System.currentTimeMillis ())));
      
      if (etag != null && !headers.contains ("ETag"))
        headers.add ("ETag", etag);
      
      if (request != null && "close".equalsIgnoreCase (request.getHeaders ().get ("Connection"))
            && !headers.contains ("Connection"))
        headers.add ("Connection", "close"); // #RFC7230#6.6: should reply to close with close
      
      sendHeaders ();
      
    }
    
    public Response setStatus (HttpStatus status) {
      
      this.status = status;
      
      return this;
      
    }
    
    public Status getStatus () {
      return status;
    }
    
    /**
     * Sends the full response with the given status, and the given string
     * as the body. The text is sent in the UTF-8 charset. If a
     * Content-Type header was not explicitly set, it will be set to
     * text/html, and so the text must contain valid (and properly
     * {@link uplx.server.handlers.TextHandler#escapeHTML escaped}) HTML.
     *
     * @param text Text to send (sent as text/html)
     * @throws ServerException if an error occurs
     */
    public void send (String text) throws ServerException {
      send (text, StandardCharsets.UTF_8);
    }
    
    /**
     * Sends the full response with the given status, and the given string
     * as the body. The text is sent in the UTF-8 charset. If a
     * Content-Type header was not explicitly set, it will be set to
     * text/html, and so the text must contain valid (and properly
     * {@link uplx.server.handlers.TextHandler#escapeHTML escaped}) HTML.
     *
     * @param text    Text to send (sent as text/html)
     * @param charset Text charset
     * @throws ServerException if an error occurs
     */
    public void send (String text, Charset charset) throws ServerException {
      
      try {
        
        byte[] content = text.getBytes (charset);
        
        sendHeaders (content.length, -1, "W/\"" + Integer.toHexString (text.hashCode ()) + "\"", "text/html; charset=utf-8", null);
        
        OutputStream out = getBody ();
        
        if (out != null)
          out.write (content);
        
      } catch (IOException e) {
        throw new ServerException (e);
      }
      
    }
    
    /**
     * Sends an error response with the given status and detailed message.
     * An HTML body is created containing the status and its description,
     * as well as the message, which is escaped using the
     * {@link uplx.server.handlers.TextHandler#escapeHTML escape} method.
     *
     * @param mess Error message
     * @throws ServerException if an error occurs
     */
    public void sendError (String mess) throws ServerException {
      sendError (new ServerException (mess));
    }
    
    /**
     * Sends an error response with the given status and detailed message.
     * An HTML body is created containing the status and its description,
     * as well as the message, which is escaped using the
     * {@link uplx.server.handlers.TextHandler#escapeHTML escape} method.
     *
     * @param e Exception
     * @throws ServerException if an error occurs
     */
    public void sendError (ServerException e) throws ServerException {
      
      send (new StringTemplate ("<!DOCTYPE html>\n<html>\n\t<head>\n\t\t<title>$code $message</title>\n\t</head>\n\t<body>\n\t\t<h1>$code $message</h1>\n\t\t$text\n\t\t<p>\n\t\t\t$stacktrace\n\t\t</p>\n\t</body>\n</html>", new JSONObject ()
        .put ("code", status.getCode ())
        .put ("message", status.getMessage ())
        .put ("text", e.getMessage ())
        .put ("stacktrace", Arrays.implode ("<br/>\n\t\t\t", e.getStackTrace ()))
      ).toString ());
      
    }
    
    /**
     * Sends an error response with the given status and default body.
     *
     * @throws ServerException if an error occurs
     */
    public void sendError () throws ServerException {
      sendError (new ServerException (status.toString ()));
    }
    
    /**
     * Sends the response body. This method must be called only after the
     * response headers have been sent (and indicate that there is a body).
     *
     * @param body   a stream containing the response body
     * @param length the full length of the response body, or -1 for the whole stream
     * @param range  the sub-range within the response body that should be
     *               sent, or null if the entire body should be sent
     * @throws ServerException if an error occurs
     */
    public void sendBody (InputStream body, long length, long[] range) throws ServerException {
      
      try {
        
        OutputStream out = getBody ();
        
        if (out != null) {
          
          if (range != null) {
            long offset = range[0];
            length = range[1] - range[0] + 1;
            while (offset > 0) {
              long skip = body.skip (offset);
              if (skip == 0)
                throw new ServerException ("can't skip to " + range[0]);
              
              offset -= skip;
            }
          }
          
          transfer (body, out, length);
          
        }
        
      } catch (IOException e) {
        throw new ServerException (e);
      }
      
    }
    
    /**
     * Transfers data from an input stream to an output stream.
     *
     * @param in  the input stream to transfer from
     * @param out the output stream to transfer to (or null to discard output)
     * @param len the number of bytes to transfer. If negative, the entire
     *            contents of the input stream are transferred.
     * @throws ServerException if an IO error occurs or the input stream ends
     *                         before the requested number of bytes have been read
     */
    public static void transfer (InputStream in, OutputStream out, long len) throws ServerException { // TODO
      
      try {
        
        if (len == 0 || out == null && len < 0 && in.read () < 0)
          return; // small optimization - avoid buffer creation
        
        byte[] buf = new byte[4096];
        
        while (len != 0) {
          int count = len < 0 || buf.length < len ? buf.length : (int) len;
          count = in.read (buf, 0, count);
          if (count < 0) {
            if (len > 0)
              throw new ServerException ("unexpected end of stream");
            break;
          }
          
          if (out != null)
            out.write (buf, 0, count);
          
          len -= len > 0 ? count : 0;
          
        }
        
      } catch (IOException e) {
        throw new ServerException (e);
      }
      
    }
    
    /**
     * Sends a 301 or 302 response, redirecting the client to the given URL.
     *
     * @param url the absolute URL to which the client is redirected
     * @throws ServerException if an IO error occurs or url is malformed
     */
    public void redirect (String url) throws ServerException {
      
      try {
        url = new URI (url).toASCIIString ();
      } catch (URISyntaxException e) {
        throw new ServerException ("Malformed URL: " + url);
      }
      
      headers.add ("Location", url);
      
      // some user-agents expect a body, so we send it
      sendError ();
      
    }
    
    public void setRedirect (String url) {
      setRedirect (url, HttpStatus.REDIRECT_FOUND);
    }
    
    public void setRedirect (String url, HttpStatus status) {
      setStatus (status).redirect (url);
    }
    
    /**
     * Formats the given time value as a string in RFC 1123 format.
     *
     * @param time the time in milliseconds since January 1, 1970, 00:00:00 GMT
     * @return the given time value as a string in RFC 1123 format
     */
    public String formatDate (long time) {
      
      // this implementation performs far better than SimpleDateFormat instances, and even
      // quite better than ThreadLocal SDFs - the server's CPU-bound benchmark gains over 20%!
      if (time < -62167392000000L || time > 253402300799999L)
        throw new IllegalArgumentException ("year out of range (0001-9999): " + time);
      
      char[] s = "DAY, 00 MON 0000 00:00:00 GMT".toCharArray (); // copy the format template
      
      Calendar cal = new GregorianCalendar (Server.GMT, Locale.US);
      cal.setTimeInMillis (time);
      
      System.arraycopy (DAYS, 4 * (cal.get (Calendar.DAY_OF_WEEK) - 1), s, 0, 3);
      System.arraycopy (MONTHS, 4 * cal.get (Calendar.MONTH), s, 8, 3);
      
      int n = cal.get (Calendar.DATE);
      
      s[5] += n / 10;
      s[6] += n % 10;
      
      n = cal.get (Calendar.YEAR);
      
      s[12] += n / 1000;
      s[13] += n / 100 % 10;
      s[14] += n / 10 % 10;
      s[15] += n % 10;
      
      n = cal.get (Calendar.HOUR_OF_DAY);
      
      s[17] += n / 10;
      s[18] += n % 10;
      
      n = cal.get (Calendar.MINUTE);
      
      s[20] += n / 10;
      s[21] += n % 10;
      
      n = cal.get (Calendar.SECOND);
      
      s[23] += n / 10;
      s[24] += n % 10;
      
      return new String (s);
      
    }
    
    /**
     * Checks whether data of the given content type (MIME type) is compressible.
     *
     * @param contentType the content type
     * @return true if the data is compressible, false if not
     */
    public boolean isCompressible (String contentType) {
      
      int pos = contentType.indexOf (';'); // exclude params
      String ct = pos < 0 ? contentType : contentType.substring (0, pos);
      
      for (String s : compressibleContentTypes)
        if (
          s.equals (ct) ||
            s.charAt (0) == '*' && ct.endsWith (s.substring (1)) ||
            s.charAt (s.length () - 1) == '*' && ct.startsWith (s.substring (0, s.length () - 1))
        )
          return true;
      
      return false;
      
    }
    
  }