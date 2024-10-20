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
	
	package uplx.chat;
	
	import java.io.IOException;
	import java.io.PrintWriter;
	import java.net.InetSocketAddress;
	import java.net.Socket;
	import java.util.Scanner;
	import upl.io.BufferedInputStream;
	
	public class ChatClient extends Socket {
		
		private static final String SERVER_ADDRESS = "localhost";
		private static final int SERVER_PORT = 8082;
		
		public void start () throws IOException {
			
			connect (new InetSocketAddress (SERVER_ADDRESS, SERVER_PORT));
			
			System.out.println ("Connected to the chat server!");
			
			// Setting up input and output streams
			BufferedInputStream in = new BufferedInputStream (getInputStream ());
			PrintWriter out = new PrintWriter (getOutputStream (), true);
			
			new Thread () { // Start a thread to handle incoming messages
				
				@Override
				public void run () {
					
					try {
						
						while (in.readLine ())
							System.out.println (in.getLine ());
						
					} catch (IOException e) {
						e.printStackTrace ();
					}
					
				}
				
			}.start ();
			
			// Read messages from the console and send to the server
			Scanner scanner = new Scanner (System.in);
			
			while (true)
				out.println (scanner.nextLine ());
			
		}
		
	}