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
	
	package upl.app;
	
	import java.io.IOException;
	import upl.core.File;
	import upl.core.exceptions.OutOfMemoryException;
	import upl.json.JSONObject;
	
	public class Manifest extends JSONObject {
		
		public Application app;
		
		protected JSONObject data;
		
		public JSONObject read () {
			
			try {
				
				if (data == null)
					data = new JSONObject (new File (app.getPlatform ().getEnvironment ().getProjectPath (), "manifest.json").read ());
				
			} catch (IOException | OutOfMemoryException e) {
				data = new JSONObject ();
			}
			
			return data;
			
		}
		
		public String getTitle () {
			return read ().getString ("title", "");
		}
		
		public String getVersion () {
			return read ().getString ("version", "1.0.0");
		}
		
		public String getLanguage () {
			return read ().getString ("language", "en");
		}
		
	}