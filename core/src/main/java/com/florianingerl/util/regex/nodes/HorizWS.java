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
	
	/**
	 * Node class that matches a Perl horizontal whitespace
	 */
	public class HorizWS extends BmpCharProperty {
		
		@Override
		public boolean isSatisfiedBy (int cp) {
			return cp == 0x09 || cp == 0x20 || cp == 0xa0 || cp == 0x1680 || cp == 0x180e
				       || cp >= 0x2000 && cp <= 0x200a || cp == 0x202f || cp == 0x205f || cp == 0x3000;
		}
		
	}