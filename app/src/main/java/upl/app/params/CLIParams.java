	/*
	 * Copyright (c) 2020 - 2024 UPL Foundation
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 *     http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */
	
	package upl.app.params;
	
	import upl.app.Params;
	import upl.core.Int;
	import upl.util.ArrayList;
	import upl.util.List;
	
	public class CLIParams extends Params {
		
		protected String[] args;
		protected List<String> files = new ArrayList<> ();
		
		protected int i = 0;
		protected String key;
		protected boolean isFile = true, isArg = false, isValue = false;
		
		public CLIParams (String[] args) {
			this.args = args;
		}
		
		@Override
		public void process () {
			
			while (i < args.length) {
				
				String arg = args[i];
				
				if (arg.startsWith ("-") && !Int.isNumeric (arg)) {
					
					isFile = false;
					
					if (isArg && !isValue) { // Next item is argument too, so set previous value to true which means it's simply set.
						
						put (key, true);
						isArg = false;
						
					} else isArg = true;
					
					if (arg.startsWith ("--"))
						key = arg.substring (2);
					else
						key = shortKeys.get (arg.substring (1));
					
					isValue = false;
					
				} else {
					
					if (!isFile) {
						
						put (key, arg);
						
						isArg = false;
						isValue = true;
						
					} else files.put (arg);
					
				}
				
				i++;
				
				process ();
				
			}
			
		}
		
		public List<String> getFiles () {
			return files;
		}
		
		@Override
		protected RuntimeException typeMismatch (Object indexOrName, Object actual, String requiredType) {
			throw new IllegalArgumentException (actual == null ? "Value at " + indexOrName + " is null." : "Value " + actual + " at " + indexOrName + " of type " + actual.getClass ().getName () + " cannot be converted to " + requiredType + ".");
		}
		
	}