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
	 * Node to anchor at the end of input. This is the absolute end, so this should
	 * not match at the last newline before the end as $ will.
	 */
	public class End extends Node {
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			
			int endIndex = (matcher.anchoringBounds) ? matcher.to : matcher.getTextLength ();
			
			if (i == endIndex) {
				
				matcher.hitEnd = true;
				return getNext ().match (matcher, i, seq);
				
			}
			
			return false;
			
		}
		
	}