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
	
	package upl.app;
	
	import upl.core.Struct;
	
	public abstract class Preferences extends Struct {
		
		public final String[] getStringArray (String key) {
			return optString (key).split (",");
		}
		
		public void apply () {}
		
		public final void set (String key, Object value) {
			
			put (key, value);
			apply ();
			
		}
		
	}