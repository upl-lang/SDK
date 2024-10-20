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
	import upl.cipher.exceptions.DecryptException;
	import upl.cipher.exceptions.EncryptException;
	import upl.json.JSONArray;
	
	/**
	 * RSA with AES implementation. RSA is slow for big records, so it's better to use AES but which is using unsecured password which we need to send to receiver and which can be intercepted, so we can use RSA for AES password encryption/decryption and AES for data encryption/decryption with this password.
	 *
	 * 1. Alice generates public and private keys
	 * 2. Alice sends her public key to Bob
	 * 3. Bob encrypts his message with AES and his password with RSA using Alice's public
	 * key and sends it to her
	 * 4. Alice decrypts Bob's password with her private key and decrypts Bob's AES
	 * message with this password
	 *
	 */
	
	public class RSAAESInputStream extends AESInputStream {
		
		protected byte[] publicKey, privateKey;
		
		protected byte[] decryptData;
		
		public RSAAESInputStream (InputStream stream) {
			super (stream);
		}
		
		public RSAAESInputStream (byte[] bytes) {
			super (bytes);
		}
		
		@Override
		public byte[] encrypt () throws EncryptException {
			
			byte[] data = super.encrypt ();
			
			JSONArray decryptData = new JSONArray ();
			
			decryptData.put (new String (new Base64InputStream (password).encrypt ()));
			decryptData.put (new String (new Base64InputStream (salt).encrypt ()));
			decryptData.put (new String (new Base64InputStream (iv).encrypt ()));
			decryptData.put (iterationCount);
			
			RSAInputStream rsaStream = new RSAInputStream (decryptData.toString ().getBytes ()); // TODO new String ().toByteArray ()
			
			rsaStream.setPublicKey (publicKey);
			
			this.decryptData = rsaStream.encrypt ();
			
			return data;
			
		}
		
		public RSAAESInputStream setKey (byte[] data) {
			
			this.decryptData = data;
			return this;
			
		}
		
		public byte[] getKey () {
			return decryptData;
		}
		
		public RSAAESInputStream setPublicKey (byte[] key) {
			
			this.publicKey = key;
			
			return this;
			
		}
		
		public RSAAESInputStream setPrivateKey (byte[] key) {
			
			this.privateKey = key;
			
			return this;
			
		}
		
		@Override
		public byte[] decrypt () throws DecryptException {
			
			RSAInputStream rsaStream = new RSAInputStream (getKey ());
			
			rsaStream.setPrivateKey (privateKey);
			
			JSONArray decryptData = new JSONArray (new String (rsaStream.decrypt ()));
			
			setPassword (new Base64InputStream (decryptData.getString (0).getBytes ()).decrypt ());
			setSalt (new Base64InputStream (decryptData.getString (1).getBytes ()).decrypt ());
			setIV (new Base64InputStream (decryptData.getString (2).getBytes ()).decrypt ());
			setIterationCount (decryptData.getInt (3));
			
			return super.decrypt ();
			
		}
		
	}