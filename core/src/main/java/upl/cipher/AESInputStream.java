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
  
  package upl.cipher;
  
  import java.io.InputStream;
  import java.security.InvalidKeyException;
  import java.security.Key;
  import java.security.NoSuchAlgorithmException;
  import javax.crypto.Cipher;
  import javax.crypto.NoSuchPaddingException;
  import javax.crypto.spec.SecretKeySpec;
  import upl.cipher.exceptions.CryptoException;
  
  public class AESInputStream extends CipherInputStream {
    
    public AESInputStream (InputStream stream) {
      super (stream);
    }
    
    public AESInputStream (byte[] bytes) {
      super (bytes);
    }
    
    @Override
    public Cipher getCipher (int mode) throws CryptoException {
      
      try {
        
        Cipher cipher = Cipher.getInstance (Algorithm.AES.toString ());
        
        Key secretKey = new SecretKeySpec (password.getBytes (), Algorithm.AES.toString ());
        
        cipher.init (mode, secretKey);
        
        return cipher;
        
      } catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
        throw new CryptoException (e);
      }
      
    }
    
  }