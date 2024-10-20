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
	
	package uplx.server.http.handlers;
	
	import java.io.IOException;
	import upl.app.Application;
	import upl.core.File;
	import upl.core.exceptions.OutOfMemoryException;
	import upl.json.JSONObject;
	import upl.type.StringTemplate;
	import uplx.server.http.Request;
	import uplx.server.http.Response;
	import uplx.server.ServerException;
	
	public abstract class HTMLHandler extends TextHandler {
		
		@Override
		public void serve (Application app, Request request, Response response) throws ServerException {
			
			response.getHeaders ().add ("Content-Type", "text/html; charset=utf-8");
			
			JSONObject data = new JSONObject ();
			
			data.put ("header", onHeader (app, request, response));
			data.put ("content", onContent (app, request, response));
			data.put ("footer", onFooter (app, request, response));
			
			//response.send (loadTemplate (data, "content").toString ());
			
			response.send (loadTemplate (data, "content"));
			
		}
		
		protected StringTemplate loadTemplate (JSONObject data, String... path) throws ServerException {
			
			try {
				return new StringTemplate (new File (path).read (), data);
			} catch (IOException | OutOfMemoryException e) {
				throw new ServerException (e);
			}
			
		}
		
		protected abstract StringTemplate onHeader (Application app, Request request, Response response) throws ServerException;
		protected abstract StringTemplate onFooter (Application app, Request request, Response response) throws ServerException;
		
	}