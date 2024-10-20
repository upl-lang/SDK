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
	
	public class LastNode extends Node {
		
		/**
		 * This method implements the classic accept node with the addition of a check
		 * to see if the match occurred using all of the input.
		 */
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			if (matcher.acceptMode == Matcher.ENDANCHOR && i != matcher.to)
				return false;
			matcher.last = i;
			matcher.setGroup0 (seq, matcher.first, matcher.last);
			return true;
		}
		
	}