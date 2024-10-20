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
	
	import java.io.FilterOutputStream;
	import java.io.IOException;
	import java.io.InputStream;
	import java.io.OutputStream;
	import java.net.URI;
	import java.net.URISyntaxException;
	import java.util.zip.DeflaterOutputStream;
	import java.util.zip.GZIPOutputStream;
	import upl.core.Arrays;
	import upl.http.HttpVersion;
	import upl.json.JSONObject;
	import upl.type.StringTemplate;
	import upl.util.List;
	import upl.http.HttpStatus;
	import uplx.server.Headers;
	import uplx.server.Server;
	import uplx.server.ServerException;
	import uplx.server.http.handlers.TextHandler;
	import uplx.server.io.ChunkedOutputStream;
	
	/**
	 * The {@code HttpResponse} class encapsulates a single HTTP response.
	 */
	public class Response extends uplx.server.Response {
		
		protected Headers headers = new Headers ();
		
		/**
     * Constructs a HttpResponse whose output is written to the given stream.
     *
     */
		public Response () {
			
			compressibleContentTypes.put ("text/*");
			compressibleContentTypes.put ("*/javascript");
			compressibleContentTypes.put ("*icon");
			compressibleContentTypes.put ("*+xml");
			compressibleContentTypes.put ("*/json");
			
		}
		
		@Override
		public Response getInstance () {
			return new Response ();
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
		@Override
		public OutputStream getBody () throws ServerException {
			
			try {
				
				if (encoders[0] != null || discardBody)
					return encoders[0]; // return the existing stream (or null)
				
				// set up chain of encoding streams according to headers
				List<String> te = Server.split (headers.get ("Transfer-Encoding"), true);
				List<String> ce = Server.split (headers.get ("Content-Encoding"), true);
				
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
     * Sends the response headers with the given response status.
     * A Date header is added if it does not already exist.
     * If the response has a body, the Content-Length/Transfer-Encoding
     * and Content-Type headers must be set before sending the headers.
     *
     * @throws ServerException if an error occurs or headers were already sent
     * @see #sendHeaders (long, long, String, String, long[])
     */
		@Override
		public void sendHeaders () throws ServerException {
			
			try {
				
				if (headersSent ())
					throw new ServerException ("Headers already sent");
				
				if (!headers.contains ("Date"))
					headers.add ("Date", formatDate (System.currentTimeMillis ()));
				
				headers.add ("Server", "UPLServer/" + Server.VERSION);
				
				out.write (Server.getBytes (HttpVersion.HTTP_11 + " ", String.valueOf (status.getCode ()), " ", status.getMessage ()));
				
				out.write (Server.CRLF);
				
				headers.writeTo (out);
				
				state = 1; // headers sent
				
			} catch (IOException e) {
				throw new ServerException (e);
			}
			
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
     * Sends the response headers, including the given response status
     * and description, and all response headers. If they do not already
     * exist, the following headers are added as necessary:
     * Content-Range, Content-Type, Transfer-Encoding, Content-Encoding,
     * Content-Length, Last-Modified, ETag, Connection	and Date. Ranges are
     * properly calculated as well, with a 200 status changed to a 206 status.
     *
     * @param length	     the response body length, or zero if there is no body,
     *						         or negative if there is a body but its length is not yet known
     * @param lastModified the last modified date of the response resource,
     *						         or non-positive if unknown. A time in the future will be
     *						         replaced with the current system time.
     * @param etag         the ETag of the response resource, or null if unknown
     *						         (see RFC2616#3.11)
     * @param contentType	 the content type of the response resource, or null
     *						         if unknown (in which case "application/octet-stream" will be sent)
     * @param range         the content range that will be sent, or null if the
     *						         entire resource will be sent
     * @throws ServerException if an error occurs
     */
		public void sendHeaders (long length, long lastModified, String etag, String contentType, long[] range) throws ServerException {
			
			if (range != null) {
				
				headers.add ("Content-Range", "bytes " + range[0] + "-" + range[1] + "/" + (length >= 0 ? length : "*"));
				
				length = range[1] - range[0] + 1;
				
				if (status.isSuccess ()) // TODO Or only 200?
					status = HttpStatus.SUCCESS_PARTIAL_CONTENT;
				
			}
			
			String ct = headers.get ("Content-Type");
			
			if (ct == null) {
				
				ct = contentType != null ? contentType : "application/octet-stream";
				headers.add ("Content-Type", ct);
				
			}
			
			if (!headers.contains ("Content-Length") && !headers.contains ("Transfer-Encoding")) { // RFC2616#3.6: transfer encodings are case-insensitive and must not be sent to an HTTP/1.0 client
				
				boolean modern = request != null && ((Request) request).getVersion () == HttpVersion.HTTP_11;
				
				String accepted = request == null ? null : request.getHeaders ().get ("Accept-Encoding");
				
				List<String> encodings = Server.split (accepted, true);
				
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
		
		/**
     * Sends the full response with the given status, and the given string
     * as the body. The text is sent in the UTF-8 charset. If a
     * Content-Type header was not explicitly set, it will be set to
     * text/html, and so the text must contain valid (and properly
     * {@link TextHandler#escapeHTML escaped}) HTML.
     *
     * @param content		Content bytes array
     * @throws ServerException if an error occurs
     */
		@Override
		public void send (byte[] content) throws ServerException {
			
			try {
				
				sendHeaders (content.length, -1, "W/\"" + Integer.toHexString (new String (content).hashCode ()) + "\"", "text/html; charset=utf-8", null);
				
				getBody ().write (content);
				
			} catch (IOException e) {
				throw new ServerException (e);
			}
			
		}
		
		/**
     * Sends an error response with the given status and detailed message.
     * An HTML body is created containing the status and its description,
     * as well as the message, which is escaped using the
     * {@link TextHandler#escapeHTML escape} method.
     *
     * @param e Exception
     * @throws ServerException if an error occurs
     */
		@Override
		public void sendError (ServerException e) throws ServerException {
			
			send (new StringTemplate (
					"<!DOCTYPE html>\n<html>\n\t<head>\n\t\t<title>$code $message</title>\n\t</head>\n\t<body>\n\t\t<h1>$code $message</h1>\n\t\t$text\n\t\t<p>\n\t\t\t$stacktrace\n\t\t</p>\n\t</body>\n</html>",
				new JSONObject ()
					.put ("code", status.getCode ())
					.put ("message", status.getMessage ())
					.put ("text", e.getMessage ())
					.put ("stacktrace", Arrays.implode ("<br/>\n\t\t\t", e.getStackTrace ()))
				)
			);
			
		}
		
		public void setRedirect (String url) {
			setRedirect (url, HttpStatus.REDIRECT_FOUND);
		}
		
		public void setRedirect (String url, HttpStatus status) {
			((Response) setStatus (status)).redirect (url);
		}
		
		/**
     * Sends the response body. This method must be called only after the
     * response headers have been sent (and indicate that there is a body).
     *
     * @param body	 a stream containing the response body
     * @param length the full length of the response body, or -1 for the whole stream
     * @param range	 the sub-range within the response body that should be
     *			         sent, or null if the entire body should be sent
     * @throws       ServerException if an error occurs
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
								throw new ServerException ("Can't skip to " + range[0]);
							
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
			
			sendError (); // some user-agents expect a body, so we send it
			
		}
		
	}