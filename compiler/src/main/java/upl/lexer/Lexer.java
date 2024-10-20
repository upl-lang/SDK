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
	
	package upl.lexer;
	
	import upl.core.Log;
	import upl.exceptions.EmptyException;
	import upl.util.ArrayList;
	import upl.util.List;
	
	public abstract class Lexer {
		
		public List<Token> tokens;
		protected Lexeme[][] lexemes;
		protected int index = 0, line = 0;
		
		public StringBuilder content;
		
		public Lexer (StringBuilder builder) {
			this.content = builder;
		}
		
		protected Lexer (StringBuilder content, Lexeme[]... lexemes) {
			
			if (lexemes.length == 0)
				throw new EmptyException ("Lexemes not found");
			
			this.content = content;
			this.lexemes = lexemes;
			
		}
		
		public final List<Token> getTokens () {
			
			if (tokens == null) {
				
				int length = content.length ();
				
				tokens = new ArrayList<> ();
				int lexemesNum = 0, lexemeNum = 0;
				
				while (index < length) {
					
					for (Lexeme[] lexeme2 : lexemes) {
						
						int i = 0;
						lexemeNum++;
						
						for (Lexeme lexeme : lexeme2) {
							
							if (lexeme.getValue () != null) {
								
								Rule rule = Rule.toRule (lexeme.getValue ());
								
								rule.channel.regex = rule.regex;
								rule.channel.value = rule.value;
								Log.w (rule.channel.regex, rule.process (this));
								if (rule.process (this)) {
									
									Token token = new Token ();
									
									token.lexer = this;
									token.name = lexeme.getName ();
									token.value = rule.channel.value;
									token.regex = rule.regex;
									token.index = index;
									
									tokens.put (token);
									
									i++;
									
								}
								
							}
							
						}
						
						if (i == 1) lexemesNum++;
						
					}
					
					if (lexemeNum >= lexemes.length && lexemesNum == 0)
						break;
					
				}
				
			}
			
			return tokens;
			
		}
		
		public static abstract class Listener {
			
			public abstract void process (Token token);
			public abstract void onInit (Rule rule);
			
		}
		
		protected Listener listener;
		
		public void setListener (Listener listener) {
			this.listener = listener;
		}
		
	}