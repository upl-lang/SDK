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
	
	import com.florianingerl.util.regex.ASCII;
	
	/**
	 * Node class that matches a POSIX type.
	 */
	public class Ctype extends BmpCharProperty {
		
		protected int ctype;
		
		public Ctype (int ctype) {
			this.ctype = ctype;
		}
		
		@Override
		public boolean isSatisfiedBy (int ch) {
			return ch < 128 && ASCII.isType (ch, ctype);
		}
		
	}