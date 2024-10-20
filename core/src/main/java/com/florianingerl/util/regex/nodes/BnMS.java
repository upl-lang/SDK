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
	
	/**
	 * Supplementary support version of BnM(). Unpaired surrogates are also handled
	 * by this class.
	 */
	public class BnMS extends BnM {
		
		public int lengthInChars;
		
		public BnMS (int[] src, int[] lastOcc, int[] optoSft, Node next) {
			super (src, lastOcc, optoSft, next);
			for (int i : buffer) {
				lengthInChars += Character.charCount (i);
			}
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			int[] src = buffer;
			int patternLength = src.length;
			int last = matcher.to - lengthInChars;
			
			// Loop over all possible match positions in text
				NEXT:
			while (i <= last) {
				// Loop over pattern from right to left
				int ch;
				for (int j = Pattern.countChars (seq, i, patternLength), x = patternLength - 1; j > 0; j -= Character
					.charCount (ch), x--) {
					ch = Character.codePointBefore (seq, i + j);
					if (ch != src[x]) {
						// Shift search to the right by the maximum of the
						// bad character shift and the good suffix shift
						int n = Math.max (x + 1 - lastOcc[ch & 0x7F], optoSft[x]);
						i += Pattern.countChars (seq, i, n);
						continue NEXT;
					}
				}
				// Entire pattern matched starting at i
				matcher.first = i;
				boolean ret = getNext ().match (matcher, i + lengthInChars, seq);
				if (ret) {
					matcher.first = i;
					matcher.setGroup0 (seq, matcher.first, matcher.last);
					return true;
				}
				i += Pattern.countChars (seq, i, 1);
			}
			
			matcher.hitEnd = true;
			
			return false;
			
		}
		
	}