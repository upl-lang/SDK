	/*
	 * Copyright (c) 2020 - 2023 UPL Foundation
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
	
	package upl.cipher;
	
	import java.io.InputStream;
	import java.security.InvalidAlgorithmParameterException;
	import java.security.InvalidKeyException;
	import java.security.NoSuchAlgorithmException;
	import java.security.spec.InvalidKeySpecException;
	import java.security.spec.KeySpec;
	import javax.crypto.Cipher;
	import javax.crypto.KeyGenerator;
	import javax.crypto.NoSuchPaddingException;
	import javax.crypto.SecretKey;
	import javax.crypto.SecretKeyFactory;
	import javax.crypto.spec.GCMParameterSpec;
	import javax.crypto.spec.PBEKeySpec;
	import javax.crypto.spec.SecretKeySpec;
	import upl.cipher.exceptions.CryptoException;
	import upl.cipher.exceptions.EncryptException;
	
	public class AESInputStream extends CipherInputStream {
		
		public int saltSize = 8, ivSize = 16, keyLength = 128, ivLength = 128;
		public int iterationCount = 1;
		
		public byte[] password;
		
		public static String[] symmetricAlgorithms = {"AES-128", "AES-192", "AES-256"};
		
		protected byte[] salt, iv;
		
		public enum Algorithm {
			
			AES ("AES"),
			AES_CGM_NOPADDING ("AES/GCM/NoPadding"),
			PBKDF2_WITH_HMAC_SHA512 ("PBKDF2WithHmacSHA512");
			
			protected String value;
			
			Algorithm (String value) {
				this.value = value;
			}
			
			@Override
			public String toString () {
				return value;
			}
			
		}
		
		public AESInputStream () {
			super ();
		}
		
		public AESInputStream (InputStream stream) {
			super (stream);
		}
		
		public AESInputStream (byte[] bytes) {
			super (bytes);
		}
		
		/**
     * Creates a new AES key
     */
		public byte[] generateKey () throws CryptoException {
			
			try {
				
				KeyGenerator kgen = KeyGenerator.getInstance (Algorithm.AES.toString ());
				kgen.init (keyLength);
				
				return kgen.generateKey ().getEncoded ();
				
			} catch (NoSuchAlgorithmException e) {
				throw new CryptoException (e);
			}
			
		}
		
		public byte[] generateIV () {
			
			iv = new byte[ivSize];
			sr.nextBytes (iv);
			
			return iv;
			
		}
		
		public byte[] getIV () {
			return iv;
		}
		
		public AESInputStream setIV (byte[] iv) {
			
			this.iv = iv;
			
			return this;
			
		}
		
		public byte[] generateSalt () throws EncryptException {
			
			try {
				
				sr.setSeed (password);
				
				KeyGenerator keyGen = KeyGenerator.getInstance (Algorithm.AES.toString ());
				
				keyGen.init (keyLength, sr);
				
				return keyGen.generateKey ().getEncoded ();
				
			} catch (NoSuchAlgorithmException e) {
				throw new EncryptException (e);
			}
			
		}
		
		/*public byte[] generateSalt () {
			
			salt = new byte[saltSize];
			sr.nextBytes (salt);
			
			return salt;
			
		}*/
		
		public AESInputStream setSalt (byte[] salt) {
			
			this.salt = salt;
			
			return this;
			
		}
		
		public byte[] getSalt () {
			return salt;
		}
		
		public AESInputStream setPassword (String password) {
			return setPassword (password.getBytes ());
		}
		
		public AESInputStream setPassword (byte[] password) {
			
			this.password = password;
			
			return this;
			
		}
		
		public AESInputStream setIterationCount (int count) {
			
			this.iterationCount = count;
			
			return this;
			
		}
		
		protected SecretKey secretKey;
		
		@Override
		public Cipher getCipher (int mode) throws CryptoException {
			
			try {
				
				checkPassword ();
				
				Cipher cipher = Cipher.getInstance (Algorithm.AES_CGM_NOPADDING.toString ());
				
				if (salt == null) salt = generateSalt ();
				if (iv == null) iv = generateIV ();
				
				KeySpec spec = new PBEKeySpec (new String (password).toCharArray (), salt, iterationCount, keyLength);
				secretKey = SecretKeyFactory.getInstance (Algorithm.PBKDF2_WITH_HMAC_SHA512.toString ()).generateSecret (spec);
				secretKey = new SecretKeySpec (secretKey.getEncoded (), Algorithm.AES.toString ());
				
				cipher.init (mode, secretKey, new GCMParameterSpec (ivLength, iv));
				
				return cipher;
				
			} catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
				throw new CryptoException (e);
			}
			
		}
		
		protected void checkPassword () throws CryptoException {
			
			if (password == null) password = generateKey ();
			
			if (password.length <= 7)
				throw new IllegalArgumentException ("Passphrase length must be more than 7 symbols");
			
		}
		
	}