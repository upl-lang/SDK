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
	
	package uplx.server.router;
	
	import upl.util.HashMap;
	import upl.util.Map;
	import uplx.server.Server;
	import uplx.server.ServerException;
	
	public abstract class Router<Request extends uplx.server.Request, Response extends uplx.server.Response> {
		
		public Server<Request, Response> server;
		
		public Map<String, RewriteRule<Request, Response>> rules = new HashMap<> ();
		
		/**
     * Add rewrite rule.
     *
     * @param rule RewriteRule rule
     */
		public final Router<Request, Response> addRewriteRule (String regex, RewriteRule<Request, Response> rule) { // PARSER Use of parametrized class fatal error
			
			rules.put (regex, rule);
			
			return this;
			
		}
		
		public abstract RewriteRule<Request, Response> getRule () throws ServerException; // PARSER public abstract order must be exact
		
	}