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
	
	import com.florianingerl.util.regex.CaptureTreeNode;
	import com.florianingerl.util.regex.Matcher;
	import com.florianingerl.util.regex.Node;
	
	/**
	 * The GroupHead saves the location where the group begins in the locals and
	 * restores them when the match is done.
	 * <p>
	 * The matchRef is used when a reference to this group is accessed later in the
	 * expression. The locals will have a negative value in them to indicate that we
	 * do not want to unset the group if the reference doesn't match.
	 */
	public class GroupHead extends Node {
		
		public int localIndex;
		public int groupIndex;
		public boolean inLookaround;
		
		public GroupHead (int localCount, int groupCount, boolean inLookaround) {
			localIndex = localCount;
			groupIndex = groupCount;
			this.inLookaround = inLookaround;
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			
			CaptureTreeNode t = null;
			if (groupIndex > 0 && matcher.captureTreeMode) {
				t = new CaptureTreeNode ();
				t.recursion = false;
				t.inLookaround = inLookaround;
				t.groupNumber = groupIndex;
				t.parent = matcher.captureTreeNode;
				matcher.captureTreeNode.children.add (t);
				matcher.captureTreeNode = t;
			}
			matcher.localVector.get (localIndex).push (i);
			boolean r = getNext ().match (matcher, i, seq);
			matcher.localVector.get (localIndex).pop ();
			if (t != null) {
				matcher.captureTreeNode = t.parent;
				if (!r)
					matcher.captureTreeNode.children.remove (t);
			}
			return r;
		}
		
	}