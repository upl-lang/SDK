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
	
	import java.io.IOException;
	import java.net.InetSocketAddress;
	import java.net.Socket;
	import upl.app.Application;
	
	public class Client extends Socket {
		
		public Application app;
		
		protected String address;
		protected volatile int port;
		
		/**
		 * Constructs an Server which can accept connections on the default HTTP port 80.
		 * Note: the {@link #start()} method must be called to start accepting connections.
		 */
		public Client (Application app) {
			this (app, "localhost");
		}
		
		/**
		 * Constructs an Server which can accept connections on the default HTTP port 80.
		 * Note: the {@link #start()} method must be called to start accepting connections.
		 */
		public Client (Application app, String address) {
			this (app, address, 80);
		}
		
		/**
		 * Constructs an Server which can accept connections on the given port.
		 * Note: the {@link #start()} method must be called to start accepting
		 * connections.
		 *
		 * @param port the port on which this server will accept connections
		 */
		public Client (Application app, String address, int port) {
			
			this.app = app;
			this.address = address;
			this.port = port;
			
		}
		
		protected ClientListener listener;
		
		public Client setListener (ClientListener listener) {
			
			this.listener = listener;
			return this;
			
		}
		
		public void start () throws ServerException {
			
			try {
				
				bind (new InetSocketAddress (address, port));
				
				new ClientThread (this).start (); // Start a thread to handle incoming messages
				
				if (listener != null)
					listener.onConnect (getOutputStream ());
				
			} catch (IOException e) {
				throw new ServerException (e);
			}
			
		}
		
	}