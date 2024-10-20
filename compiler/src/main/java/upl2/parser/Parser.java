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
	
	package upl2.parser;
	
	import upl.core.Log;
	import upl.json.JSONArray;
	import upl.json.JSONObject;
	import upl2.lexer.Lexeme;
	import upl2.lexer.Lexer;
	
	public abstract class Parser {
		
		protected JSONArray tree = new JSONArray ();
		protected Lexer lexer = getLexer ();
		protected char[] stream;
		protected StringBuilder string = new StringBuilder ();
		
		protected int index = 0;
		protected char ch;
		
		public Parser (char[] stream) {
			this.stream = stream;
		}
		
		protected Lexer getLexer () {
			return new Lexer () {};
		}
		
		public abstract void process ();
		
		public abstract static class Rule {
			
			protected Parser parser;
			
			protected Quantifier quantifier;
			protected String token;
			protected Case cond;
			
			public enum Quantifier {
				
				ZERO_OR_MORE,
				ONE,
				ONE_OR_MORE,
				ZERO_OR_ONE,
				
			}
			
			public enum Case {
				
				AND,
				OR,
				
			}
			
			public interface Listener {
				String onRule (String name);
			}
			
			protected Listener listener;
			
			protected abstract void ruleProcess ();
			
			public final JSONArray process () {
				
				JSONArray output = new JSONArray ();
				
				while (parser.index < parser.stream.length) {
					
					parser.next ();
					
					while (parser.ch == ' ') parser.next ();
					
					if (parser.ch == '(')
						output.put (process ());
					else if (parser.ch == ')')
						break;
					else {
						
						parser.string.append (parser.ch);
						ruleProcess ();
						
					}
					
				}
				
				parser.tree = output;
				
				return output;
				
			}
			
			protected String getString () {
				return parser.string.toString ();
			}
			
			protected Lexeme getLexeme (String name) {
				return parser.lexer.lexemes.get (name);
			}
			
			protected void clean () {
				parser.string = new StringBuilder ();
			}
			
			protected Rule setParser (Parser parser) {
				
				this.parser = parser;
				return this;
				
			}
			
			protected void error (String msg) {
				Log.w (msg);
			}
			
		}
		
		protected void next () {
			
			ch = stream[index];
			index++;
			
		}
		
		public JSONObject item = new JSONObject ();
		
		protected void setRule (Rule rule) {
			
			item = new JSONObject ();
			
			rule.setParser (this).process ();
			
			tree.put (item);
			
		}
		
		public JSONArray getTree () {
			return tree;
		}
		
	}