	/*
	 * Copyright (c) 2020 - 2024 UPL Foundation
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 *     http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */
	
	package upl.io;
	
	import java.io.BufferedReader;
	import java.io.ByteArrayInputStream;
	import java.io.ByteArrayOutputStream;
	import java.io.File;
	import java.io.FileInputStream;
	import java.io.FileNotFoundException;
	import java.io.FileOutputStream;
	import java.io.IOException;
	import java.io.InputStream;
	import java.io.InputStreamReader;
	import java.io.OutputStream;
	import java.nio.charset.Charset;
	import java.nio.charset.StandardCharsets;
	import upl.exceptions.OutOfMemoryException;
	import upl.type.Strings;
	import upl.util.List;
	import upl.util.Map;
	
	public class BufferedInputStream extends java.io.BufferedInputStream {
		
		protected static Charset charset;
		protected static int bufferLength = 4096;
		
		public String line;
		public byte[] buffer;
		protected byte[] bytes;
		
		@SuppressWarnings ("CopyConstructorMissesField")
		public BufferedInputStream (BufferedInputStream stream) {
			
			super (stream);
			throw new IllegalArgumentException ("BufferedInputStream is already implemented");
			
		}
		
		public BufferedInputStream (InputStream stream) {
			this (stream, bufferLength);
		}
		
		public BufferedInputStream (File stream) throws FileNotFoundException {
			this (new FileInputStream (stream));
		}
		
		public BufferedInputStream (InputStream stream, int bufferLength) {
			this (stream, bufferLength, StandardCharsets.UTF_8);
		}
		
		public BufferedInputStream (InputStream stream, int bufferLength, Charset charset) {
			
			super (stream, bufferLength);
			
			setBufferLength (bufferLength);
			BufferedInputStream.charset = charset;
			
		}
		
		public BufferedInputStream setBufferLength (int bufferLength) {
			
			BufferedInputStream.bufferLength = bufferLength;
			
			buffer = new byte[bufferLength];
			
			return this;
			
		}
		
		public BufferedInputStream (byte[] bytes) {
			
			super (new ByteArrayInputStream (bytes));
			this.bytes = bytes;
			
		}
		
		/*public static OutputStream toOutputStream (File file) throws IOException {
			
			OutputStream stream = new FileOutputStream (file);
			return toOutputStream (stream);
			
		}
		
		public static OutputStream toOutputStream (OutputStream stream) {
			return new BufferedOutputStream (stream);
		}*/
		
		public int readLength;
		
		public boolean isRead () throws IOException {
			return (readLength = read (buffer)) > 0;
		}
		
		public File copy (File file) throws IOException {
			
			copy (new FileOutputStream (file));
			
			return file;
			
		}
		
		public BufferedInputStream copy (OutputStream out) throws IOException {
			
			readLength = 0;
			
			while (isRead ())
				out.write (buffer, 0, readLength);
			
			return this;
			
		}
		
		public byte[] getBytes () throws IOException {
			return getBytes (true);
		}
		
		public byte[] getBytes (boolean flush) throws IOException {
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream ();
			
			copy (baos); // We don't know stream length, so we need to read it at first
			
			byte[] out = baos.toByteArray ();
			
			if (flush) baos.flush ();
			
			return out;
			
		}
		
		protected BufferedReader br;
		
		public BufferedReader getReader () {
			
			if (br == null)
				br = getReader (this, charset, bufferLength);
			
			return br;
			
		}
		
		public static BufferedReader getReader (InputStream in) {
			return getReader (in, charset);
		}
		
		public static BufferedReader getReader (InputStream in, Charset ch) {
			return getReader (in, ch, bufferLength);
		}
		
		public static BufferedReader getReader (InputStream in, Charset ch, int bufferLength) {
			return new BufferedReader (new InputStreamReader (in, ch), bufferLength);
		}
		
		public List<String> read (List<String> output) throws IOException {
			return read (output, true);
		}
		
		public boolean readLine () throws IOException {
			return (line = getReader ().readLine ()) != null;
		}
		
		public List<String> read (List<String> output, boolean close) throws IOException {
			
			while (readLine ())
				output.add (line);
			
			if (close) close ();
			
			return output;
			
		}
		
		public Map<String, String> read (Map<String, String> output) throws IOException {
			return read (output, "=");
		}
		
		public Map<String, String> read (Map<String, String> output, String sep) throws IOException {
			return read (output, sep, true);
		}
		
		public Map<String, String> read (Map<String, String> output, String sep, boolean close) throws IOException {
			
			while (readLine ()) {
				
				String[] parts = line.split (sep);
				output.add (new Strings (parts[0]).trim (), new Strings (parts[1]).trim ());
				
			}
			
			if (close) close ();
			
			return output;
			
		}
		
		public String read (String str) throws IOException, OutOfMemoryException {
			return read (new StringBuilder (str)).toString ();
		}
		
		public StringBuilder read (StringBuilder builder) throws IOException, OutOfMemoryException {
			
			try {
				
				while (readLine ()) {
					
					builder.append (getLine ());
					builder.append (Strings.LS);
					
				}
				
				return builder;
				
			} catch (OutOfMemoryError e) {
				throw new OutOfMemoryException (e);
			}
			
		}
		
		public String getLine () {
			return line;
		}
		
		public byte[] read (int length) throws IOException {
			
			byte[] bytes = new byte[length];
			read (bytes);
			
			return bytes;
			
		}
		
		public byte[] readBytes (int size) {
			
			byte[] bytes = new byte[size];
			System.arraycopy (this.bytes, 0, bytes, 0, bytes.length);
			
			return bytes;
			
		}
		
		public byte[] toByteArray () throws IOException {
			
			bytes = null;
			
			while (readBytes ()) {}
			
			return bytes;
			
		}
		
		public boolean readBytes () throws IOException {
			
			if (bytes == null) {
				
				bytes = new byte[bufferLength];
				readLength = 0;
				
			}
			
			return (readLength = read (bytes)) > 0;
			
		}
		
	}