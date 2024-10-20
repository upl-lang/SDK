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
	
	package upl.parser;
	
	import upl.compiler.exceptions.UnexpectedTokenException;
	import upl.exceptions.EmptyException;
	import upl.lexer.Channel;
	import upl.lexer.Lexeme;
	import upl.lexer.Lexer;
	import upl.lexer.Token;
	
	public class Rule {
		
		protected int tokenIndex = 0;
		
		public Lexer lexer;
		public Token token;
		
		protected String expected;
		
		protected Rule[] rules;
		
		protected Rule (String expected) {
			this.expected = expected;
		}
		
		public Rule (Object... rules) {
			
			if (rules.length > 0) {
				
				this.rules = new Rule[rules.length];
				
				for (int i = 0; i < rules.length; i++) {
					
					Rule rule = toRule (rules[i]);
					
					//if (listener != null)
					//	listener.onInit (rule);
					
					this.rules[i] = rule;
					
				}
				
			} else throw new EmptyException ("Rules not found");
			
		}
		
		protected boolean getToken () {
			
			if (tokenIndex < lexer.tokens.length ()) {
				
				token = lexer.tokens.get (tokenIndex);
				return true;
				
			}
			
			return false;
			
		}
		
		protected Rule toRule (Object obj) {
			
			if (obj instanceof Rule)
				return (Rule) obj;
			else if (obj instanceof Lexeme)
				return new Rule (obj);
			else if (obj instanceof String)
				return new Rule (obj.toString ());
			else
				throw new IllegalArgumentException ();
			
		}
		
		public final boolean checkToken () {
			return token.name.equals (expected);
		}
		
		public final boolean isToken () {
			return expected != null;
		}
		
		public void process () {
			
			if (checkToken ())
				setRule ();
			else
				throw new UnexpectedTokenException (token, expected);
			
		}
		
		public void setRule () {
		
		}
		
		public Channel channel;
		
		public Rule setChannel (Channel channel) {
			
			this.channel = channel;
			
			return this;
			
		}
		
		public upl.lexer.Rule.Action type;
		
		public Rule setType (upl.lexer.Rule.Action type) {
			
			this.type = type;
			
			return this;
			
		}
		
		public String mode, prevMode;
		
		public Rule pushMode (String mode) {
			
			this.mode = mode;
			
			return this;
			
		}
		
		public Rule popMode () {
			
			mode = prevMode;
			return this;
			
		}
		
		public final void start () {
			
			process ();
			
			//if ((tokenIndex - 1) < tokens.length ())
			//	throw new UnexpectedTokenException (tokens.get (tokenIndex - 1));
			
		}
		
		@Override
		public String toString () {
			return (isToken () ? expected : getClass ().getSimpleName ());
		}
		
	}