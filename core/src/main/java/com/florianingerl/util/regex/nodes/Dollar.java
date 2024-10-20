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
	
	/**
	 * Node to anchor at the end of a line or the end of input based on the
	 * multiline mode.
	 * <p>
	 * When not in multiline mode, the $ can only match at the very end of the
	 * input, unless the input ends in a line terminator in which it matches right
	 * before the last line terminator.
	 * <p>
	 * Note that \r\n is considered an atomic line terminator.
	 * <p>
	 * Like ^ the $ operator matches at a position, it does not match the line
	 * terminators themselves.
	 */
	public class Dollar extends Node {
		
		public boolean multiline;
		
		public Dollar (boolean mul) {
			multiline = mul;
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			int endIndex = (matcher.anchoringBounds) ? matcher.to : matcher.getTextLength ();
			if (!multiline) {
				if (i < endIndex - 2)
					return false;
				if (i == endIndex - 2) {
					char ch = seq.charAt (i);
					if (ch != '\r')
						return false;
					ch = seq.charAt (i + 1);
					if (ch != '\n')
						return false;
				}
			}
			// Matches before any line terminator; also matches at the
			// end of input
			// Before line terminator:
			// If multiline, we match here no matter what
			// If not multiline, fall through so that the end
			// is marked as hit; this must be a /r/n or a /n
			// at the very end so the end was hit; more input
			// could make this not match here
			if (i < endIndex) {
				char ch = seq.charAt (i);
				if (ch == '\n') {
					// No match between \r\n
					if (i > 0 && seq.charAt (i - 1) == '\r')
						return false;
					if (multiline)
						return getNext ().match (matcher, i, seq);
				} else if (ch == '\r' || ch == '\u0085' || (ch | 1) == '\u2029') {
					if (multiline)
						return getNext ().match (matcher, i, seq);
				} else { // No line terminator, no match
					return false;
				}
			}
			// Matched at current end so hit end
			matcher.hitEnd = true;
			// If a $ matches because of end of input, then more input
			// could cause it to fail!
			matcher.requireEnd = true;
			return getNext ().match (matcher, i, seq);
		}
		
		@Override
		public boolean study (TreeInfo info) {
			getNext ().study (info);
			return info.deterministic;
		}
		
	}