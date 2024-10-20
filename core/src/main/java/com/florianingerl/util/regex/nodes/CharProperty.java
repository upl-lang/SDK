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
	
	/**
	 * Abstract node class to match one character satisfying some boolean property.
	 */
	public abstract class CharProperty extends Node {
		
		public abstract boolean isSatisfiedBy (int ch);
		
		public CharProperty complement () {
			
			return new CharProperty () {
				
				@Override
				public boolean isSatisfiedBy (int ch) {
					return !CharProperty.this.isSatisfiedBy (ch);
				}
				
			};
			
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			
			if (i < matcher.to) {
				
				int ch = Character.codePointAt (seq, i);
				
				if (isSatisfiedBy (ch)) {
					
					int count = Character.charCount (ch);
					matcher.activity += count;
					
					return getNext ().match (matcher, i + count, seq);
					
				}
				
				return false;
				
				//return isSatisfiedBy(ch) && getNext().match(matcher, i + Character.charCount(ch), seq);
				
			} else {
				
				matcher.hitEnd = true;
				
				return false;
				
			}
			
		}
		
		@Override
		public boolean study (TreeInfo info) {
			
			info.minLength++;
			info.maxLength++;
			
			return getNext ().study (info);
			
		}
		
	}