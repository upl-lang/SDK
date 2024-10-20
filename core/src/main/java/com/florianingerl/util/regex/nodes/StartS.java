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
	
	/*
	 * StartS supports supplementary characters, including unpaired surrogates.
	 */
	public class StartS extends Start {
		
		public StartS (Node node) {
			super (node);
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			if (i > matcher.to - minLength) {
				matcher.hitEnd = true;
				return false;
			}
			int guard = matcher.to - minLength;
			while (i <= guard) {
				// if ((ret = getNext().match(matcher, i, seq)) || i == guard)
				if (getNext ().match (matcher, i, seq)) {
					matcher.first = i;
					matcher.setGroup0 (seq, matcher.first, matcher.last);
					return true;
				}
				if (i == guard)
					break;
				// Optimization to move to the next character. This is
				// faster than countChars(seq, i, 1).
				if (Character.isHighSurrogate (seq.charAt (i++))) {
					if (i < seq.length () && Character.isLowSurrogate (seq.charAt (i))) {
						i++;
					}
				}
			}
			
			matcher.hitEnd = true;
			
			return false;
			
		}
		
	}