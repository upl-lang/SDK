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
	
	package upl.io;
	
	import java.io.FilterOutputStream;
	import java.io.IOException;
	import java.io.OutputStream;
	import upl.json.JSONArray;
	import upl.json.JSONObject;
	
	public class DataOutputStream extends java.io.DataOutputStream {
		
		/**
		 * Creates a new data output stream to write data to the specified
		 * underlying output stream. The counter <code>written</code> is
		 * set to zero.
		 *
		 * @param out the underlying output stream, to be saved for later use.
		 * @see FilterOutputStream#out
		 */
		public DataOutputStream (OutputStream out) {
			super (out);
		}
		
		public void write (JSONArray data) throws IOException {
			write (data.toString ());
		}
		
		public void write (JSONObject data) throws IOException {
			write (data.toString ());
		}
		
		public void write (String mess) throws IOException {
			write (mess.getBytes ());
		}
		
		@Override
		public void write (byte... mess) throws IOException {
			
			writeInt (mess.length);
			super.write (mess);
			
		}
		
	}