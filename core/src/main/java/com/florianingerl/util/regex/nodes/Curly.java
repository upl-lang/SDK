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
	import java.util.Arrays;
	
	/**
	 * Handles the curly-brace style repetition with a specified minimum and maximum
	 * occurrences. The * quantifier is handled as a special case. This class
	 * handles the three types.
	 */
	public class Curly extends CurlyBase {
		
		public Navigator endNode;
		
		public Curly (Pattern pattern, Node beginNode, int cmin, int cmax, int type) {
			super (pattern, beginNode, cmin, cmax, type);
		}
		
		@Override
		public void setBeginNode (Node beginNode) {
			super.setBeginNode (beginNode);
			Node end = findEndNode (beginNode);
			this.endNode = pattern.createNavigator (end);
		}
		
		public Node findEndNode (Node beginNode) {
			Node end = beginNode;
			while (end.getNext () != null && end.getNext () != Pattern.accept)
				end = end.getNext ();
			return end;
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			
			if (type == Pattern.GREEDY) {
				CurlyRepeater mgr = new CurlyRepeater (this, this.getNext (), cmax - cmin, true);
				CurlyRepeater mr = new CurlyRepeater (this, mgr, cmin, false);
				return mr.match (matcher, i, seq);
			} else if (type == Pattern.LAZY) {
				CurlyLazyRepeater mlr = new CurlyLazyRepeater (this, cmax - cmin);
				CurlyRepeater mr = new CurlyRepeater (this, mlr, cmin, false);
				return mr.match (matcher, i, seq);
			} else { // type == POSSESSIVE
				CurlyRepeater mr = new CurlyRepeater (this, Pattern.accept, cmin, false);
				// Vector<Stack<Capture>> captures = matcher.cloneCaptures();
				int save = matcher.captureTreeMode ? matcher.captureTreeNode.children.size () : -1;
				int[] savedGroups = Arrays.copyOf (matcher.groups, matcher.groups.length);
				if (!mr.match (matcher, i, seq))
					return false;
				i = matcher.last;
				int j = cmin;
				for (; j < cmax; ++j) {
					if (!beginNode.match (matcher, i, seq))
						break;
					i = matcher.last;
				}
				if (getNext ().match (matcher, i, seq))
					return true;
				matcher.groups = savedGroups;
				if (matcher.captureTreeMode)
					matcher.captureTreeNode.shrinkChildrenTo (save);
				return false;
			}
			
		}
		
	}