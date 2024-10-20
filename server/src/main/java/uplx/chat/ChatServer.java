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
	import java.net.ServerSocket;
	import java.net.Socket;
	import java.util.List;
	import java.util.concurrent.CopyOnWriteArrayList;
	import upl.io.BufferedInputStream;
	
	public class ChatServer extends ServerSocket {
		
		protected List<ClientHandler> clients = new CopyOnWriteArrayList<> ();
		
		protected static final String SERVER_ADDRESS = "localhost";
		protected static final int SERVER_PORT = 8082;
		
		public ChatServer () throws IOException {
		}
		
		public void start () throws IOException {
			
			bind (new InetSocketAddress (SERVER_ADDRESS, SERVER_PORT));
			
			System.out.println ("Server is running and waiting for connections...");
			
			while (true) { // Accept incoming connections
				
				Socket clientSocket = accept ();
				
				System.out.println ("New client connected: " + clientSocket);
				
				// Create a new client handler for the connected client
				ClientHandler clientHandler = new ClientHandler (clientSocket);
				
				clients.add (clientHandler);
				
				new Thread (clientHandler).start ();
				
			}
			
		}
		
		// Internal class to handle client connections
		protected class ClientHandler implements Runnable {
			
			protected Socket socket;
			protected PrintWriter out;
			protected BufferedInputStream in;
			
			// Constructor
			public ClientHandler (Socket socket) throws IOException {
				
				this.socket = socket;
				
				// Create input and output streams for communication
				in = new BufferedInputStream (socket.getInputStream ());
				out = new PrintWriter (socket.getOutputStream (), true);
				
			}
			
			// Run method to handle client communication
			@Override
			public void run () {
				
				try {
					
					out.println ("Enter your username:");
					
					String username = in.getReader ().readLine (); // Get the username from the client
					
					System.out.println ("User " + username + " connected."); // Use Username consistently
					
					out.println ("Welcome to the chat, " + username + "!");
					out.println ("Type your message:");
					
					// Continue receiving messages from the client
					while (in.readLine ()) {
						
						for (ClientHandler client : clients) { // Broadcast the message to all clients
							
							if (client != this) {
								
								client.out.println ("[" + username + "]: " + in.getLine ());
								client.out.println ("Type Your Message");
								
							}
							
						}
						
					}
					
					// Remove the client handler from the list
					clients.remove (this);
					
					// Close the input and output streams and the client socket
					in.close ();
					out.close ();
					
					socket.close ();
					
				} catch (IOException e) {
					e.printStackTrace ();
				}
				
			}
			
		}
		
	}