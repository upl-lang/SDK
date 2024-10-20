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
	 * multiline mode when in unix lines mode.
	 */
	public class UnixDollar extends Node {
		
		public boolean multiline;
		
		public UnixDollar (boolean mul) {
			multiline = mul;
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			int endIndex = (matcher.anchoringBounds) ? matcher.to : matcher.getTextLength ();
			if (i < endIndex) {
				char ch = seq.charAt (i);
				if (ch == '\n') {
					// If not multiline, then only possible to
					// match at very end or one before end
					if (!multiline && i != endIndex - 1)
						return false;
					// If multiline return getNext().match without setting
					// matcher.hitEnd
					if (multiline)
						return getNext ().match (matcher, i, seq);
				} else {
					return false;
				}
			}
			// Matching because at the end or 1 before the end;
			// more input could change this so set hitEnd
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
