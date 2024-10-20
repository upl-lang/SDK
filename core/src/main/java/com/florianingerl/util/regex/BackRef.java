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
	
	package com.florianingerl.util.regex;
	
	import com.florianingerl.util.regex.nodes.BackRefBase;
	
	/**
	 * Refers to a group in the regular expression. Attempts to match whatever the
	 * group referred to last matched.
	 */
	public class BackRef extends BackRefBase {
		
		public BackRef (int groupCount) {
			super (groupCount);
		}
		
		public BackRef () {
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			// If the referenced group didn't match, neither can this
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
			for (int index = 0; index < groupSize; index++) {
				if (seq.charAt (i + index) != seq.charAt (j + index))
					return false;
				++matcher.activity;
			}
			return getNext ().match (matcher, i + groupSize, seq);
			/*
			 * Capture last = matcher.captureTreeNode.findGroup(groupIndex); if (last ==
			 * null) return false;
			 *
			 * int j = last.getStart(); int k = last.getEnd();
			 *
			 * int groupSize = k - j;
			 *
			 * // If there isn't enough input left no match if (i + groupSize > matcher.to)
			 * { matcher.hitEnd = true; return false; } // Check each new char to make sure
			 * it matches what the group // referenced matched last time around for (int
			 * index = 0; index < groupSize; index++) if (seq.charAt(i + index) !=
			 * seq.charAt(j + index)) return false;
			 *
			 * return getNext().match(matcher, i + groupSize, seq);
			 */
		}
		
		@Override
		public boolean study (TreeInfo info) {
			info.maxValid = false;
			return getNext ().study (info);
		}
		
	}
