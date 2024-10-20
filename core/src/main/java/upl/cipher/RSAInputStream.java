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
	import java.security.InvalidAlgorithmParameterException;
	import java.security.InvalidKeyException;
	import java.security.KeyFactory;
	import java.security.KeyPair;
	import java.security.KeyPairGenerator;
	import java.security.NoSuchAlgorithmException;
	import java.security.Signature;
	import java.security.SignatureException;
	import java.security.spec.AlgorithmParameterSpec;
	import java.security.spec.InvalidKeySpecException;
	import java.security.spec.MGF1ParameterSpec;
	import java.security.spec.PKCS8EncodedKeySpec;
	import java.security.spec.X509EncodedKeySpec;
	import java.lang.String;
	import javax.crypto.Cipher;
	import javax.crypto.NoSuchPaddingException;
	import javax.crypto.spec.OAEPParameterSpec;
	import javax.crypto.spec.PSource;
	import upl.cipher.exceptions.CryptoException;
	
	/**
	 * Rivest–Shamir–Adleman (RSA) implementation.
	 *
	 * Example of its using as follows:
	 *
	 * 1. Alice generates public and private keys
	 * 2. Alice sends her public key to Bob
	 * 3. Bob encrypts his message with Alice's public key and sends it to her
	 * 4. Alice decrypts Bob's message with her private key
	 *
	 * Using RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING algorithm. ECB here is just a Java
	 * syntax thing because RSA don't break the message into the blocks.
	 *
	 */
	
	public class RSAInputStream extends CipherInputStream {
		
		protected KeyPair keyPair;
		
		public int keyPairLength = 2048;
		
		public enum Algorithm {
			
			RSA_OAEP_WITH_SHA256ANDMGF1PADDING ("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING"),
			SHA256_WITH_RSA ("SHA256withRSA");
			
			protected String value;
			
			Algorithm (String value) {
				this.value = value;
			}
			
			@Override
			public String toString () {
				return value;
			}
			
		}
		
		public RSAInputStream () {
			super ();
		}
		
		public RSAInputStream (InputStream stream) {
			super (stream);
		}
		
		public RSAInputStream (byte[] bytes) {
			super (bytes);
		}
		
		@Override
		public Cipher getCipher (int mode) throws CryptoException {
			
			try {
				
				Cipher cipher = Cipher.getInstance (Algorithm.RSA_OAEP_WITH_SHA256ANDMGF1PADDING.toString ());
				
				KeyFactory keyFactory = KeyFactory.getInstance (CipherInputStream.Algorithm.RSA.toString ());
				
				AlgorithmParameterSpec parameterSpec = new OAEPParameterSpec ("SHA-256",
					"MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
				
				if (mode == Cipher.ENCRYPT_MODE) {
					
					if (publicKey == null)
						publicKey = getKeyPair ().getPublic ().getEncoded ();
					
					cipher.init (mode, keyFactory.generatePublic (new X509EncodedKeySpec (publicKey)), parameterSpec);
					
				} else {
					
					if (privateKey == null)
						privateKey = getKeyPair ().getPrivate ().getEncoded ();
					
					cipher.init (mode, keyFactory.generatePrivate (new PKCS8EncodedKeySpec (privateKey)), parameterSpec);
					
				}
				
				return cipher;
				
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException | InvalidAlgorithmParameterException e) {
				throw new CryptoException (e);
			}
			
		}
		
		public KeyPair getKeyPair () throws CryptoException {
			
			try {
				
				if (keyPair == null) {
					
					KeyPairGenerator generator = KeyPairGenerator.getInstance (CipherInputStream.Algorithm.RSA.toString ());
					
					generator.initialize (keyPairLength, sr);
					
					keyPair = generator.generateKeyPair ();
					
				}
				
				return keyPair;
				
			} catch (NoSuchAlgorithmException e) {
				throw new CryptoException (e);
			}
			
		}
		
		protected byte[] publicKey, privateKey;
		
		public RSAInputStream setPublicKey (byte[] key) {
			
			this.publicKey = key;
			
			return this;
			
		}
		
		public RSAInputStream setPrivateKey (byte[] key) {
			
			this.privateKey = key;
			
			return this;
			
		}
		
		public byte[] generateSignature () throws CryptoException {
			
			try {
				
				Signature privSignature = Signature.getInstance (Algorithm.SHA256_WITH_RSA.toString ());
				
				privSignature.initSign (getKeyPair ().getPrivate ());
				privSignature.update (bytes);
				
				return privSignature.sign ();
				
			} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
				throw new CryptoException (e);
			}
			
		}
		
		public boolean verifySignature (byte[] signature) throws CryptoException {
			
			try {
				
				Signature pubSignature = Signature.getInstance (Algorithm.SHA256_WITH_RSA.toString ());
				
				pubSignature.initVerify (getKeyPair ().getPublic ());
				pubSignature.update (bytes);
				
				return pubSignature.verify (signature);
				
			} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
				throw new CryptoException (e);
			}
			
		}
		
	}