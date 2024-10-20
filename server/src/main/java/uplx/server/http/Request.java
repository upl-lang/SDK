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
	
	package uplx.server.http;
	
	import java.io.IOException;
	import java.net.MalformedURLException;
	import java.net.URI;
	import java.net.URISyntaxException;
	import java.net.URL;
	import java.net.URLDecoder;
	import java.nio.charset.StandardCharsets;
	import java.util.Arrays;
	import java.util.Locale;
	import upl.core.Log;
	import upl.http.HttpMethod;
	import upl.http.HttpVersion;
	import upl.io.LimitedInputStream;
	import uplx.server.Headers;
	import uplx.server.Params;
	import uplx.server.Server;
	import uplx.server.ServerException;
	import uplx.server.io.ChunkedInputStream;
	import upl.util.List;
	
	/**
	 * The {@code Request} class encapsulates a single HTTP request.
	 */
	public class Request extends uplx.server.Request {
		
		protected HttpMethod method;
		protected HttpVersion version;
		
		@Override
		public Request getInstance () {
			return new Request ();
		}
		
		/**
     * Constructs a HttpRequest from the data in the given input stream.
     *
     * @throws ServerException if an error occurs
     */
		public void readHeaders () throws ServerException { // PARSER Исключение необходимо если имеется у родителя
			
			// RFC2616#4.1: should accept empty lines before request line
			// RFC2616#19.3: tolerate additional whitespace between tokens
			
      // Reads the request line, parsing the method, URI and version string.
			
			try {
				
				String line = in.getReader ().readLine ();
				
				List<String> lexemes = Server.split (line, " ", -1);
				
				if (lexemes.length () != 3)
					throw new ServerException ("invalid request line: \"" + line + "\"");
				
				try {
					
					setMethod (HttpMethod.get (lexemes.get (0)));
					
					uri = new URI (trimDuplicates (lexemes.get (1), '/')); // must remove '//' prefix which constructor parses as host name
					version = HttpVersion.get (lexemes.get (2)); // RFC2616#2.1: allow implied LWS; RFC7230#3.1.1: disallow it
					
				} catch (URISyntaxException use) {
					throw new ServerException ("Invalid URI: " + use.getMessage ());
				}
				
				headers = new Headers ().readHeaders (in.getReader ());
				
				// RFC2616#3.6 - if "chunked" is used, it must be the last one
				// RFC2616#4.4 - if non-identity Transfer-Encoding is present,
				// it must either include "chunked" or close the connection after
				// the body, and in any case ignore Content-Length.
				// if there is no such Transfer-Encoding, use Content-Length
				// if neither header exists, there is no body
				
				String header = headers.get ("Transfer-Encoding");
				
				if (header != null && !header.toLowerCase (Locale.US).equals ("identity")) {
					
					if (Arrays.asList (Server.split (header, true)).contains ("chunked"))
						body = new ChunkedInputStream (in, headers);
					else
						body = in; // body ends when connection closes
					
				} else {
					
					header = headers.get ("Content-Length");
					
					long len = header == null ? 0 : parseULong (header, 10);
					body = new LimitedInputStream (in, len, false);
					
				}
				
			} catch (IOException e) { // if EOF, timeout etc.
				Log.w (e);
				throw new ServerException (e); // signal that the request did not begin
			}
			
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
     * Returns the request version string.
     *
     * @return the request version string
     */
		public HttpVersion getVersion () {
			return version;
		}
		
		protected Params queryParams, bodyParams;
		
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
						bodyParams = parseParamsList (readToken (body, -1, server.charset, 2097152)); // 2MB limit
					
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
				
				for (String pair : Server.split (s, "&", -1)) {
					
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
		
		@Override
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
			
			return baseURL = new URL (server.secure ? "https" : "http", host, server.getPort (), "");
			
		}
		
		/**
     * Returns the absolute (zero-based) content range value specified
     * by the given range string. If multiple ranges are requested, a single
     * range containing all of them is returned.
     *
     * @param range	the string containing the range description
     * @param length the full length of the requested resource
     * @return the requested range, or null if the range value is invalid
     */
		public long[] parseRange (String range, long length) {
			
			long min = Long.MAX_VALUE;
			long max = Long.MIN_VALUE;
			
			try {
				
				for (String token : Server.split (range, false)) {
					
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
		
	}