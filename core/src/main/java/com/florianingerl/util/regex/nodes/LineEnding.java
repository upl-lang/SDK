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
	 * Node class that matches a Unicode line ending '\R'
	 */
	public class LineEnding extends Node {
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			// (u+000Du+000A|[u+000Au+000Bu+000Cu+000Du+0085u+2028u+2029])
			if (i < matcher.to) {
				int ch = seq.charAt (i);
				if (ch == 0x0A || ch == 0x0B || ch == 0x0C || ch == 0x85 || ch == 0x2028 || ch == 0x2029)
					return getNext ().match (matcher, i + 1, seq);
				if (ch == 0x0D) {
					i++;
					if (i < matcher.to && seq.charAt (i) == 0x0A)
						i++;
					return getNext ().match (matcher, i, seq);
				}
			} else {
				matcher.hitEnd = true;
			}
			return false;
		}
		
		@Override
		public boolean study (TreeInfo info) {
			info.minLength++;
			info.maxLength += 2;
			return getNext ().study (info);
		}
		
	}