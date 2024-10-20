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
	import com.florianingerl.util.regex.Pattern;
	
	/**
	 * Creates a bit vector for matching Latin-1 values. A normal BitClass never
	 * matches values above Latin-1, and a complemented BitClass always matches
	 * values above Latin-1.
	 */
	public class BitClass extends BmpCharProperty {
		
		protected boolean[] bits;
		
		public BitClass () {
			bits = new boolean[256];
		}
		
		public BitClass (boolean[] bits) {
			this.bits = bits;
		}
		
		public BitClass add (int c, int flags) {
			
			assert c >= 0 && c <= 255;
			if ((flags & Pattern.CASE_INSENSITIVE) != 0) {
				if (ASCII.isAscii (c)) {
					bits[ASCII.toUpper (c)] = true;
					bits[ASCII.toLower (c)] = true;
				} else if ((flags & Pattern.UNICODE_CASE) != 0) {
					bits[Character.toLowerCase (c)] = true;
					bits[Character.toUpperCase (c)] = true;
				}
			}
			bits[c] = true;
			return this;
		}
		
		@Override
		public boolean isSatisfiedBy (int ch) {
			return ch < 256 && bits[ch];
		}
		
	}