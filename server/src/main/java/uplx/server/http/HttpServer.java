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
	import upl.app.Application;
	import uplx.server.Server;
	
	public class HttpServer extends Server<Request, Response> {
		
		public HttpServer (Application app) throws IOException {
			this (app, "localhost");
		}
		
		public HttpServer (Application app, String address) throws IOException {
			this (app, address, 80);
		}
		
		public HttpServer (Application app, String address, int port) throws IOException {
			
			super (app, address, port);
			
			request = new Request ();
			response = new Response ();
			
			thread = new HttpServerThread ();
			router = new HttpRouter ();
			
		}
		
	}