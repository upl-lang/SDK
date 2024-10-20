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
	
	package com.florianingerl.util.regex;
	
	/**
	 * Base class for all node classes. Subclasses should override the match ()
	 * method as appropriate. This class is an accepting node, so its match () always
	 * returns true.
	 */
	public class Node {
		
		public Node next;
		public Node previous;
		
		public Node getNext () {
			return next;
		}
		
		public Node getPrevious () {
			return previous;
		}
		
		public void setNext (Node next) {
			this.next = next;
			next.previous = this;
		}
		
		public Node () {
			this.next = Pattern.accept;
		}
		
		/**
		 * This method implements the classic accept node.
		 */
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			matcher.last = i;
			// matcher.setGroup0 (seq, matcher.first, matcher.last);
			return true;
		}
		
		/**
		 * This method is good for all zero length assertions.
		 */
		public boolean study (TreeInfo info) {
			
			if (getNext () != null)
				return getNext ().study (info);
			else
				return info.deterministic;
			
		}
		
	}