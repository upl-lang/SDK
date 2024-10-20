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
	
	import upl.app.Application;
	
	/**
	 * A {@code Handler} serves the content of resources within a context.
	 *
	 * @see VirtualHost#addContext
	 */
	public abstract class Handler<Request extends uplx.server.Request, Response extends uplx.server.Response> {
		
		/**
     * Serves the given request using the given response.
     *
     * @param app	application class
     * @param request	the request to be served
     * @param response the response to be filled
     * a default response appropriate for this status. If this
     * method invocation already sent anything in the response
     * (headers or content), it must return 0, and no further
     * processing will be done
     * @throws ServerException if an IO error occurs
     */
		public abstract void serve (Application app, Request request, Response response) throws ServerException;
		
		public Validator getValidator () {
			return null;
		}
		
	}