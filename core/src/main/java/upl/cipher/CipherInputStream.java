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
  
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.OutputStream;
  import java.security.InvalidAlgorithmParameterException;
  import java.security.InvalidKeyException;
  import java.security.MessageDigest;
  import java.security.NoSuchAlgorithmException;
  import java.security.SecureRandom;
  import java.security.spec.AlgorithmParameterSpec;
  import java.security.spec.InvalidKeySpecException;
  import java.security.spec.KeySpec;
  import javax.crypto.BadPaddingException;
  import javax.crypto.Cipher;
  import javax.crypto.IllegalBlockSizeException;
  import javax.crypto.NoSuchPaddingException;
  import javax.crypto.SecretKey;
  import javax.crypto.SecretKeyFactory;
  import javax.crypto.spec.IvParameterSpec;
  import javax.crypto.spec.PBEKeySpec;
  import javax.crypto.spec.PBEParameterSpec;
  import javax.crypto.spec.SecretKeySpec;
  import upl.cipher.exceptions.DecryptException;
  import upl.core.File;
  import upl.core.Int;
  import upl.cipher.exceptions.CryptoException;
  import upl.cipher.exceptions.EncryptException;
  import upl.core.Log;
  import upl.io.BufferedInputStream;
  
  public class CipherInputStream extends BufferedInputStream {
    
    protected String keyFactoryAlgorithm = Algorithm.PBKDF2_WITH_HMAC_SHA1.toString (), keySpecAlgorithm = Algorithm.AES.toString ();
    
    public byte[] salt, iv;
    
    public String password;
    public int saltLength = 16, keyLength = 16, ivSize = 16;
    public int PBEKeySize = 128;
    public int MAC_LENGTH = 20;
    
    private static final int AES_KEY_LENGTH = 16;
    private static final int MAC_KEY_LENGTH = 16;
    public int iterationCount = 1;
    
    public enum Algorithm {
      
      AES ("AES"),
      RSA ("RSA"),
      PBKDF2_WITH_HMAC_SHA1 ("PBKDF2WithHmacSHA1"),
      AES_CBC_PKCS_5_PADDING ("AES/CBC/PKCS5Padding"),
      AES_CGM_PKCS_5_PADDING ("AES/GCM/PKCS5Padding");
      
      private final String value;
      
      Algorithm (String value) {
        this.value = value;
      }
      
      @Override
      public String toString () {
        return value;
      }
      
    }
    
    public static final String DIGEST_MD5 = "MD5";
    public static final String DIGEST_SHA1 = "SHA-1";
    
    public final static String[] symmetricAlgorithms = {"AES-128", "AES-192", "AES-256"};
    
    public CipherInputStream (InputStream stream) {
      super (stream);
    }
    
    public CipherInputStream (byte[] bytes) {
      super (bytes);
    }
    
    public CipherInputStream setIV (byte[] iv) {
      
      this.iv = iv;
      
      return this;
      
    }
    
    /*public byte[] generateSalt () throws EncryptException {
      
      try {
        
        SecureRandom sr = SecureRandom.getInstance ("SHA1PRNG", "Crypto"); // TODO
        
        sr.setSeed (password.getBytes ());
        
        KeyGenerator kgen = KeyGenerator.getInstance (keySpecAlgorithm);
        
        kgen.init (saltLength, sr);
        
        return kgen.generateKey ().getEncoded ();
        
      } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
        throw new EncryptException (e);
      }
      
    }*/
    
    public CipherInputStream setSalt (byte[] salt) {
      
      this.salt = salt;
      
      return this;
      
    }
    
    public byte[] generateSalt () {
      
      byte[] salt = new byte[saltLength];
      sr.nextBytes (salt);
      
      return salt;
      
    }
    
    public CipherInputStream setPassword (String password) {
      
      this.password = password;
      
      return this;
      
    }
    
    public CipherInputStream setKeyFactoryAlgorithm (String algo) {
      
      keyFactoryAlgorithm = algo;
      return this;
      
    }
    
    public CipherInputStream setKeySpecAlgorithm (String algo) {
      
      keySpecAlgorithm = algo;
      return this;
      
    }
    
    public Cipher getCipher (int mode) throws CryptoException {
      
      try {
        
        checkPassword ();
        
        Cipher cipher;
        
        if (salt != null && iv != null) {
          
          cipher = Cipher.getInstance ("AES/CBC/PKCS5Padding");
          
          KeySpec keySpec = new PBEKeySpec (password.toCharArray (), salt, iterationCount, PBEKeySize);
          
          SecretKeyFactory factory = SecretKeyFactory.getInstance ("PBKDF2WithHmacSHA1");
          SecretKey secret = new SecretKeySpec (factory.generateSecret (keySpec).getEncoded (), "AES");
          
          AlgorithmParameterSpec spec = new IvParameterSpec (iv);
          
          cipher.init (mode, secret, spec);
          
        } else if (salt != null) {
          
          cipher = Cipher.getInstance ("PBKDF2WithHmacSHA256");
          
          KeySpec keySpec = new PBEKeySpec (password.toCharArray (), salt, iterationCount, PBEKeySize);
          
          SecretKeyFactory keyFactory = SecretKeyFactory.getInstance (keyFactoryAlgorithm);
          
          SecretKey secretKey = keyFactory.generateSecret (keySpec);
          
          secretKey = new SecretKeySpec (secretKey.getEncoded (), keySpecAlgorithm);
          
          AlgorithmParameterSpec spec = new PBEParameterSpec (salt, iterationCount);
          
          cipher.init (mode, secretKey, spec);
          
        } else if (iv != null) {
          
          //cipher = Cipher.getInstance (Algorithm.AES_CGM_PKCS_5_PADDING.toString ());
          
          //spec = new GCMParameterSpec (ivSize * 8, iv);
          
          cipher = Cipher.getInstance ("AES/CBC/PKCS5Padding");
          
          SecretKeySpec keySpec = new SecretKeySpec (keyBytes, keySpecAlgorithm);
          
          AlgorithmParameterSpec spec = new IvParameterSpec (iv);
          
          cipher.init (mode, keySpec, spec);
          
        } else cipher = null;
        
        return cipher;
        
      } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
        throw new CryptoException (e);
      }
      
    }
    
    protected void checkPassword () {
      
      if (password == null || Int.size (password) <= 7)
        throw new IllegalArgumentException ("Password length must be more than 7 symbols");
      
    }
    
    public byte[] keyBytes;
    
    public SecureRandom sr = new SecureRandom ();
    
    public byte[] generateIV () {
      
      byte[] iv = new byte[ivSize];
      sr.nextBytes (iv);
      
      return iv;
      
    }
    
    protected void setKeyBytes () throws NoSuchAlgorithmException {
      
      MessageDigest digest = MessageDigest.getInstance ("SHA-256");
      
      digest.update (password.getBytes ());
      
      keyBytes = new byte[keyLength];
      
      System.arraycopy (digest.digest (), 0, keyBytes, 0, keyBytes.length);
      
    }
    
    public byte[] encrypt () throws EncryptException {
      
      try {
        
        checkPassword ();
        
        setKeyBytes ();
        
        byte[] encrypted = getCipher (Cipher.ENCRYPT_MODE).doFinal (bytes);
        
        int encryptedSize = encrypted.length;
        
        if (iv != null)
          encryptedSize += iv.length;
        
        if (salt != null)
          encryptedSize += salt.length;
        
        byte[] encryptedIText = new byte[encryptedSize];
        
        if (iv != null) {
          
          System.arraycopy (iv, 0, encryptedIText, 0, iv.length);
          
        }
        
        if (salt != null) {
          
          System.arraycopy (salt, 0, encryptedIText, 0, salt.length);
          
        }
        
        System.arraycopy (encrypted, 0, encryptedIText, iv.length, encrypted.length);
        
        return encryptedIText;
        
      } catch (CryptoException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException e) {
        throw new EncryptException (e);
      }
      
    }
    
    protected void getKeyBytes () throws NoSuchAlgorithmException {
      
      keyBytes = new byte[keyLength];
      
      MessageDigest md = MessageDigest.getInstance ("SHA-256");
      
      md.update (password.getBytes ());
      
      System.arraycopy (md.digest (), 0, keyBytes, 0, keyBytes.length);
      
    }
    
    public byte[] decrypt () throws DecryptException {
      
      try {
        
        checkPassword ();
        
        getKeyBytes ();
        
        int encryptedSize = bytes.length;
        
        if (iv != null)
          encryptedSize -= iv.length;
        
        if (salt != null)
          encryptedSize -= salt.length;
        
        byte[] encryptedBytes = new byte[encryptedSize];
        
        if (iv != null)
          System.arraycopy (bytes, iv.length, encryptedBytes, 0, encryptedSize);
        
        if (salt != null)
          System.arraycopy (bytes, salt.length, encryptedBytes, 0, encryptedSize);
        
        return getCipher (Cipher.DECRYPT_MODE).doFinal (encryptedBytes);
        
      } catch (CryptoException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException e) {
        throw new DecryptException (e);
      }
      
    }
    
    public void encrypt (OutputStream stream) throws EncryptException {
      
      try {
        
        checkPassword ();
        
        process (Cipher.ENCRYPT_MODE, stream);
        
      } catch (IllegalArgumentException | CryptoException e) {
        throw new EncryptException (e);
      }
      
    }
    
    public void decrypt (OutputStream out) throws DecryptException {
      
      try {
        
        checkPassword ();
        
        process (Cipher.DECRYPT_MODE, out);
        
      } catch (CryptoException e) {
        throw new DecryptException (e);
      }
      
    }
    
    protected void process (int mode, OutputStream out) throws CryptoException {
      
      try {
        
        int bytesRead;
        
        Cipher cipher = getCipher (mode);
        
        while ((bytesRead = read (File.BUFFER)) != -1) {
          
          byte[] output = cipher.update (File.BUFFER, 0, bytesRead);
          out.write (output);
          
        }
        
        byte[] output = cipher.doFinal ();
        out.write (output);
        
        //stream.close ();
        
        out.flush ();
        //out.close ();
        
      } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
        throw new CryptoException (e);
      }
      
    }
    
  }