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
	
	import com.florianingerl.util.regex.Node;
	import com.florianingerl.util.regex.Pattern;
	import com.florianingerl.util.regex.TreeInfo;
	
	public class CurlyBase extends Node {
		
		public Node beginNode;
		public int type;
		public int cmin;
		public int cmax;
		
		protected Pattern pattern;
		
		public CurlyBase (Pattern pattern, Node beginNode, int cmin, int cmax, int type) {
			
			this.pattern = pattern;
			
			new Node () {
				@Override
				public void setNext (Node a) {
					CurlyBase.this.setBeginNode (a);
					if (a != null)
						a.previous = this;
				}
			}.setNext (beginNode);
			this.cmin = cmin;
			this.cmax = cmax;
			this.type = type;
		}
		
		public void setBeginNode (Node beginNode) {
			this.beginNode = beginNode;
		}
		
		@Override
		public boolean study (TreeInfo info) {
			// Save original info
			int minL = info.minLength;
			int maxL = info.maxLength;
			boolean maxV = info.maxValid;
			boolean detm = info.deterministic;
			info.reset ();
			
			beginNode.study (info);
			
			int temp = info.minLength * cmin + minL;
			if (temp < minL) {
				temp = 0xFFFFFFF; // arbitrary large number
			}
			info.minLength = temp;
			
			if (maxV & info.maxValid) {
				temp = info.maxLength * cmax + maxL;
				info.maxLength = temp;
				if (temp < maxL) {
					info.maxValid = false;
				}
			} else {
				info.maxValid = false;
			}
			
			if (info.deterministic && cmin == cmax)
				info.deterministic = detm;
			else
				info.deterministic = false;
			return getNext ().study (info);
		}
		
	}