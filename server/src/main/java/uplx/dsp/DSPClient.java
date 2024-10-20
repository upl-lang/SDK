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
	
	import java.io.FileInputStream;
	import java.io.FileOutputStream;
	import java.io.IOException;
	import java.net.InetSocketAddress;
	import java.net.MalformedURLException;
	import java.net.ProtocolException;
	import java.net.Socket;
	import upl.app.apps.CLIApplication;
	import upl.core.File;
	import upl.core.Folder;
	import upl.core.Hash;
	import upl.core.exceptions.OutOfMemoryException;
	import upl.http.HttpStatus;
	import upl.io.BufferedInputStream;
	import upl.io.DataInputStream;
	import upl.io.DataOutputStream;
	import upl.json.JSONArray;
	import upl.json.JSONException;
	import upl.json.JSONObject;
	
	public class DSPClient extends Socket {
		
		protected CLIApplication app;
		
		protected String hubDir;
		
		protected JSONObject url;
		
		public DSPClient (CLIApplication app) throws IOException {
			
			this.app = app;
			
			url = parseUrl (app.params.getString ("url"));
			
			hubDir = app.params.getString ("temp-dir") + File.DS + url.getString ("hub");
			
		}
		
		public void start () throws IOException {
			
			connect (new InetSocketAddress (url.getString ("address"), url.getInt ("port")));
			
			System.out.println ("Connected to node " + this);
			
			DataInputStream in = new DataInputStream (getInputStream ());
			
			JSONObject info = new JSONObject ();
			
			info.put ("action", "get");
			info.put ("url", url);
			info.put ("allocatedSpace", app.params.getLong ("allocatedSpace"));
			
			byte[] mess = info.toString ().getBytes ();
			
			DataOutputStream out = new DataOutputStream (getOutputStream ());
			
			out.write (mess); // Send client info to the server
			
			new Thread () { // Start a thread to handle server messages
				
				@Override
				public void run () {
					
					try {
						
						JSONObject serverInfo = null, clientInfo = new JSONObject (), clientTempInfo = new JSONObject ();
						JSONArray serverParts = new JSONArray (), clientParts = new JSONArray ();
						
						long clientSize = 0;
						File clientTempFile;
						boolean dataPacket = false;
						
						while (in.isRead ()) {
							
							byte[] message = in.getBytes ();
							
							if (serverInfo == null || dataPacket) {
								
								serverInfo = new JSONObject (new String (message));
								
								dataPacket = false;
								
								if (serverInfo.getInt ("code") == HttpStatus.SUCCESS_OK.getCode ()) {
									
									switch (serverInfo.getString ("action")) {
										
										case "get": { // Receive parts from server
											
											String file = url.getString ("file");
											
											File clientFile = new File (hubDir, file.substring (0, DSPServer.FOLDER_LENGTH), file, file + ".info.json");
											clientTempFile = new File (hubDir, file.substring (0, DSPServer.FOLDER_LENGTH), file, file + ".temp.json");
											
											new File (hubDir, file.substring (0, DSPServer.FOLDER_LENGTH), file).makeDir ();
											
											JSONObject data = serverInfo.getJSONObject ("data");
											
											if (!clientFile.exists ())
												clientFile.write (data.toString (true)); // Save files structure from server at first time to prevent its changing by sharp servers
											else
												clientInfo = new JSONObject (clientFile.read ());
											
											if (!clientTempFile.exists ()) {
												
												clientTempInfo.put ("size", data.get ("size"));
												clientTempInfo.put ("hash", data.get ("hash"));
												
											} else clientTempInfo = new JSONObject (clientTempFile.read ());
											
											clientSize = clientTempInfo.getLong ("size");
											
											serverParts = clientInfo.getJSONArray ("parts");
											clientParts = clientTempInfo.getJSONArray ("parts");
											
											break;
											
										}
										
									}
									
								} else System.out.println (serverInfo.getString ("message"));
								
							} else if (message.length == 1 && message[0] == 10) // \n appeared so next packet will be info packet
								dataPacket = true;
							else { // Another one packets are data packets
								
								switch (serverInfo.getString ("action")) {
									
									case "get": {
										
										String hash = new Hash ().process (message).toString (); // Get file of received part
										
										if (serverParts.contains (hash)) { // Server parts array contains this part file
											
											String file = url.getString ("file");
											
											Folder dir = new Folder (hubDir, file.substring (0, DSPServer.FOLDER_LENGTH), file);
											
											dir.makeDir ();
											
											if (!clientParts.contains (hash)) { // Client parts array does not contains this part file yet
												
												clientSize += message.length;
												new File (dir, hash).write (message);
												
												clientParts.put (hash);
												
											}
											
										}
										
										clientTempInfo.put ("size", clientSize);
										
										String file = url.getString ("file");
										
										new File (hubDir, file.substring (0, DSPServer.FOLDER_LENGTH), file, file + ".temp.json").write (clientTempInfo.toString (true));
										
										if (clientSize == clientInfo.getLong ("size")) { // Final file size is equals
											
											FileOutputStream outputFile = new FileOutputStream (app.params.getString ("output-dir") + File.DS + clientInfo.getString ("dsp-file"));
											
											for (Object hash2 : serverParts.getList ()) {
												
												File partFile = new File (hubDir, file, hash2.toString ().substring (0, DSPServer.FOLDER_LENGTH), hash2);
												
												BufferedInputStream bis = new BufferedInputStream (new FileInputStream (partFile), (int) partFile.length ());
												
												bis.copy (outputFile);
												
											}
											
										}
										
										break;
										
									}
									
								}
								
							}
							
						}
						
					} catch (JSONException e) {
						//e.printStackTrace ();
					} catch (IOException | OutOfMemoryException e) {
						e.printStackTrace ();
					}
					
				}
				
			}.start ();
			
		}
		
		public static class URL {
			
			public String address, hub, file;
			public int port;
			
		}
		
		protected JSONObject parseUrl (String url) throws IOException {
			
			String[] parts = url.split ("://");
			
			if (!parts[0].equals ("dsp"))
				throw new ProtocolException ("Invalid protocol, only dsp protocol allowed.");
			
			String[] parts2 = parts[1].split ("/");
			
			if (parts2.length == 3) {
				
				JSONObject url2 = new JSONObject ();
				
				String[] parts3 = parts2[0].split (":");
				
				if (parts3.length <= 2) {
					
					url2.put ("address", parts3[0]);
					
					if (parts3.length == 2) {
						
						try {
							url2.put ("port", Integer.parseInt (parts3[1]));
						} catch (NumberFormatException e) {
							throw new MalformedURLException ("Invalid port " + parts3[1]);
						}
						
					} else url2.put ("port", DSPServer.DEFAULT_PORT);
					
					url2.put ("hub", parts2[1]);
					url2.put ("file", parts2[2]);
					
					return url2;
					
				} else throw new MalformedURLException ("Invalid url");
				
			} else throw new MalformedURLException ("Invalid url");
			
		}
		
	}