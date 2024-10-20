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
	
	package upl.core;
	
	import java.io.IOException;
	import java.io.InputStream;
	import java.security.MessageDigest;
	import java.security.NoSuchAlgorithmException;
	import upl.cipher.exceptions.EncryptException;
	import upl.io.BufferedInputStream;
	
	public class Hash {
		
		protected static String ALGORITHM = "SHA-256";
		
		public Hash setAlgorithm (String algorithm) {
			
			Hash.ALGORITHM = algorithm;
			return this;
			
		}
		
		public MessageDigest md;
		
		public MessageDigest getDigest () {
			
			try {
				
				if (md == null)
					md = MessageDigest.getInstance (ALGORITHM);
				
				return md;
				
			} catch (NoSuchAlgorithmException e) {
				throw new EncryptException (e);
			}
			
		}
		
		public Hash process (InputStream stream) {
			return process (new BufferedInputStream (stream));
		}
		
		public Hash process (BufferedInputStream stream) {
			
			try {
				
				while (stream.isRead ())
					process (stream.buffer, 0, stream.readLength);
				
				return this;
				
			} catch (IOException e) {
				throw new EncryptException (e);
			}
			
		}
		
		public Hash process (String key) {
			return process (key.getBytes ());
		}
		
		public Hash process (byte[] bytes) {
			return process (bytes, 0, bytes.length);
		}
		
		public Hash process (byte[] bytes, int offset, int length) {
			
			getDigest ().update (bytes, offset, length);
			
			return this;
			
		}
		
		protected long longHash;
		
		public long toLong () {
			
			if (longHash == 0) {
				
				byte[] digest = md.digest ();
				
				for (int i = 0; i < 4; i++) {
					
					longHash <<= 8;
					longHash |= ((int) digest[i]) & 0xFF;
					
				}
				
				return longHash;
				
			}
			
			return longHash;
			
		}
		
		protected String stringHash;
		
		@Override
		public String toString () {
			
			if (stringHash == null) {
				
				StringBuilder result = new StringBuilder ();
				
				for (byte value : getDigest ().digest ())
					result.append (Integer.toString ((value & 0xff) + 0x100, 16).substring (1));
				
				stringHash = result.toString ();
				
			}
			
			return stringHash;
			
		}
		
	}