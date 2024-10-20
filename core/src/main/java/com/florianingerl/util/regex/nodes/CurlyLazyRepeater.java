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
	
	public class CurlyLazyRepeater extends Node {
		
		protected Curly curly;
		public int counter;
		public Node initialEndNext;
		public int beginIndex = -1;
		
		public CurlyLazyRepeater (Curly curly, int cmax) {
			this.curly = curly;
			this.counter = cmax;
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			
			if (beginIndex >= i)
				return false;
			if (initialEndNext == null)
				initialEndNext = curly.endNode.getNext (matcher);
			Node oldEndNodeNext = curly.endNode.getNext (matcher);
			curly.endNode.setNext (matcher, initialEndNext);
			boolean r = curly.getNext ().match (matcher, i, seq);
			curly.endNode.setNext (matcher, oldEndNodeNext);
			if (r)
				return true;
			
			if (counter == 0) {
				return false;
			}
			--counter;
			oldEndNodeNext = curly.endNode.getNext (matcher);
			curly.endNode.setNext (matcher, this);
			int oldBeginIndex = beginIndex;
			beginIndex = i;
			r = curly.beginNode.match (matcher, i, seq);
			++counter;
			curly.endNode.setNext (matcher, oldEndNodeNext);
			beginIndex = oldBeginIndex;
			
			return r;
			
		}
		
	}