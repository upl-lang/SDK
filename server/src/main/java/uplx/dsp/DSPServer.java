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
	
	package uplx.dsp;
	
	import java.io.IOException;
	import java.net.InetSocketAddress;
	import java.net.ServerSocket;
	import java.net.Socket;
	import java.util.List;
	import java.util.concurrent.CopyOnWriteArrayList;
	import upl.app.apps.CLIApplication;
	import upl.core.Folder;
	import upl.core.exceptions.OutOfMemoryException;
	import upl.http.HttpStatus;
	import upl.io.DataInputStream;
	import upl.io.DataOutputStream;
	import upl.json.JSONArray;
	import upl.json.JSONException;
	import upl.json.JSONObject;
	
	public class DSPServer extends ServerSocket {
		
		protected List<ClientHandler> clients = new CopyOnWriteArrayList<> ();
		
		public final static String DEFAULT_ADDRESS = "localhost";
		public final static int DEFAULT_PORT = 8081;
		public final static int PART_LENGTH = 10 * 1024 * 1024; // 10 Mb
		public final static int FOLDER_LENGTH = 1;
		
		protected CLIApplication app;
		protected JSONObject options;
		
		public DSPServer (CLIApplication app, JSONObject options) throws IOException {
			
			this.app = app;
			this.options = options;
			
		}
		
		public void start () throws IOException {
			
			bind (new InetSocketAddress (options.getString ("address", DEFAULT_ADDRESS), options.getInt ("port", DEFAULT_PORT)
			));
			
			System.out.println ("Server " + options.getString ("address", DEFAULT_ADDRESS) + ":" + options.getInt ("port", DEFAULT_PORT) + " is running and waiting for connections...");
			
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
			protected DataInputStream in;
			protected DataOutputStream out;
			
			// Constructor
			public ClientHandler (Socket socket) throws IOException {
				
				this.socket = socket;
				
				// Create input and output streams for communication
				in = new DataInputStream (socket.getInputStream ());
				out = new DataOutputStream (socket.getOutputStream ());
				
			}
			
			// Run method to handle client communication
			@Override
			public void run () {
				
				try {
					
					if (in.isRead ()) {
						
						JSONObject clientInfo = new JSONObject (new String (in.getBytes ())); // Get info from the client
						
						System.out.println ("Leech " + socket + " connected with " + clientInfo);
						
						clientInfo.put ("requiredSpace", options.getLong ("requiredSpace", 0L));
						
						switch (clientInfo.getString ("action")) {
							
							case "get": { // Client requires the file from server
								
								try {
									
									JSONObject url = clientInfo.getJSONObject ("url");
									
									String file = url.getString ("file");
									
									JSONObject data = new JSONObject (new File (app.params.getString ("temp-dir"), url.getString ("hub"), file.substring (0, FOLDER_LENGTH), file, file + ".info.json").read ());
									
									clientInfo.put ("code", HttpStatus.SUCCESS_OK.getCode ());
									clientInfo.put ("data", data);
									
									out.write (clientInfo.toString ()); // Send info packet to client...
									
									File file2 = new File ();
									
									file2.process (data.getString ("path"), data, out); // ...and starts to distribute founded file or folder to client
									
								} catch (IOException e) {
									
									clientInfo.put ("code", HttpStatus.CLIENT_ERROR_NOT_FOUND.getCode ());
									clientInfo.put ("message", "Requested file " + clientInfo.getString ("url") + " not found on node");
									
									out.write (clientInfo.toString ().getBytes ());
									
								} catch (JSONException e) {
									
									clientInfo.put ("code", HttpStatus.CLIENT_ERROR_BAD_REQUEST.getCode ());
									clientInfo.put ("message", e.getMessage ());
									
									out.write (clientInfo.toString ().getBytes ());
									
								} catch (OutOfMemoryException e) {
									
									clientInfo.put ("code", HttpStatus.SERVER_ERROR_INTERNAL.getCode ());
									clientInfo.put ("message", "Node server error, contact node administrator");
									
									out.write (clientInfo.toString ().getBytes ());
									
								}
								
								//out.close ();
								
								break;
								
							}
							
							case "connect": {
							
							// Continue receiving messages from the client
							/*while (in.readLine ())
								for (ClientHandler client : clients) // Broadcast the message to all clients
									if (client != this)
										client.out.println (in.getLine ());*/
								
								break;
								
							}
							
						}
						
						// Remove the client handler from the list
						clients.remove (this);
						
						// Close the input and output streams and the client socket
						in.close ();
						out.close ();
						
						socket.close ();
						
					}
					
				} catch (IOException ignored) {}
				
			}
			
		}
		
		public void createHub (JSONObject data) throws IOException {
			
			data = new JSONObject ();
			
			String hubName = app.params.getString ("hub");
			String hubDir = app.params.getString ("hubs-dir") + File.DS + hubName;
			
			data.put ("name", hubName);
			
			new Folder (hubDir).makeDir ();
			
			new File (hubDir, hubName + ".json").write (data.toString (true));
			
		}
		
		public String buildFileUrl () {
			
			String str = "dsp://";
			
			str += options.getString ("address", DEFAULT_ADDRESS);
			
			if (!options.optInt ("port").equals (DEFAULT_PORT))
				str += ":" + options.optInt ("port");
			
			str += "/" + app.params.getString ("hub");
			str += "/" + app.params.getString ("file");
			
			return str;
			
		}
		
	}