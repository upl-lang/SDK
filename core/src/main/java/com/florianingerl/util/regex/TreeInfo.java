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
	
	import java.util.HashMap;
	import java.util.Map;
	
	/**
	 * Used to accumulate information about a subtree of the object graph so that
	 * optimizations can be applied to the subtree.
	 */
	public class TreeInfo {
		
		public int minLength;
		public int maxLength;
		public boolean maxValid;
		public boolean deterministic;
		public Map<Integer, Boolean> recursive = new HashMap<Integer, Boolean> ();
		
		public TreeInfo () {
			reset ();
		}
		
		public void reset () {
			minLength = 0;
			maxLength = 0;
			maxValid = true;
			deterministic = true;
		}
		
	}
