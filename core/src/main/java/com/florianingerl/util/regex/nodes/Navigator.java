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
	
	/**
	 * The following classes are the building components of the object tree that
	 * represents a compiled regular expression. The object tree is made of
	 * individual elements that handle constructs in the Pattern. Each type of
	 * object knows how to match its equivalent construct with the match() method.
	 */
	
	public class Navigator extends Node {
		
		protected int localIndex;
		
		public Navigator (int localIndex) {
			this.localIndex = localIndex;
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			return getNext (matcher).match (matcher, i, seq);
		}
		
		public Node getNext (Matcher matcher) {
			if (matcher.nextNodes[localIndex] != null)
				return matcher.nextNodes[localIndex];
			return getNext ();
		}
		
		public void setNext (Matcher matcher, Node next) {
			matcher.nextNodes[localIndex] = next;
		}
		
	}