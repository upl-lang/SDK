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
	import java.util.Arrays;
	
	public class ConditionalLookahead extends Conditional {
		
		public Pos cond;
		
		public ConditionalLookahead (Pos cond) {
			this.cond = cond;
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			int save = matcher.captureTreeMode ? matcher.captureTreeNode.children.size () : -1;
			int[] savedGroups = Arrays.copyOf (matcher.groups, matcher.groups.length);
			if (cond.match (matcher, i, seq)) {
				if (!yes.match (matcher, i, seq)) {
					matcher.groups = savedGroups;
					if (matcher.captureTreeMode) matcher.captureTreeNode.shrinkChildrenTo (save);
					return false;
				}
				return true;
			} else if (not != null) {
				return not.match (matcher, i, seq);
			} else {
				return getNext ().match (matcher, i, seq);
			}
		}
		
	}