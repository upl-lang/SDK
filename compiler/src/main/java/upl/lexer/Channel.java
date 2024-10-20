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
	
	import java.util.regex.Matcher;
	import java.util.regex.Pattern;
	import upl.compiler.Regex;
	import upl.core.Log;
	
	public abstract class Channel {
		
		public Lexer lexer;
		protected Matcher matcher;
		
		public String value;
		public Regex regex;
		
		protected int flags;
		
		public Channel () {
			this (0);
		}
		
		public Channel (int flags) {
			this.flags = flags;
		}
		
		public abstract boolean process ();
		
		protected boolean find () {
			
			String regex;
			
			if (value != null)
				regex = value.replaceAll ("[.\\\\+*?\\[\\]^$(){}=!<>|:\\-#~]", "\\\\$0");
			else
				regex = this.regex.toString ();
			
			matcher = Pattern.compile ("^" + regex, flags).matcher (lexer.content);
			
			return matcher.find ();
			
		}
		
		protected void setIndex () {
			lexer.index += matcher.end ();
		}
		
		protected void setValue () {
			
			if (lexer.content.substring (0, matcher.end ()).contains ("\n"))
				lexer.line++;
			
		}
		
		protected void offsetContent () {
			lexer.content.delete (0, matcher.end ());
		}
		
	}