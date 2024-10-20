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
	
	package uplx.server.io;
	
	import java.io.BufferedReader;
	import java.io.IOException;
	import java.io.InputStream;
	import java.nio.charset.Charset;
	import java.util.Iterator;
	import java.util.Map;
	import java.util.NoSuchElementException;
	import upl.io.BufferedInputStream;
	import uplx.server.Headers;
	import uplx.server.http.Request;
	import uplx.server.Server;
	import uplx.server.ServerException;
	import uplx.server.VirtualHost;
	import uplx.server.router.Context;
	
	/**
	 * The {@code MultipartIterator} iterates over the parts of a multipart/form-data request.
	 * <p>
	 * For example, to support file upload from a web browser:
	 * <ol>
	 * <li>Create an HTML form which includes an input field of type "file", attributes
	 *     method="post" and enctype="multipart/form-data", and an action URL of your choice,
	 *     for example action="/upload". This form can be served normally like any other
	 *     resource, e.g. from an HTML file on disk.
	 * <li>Add a context handler for the action path ("/upload" in this example), using either
	 *     the explicit {@link VirtualHost#addContext} method or the {@link Context} annotation.
	 * <li>In the context handler implementation, construct a {@code MultipartIterator} from
	 *     the client {@code Request}.
	 * <li>Iterate over the form {@link Part}s, processing each named field as appropriate -
	 *     for the file input field, read the uploaded file using the body input stream.
	 * </ol>
	 */
	public class MultipartIterator implements Iterator<MultipartIterator.Part> {
		
		protected Server<?, ?> server;
		
		/**
     * The {@code Part} class encapsulates a single part of the multipart.
     */
		public static class Part {
			
			public String name;
			public String filename;
			public InputStream body;
			
			protected Server<?, ?> server;
			
			protected Headers headers;
			
			/**
	     * Returns the part's name (form field name).
	     *
	     * @return the part's name
	     */
			public String getName () {
				return name;
			}
			
			/**
	     * Returns the part's filename (original filename entered in file form field).
	     *
	     * @return the part's filename, or null if there is none
	     */
			public String getFilename () {
				return filename;
			}
			
			/**
	     * Returns the part's headers.
	     *
	     * @return the part's headers
	     */
			public Headers getHeaders () {
				return headers;
			}
			
			/**
	     * Returns the part's body (form field value).
	     *
	     * @return the part's body
	     */
			public InputStream getBody () {
				return body;
			}
			
			/***
	     * Returns the part's body as a string. If the part
	     * headers do not specify a charset, UTF-8 is used.
	     *
	     * @return the part's body as a string
	     * @throws ServerException if an IO error occurs
	     */
			public String getString () throws IOException {
				
				String charset = headers.getParams ("Content-Type").get ("charset");
				return Request.readToken (body, -1, charset == null ? server.charset : Charset.forName (charset), 8192);
				
			}
			
		}
		
		protected Request request;
		protected BufferedReader reader;
		
		protected MultipartInputStream in;
		protected boolean next;
		
		/**
     * Creates a new MultipartIterator from the given request.
     *
     * @param request the multipart/form-data request
     * @throws ServerException					if an IO error occurs
     * @throws IllegalArgumentException if the given request's content type
     *																	is not multipart/form-data, or is missing the boundary
     */
		public MultipartIterator (Server<?, ?> server, Request request) {
			
			this.server = server;
			this.request = request;
			
			Map<String, String> ct = request.getHeaders ().getParams ("Content-Type");
			
			if (!ct.containsKey ("multipart/form-data"))
				throw new IllegalArgumentException ("Content-Type is not multipart/form-data");
			
			String boundary = ct.get ("boundary"); // should be US-ASCII
			
			if (boundary == null)
				throw new IllegalArgumentException ("Content-Type is missing boundary");
			
			in = new MultipartInputStream (request.getBody (), Server.getBytes (boundary));
			reader = BufferedInputStream.getReader (in, server.charset);
			
		}
		
		@Override
		public boolean hasNext () {
			
			try {
				return next || (next = in.nextPart ());
			} catch (IOException ioe) {
				throw new RuntimeException (ioe);
			}
			
		}
		
		@Override
		public Part next () {
			
			if (!hasNext ())
				throw new NoSuchElementException ();
			
			next = false;
			
			Part part = new Part ();
			
			part.server = server;
			
			part.headers = request.getHeaders ().readHeaders (reader); // TODO
			
			Map<String, String> cd = part.headers.getParams ("Content-Disposition");
			
			part.name = cd.get ("name");
			part.filename = cd.get ("filename");
			part.body = in;
			
			return part;
			
		}
		
		@Override
		public void remove () {
			throw new UnsupportedOperationException ();
		}
		
	}