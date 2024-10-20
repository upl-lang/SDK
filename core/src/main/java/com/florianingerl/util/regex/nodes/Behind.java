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
	import java.util.Arrays;
	
	/**
	 * Zero width positive lookbehind.
	 */
	public class Behind extends BehindBase {
		
		public Behind (Node cond) {
			super (cond);
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			int savedFrom = matcher.from;
			boolean conditionMatched = false;
			int startIndex = (!matcher.transparentBounds) ? matcher.from : 0;
			int from = Math.max (i - rmax, startIndex);
			// Set end boundary
			int savedLBT = matcher.lookbehindTo;
			matcher.lookbehindTo = i;
			// Relax transparent region boundaries for lookbehind
			if (matcher.transparentBounds)
				matcher.from = 0;
			int save = matcher.captureTreeMode ? matcher.captureTreeNode.children.size () : -1;
			int[] savedGroups = Arrays.copyOf (matcher.groups, matcher.groups.length);
			for (int j = i - rmin; !conditionMatched && j >= from; j--) {
				conditionMatched = cond.match (matcher, j, seq);
			}
			matcher.from = savedFrom;
			matcher.lookbehindTo = savedLBT;
			if (conditionMatched) {
				conditionMatched = getNext ().match (matcher, i, seq);
				if (!conditionMatched) {
					matcher.groups = savedGroups;
					if (matcher.captureTreeMode) matcher.captureTreeNode.shrinkChildrenTo (save);
				}
			}
			return conditionMatched;
		}
		
	}