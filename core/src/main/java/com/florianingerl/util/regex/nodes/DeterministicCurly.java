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
	import java.util.Stack;
	
	public class DeterministicCurly extends CurlyBase {
		
		public DeterministicCurly (Pattern pattern, Node beginNode, int cmin, int cmax, int type) {
			super (pattern, beginNode, cmin, cmax, type);
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			int j = 0;
			for (; j < cmin; ++j) {
				if (!beginNode.match (matcher, i, seq))
					return false;
				i = matcher.last;
			}
			
			if (type == Pattern.GREEDY) {
				Stack<Integer> backtrack = new Stack<> ();
				backtrack.push (i);
				for (; j < cmax; ++j) {
					if (!beginNode.match (matcher, i, seq))
						break;
					if (i == matcher.last)
						break;
					i = matcher.last;
					backtrack.push (i);
				}
				
				while (!backtrack.isEmpty ()) {
					i = backtrack.pop ();
					if (getNext ().match (matcher, i, seq))
						return true;
				}
				return false;
			} else if (type == Pattern.LAZY) {
				while (true) {
					if (getNext ().match (matcher, i, seq))
						return true;
					if (j >= cmax)
						return false;
					if (!beginNode.match (matcher, i, seq))
						return false;
					if (i == matcher.last)
						return false;
					i = matcher.last;
					++j;
				}
			} else { // type == POSSESSIVE
				for (; j < cmax; ++j) {
					if (!beginNode.match (matcher, i, seq))
						break;
					i = matcher.last;
				}
				return getNext ().match (matcher, i, seq);
			}
		}
		
	}