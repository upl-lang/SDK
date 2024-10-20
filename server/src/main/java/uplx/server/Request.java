	/*
	 * Copyright (c) 2020 - 2024 UPL Foundation
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 * 	  http://www.apache.org/licenses/LICENSE-2.0
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
	import java.net.UnknownHostException;
	import java.nio.charset.Charset;
	import java.nio.charset.StandardCharsets;
	import java.util.regex.Matcher;
	import upl.io.BufferedInputStream;
	
	public abstract class Request {
		
		public Server<?, ?> server;
		public BufferedInputStream in;
		
		protected Headers headers;
		
		public URI uri;
		protected URL baseURL; // cached value
		public InputStream body;
		protected VirtualHost host; // cached value
		protected VirtualHost.ContextInfo context; // cached value
		
		public Matcher matcher;
		
		public Matcher getMatcher () { // TODO
			return matcher;
		}
		
		public abstract Request getInstance ();
		
		public abstract void readHeaders () throws ServerException;
		
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
     * @throws IOException                  if an IO error occurs, or the maximum length
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
     * Returns the request server headers.
     *
     * @return the request headers
     */
		public Headers getHeaders () {
			return headers;
		}
		
		/**
     * Reads the ISO-8859-1 encoded string starting at the current stream
     * position and ending at the first occurrence of the LF character.
     *
     * @param in the stream from which the line is read
     * @return the read string, excluding the terminating LF character
     * and (if exists) the CR character immediately preceding it
     * @throws EOFException if the stream end is reached before an LF character is found
     * @throws IOException  if an IO error occurs, or the line is longer than 8192 bytes
     * @see #readToken (InputStream, Charset)
     */
		public static String readLine2 (InputStream in) throws IOException {
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
     * Returns the request URI.
     *
     * @return the request URI
     */
		public URI getURI () {
			return uri;
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
			return baseURL;
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
				context = getVirtualHost ().getContext (getURI ().getPath ());
			
			return context;
			
		}
		
		/**
     * Returns the local host's auto-detected name.
     *
     * @return the local host name
     */
		protected String detectLocalHostName () {
			
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