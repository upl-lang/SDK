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
	
	import com.florianingerl.util.regex.Capture;
	import com.florianingerl.util.regex.CaptureTreeNode;
	import com.florianingerl.util.regex.Matcher;
	import com.florianingerl.util.regex.TreeInfo;
	
	/**
	 * The GroupTail handles the setting of group beginning and ending locations
	 * when groups are successfully matched. It must also be able to unset groups
	 * that have to be backed off of.
	 * <p>
	 * The GroupTail node is also used when a previous group is referenced, and in
	 * that case no group information needs to be set.
	 */
	public class GroupTail extends Navigator {
		
		public int groupIndex;
		
		public GroupTail (int localCount, int groupCount) {
			super (localCount);
			// if groupCount <= 0, it's an anonymous group
			groupIndex = groupCount;
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			int tmp = matcher.localVector.get (localIndex).pop ();
			
			CaptureTreeNode t = null;
			int saveStart = matcher.groups[groupIndex * 2];
			int saveEnd = matcher.groups[groupIndex * 2 + 1];
			if (groupIndex > 0) {
				matcher.groups[groupIndex * 2] = tmp;
				matcher.groups[groupIndex * 2 + 1] = i;
				if (matcher.captureTreeMode) {
					matcher.captureTreeNode.capture = new Capture (seq, tmp, i);
					t = matcher.captureTreeNode;
					matcher.captureTreeNode = t.parent;
				}
			}
			boolean r = getNext (matcher).match (matcher, i, seq);
			if (groupIndex > 0) {
				if (t != null) matcher.captureTreeNode = t;
				if (!r) {
					matcher.groups[groupIndex * 2] = saveStart;
					matcher.groups[groupIndex * 2 + 1] = saveEnd;
					if (t != null) t.capture = null;
				}
			}
			matcher.localVector.get (localIndex).push (tmp);
			return r;
		}
		
		@Override
		public boolean study (TreeInfo info) {
			info.deterministic = false;
			return getNext ().study (info);
		}
		
	}