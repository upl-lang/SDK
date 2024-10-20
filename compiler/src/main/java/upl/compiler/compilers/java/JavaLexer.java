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
	
	package upl.compiler.compilers.java;
	
	import upl.compiler.Regex;
	import upl.lexer.BaseLexer;
	import upl.lexer.Lexeme;
	import upl.lexer.Rule;
	import upl.lexer.channels.SkipChannel;
	import upl.lexer.rules.RuleAnd;
	import upl.lexer.rules.RuleAny;
	
	public class JavaLexer extends BaseLexer {
		
		public JavaLexer (StringBuilder builder) {
			super (builder, JavaLexeme.values (), BaseLexeme.values ());
		}
		
		public enum JavaLexeme implements Lexeme {
			
			IMPORT ("import"),
			//SYMBOL (JavaLetter ()),
			LPAREN ("{"),
			RPAREN ("}"),
			LBRACKET ("("),
			RBRACKET (")"),
			TWO ("=="),
			OPERATOR (new Regex ("[\\+\\-\\*/=;]")),
			DIGIT (new RuleAnd (JavaLetter (), JavaDigit ())),
			WS (new Rule (new Regex ("[ \\t\\r\\n\\u000C]+")).setChannel (new SkipChannel ())),
			//ERRCHAR (new RuleAny ().setChannel (new SkipChannel ())),
			;
			
			public final Object value;
			
			JavaLexeme (Object value) {
				this.value = value;
			}
			
			@Override
			public String getName () {
				return name ();
			}
			
			@Override
			public Object getValue () {
				return value;
			}
			
			private static Regex JavaDigit () {
				return new Regex ("[0-9$_]+");
			}
			
			private static Regex JavaLetter () {
				return new Regex ("[a-zA-Z$_]+");
			}
			
		}
		
	}