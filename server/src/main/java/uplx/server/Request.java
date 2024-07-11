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
  
  import java.io.EOFException;
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.UnsupportedEncodingException;
  import java.net.InetAddress;
  import java.net.MalformedURLException;
  import java.net.URI;
  import java.net.URISyntaxException;
  import java.net.URL;
  import java.net.URLDecoder;
  import java.net.UnknownHostException;
  import java.nio.charset.Charset;
  import java.nio.charset.StandardCharsets;
  import java.util.Arrays;
  import java.util.Locale;
  import java.util.regex.Matcher;
  import upl.http.HttpMethod;
  import upl.http.HttpVersion;
  import upl.io.LimitedInputStream;
  import uplx.server.io.ChunkedInputStream;
  import upl.util.List;
  
  /**
   * The {@code Request} class encapsulates a single HTTP request.
   */
  public class Request {
    
    private final Server server;
    
    protected HttpMethod method;
    protected URI uri;
    protected URL baseURL; // cached value
    protected HttpVersion version;
    protected InputStream body;
    protected VirtualHost host; // cached value
    protected VirtualHost.ContextInfo context; // cached value
    
    protected Headers headers;
    protected Matcher matcher;
    
    /**
     * Constructs a Request from the data in the given input stream.
     *
     * @param in the input stream from which the request is read
     * @throws ServerException if an error occurs
     */
    public Request (Server server, InputStream in) throws ServerException {
      
      this.server = server;
      
      readRequestLine (in);
      
      headers = new Headers (server).readHeaders (in);
      
      // RFC2616#3.6 - if "chunked" is used, it must be the last one
      // RFC2616#4.4 - if non-identity Transfer-Encoding is present,
      // it must either include "chunked" or close the connection after
      // the body, and in any case ignore Content-Length.
      // if there is no such Transfer-Encoding, use Content-Length
      // if neither header exists, there is no body
      
      String header = headers.get ("Transfer-Encoding");
      
      if (header != null && !header.toLowerCase (Locale.US).equals ("identity")) {
        
        if (Arrays.asList (server.split (header, true)).contains ("chunked"))
          body = new ChunkedInputStream (in, headers);
        else
          body = in; // body ends when connection closes
        
      } else {
        
        header = headers.get ("Content-Length");
        long len = header == null ? 0 : parseULong (header, 10);
        
        body = new LimitedInputStream (in, len, false);
        
      }
      
    }
    
    public Matcher getMatcher () {
      return matcher;
    }
    
    /**
     * Reads the token starting at the current stream position and ending at
     * the first occurrence of the given delimiter byte, in the given encoding.
     * If LF is specified as the delimiter, a CRLF pair is also treated as one.
     *
     * @param in        the stream from which the token is read
     * @param delim     the byte value which marks the end of the token,
     *                  or -1 if the token ends at the end of the stream
     * @param enc       a character-encoding name
     * @param maxLength the maximum length (in bytes) to read
     * @return the read token, excluding the delimiter
     * @throws UnsupportedEncodingException if the encoding is not supported
     * @throws EOFException                 if the stream end is reached before a delimiter is found
     * @throws ServerException              if an IO error occurs, or the maximum length
     *                                      is reached before the token end is reached
     */
    public static String readToken (InputStream in, int delim, Charset enc, int maxLength) throws IOException {
      
      // note: we avoid using a ByteArrayOutputStream here because it
      // suffers the overhead of synchronization for each byte written
      
      int b;
      int len = 0; // buffer length
      int count = 0; // number of read bytes
      byte[] buf = null; // optimization - lazy allocation only if necessary
      
      while ((b = in.read ()) != -1 && b != delim) {
        
        if (count == len) { // expand buffer
          
          if (count == maxLength)
            throw new IOException ("token too large (" + count + ")");
          
          len = len > 0 ? 2 * len : 256; // start small, double each expansion
          len = Math.min (maxLength, len);
          
          byte[] expanded = new byte[len];
          
          if (buf != null)
            System.arraycopy (buf, 0, expanded, 0, count);
          
          buf = expanded;
          
        }
        
        buf[count++] = (byte) b;
        
      }
      
      if (b < 0 && delim != -1)
        throw new EOFException ("unexpected end of stream");
      
      if (delim == '\n' && count > 0 && buf[count - 1] == '\r')
        count--;
      
      return count > 0 ? new String (buf, 0, count, enc) : "";
      
    }
    
    /**
     * Reads the ISO-8859-1 encoded string starting at the current stream
     * position and ending at the first occurrence of the LF character.
     *
     * @param in the stream from which the line is read
     * @return the read string, excluding the terminating LF character
     * and (if exists) the CR character immediately preceding it
     * @throws EOFException    if the stream end is reached before an LF character is found
     * @throws ServerException if an IO error occurs, or the line is longer than 8192 bytes
     * @see #readToken(InputStream, int, Charset, int)
     */
    public static String readLine (InputStream in) throws IOException {
      return readToken (in, '\n', StandardCharsets.ISO_8859_1, 8192);
    }
    
    /**
     * Parses an unsigned long value. This method behaves the same as calling
     * {@link Long#parseLong(String, int)}, but considers the string invalid
     * if it starts with an ASCII minus sign ('-') or plus sign ('+').
     *
     * @param s     the String containing the long representation to be parsed
     * @param radix the radix to be used while parsing s
     * @return the long represented by s in the specified radix
     * @throws NumberFormatException if the string does not contain a parsable
     *                               long, or if it starts with an ASCII minus sign or plus sign
     */
    public static long parseULong (String s, int radix) throws NumberFormatException {
      long val = Long.parseLong (s, radix); // throws NumberFormatException
      if (s.charAt (0) == '-' || s.charAt (0) == '+')
        throw new NumberFormatException ("invalid digit: " + s.charAt (0));
      return val;
    }
    
    /**
     * Sets method.
     *
     * @param method HttpMethod method
     */
    public Request setMethod (HttpMethod method) {
      
      this.method = method;
      return this;
      
    }
    
    /**
     * Returns the request method.
     *
     * @return the request method
     */
    public HttpMethod getMethod () {
      return method;
    }
    
    /**
     * Returns the request URI.
     *
     * @return the request URI
     */
    public URI getURI () {
      return uri;
    }
    
    /**
     * Returns the request version string.
     *
     * @return the request version string
     */
    public HttpVersion getVersion () {
      return version;
    }
    
    /**
     * Returns the request server headers.
     *
     * @return the request headers
     */
    public Headers getHeaders () {
      return headers;
    }
    
    /**
     * Returns the input stream containing the request body.
     *
     * @return the input stream containing the request body
     */
    public InputStream getBody () {
      return body;
    }
    
    /**
     * Returns the path component of the request URI, after
     * URL decoding has been applied (using the UTF-8 charset).
     *
     * @return the decoded path component of the request URI
     */
    public String getPath () {
      return uri.getPath ();
    }
    
    /**
     * Sets the path component of the request URI. This can be useful
     * in URL rewriting, etc.
     *
     * @param path the path to set
     * @throws IllegalArgumentException if the given path is malformed
     */
    public void setPath (String path) {
      try {
        uri = new URI (uri.getScheme (), uri.getUserInfo (), uri.getHost (), uri.getPort (),
          trimDuplicates (path, '/'), uri.getQuery (), uri.getFragment ());
        context = null; // clear cached context so it will be recalculated
      } catch (URISyntaxException use) {
        throw new IllegalArgumentException ("error setting path", use);
      }
    }
    
    /**
     * Returns the base URL (scheme, host and port) of the request resource.
     * The host name is taken from the request URI or the Host header or a
     * default host (see RFC2616#5.2).
     *
     * @return the base URL of the requested resource, or null if it
     * is malformed
     */
    public URL getBaseURL () throws MalformedURLException {
      
      if (baseURL != null)
        return baseURL;
      
      // normalize host header
      
      String host = uri.getHost ();
      
      if (host == null) {
        
        host = headers.get ("Host");
        
        if (host == null) // missing in HTTP/1.0
          host = detectLocalHostName ();
        
      }
      
      int pos = host.indexOf (':');
      host = pos < 0 ? host : host.substring (0, pos);
      
      return baseURL = new URL (server.secure ? "https" : "http", host, server.port, "");
      
    }
    
    public Params queryParams, bodyParams;
    
    /**
     * Returns the request parameters, which are parsed both from the query
     * part of the request URI, and from the request body if its content
     * type is "application/x-www-form-urlencoded" (i.e. a submitted form).
     * UTF-8 encoding is assumed in both cases.
     * <p>
     * The parameters are returned as a list of string arrays, each containing
     * the parameter name as the first element and its corresponding value
     * as the second element (or an empty string if there is no value).
     * <p>
     * The list retains the original order of the parameters.
     *
     * @return the request parameters name-value pairs,
     * or an empty list if there are none
     * @see #parseParamsList (String)
     */
    public Params getQueryParams () {
      
      if (queryParams == null)
        queryParams = parseParamsList (uri.getRawQuery ());
      
      return queryParams;
      
    }
    
    /**
     * Returns the request parameters, which are parsed both from the query
     * part of the request URI, and from the request body if its content
     * type is "application/x-www-form-urlencoded" (i.e. a submitted form).
     * UTF-8 encoding is assumed in both cases.
     * <p>
     * The parameters are returned as a list of string arrays, each containing
     * the parameter name as the first element and its corresponding value
     * as the second element (or an empty string if there is no value).
     * <p>
     * The list retains the original order of the parameters.
     *
     * @return the request parameters name-value pairs,
     * or an empty list if there are none
     * @throws ServerException if an error occurs
     * @see #parseParamsList (String)
     */
    public Params getBodyParams () throws ServerException {
      
      try {
        
        if (bodyParams == null) {
          
          String contentType = headers.get ("Content-Type");
          
          if (contentType != null && contentType.toLowerCase (Locale.US).startsWith ("application/x-www-form-urlencoded"))
            bodyParams = parseParamsList (readToken (body, -1, StandardCharsets.UTF_8, 2097152)); // 2MB limit
          
        }
        
        return bodyParams;
        
      } catch (IOException e) {
        throw new ServerException (e);
      }
      
    }
    
    /**
     * Returns the absolute (zero-based) content range value read
     * from the Range header. If multiple ranges are requested, a single
     * range containing all of them is returned.
     *
     * @param length the full length of the requested resource
     * @return the requested range, or null if the Range header
     * is missing or invalid
     */
    public long[] getRange (long length) {
      
      String header = headers.get ("Range");
      
      return header == null || !header.startsWith ("bytes=")
               ? null : parseRange (header.substring (6), length);
      
    }
    
    /**
     * Parses name-value pair parameters from the given "x-www-form-urlencoded"
     * MIME-type string. This is the encoding used both for parameters passed
     * as the query of an HTTP GET method, and as the content of HTML forms
     * submitted using the HTTP POST method (as long as they use the default
     * "application/x-www-form-urlencoded" encoding in their ENCTYPE attribute).
     * UTF-8 encoding is assumed.
     * <p>
     * The parameters are returned as a list of string arrays, each containing
     * the parameter name as the first element and its corresponding value
     * as the second element (or an empty string if there is no value).
     * <p>
     * The list retains the original order of the parameters.
     *
     * @param s an "application/x-www-form-urlencoded" string
     * @return the parameter name-value pairs parsed from the given string,
     * or an empty list if there are none
     */
    protected Params parseParamsList (String s) {
      
      Params params = new Params ();
      
      if (s != null && s.length () != 0) {
        
        for (String pair : server.split (s, "&", -1)) {
          
          int pos = pair.indexOf ('=');
          
          String name = pos < 0 ? pair : pair.substring (0, pos);
          String val = pos < 0 ? "" : pair.substring (pos + 1);
          
          name = URLDecoder.decode (name.trim (), StandardCharsets.UTF_8);
          val = URLDecoder.decode (val.trim (), StandardCharsets.UTF_8);
          
          if (name.length () > 0)
            params.put (name, val);
          
        }
        
      }
      
      return params;
      
    }
    
    /**
     * Returns the absolute (zero-based) content range value specified
     * by the given range string. If multiple ranges are requested, a single
     * range containing all of them is returned.
     *
     * @param range  the string containing the range description
     * @param length the full length of the requested resource
     * @return the requested range, or null if the range value is invalid
     */
    public long[] parseRange (String range, long length) {
      
      long min = Long.MAX_VALUE;
      long max = Long.MIN_VALUE;
      
      try {
        
        for (String token : server.split (range, false)) {
          
          long start, end;
          int dash = token.indexOf ('-');
          
          if (dash == 0) { // suffix range
            start = length - parseULong (token.substring (1), 10);
            end = length - 1;
          } else if (dash == token.length () - 1) { // open range
            start = parseULong (token.substring (0, dash), 10);
            end = length - 1;
          } else { // explicit range
            start = parseULong (token.substring (0, dash), 10);
            end = parseULong (token.substring (dash + 1), 10);
          }
          
          if (end < start)
            throw new RuntimeException ();
          if (start < min)
            min = start;
          if (end > max)
            max = end;
          
        }
        
        if (max < 0) // no tokens
          throw new RuntimeException ();
        
        if (max >= length && min < length)
          max = length - 1;
        
        return new long[] {min, max}; // start might be >= length!
        
      } catch (RuntimeException re) { // NFE, IOOBE or explicit RE
        return null; // RFC2616#14.35.1 - ignore header if invalid
      }
      
    }
    
    /**
     * Reads the request line, parsing the method, URI and version string.
     *
     * @param in the input stream from which the request line is read
     * @throws ServerException if an error occurs or the request line is invalid
     */
    protected void readRequestLine (InputStream in) throws ServerException {
      
      // RFC2616#4.1: should accept empty lines before request line
      // RFC2616#19.3: tolerate additional whitespace between tokens
      
      String line;
      
      try {
        
        do {
          line = readLine (in);
        } while (line.length () == 0);
        
      } catch (IOException ioe) { // if EOF, timeout etc.
        throw new ServerException ("Missing request line"); // signal that the request did not begin
      }
      
      List<String> lexemes = server.split (line, " ", -1);
      
      if (lexemes.length () != 3)
        throw new ServerException ("invalid request line: \"" + line + "\"");
      
      try {
        
        setMethod (HttpMethod.get (lexemes.get (0)));
        
        uri = new URI (trimDuplicates (lexemes.get (1), '/')); // must remove '//' prefix which constructor parses as host name
        version = HttpVersion.get (lexemes.get (2)); // RFC2616#2.1: allow implied LWS; RFC7230#3.1.1: disallow it
        
      } catch (URISyntaxException use) {
        throw new ServerException ("Invalid URI: " + use.getMessage ());
      }
      
    }
    
    /**
     * Returns the virtual host corresponding to the requested host name,
     * or the default host if none exists.
     *
     * @return the virtual host corresponding to the requested host name,
     * or the default virtual host
     */
    public VirtualHost getVirtualHost () throws ServerException {
      
      try {
        
        if (host == null) {
          
          host = server.getVirtualHost (getBaseURL ().getHost ());
          
          if (host == null)
            host = server.getVirtualHost (null);
          
        }
        
        return host;
        
      } catch (MalformedURLException e) {
        throw new ServerException (e);
      }
      
    }
    
    /**
     * Returns the info of the context handling this request.
     *
     * @return the info of the context handling this request, or an empty context
     */
    public VirtualHost.ContextInfo getContext () throws ServerException {
      
      if (context == null)
        context = getVirtualHost ().getContext (getPath ());
      
      return context;
      
    }
    
    /**
     * Returns the local host's auto-detected name.
     *
     * @return the local host name
     */
    public String detectLocalHostName () {
      
      try {
        return InetAddress.getLocalHost ().getCanonicalHostName ();
      } catch (UnknownHostException uhe) {
        return "localhost";
      }
      
    }
    
    /**
     * Trims duplicate consecutive occurrences of the given character within the
     * given string, replacing them with a single instance of the character.
     *
     * @param s the string to trim
     * @param c the character to trim
     * @return the given string with duplicate consecutive occurrences of c
     * replaced by a single instance of c
     */
    public String trimDuplicates (String s, char c) {
      int start = 0;
      while ((start = s.indexOf (c, start) + 1) > 0) {
        int end;
        for (end = start; end < s.length () && s.charAt (end) == c; end++) ;
        if (end > start)
          s = s.substring (0, start) + s.substring (end);
      }
      return s;
    }
    
  }