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
	import com.florianingerl.util.regex.Pattern;
	import com.florianingerl.util.regex.UnicodeProp;
	
	/**
	 * Handles word boundaries. Includes a field to allow this one class to deal
	 * with the different types of word boundaries we can match. The word characters
	 * include underscores, letters, and digits. Non spacing marks can are also part
	 * of a word if they have a base character, otherwise they are ignored for
	 * purposes of finding word boundaries.
	 */
	public class Bound extends Node {
		
		public static int LEFT = 0x1;
		public static int RIGHT = 0x2;
		public static int BOTH = 0x3;
		public static int NONE = 0x4;
		public int type;
		public boolean useUWORD;
		
		public Bound (int n, boolean useUWORD) {
			type = n;
			this.useUWORD = useUWORD;
		}
		
		public boolean isWord (int ch) {
			return useUWORD ? UnicodeProp.WORD.is (ch) : (ch == '_' || Character.isLetterOrDigit (ch));
		}
		
		public int check (Matcher matcher, int i, CharSequence seq) {
			int ch;
			boolean left = false;
			int startIndex = matcher.from;
			int endIndex = matcher.to;
			if (matcher.transparentBounds) {
				startIndex = 0;
				endIndex = matcher.getTextLength ();
			}
			if (i > startIndex) {
				ch = Character.codePointBefore (seq, i);
				left = (isWord (ch) || ((Character.getType (ch) == Character.NON_SPACING_MARK)
					                        && Pattern.hasBaseCharacter (matcher, i - 1, seq)));
			}
			boolean right = false;
			if (i < endIndex) {
				ch = Character.codePointAt (seq, i);
				right = (isWord (ch) || ((Character.getType (ch) == Character.NON_SPACING_MARK)
					                         && Pattern.hasBaseCharacter (matcher, i, seq)));
			} else {
				// Tried to access char past the end
				matcher.hitEnd = true;
				// The addition of another char could wreck a boundary
				matcher.requireEnd = true;
			}
			return ((left ^ right) ? (right ? LEFT : RIGHT) : NONE);
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			return (check (matcher, i, seq) & type) > 0 && getNext ().match (matcher, i, seq);
		}
		
	}