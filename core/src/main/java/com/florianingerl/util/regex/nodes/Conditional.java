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
	import com.florianingerl.util.regex.TreeInfo;
	
	public class Conditional extends Node {
		
		public Node yes;
		public Node not;
		
		public void setYes (Node yes) {
			
			new Node () {
				@Override
				public void setNext (Node a) {
					Conditional.this.yes = a;
					if (a != null)
						a.previous = this;
				}
				
			}.setNext (yes);
			
		}
		
		public void setNot (Node not) {
			new Node () {
				@Override
				public void setNext (Node a) {
					Conditional.this.not = a;
					if (a != null)
						a.previous = this;
				}
			}.setNext (not);
		}
		
		@Override
		public boolean study (TreeInfo info) {
			int minL = info.minLength;
			int maxL = info.maxLength;
			boolean maxV = info.maxValid;
			
			int minL2; // arbitrary large enough num
			int maxL2 = -1;
			info.reset ();
			yes.study (info);
			minL2 = info.minLength;
			maxL2 = Math.max (maxL2, info.maxLength);
			maxV = (maxV & info.maxValid);
			if (not != null) {
				info.reset ();
				not.study (info);
				minL2 = Math.min (minL2, info.minLength);
				maxL2 = Math.max (maxL2, info.maxLength);
				maxV = (maxV & info.maxValid);
				
			} else {
				info.reset ();
				getNext ().study (info);
				minL2 = Math.min (minL2, info.minLength);
				// Maximum can't get higher as with yes or no
			}
			
			info.minLength = minL + minL2;
			info.maxLength = maxL + maxL2;
			info.maxValid = maxV;
			info.deterministic = false;
			
			return false;
			
		}
		
	}