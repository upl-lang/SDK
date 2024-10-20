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
	
	import java.io.File;
	import java.io.FileInputStream;
	import java.io.FileNotFoundException;
	import java.net.URI;
	import java.util.Arrays;
	import upl.app.Application;
	import upl.io.BufferedInputStream;
	import upl.type.StringTemplate;
	import uplx.server.Handler;
	import uplx.server.ServerException;
	import uplx.server.http.handlers.FilesHandler;
	import uplx.server.http.handlers.TextHandler;
	import uplx.server.router.Condition;
	import uplx.server.router.Router;
	import uplx.server.router.RuleProcessor;
	
	public class HttpRouter extends Router<Request, Response> {
		
		public boolean folderIndex = false;
		protected FilesHandler handler;
		
		public HttpRouter () throws ServerException { // PARSER Исключения должен иметь и родитель если имеет ребенок
			
			try {
				
				if (folderIndex) { // PARSER No is always false condition
					
					File dir = new File (".");
					
					if (!dir.canRead ())
						throw new FileNotFoundException (dir.getAbsolutePath ());
					
					handler = new FilesHandler (dir);
					
					handler.setAllowGeneratedIndex (true); // with directory index pages
					
					// set up server
					for (File f : Arrays.asList (new File ("/etc/mime.types"), new File (dir, ".mime.types")))
						if (f.exists ())
							handler.addContentTypes (new BufferedInputStream (f));
					
					addRewriteRule ("/", handler);
					
				} else addRewriteRule ("/", new TextHandler () {
					
					@Override
					public StringTemplate onContent (Application app, Request request, Response response) throws ServerException {
						return new StringTemplate ("Hello world!");
					}
					
				});
				
			} catch (FileNotFoundException e) {
				throw new ServerException (e);
			}
			
		}
		
		public Router<Request, Response> setFolderIndex (boolean index) {
			
			folderIndex = index;
			return this;
			
		}
		
		String url;
		
		@Override
		public RewriteRule getRule () throws ServerException {
			
			URI uri = server.request.getURI ();
			
			for (String regex : rules.keySet ()) {
				
				boolean match = false;
				url = uri.toString ();
				
				if (!regex.equals ("/")) {
					
					RewriteRule rule = (RewriteRule) rules.get (regex);
					
					//if (!rule.checkFlag (RewriteRule.Flag.APPEND_QUERY))
					//	request.queryParams = new Params (); // TODO
					
					if (rule.condition != null) {
						
						if (rule.condition.type.equals (Condition.HTTP_REFERER)) {
							
							regex = server.response.getHeaders ().get ("Referer");
							match = true;
							
						}
						
					} else {
						
						url = uri.getPath ();
						match = true;
						
					}
					
					if (rule.status != null)
						match = (rule.status.equals (server.response.getStatus ()));
					
					if (match) {
						
						server.request.matcher = rule.getMatcher (regex, url);
						
						if (server.request.matcher.find ())
							return getRule (rule);
						
						//if (!rule.checkFlag (RewriteRule.Flag.CONTINUE))
						//	break;
						
					}
					
				}
				
			}
			
			RewriteRule rule = (RewriteRule) rules.get ("/");
			
			if (uri.toString ().equals ("/"))
				return getRule (rule);
			else if (folderIndex) {
				
				handler.url = uri.getPath ();
				
				return new RewriteRule (handler);
				
			}
			
			return null;
			
		}
		
		protected RewriteRule getRule (RewriteRule rule) {
			
			if (rule.processor != null)
				rule.handler = rule.processor.process (server.app, server.request, server.response);
			
			((HttpHandler) rule.handler).url = url;
			
			return rule;
			
		}
		
		/**
     * Add rewrite rule.
     *
     * @param handler Handler
     */
		public final Router<Request, Response> addRewriteRule (String regex, Handler<Request, Response> handler) {
			return addRewriteRule (regex, new RewriteRule (handler));
		}
		
		/**
     * Add rewrite rule.
     *
     * @param handler Handler
     */
		public final Router<Request, Response> addRewriteRule (String regex, RuleProcessor<Request, Response> handler) {
			return addRewriteRule (regex, new RewriteRule (handler));
		}
		
	}