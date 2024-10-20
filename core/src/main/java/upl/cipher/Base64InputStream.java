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
	
	package upl.cipher;
	
	import java.io.InputStream;
	import java.util.Base64;
	import upl.cipher.exceptions.DecryptException;
	import upl.cipher.exceptions.EncryptException;
	
	public class Base64InputStream extends CipherInputStream {
		
		public Base64InputStream (InputStream stream) {
			super (stream);
		}
		
		public Base64InputStream (byte[] bytes) {
			super (bytes);
		}
		
		@Override
		public byte[] encrypt () throws EncryptException {
			return Base64.getEncoder ().encode (bytes);
		}
		
		@Override
		public byte[] decrypt () throws DecryptException {
			return Base64.getDecoder ().decode (bytes);
		}
		
	}