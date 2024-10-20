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
	
	import upl.compiler.Regex;
	import upl.exceptions.EmptyException;
	import upl.lexer.channels.DefaultChannel;
	
	public class Rule {
		
		public Token token;
		
		public Lexer lexer;
		public Regex regex;
		public String value;
		
		public Lexeme lexeme;
		
		protected Rule[] rules;
		
		public Rule (String value) {
			this.value = value;
		}
		
		public Rule (Regex regex) {
			this.regex = regex;
		}
		
		public Rule (Object... rules) {
			
			if (rules.length > 0) {
				
				this.rules = new Rule[rules.length];
				
				for (int i = 0; i < rules.length; i++) {
					
					Rule rule = toRule (rules[i]);
					
					rule.channel.regex = rule.regex;
					rule.channel.value = rule.value;
					
					this.rules[i] = rule;
					
				}
				
			} else throw new EmptyException ("Rules not found");
			
		}
		
		public Rule setRules () {
			
			rules = new Rule[rules.length];
			
			for (int i = 0; i < rules.length; i++) {
				
				Rule rule = toRule (rules[i]);
				
				rule.channel.regex = rule.regex;
				rule.channel.value = rule.value;
				
				rules[i] = rule;
				
			}
			
			return this;
			
		}
		
		public static Rule toRule (Object obj) {
			
			if (obj instanceof Regex)
				return new Rule ((Regex) obj);
			else if (obj instanceof Rule)
				return (Rule) obj;
			else
				return new Rule (obj.toString ());
			
		}
		
		public boolean process (Lexer lexer) {
			
			channel.lexer = lexer;
			
			return channel.process ();
			
		}
		
		protected Channel channel = new DefaultChannel ();
		
		public Rule setChannel (Channel channel) {
			
			this.channel = channel;
			
			return this;
			
		}
		
		public enum Action {
			
			CONTENT,
			ARGUMENT,
			
		}
		
		public Action type;
		
		public Rule setType (Action type) {
			
			this.type = type;
			
			return this;
			
		}
		
		public enum Mode {
			
			Argument,
			TargetLanguageAction,
			
		}
		
		public Mode mode, prevMode;
		
		public Rule pushMode (Mode mode) {
			
			this.mode = mode;
			
			return this;
			
		}
		
		public Rule popMode () {
			
			mode = prevMode;
			return this;
			
		}
		
		@Override
		public String toString () {
			return (regex != null ? regex.toString () : "");
		}
		
	}