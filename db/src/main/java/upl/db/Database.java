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
	
	package upl.db;
	
	import java.io.IOException;
	import upl.core.File;
	import upl.json.JSONObject;
	
	public class Database {
		
		public File file;
		
		public Database (File file) {
			this.file = file;
		}
		
		public void write () throws IOException {
			
			for (int i = 0; i < 1_000_000; i++) {
				
				JSONObject data = new JSONObject ();
				
				for (int i2 = 1; i2 < 5; i2++)
					data.put ("key" + i2, "value" + (i + i2));
				
				file.getPrintWriter ().println (data);
				
			}
			
			file.getPrintWriter ().flush ();
			
		}
		
		public JSONObject select (int i) throws IOException {
			
			while (file.readLine ()) {
				
				JSONObject data = new JSONObject (file.getLine ());
				
				if (data.getString ("key2").equals ("value" + (i + 2)))
					return data;
				
			}
			
			return null;
			
		}
		
	}