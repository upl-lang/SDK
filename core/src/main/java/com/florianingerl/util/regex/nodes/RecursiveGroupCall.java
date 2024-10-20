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
	import com.florianingerl.util.regex.Pattern;
	import com.florianingerl.util.regex.TreeInfo;
	import java.util.Arrays;
	
	public class RecursiveGroupCall extends Node {
		
		protected Pattern pattern;
		public GroupHead groupHead;
		public GroupTail groupTail;
		public boolean recursion;
		public boolean inLookaround;
		
		public RecursiveGroupCall (Pattern pattern, int groupNumber, boolean recursion, boolean inLookaround) {
			this (pattern, recursion, inLookaround);
			setGroupNumber (groupNumber);
		}
		
		public RecursiveGroupCall (Pattern pattern, boolean recursion, boolean inLookaround) {
			this.pattern = pattern;
			this.recursion = recursion;
			this.inLookaround = inLookaround;
		}
		
		public void setGroupNumber (int groupNumber) {
			Pattern.GroupHeadAndTail ghat = pattern.groupHeadAndTailNodes ().get (groupNumber);
			groupHead = ghat.groupHead;
			groupTail = ghat.groupTail;
		}
		
		public class InternalRecursiveGroupCall extends Node {
			
			public boolean first = true;
			public Node groupTailsNext;
			public int[] savedGroups;
			public int[] savedRecursion = new int[3];
			
			@Override
			public boolean match (Matcher matcher, int i, CharSequence seq) {
				if (first) {
					first = false;
					for (int k = 0; k < 3; ++k) {
						savedRecursion[k] = matcher.recursions[groupHead.groupIndex * 3 + k];
					}
					if (matcher.recursions[groupHead.groupIndex * 3] == i) {
						int activity = matcher.activity - matcher.recursions[groupHead.groupIndex * 3 + 1];
						if (activity <= matcher.recursions[groupHead.groupIndex * 3 + 2]) {
							return false;
						}
						matcher.recursions[groupHead.groupIndex * 3 + 2] = activity;
					}
					matcher.recursions[groupHead.groupIndex * 3] = i;
					matcher.recursions[groupHead.groupIndex * 3 + 1] = matcher.activity;
					groupTailsNext = groupTail.getNext (matcher);
					groupTail.setNext (matcher, this);
					if (recursion) {
						savedGroups = matcher.groups;
						matcher.groups = new int[matcher.groups.length];
						Arrays.fill (matcher.groups, -1);
					}
					boolean r = groupHead.match (matcher, i, seq);
					groupTail.setNext (matcher, groupTailsNext);
					if (recursion)
						matcher.groups = savedGroups;
					for (int k = 0; k < 3; ++k) {
						matcher.recursions[groupHead.groupIndex * 3 + k] = savedRecursion[k];
					}
					return r;
				} else {
					groupTail.setNext (matcher, groupTailsNext);
					if (matcher.captureTreeMode) {
						CaptureTreeNode t = matcher.captureTreeNode.children.getLast ();
						t.recursion = recursion;
						t.inLookaround = inLookaround;
					}
					int saveStart = -1, saveEnd = -1;
					int[] z = null;
					if (recursion) {
						saveStart = savedGroups[groupHead.groupIndex * 2];
						saveEnd = savedGroups[groupHead.groupIndex * 2 + 1];
						savedGroups[groupHead.groupIndex * 2] = matcher.groups[groupHead.groupIndex * 2];
						savedGroups[groupHead.groupIndex * 2 + 1] = matcher.groups[groupHead.groupIndex * 2 + 1];
						z = matcher.groups;
						matcher.groups = savedGroups;
					}
					int[] y = new int[3];
					for (int k = 0; k < 3; ++k) {
						y[k] = matcher.recursions[groupHead.groupIndex * 3 + k];
						matcher.recursions[groupHead.groupIndex * 3 + k] = savedRecursion[k];
					}
					boolean r = RecursiveGroupCall.this.getNext ().match (matcher, i, seq);
					if (recursion) {
						if (!r) {
							matcher.groups[groupHead.groupIndex * 2] = saveStart;
							matcher.groups[groupHead.groupIndex * 2 + 1] = saveEnd;
						}
						matcher.groups = z;
					}
					groupTail.setNext (matcher, this);
					for (int k = 0; k < 3; ++k) {
						matcher.recursions[groupHead.groupIndex * 3 + k] = y[k];
					}
					return r;
				}
			}
			
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			InternalRecursiveGroupCall ircc = new InternalRecursiveGroupCall ();
			return ircc.match (matcher, i, seq);
		}
		
		@Override
		public boolean study (TreeInfo info) {
			if (info.recursive.containsKey (groupTail.groupIndex) && info.recursive.get (groupTail.groupIndex)) {
				info.maxValid = false;
				info.minLength = 0xFFFFFFF; // arbitrary large number
				return false;
			}
			info.recursive.put (groupTail.groupIndex, true);
			groupHead.study (info);
			info.recursive.put (groupTail.groupIndex, false);
			info.deterministic = false;
			return getNext ().study (info);
		}
		
	}
