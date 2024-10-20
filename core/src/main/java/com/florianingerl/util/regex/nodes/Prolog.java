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
	
	/*
	 * This sets up a loop to handle a recursive quantifier structure.
	 */
	public class Prolog extends Node {
		
		public Loop loop;
		
		public Prolog (Loop loop) {
			this.loop = loop;
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			return loop.matchInit (matcher, i, seq);
		}
		
		@Override
		public boolean study (TreeInfo info) {
			return loop.study (info);
		}
		
	}