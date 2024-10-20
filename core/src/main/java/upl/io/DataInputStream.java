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
	
	import java.io.EOFException;
	import java.io.IOException;
	import java.io.InputStream;
	
	public class DataInputStream extends java.io.DataInputStream {
		
		/**
		 * Creates a DataInputStream that uses the specified
		 * underlying InputStream.
		 *
		 * @param in the specified input stream
		 */
		public DataInputStream (InputStream in) {
			super (in);
		}
		
		byte[] buffer;
		
		public int readLength;
		
		public boolean isRead () throws IOException {
			
			try {
				return ((readLength = readInt ()) > 0);
			} catch (EOFException e) {
				return false;
			}
			
		}
		
		public byte[] getBytes () throws IOException {
			
			if (readLength > 0) {
				
				buffer = new byte[readLength];
				readFully (buffer);
				
				return buffer;
				
			}
			
			return null;
			
		}
		
	}