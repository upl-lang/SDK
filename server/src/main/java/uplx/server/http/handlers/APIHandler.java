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
	
	import upl.app.Application;
	import upl.http.HttpMethod;
	import uplx.server.ServerException;
	import uplx.server.http.Request;
	import uplx.server.http.Response;
	
	public abstract class APIHandler extends JSONHandler {
		
		@Override
		protected String onContent (Application app, Request request, Response response) throws ServerException {
			
			if (request.getMethod () == HttpMethod.GET)
				return get (app, request, response);
			else if (request.getMethod () == HttpMethod.POST)
				return post (app, request, response);
			else if (request.getMethod () == HttpMethod.PUT)
				return put (app, request, response);
			else if (request.getMethod () == HttpMethod.DELETE)
				return delete (app, request, response);
			else
				throw new ServerException ("Invalid method " + request.getMethod ());
			
		}
		
		protected String get (Application app, Request request, Response response) {
			return null;
		}
		
		protected String post (Application app, Request request, Response response) {
			return null;
		}
		
		protected String put (Application app, Request request, Response response) {
			return null;
		}
		
		protected String delete (Application app, Request request, Response response) {
			return null;
		}
		
	}