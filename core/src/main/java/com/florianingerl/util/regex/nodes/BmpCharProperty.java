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
	
	/**
	 * Optimized version of CharProperty that works only for properties never
	 * satisfied by Supplementary characters.
	 */
	public abstract class BmpCharProperty extends CharProperty {
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			
			if (i < matcher.to) {
				
				if (isSatisfiedBy (seq.charAt (i))) {
					
					++matcher.activity;
					return getNext ().match (matcher, i + 1, seq);
					
				}
				
				return false;
				
			} else {
				
				matcher.hitEnd = true;
				return false;
				
			}
			
		}
		
	}