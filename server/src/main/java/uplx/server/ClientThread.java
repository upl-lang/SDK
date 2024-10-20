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
	
	package uplx.server;
	
	import java.io.BufferedReader;
	import java.io.IOException;
	import java.io.InputStreamReader;
	
	public class ClientThread extends Thread {
		
		public Client client;
		
		public ClientThread (Client client) {
			this.client = client;
		}
		
		@Override
		public void run () {
			
			try {
				
				String response;
				
				BufferedReader in = new BufferedReader (new InputStreamReader (client.getInputStream ()));
				
				while ((response = in.readLine ()) != null) {
					
					if (client.listener != null)
						client.listener.onResponse (response);
					
				}
				
			} catch (IOException e) {
				throw new ServerException (e);
			}
			
		}
		
	}