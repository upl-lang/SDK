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
	
	package upl2.parser.parsers;
	
	import upl2.lexer.Lexeme;
	import upl2.lexer.lexers.DefaultLexer;
	import upl2.compiler.Element;
	import upl2.lexer.Lexer;
	import upl2.parser.Parser;
	import upl2.parser.generators.JavaGenerator;
	import upl2.parser.rules.LettersRule;
	
	public class JavaParser extends Parser {
		
		public JavaParser (char[] stream) {
			super (stream);
		}
		
		StringBuilder builder = new StringBuilder ();
		
		@Override
		protected Lexer getLexer () {
			
			return new DefaultLexer ()
				.addLexeme (new Lexeme (JavaGenerator.Element.IMPORT, new LettersRule (Rule.Quantifier.ONE, Rule.Case.AND, "import")))
				.addLexeme (new Lexeme (Element.CLASS, new LettersRule (Rule.Quantifier.ONE, Rule.Case.AND, "class")))
				.addLexeme (new Lexeme ("identifier", new LettersRule (Rule.Quantifier.ZERO_OR_MORE, Rule.Case.AND, "class")));
			
		}
		
		@Override
		public void process () {
			
			classDeclaration ();
			
		}
		
		String className (String name) {
			return name;
		}
		
		void classDeclaration () {
			//setRule (new LexemeRule (Rule.Quantifier.ONE, Element.CLASS));
		}
		
	}