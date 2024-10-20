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
	import java.io.OutputStream;
	import java.security.SecureRandom;
	import javax.crypto.BadPaddingException;
	import javax.crypto.Cipher;
	import javax.crypto.IllegalBlockSizeException;
	import upl.cipher.exceptions.CryptoException;
	import upl.cipher.exceptions.DecryptException;
	import upl.cipher.exceptions.EncryptException;
	import upl.core.File;
	import upl.io.BufferedInputStream;
	
	public abstract class CipherInputStream extends BufferedInputStream {
		
		protected SecureRandom sr = new SecureRandom ();
		
		public enum Algorithm {
			
			RSA ("RSA");
			
			private final String value;
			
			Algorithm (String value) {
				this.value = value;
			}
			
			@Override
			public String toString () {
				return value;
			}
			
		}
		
		public CipherInputStream () {
			super ((InputStream) null);
		}
		
		public CipherInputStream (InputStream stream) {
			super (stream);
		}
		
		public CipherInputStream (byte[] bytes) {
			super (bytes);
		}
		
		public Cipher getCipher (int mode) throws CryptoException {
			return null;
		}
		
		public byte[] encrypt () throws EncryptException {
			
			try {
				return getCipher (Cipher.ENCRYPT_MODE).doFinal (bytes);
			} catch (CryptoException | IllegalBlockSizeException | BadPaddingException e) {
				throw new EncryptException (e);
			}
			
		}
		
		public byte[] decrypt () throws DecryptException {
			
			try {
				return getCipher (Cipher.DECRYPT_MODE).doFinal (bytes);
			} catch (CryptoException | IllegalBlockSizeException | BadPaddingException e) {
				throw new DecryptException (e);
			}
			
		}
		
		public final void encrypt (OutputStream stream) throws EncryptException {
			
			try {
				process (Cipher.ENCRYPT_MODE, stream);
			} catch (IllegalArgumentException | CryptoException e) {
				throw new EncryptException (e);
			}
			
		}
		
		public final void decrypt (OutputStream out) throws DecryptException {
			
			try {
				process (Cipher.DECRYPT_MODE, out);
			} catch (CryptoException e) {
				throw new DecryptException (e);
			}
			
		}
		
		protected final void process (int mode, OutputStream out) throws CryptoException {
			
			try {
				
				Cipher cipher = getCipher (mode);
				
				while (isRead ())
					out.write (cipher.update (File.BUFFER, 0, readLength));
				
				out.write (cipher.doFinal ());
				
				out.flush ();
				
			} catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
				throw new CryptoException (e);
			}
			
		}
		
	}