	/*
	 * Copyright 2020 - 2024 UPL Foundation
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 *		http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */
	
	package upl.compiler.exceptions;
	
	import upl.lexer.Token;
	
	public class UnexpectedTokenException extends RuntimeException {
		
		public Token unexpected;
		public String expected;
		
		public UnexpectedTokenException (Token unexpected) {
			this (unexpected, null);
		}
		
		public UnexpectedTokenException (Token unexpected, String expected) {
			
			this.unexpected = unexpected;
			this.expected = expected;
			
		}
		
	}