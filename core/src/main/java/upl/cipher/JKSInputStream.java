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
	
	import java.io.IOException;
	import java.io.InputStream;
	import java.security.KeyPair;
	import java.security.KeyStore;
	import java.security.KeyStoreException;
	import java.security.NoSuchAlgorithmException;
	import java.security.UnrecoverableEntryException;
	import java.security.cert.Certificate;
	import java.security.cert.CertificateException;
	import upl.cipher.exceptions.CryptoException;
	
	public class JKSInputStream extends RSAInputStream {
		
		public JKSInputStream (InputStream stream) {
			super (stream);
		}
		
		public JKSInputStream (byte[] bytes) {
			super (bytes);
		}
		
		protected InputStream keyStream;
		public String keyName = "mykey", keyStorePassword;
		
		public JKSInputStream setKeyStream (InputStream keyStream) {
			
			this.keyStream = keyStream;
			
			return this;
			
		}
		
		@Override
		public KeyPair getKeyPair () throws CryptoException {
			
			try {
				
				if (keyPair == null) {
					
					KeyStore keyStore = KeyStore.getInstance ("JCEKS");
					
					//Generate with:
					//keytool -genkeypair -alias <keyName> -storepass <keyStorePassword> -keypass <keyStorePassword> -keyalg RSA -keystore keystore.jks
					
					keyStore.load (keyStream, keyStorePassword.toCharArray ());
					
					KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection (keyStorePassword.toCharArray ());
					
					KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry (keyName, keyPassword);
					
					Certificate cert = keyStore.getCertificate (keyName);
					
					keyPair = new KeyPair (cert.getPublicKey (), privateKeyEntry.getPrivateKey ());
					
				}
				
				return keyPair;
				
			} catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableEntryException e) {
				throw new CryptoException (e);
			}
			
		}
		
	}