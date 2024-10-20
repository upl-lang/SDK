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
	import com.florianingerl.util.regex.TreeInfo;
	
	public class LazyLoop extends Loop {
		
		public LazyLoop (int countIndex, int beginIndex) {
			super (countIndex, beginIndex);
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) { // Check for zero length group
			
			if (i > matcher.locals[beginIndex]) {
				
				int count = matcher.locals[countIndex];
				
				if (count < cmin) {
					
					matcher.locals[countIndex] = count + 1;
					boolean result = body.match (matcher, i, seq); // If match failed we must backtrack, so the loop count should NOT be incremented
					
					if (!result) matcher.locals[countIndex] = count;
					
					return result;
					
				}
				
				if (getNext ().match (matcher, i, seq)) return true;
				
				if (count < cmax) {
					
					matcher.locals[countIndex] = count + 1;
					
					boolean result = body.match (matcher, i, seq); // If match failed we must backtrack, so // the loop count should NOT be incremented
					if (!result) matcher.locals[countIndex] = count;
					
					return result;
					
				}
				
				return false;
				
			}
			
			return getNext ().match (matcher, i, seq);
			
		}
		
		@Override
		public boolean matchInit (Matcher matcher, int i, CharSequence seq) {
			
			int save = matcher.locals[countIndex];
			boolean ret = false;
			
			if (0 < cmin) {
				matcher.locals[countIndex] = 1;
				ret = body.match (matcher, i, seq);
			} else if (getNext ().match (matcher, i, seq)) {
				ret = true;
			} else if (0 < cmax) {
				matcher.locals[countIndex] = 1;
				ret = body.match (matcher, i, seq);
			}
			
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