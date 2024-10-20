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
	
	package uplx.server.router;
	
	import java.util.regex.Matcher;
	import java.util.regex.Pattern;
	import upl.core.Arrays;
	import upl.http.HttpMethod;
	import upl.net.Method;
	import upl.net.Status;
	import uplx.server.Handler;
	
	public class RewriteRule<Request extends uplx.server.Request, Response extends uplx.server.Response> {
		
		public String replace;
		public Method[] methods = new HttpMethod[0];
		public RuleProcessor<Request, Response> processor;
		
		public Handler<Request, Response> handler;
		
		public Flag[] flags = new Flag[0];
		
		public Status status;
		
		public Condition condition;
		
		public RewriteRule<Request, Response> setReplace (String replace) {
			
			this.replace = replace;
			
			return this;
			
		}
		
		public RewriteRule<Request, Response> setFlags (Flag... flags) {
			
			this.flags = flags;
			
			return this;
			
		}
		
		public RewriteRule<Request, Response> setMethods (Method... methods) {
			
			this.methods = methods;
			
			return this;
			
		}
		
		public Method[] getMethods () {
			return methods;
		}
		
		/**
     * Add rewrite rule.
     *
     * @param handler Handler which will be called at this request
     */
		public RewriteRule (Handler<Request, Response> handler) {
			this.handler = handler;
		}
		
		/**
     * Add rewrite rule.
     *
     * @param processor Regex to rewrite initial request to server query (for example index.php?mod=$1&action=comments)
     */
		public RewriteRule (RuleProcessor<Request, Response> processor) {
			this.processor = processor;
		}
		
		public static class Condition {
			
			public uplx.server.router.Condition type;
			public Flag[] flags;
			
			public enum Flag {
				
				CASE_INSENSITIVE,
				
			}
			
			public Condition (uplx.server.router.Condition type) {
				this.type = type;
			}
			
			public Condition setFlags (Flag... flags) {
				
				this.flags = flags;
				
				return this;
				
			}
			
			public boolean checkFlag (Flag flag) {
				return Arrays.contains (flag, flags);
			}
			
		}
		
		/**
     * Rewrite condition to perform needed rules. You can set this conditions and add rules which you need to perform when this condition is true.
     * <pre>
     *	     if (ifRewriteCond (Condition.HTTP_REFERER, "!^http[s]://(www.)?site.com/")) {
     *
     *         addRewriteRule ("^([a-z]+)/comments(/?)+$", "index.php?mod=$1&action=comments", new Router.Listener () {
     *
     *					@Override
     *					public Module onSuccess (WebService app, Matcher matcher) throws ModuleException {
     *		         return new MyModule (app, matcher.group (1)).getComments ();
     *					}
     *
     *				});
     *
     *			}
     * </pre>
     *
     * @param cond	Condition
     * @return Returns true if condition have needed value, for example current referrer is equals to needed referrer.
     */
		public RewriteRule<Request, Response> setCondition (Condition cond) {
			
			condition = cond;
			
			return this;
			
		}
		
		public Matcher getMatcher (String regex, String value) {
			return Pattern.compile (
				regex,
					checkFlag (Flag.CASE_INSENSITIVE) ||
						condition == null || condition.checkFlag (Condition.Flag.CASE_INSENSITIVE) ? Pattern.CASE_INSENSITIVE : 0
				)
				.matcher (value);
		}
		
		public enum Flag {
			
			CONTINUE,
			APPEND_QUERY,
			CASE_INSENSITIVE,
			
		}
		
		public RewriteRule<Request, Response> setStatus (Status status) {
			
			this.status = status;
			
			return this;
			
		}
		
		public boolean checkFlag (Flag flag) {
			return Arrays.contains (flag, flags);
		}
		
	}