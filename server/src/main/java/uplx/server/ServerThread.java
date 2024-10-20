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
	import java.net.Socket;
	import java.util.Iterator;
	import upl.app.Application;
	import upl.io.BufferedInputStream;
	
	public abstract class ServerThread<Request extends uplx.server.Request, Response extends uplx.server.Response> extends Thread {
		
		public Server<Request, Response> server;
		
		public Socket socket;
		
		public Application app;
		
		public volatile int timeout = 0;
		
		public Request request;
		public Response response;
		
		public abstract ServerThread<Request, Response> getInstance ();
		
		@Override
		public void run () {
			
			setName (getClass ().getSimpleName () + "-" + server.getPort ());
			
			try {
				
				socket.setSoTimeout (timeout);
				socket.setTcpNoDelay (true); // we buffer anyway, so improve latency
				
				BufferedInputStream in = new BufferedInputStream (socket.getInputStream (), 4096, server.charset);
				OutputStream out = new BufferedOutputStream (socket.getOutputStream (), 4096);
				
				handleConnection (in, out);
				
			} catch (IOException e) {
				e.printStackTrace (); // TODO
			} finally {
				
				try {
					
					socket.close ();
					
					if (server.listener != null)
						server.listener.onDisconnect ();
					
				} catch (IOException ignore) {}
				
			}
			
		}
		
		protected abstract void handleConnection (BufferedInputStream in, OutputStream out) throws ServerException, IOException;
		
		/**
     * Returns a string constructed by joining the string representations of the
     * iterated objects (in order), with the delimiter inserted between them.
     *
     * @param delim the delimiter that is inserted between the joined strings
     * @param items the items whose string representations are joined
     * @param <T>	 the item type
     * @return the joined string
     */
		public <T> String join (String delim, Iterable<T> items) {
			
			StringBuilder sb = new StringBuilder ();
			
			for (Iterator<T> it = items.iterator (); it.hasNext ();)
				sb.append (it.next ()).append (it.hasNext () ? delim : "");
			
			return sb.toString ();
			
		}
		
		/**
     * Sets the socket timeout for established connections.
     *
     * @param timeout the socket timeout in milliseconds
     */
		public void setTimeout (int timeout) {
			this.timeout = timeout;
		}
		
	}