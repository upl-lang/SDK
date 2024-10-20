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
	
	import java.io.IOException;
	import java.io.InputStream;
	import java.io.OutputStream;
	import java.util.Calendar;
	import java.util.GregorianCalendar;
	import java.util.Locale;
	import upl.net.Status;
	import upl.type.StringTemplate;
	import upl.util.ArrayList;
	import upl.util.List;
	import uplx.server.http.handlers.TextHandler;
	
	public abstract class Response {
		
		public Server<?, ?> server;
		public OutputStream out; // the underlying output stream
		
		protected Request request; // request used in determining client capabilities
		
		protected OutputStream[] encoders = new OutputStream[4]; // chained encoder streams
		protected int state; // nothing sent, headers sent, or closed
		
		protected boolean discardBody;
		protected Status status;
		
		/**
     * The MIME types that can be compressed (prefix/suffix wildcards allowed).
     */
		public List<String> compressibleContentTypes = new ArrayList<> ();
		
		/**
     * Date format strings.
     */
		protected final char[]
			DAYS = "Sun Mon Tue Wed Thu Fri Sat".toCharArray (),
			MONTHS = "Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec".toCharArray ();
		
		public abstract Response getInstance ();
		
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
     * Sets whether this response's body is discarded or sent.
     *
     * @param discardBody specifies whether the body is discarded or not
     */
		public void setDiscardBody (boolean discardBody) {
			this.discardBody = discardBody;
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
		
		public abstract OutputStream getBody () throws ServerException;
		public abstract void sendHeaders () throws ServerException;
		
		public Response setStatus (Status status) {
			
			this.status = status;
			
			return this;
			
		}
		
		public Status getStatus () {
			return status;
		}
		
		/**
     * Transfers data from an input stream to an output stream.
     *
     * @param in	the input stream to transfer from
     * @param out the output stream to transfer to (or null to discard output)
     * @param len the number of bytes to transfer. If negative, the entire
     *						contents of the input stream are transferred.
     * @throws ServerException if an IO error occurs or the input stream ends
     *								         before the requested number of bytes have been read
     */
		public void transfer (InputStream in, OutputStream out, long len) {
			
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
				// Errors which we don't need to handle (recv failed etc.)
			}
			
		}
		
		/**
     * Sends the full response with the given status, and the given string
     * as the body. The text is sent in the UTF-8 charset. If a
     * Content-Type header was not explicitly set, it will be set to
     * text/html, and so the text must contain valid (and properly
     * {@link TextHandler#escapeHTML escaped}) HTML.
     *
     * @param text Text to send (sent as text/html)
     * @throws ServerException if an error occurs
     */
		public void send (String text) throws ServerException {
			send (text.getBytes (server.charset));
		}
		
		/**
     * Sends the full response with the given status, and the given string
     * as the body. The text is sent in the UTF-8 charset. If a
     * Content-Type header was not explicitly set, it will be set to
     * text/html, and so the text must contain valid (and properly
     * {@link TextHandler#escapeHTML escaped}) HTML.
     *
     * @param text Text to send as StringTemplate
     * @throws ServerException if an error occurs
     */
		public void send (StringTemplate text) throws ServerException {
			send (text.toString ());
		}
		
		public abstract void send (byte[] content) throws ServerException;
		
		/**
     * Sends an error response with the given status and detailed message.
     * An HTML body is created containing the status and its description,
     * as well as the message, which is escaped using the
     * {@link TextHandler#escapeHTML escape} method.
     *
     * @param mess Error message
     * @throws ServerException if an error occurs
     */
		public void sendError (String mess) throws ServerException {
			sendError (new ServerException (mess));
		}
		
		/**
     * Sends an error response with the given status and default body.
     *
     * @throws ServerException if an error occurs
     */
		public void sendError () throws ServerException {
			sendError (new ServerException (status.toString ()));
		}
		
		public abstract void sendError (ServerException e) throws ServerException;
		
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