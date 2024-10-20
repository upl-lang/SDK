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
	
	import java.io.BufferedOutputStream;
	import java.io.IOException;
	import java.io.OutputStream;
	import upl.io.BufferedInputStream;
	
	public abstract class ServerHandler<Request extends uplx.server.Request, Response extends uplx.server.Response> implements Runnable {
		
		protected Server<Request, Response> server;
		
		public volatile int timeout = 0;
		
		protected BufferedInputStream in;
		protected OutputStream out;
		
		public ServerHandler (Server<Request, Response> server) throws IOException {
			
			this.server = server;
			
			server.setSoTimeout (timeout);
			server.thread.socket.setTcpNoDelay (true); // we buffer anyway, so improve latency
			
			in = new BufferedInputStream (server.thread.socket.getInputStream (), 4096, server.charset);
			out = new BufferedOutputStream (server.thread.socket.getOutputStream (), 4096);
			
		}
		
		@Override
		public void run () {
			
			//setName (getClass ().getSimpleName () + "-" + server.getPort ());
			
			try {
				handleConnection (in, out);
			} catch (IOException e) {
				e.printStackTrace (); // TODO
			} finally {
				
				try {
					
					server.thread.socket.close ();
					
					if (server.listener != null)
						server.listener.onDisconnect ();
					
				} catch (IOException ignore) {}
				
			}
			
		}
		
		protected abstract void handleConnection (BufferedInputStream in, OutputStream out) throws ServerException, IOException;
		
		/**
		 * Sets the socket timeout for established connections.
		 *
		 * @param timeout the socket timeout in milliseconds
		 */
		public void setTimeout (int timeout) {
			this.timeout = timeout;
		}
		
	}