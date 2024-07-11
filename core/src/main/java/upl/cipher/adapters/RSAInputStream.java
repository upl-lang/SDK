  package upl.cipher.adapters;
  
  import java.io.FileInputStream;
  import java.io.FileNotFoundException;
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.UnsupportedEncodingException;
  import java.security.InvalidKeyException;
  import java.security.KeyPair;
  import java.security.KeyPairGenerator;
  import java.security.KeyStore;
  import java.security.KeyStoreException;
  import java.security.NoSuchAlgorithmException;
  import java.security.PrivateKey;
  import java.security.PublicKey;
  import java.security.SecureRandom;
  import java.security.Signature;
  import java.security.SignatureException;
  import java.security.UnrecoverableEntryException;
  import java.security.cert.Certificate;
  import java.security.cert.CertificateException;
  import java.util.Base64;
  import java.lang.String;
  import javax.crypto.BadPaddingException;
  import javax.crypto.Cipher;
  import javax.crypto.IllegalBlockSizeException;
  import javax.crypto.NoSuchPaddingException;
  import upl.cipher.CipherInputStream;
  import upl.core.File;
  import upl.cipher.exceptions.CryptoException;
  import upl.cipher.exceptions.DecryptException;
  import upl.cipher.exceptions.EncryptException;
  import upl.type.Strings;
  
  public class RSAInputStream extends CipherInputStream {
    
    protected KeyPair keyPair;
    
    public RSAInputStream (InputStream stream) {
      super (stream);
    }
    
    public RSAInputStream (byte[] bytes) {
      super (bytes);
    }
    
    public KeyPair getKeyPair () throws CryptoException {
      return getKeyPair (2048);
    }
    
    public KeyPair getKeyPair (int keySize) throws CryptoException {
      
      try {
        
        if (keyPair == null) {
          
          KeyPairGenerator generator = KeyPairGenerator.getInstance ("RSA");
          
          generator.initialize (keySize, new SecureRandom ());
          
          keyPair = generator.generateKeyPair ();
          
        }
        
        return keyPair;
        
      } catch (NoSuchAlgorithmException e) {
        throw new CryptoException (e);
      }
      
    }
    
    public void getKeyPair (File file, String password) throws FileNotFoundException, CryptoException {
      getKeyPair (file, password, "mykey");
    }
    
    public void getKeyPair (File file, String password, String keyName) throws FileNotFoundException, CryptoException {
      getKeyPair (new FileInputStream (file), password, keyName);
    }
    
    public void getKeyPair (InputStream keyStream, String keyStorePassword, String keyName) throws CryptoException {
      
      try {
        
        //Generate with:
        //keytool -genkeypair -alias <keyName> -storepass <keyStorePassword> -keypass <keyStorePassword> -keyalg RSA -keystore keystore.jks
        
        KeyStore keyStore = KeyStore.getInstance ("JCEKS");
        
        keyStore.load (keyStream, keyStorePassword.toCharArray ()); // Keystore password
        KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection (keyStorePassword.toCharArray ()); // Key password
        
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry (keyName, keyPassword);
        
        Certificate cert = keyStore.getCertificate (keyName);
        
        PublicKey publicKey = cert.getPublicKey ();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey ();
        
        keyPair = new KeyPair (publicKey, privateKey);
        
      } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableEntryException e) {
        throw new CryptoException (e);
      }
      
    }
    
    @Override
    public byte[] encrypt () throws EncryptException {
      
      try {
        
        Cipher encryptCipher = Cipher.getInstance (Algorithm.RSA.toString ());
        
        encryptCipher.init (Cipher.ENCRYPT_MODE, getKeyPair ().getPublic ());
        
        return encryptCipher.doFinal (bytes);
        
      } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | CryptoException | IllegalBlockSizeException | BadPaddingException e) {
        throw new EncryptException (e);
      }
      
    }
    
    @Override
    public byte[] decrypt () throws DecryptException {
      
      try {
        
        Cipher decriptCipher = Cipher.getInstance (Algorithm.RSA.toString ());
        
        decriptCipher.init (Cipher.DECRYPT_MODE, getKeyPair ().getPrivate ());
        
        return decriptCipher.doFinal (bytes);
        
      } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | CryptoException | IllegalBlockSizeException | BadPaddingException e) {
        throw new DecryptException (e);
      }
      
    }
    
    public String genSignature () throws CryptoException {
      
      try {
        
        Signature privateSignature = Signature.getInstance ("SHA256withRSA");
        
        privateSignature.initSign (getKeyPair ().getPrivate ());
        privateSignature.update (password.getBytes (Strings.DEF_CHARSET));
        
        return Base64.getEncoder ().encodeToString (privateSignature.sign ());
        
      } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | SignatureException e) {
        throw new CryptoException (e);
      }
      
    }
    
    public boolean verifySignature (String signature) throws CryptoException {
      
      try {
        
        Signature publicSignature = Signature.getInstance ("SHA256withRSA");
        
        publicSignature.initVerify (getKeyPair ().getPublic ());
        publicSignature.update (password.getBytes (Strings.DEF_CHARSET));
        
        return publicSignature.verify (Base64.getDecoder ().decode (signature));
        
      } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | SignatureException e) {
        throw new CryptoException (e);
      }
      
    }
    
  }