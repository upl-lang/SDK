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
	import java.security.InvalidKeyException;
	import java.security.NoSuchAlgorithmException;
	import java.security.cert.Certificate;
	import java.security.cert.CertificateException;
	import java.security.cert.CertificateFactory;
	import javax.crypto.Cipher;
	import javax.crypto.NoSuchPaddingException;
	import upl.cipher.exceptions.CryptoException;
	
	public class CertInputStream extends CipherInputStream {
		
		public CertInputStream (InputStream stream) {
			super (stream);
		}
		
		public CertInputStream (byte[] bytes) {
			super (bytes);
		}
		
		public enum Algorithm {
			
			X509 ("X.509");
			
			protected String value;
			
			Algorithm (String value) {
				this.value = value;
			}
			
			@Override
			public String toString () {
				return value;
			}
			
		}
		
		protected InputStream certStream;
		
		public CertInputStream setCertStream (InputStream certStream) {
			
			this.certStream = certStream;
			
			return this;
			
		}
		
		@Override
		public Cipher getCipher (int mode) throws CryptoException {
			
			try {
				
				Cipher cipher = Cipher.getInstance (CipherInputStream.Algorithm.RSA.toString ());
				
				Certificate cert = CertificateFactory.getInstance (Algorithm.X509.toString ()).generateCertificate (certStream);
				
				cipher.init (mode, cert.getPublicKey ());
				
				return cipher;
				
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | CertificateException e) {
				throw new CryptoException (e);
			}
			
		}
		
	}