	/*
	 * Copyright (c) 2020 - 2024 UPL Foundation
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 * 	  http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */
	
	package com.florianingerl.util.regex.nodes;
	
	import com.florianingerl.util.regex.Matcher;
	import com.florianingerl.util.regex.Node;
	
	/**
	 * Node to anchor at the beginning of a line. This is essentially the object to
	 * match for the multiline ^.
	 */
	public class Caret extends Node {
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			int startIndex = matcher.from;
			int endIndex = matcher.to;
			if (!matcher.anchoringBounds) {
				startIndex = 0;
				endIndex = matcher.getTextLength ();
			}
			// Perl does not match ^ at end of input even after newline
			if (i == endIndex) {
				matcher.hitEnd = true;
				return false;
			}
			if (i > startIndex) {
				char ch = seq.charAt (i - 1);
				if (ch != '\n' && ch != '\r' && (ch | 1) != '\u2029' && ch != '\u0085') {
					return false;
				}
				// Should treat /r/n as one newline
				if (ch == '\r' && seq.charAt (i) == '\n')
					return false;
			}
			return getNext ().match (matcher, i, seq);
		}
		
	}