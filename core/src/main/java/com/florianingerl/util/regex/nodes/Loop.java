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
	import com.florianingerl.util.regex.TreeInfo;
	
	/*
	 * Handles the repetition count for a greedy Curly. The matchInit is called from
	 * the Prolog to save the index of where the group beginning is stored. A zero
	 * length group check occurs in the normal match but is skipped in the
	 * matchInit.
	 */
	public class Loop extends Node {
		
		public Node body;
		public int countIndex; // local count index in matcher locals
		public int beginIndex; // group beginning index
		public int cmin, cmax;
		
		public Loop (int countIndex, int beginIndex) {
			this.countIndex = countIndex;
			this.beginIndex = beginIndex;
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) { // Avoid infinite loop in zero-length case.
			
			if (i > matcher.locals[beginIndex]) {
				
				int count = matcher.locals[countIndex];
				
				// This block is for before we reach the minimum
				// iterations required for the loop to match
				
				if (count < cmin) {
					
					matcher.locals[countIndex] = count + 1;
					
					boolean b = body.match (matcher, i, seq); // If match failed we must backtrack, so the loop count should NOT be incremented
					if (!b) matcher.locals[countIndex] = count; // Return success or failure since we are under minimum
					
					return b;
					
				} // This block is for after we have the minimum iterations required for the loop to match
				
				if (count < cmax) {
					
					matcher.locals[countIndex] = count + 1;
					
					boolean b = body.match (matcher, i, seq); // If match failed we must backtrack, so the loop count should NOT be incremented
					
					if (!b) matcher.locals[countIndex] = count;
					
					else return true;
					
				}
				
			}
			
			return getNext ().match (matcher, i, seq);
			
		}
		
		public boolean matchInit (Matcher matcher, int i, CharSequence seq) {
			
			int save = matcher.locals[countIndex];
			boolean ret;
			
			if (0 < cmin) {
				
				matcher.locals[countIndex] = 1;
				ret = body.match (matcher, i, seq);
				
			} else if (0 < cmax) {
				
				matcher.locals[countIndex] = 1;
				ret = body.match (matcher, i, seq);
				
				if (!ret) ret = getNext ().match (matcher, i, seq);
				
			} else ret = getNext ().match (matcher, i, seq);
			
			matcher.locals[countIndex] = save;
			
			return ret;
			
		}
		
		@Override
		public boolean study (TreeInfo info) {
			
			info.maxValid = false;
			info.deterministic = false;
			
			return false;
			
		}
		
	}