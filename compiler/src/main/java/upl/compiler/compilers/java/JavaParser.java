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
	
	import upl.lexer.Lexer;
	import upl.lexer.Rule;
	import upl.lexer.rules.RuleAnd;
	import upl.lexer.rules.RuleQuestion;
	import upl.parser.Parser;
	
	public class JavaParser extends Parser {
		
		public JavaParser (Lexer lexer) {
			super (lexer);
		}
		
		public JavaParser (StringBuilder builder) {
			super (new JavaLexer (builder));
		}
		
		public Rule compilationUnit () {
			
			return new RuleAnd (
				JavaLexer.JavaLexeme.IMPORT,
				//"{",
				new RuleQuestion (
					new RuleAnd (
						//JavaLexer.JavaLexeme.STRING,
						JavaLexer.JavaLexeme.DIGIT
					)
				)
			);
			
		}
		
	}