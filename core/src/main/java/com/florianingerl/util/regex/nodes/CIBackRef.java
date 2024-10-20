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
	
	import com.florianingerl.util.regex.ASCII;
	import com.florianingerl.util.regex.Matcher;
	import com.florianingerl.util.regex.TreeInfo;
	
	public class CIBackRef extends BackRefBase {
		
		public boolean doUnicodeCase;
		
		public CIBackRef (int groupCount, boolean doUnicodeCase) {
			super (groupCount);
			this.doUnicodeCase = doUnicodeCase;
		}
		
		public CIBackRef (boolean doUnicodeCase) {
			this.doUnicodeCase = doUnicodeCase;
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			int j = matcher.groups[groupIndex * 2];
			int k = matcher.groups[groupIndex * 2 + 1];
			
			int groupSize = k - j;
			// If the referenced group didn't match, neither can this
			if (j < 0)
				return false;
			// If there isn't enough input left no match
			if (i + groupSize > matcher.to) {
				matcher.hitEnd = true;
				return false;
			}
			// Check each new char to make sure it matches what the group
			// referenced matched last time around
			int x = i;
			for (int index = 0; index < groupSize; index++) {
				int c1 = Character.codePointAt (seq, x);
				int c2 = Character.codePointAt (seq, j);
				if (c1 != c2) {
					if (doUnicodeCase) {
						int cc1 = Character.toUpperCase (c1);
						int cc2 = Character.toUpperCase (c2);
						if (cc1 != cc2 && Character.toLowerCase (cc1) != Character.toLowerCase (cc2))
							return false;
					} else {
						if (ASCII.toLower (c1) != ASCII.toLower (c2))
							return false;
					}
				}
				int count = Character.charCount (c1);
				x += count;
				matcher.activity += count;
				j += Character.charCount (c2);
			}
			return getNext ().match (matcher, i + groupSize, seq);
		}
		
		@Override
		public boolean study (TreeInfo info) {
			info.maxValid = false;
			return getNext ().study (info);
		}
		
	}