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
    
    protected BufferedInputStream stream;
    protected String string;
    
    public Hash () {}
    
    public Hash (InputStream stream) {
      this (new BufferedInputStream (stream));
    }
    
    public Hash (BufferedInputStream stream) {
      this.stream = stream;
    }
    
    public Hash (String string) {
      this.string = string;
    }
    
    public Hash setAlgorithm (String algorithm) {
      
      Hash.ALGORITHM = algorithm;
      return this;
      
    }
    
    public MessageDigest md;
    
    public Hash setDigest (MessageDigest md) {
      
      this.md = md;
      return this;
      
    }
    
    public Hash process () throws EncryptException {
      
      try {
        
        if (stream != null) {
          
          while (stream.isRead ())
            process (stream.buffer, 0, stream.readLength);
          
        } else process (string);
        
        return this;
        
      } catch (IOException e) {
        throw new EncryptException (e);
      }
      
    }
    
    public Hash process (String key) throws EncryptException {
      return process (key.getBytes ());
    }
    
    public Hash process (byte[] bytes) throws EncryptException {
      return process (bytes, 0, bytes.length);
    }
    
    protected Hash process (byte[] bytes, int offset, int length) throws EncryptException {
      
      getDigest ();
      
      md.update (bytes, offset, length);
      
      return this;
      
    }
    
    public long toLong () {
      
      byte[] digest = md.digest ();
      
      long h = 0;
      
      for (int i = 0; i < 4; i++) {
        
        h <<= 8;
        h |= ((int) digest[i]) & 0xFF;
        
      }
      
      return h;
      
    }
    
    public void getDigest () throws EncryptException {
      
      try {
        
        if (md == null)
          setDigest (MessageDigest.getInstance (ALGORITHM));
        
      } catch (NoSuchAlgorithmException e) {
        throw new EncryptException (e);
      }
      
    }
    
    @Override
    public String toString () {
      
      StringBuilder result = new StringBuilder ();
      
      for (byte value : md.digest ())
        result.append (Integer.toString ((value & 0xff) + 0x100, 16).substring (1));
      
      return result.toString ();
      
    }
    
  }