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
	import java.util.Arrays;
	
	public class AtomicGroup extends Node {
		
		public Node atom;
		
		public AtomicGroup (Node atom) {
			
			new Node () {
				@Override
				public void setNext (Node a) {
					
					AtomicGroup.this.atom = a;
					a.previous = this;
					
				}
				
			}.setNext (atom);
			
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			int[] saveGroups = Arrays.copyOf (matcher.groups, matcher.groups.length);
			int save = matcher.captureTreeMode ? matcher.captureTreeNode.getChildren ().size () : -1;
			if (atom.match (matcher, i, seq))
				i = matcher.last;
			else
				return false;
			boolean r = getNext ().match (matcher, i, seq);
			if (!r) {
				matcher.groups = saveGroups;
				if (matcher.captureTreeMode)
					matcher.captureTreeNode.shrinkChildrenTo (save);
			}
			return r;
		}
		
		@Override
		public boolean study (TreeInfo info) {
			atom.study (info);
			return getNext ().study (info);
		}
		
	}