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
	 * Used for REs that can start anywhere within the input string. This basically
	 * tries to match repeatedly at each spot in the input string, moving forward
	 * after each try. An anchored search or a BnM will bypass this node completely.
	 */
	public class Start extends Node {
		
		public int minLength;
		
		public Start (Node node) {
			this.setNext (node);
			TreeInfo info = new TreeInfo ();
			getNext ().study (info);
			minLength = info.minLength;
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			if (i > matcher.to - minLength) {
				matcher.hitEnd = true;
				return false;
			}
			int guard = matcher.to - minLength;
			for (; i <= guard; i++) {
				if (getNext ().match (matcher, i, seq)) {
					matcher.first = i;
					matcher.setGroup0 (seq, matcher.first, matcher.last);
					return true;
				}
			}
			matcher.hitEnd = true;
			return false;
		}
		
		@Override
		public boolean study (TreeInfo info) {
			getNext ().study (info);
			info.maxValid = false;
			info.deterministic = false;
			return false;
		}
		
	}