  package upl.cipher.adapters;
  
  import java.io.FileInputStream;
  import java.io.FileNotFoundException;
  import java.io.FileOutputStream;
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.OutputStream;
  import java.security.InvalidKeyException;
  import java.security.KeyFactory;
  import java.security.NoSuchAlgorithmException;
  import java.security.PrivateKey;
  import java.security.PublicKey;
  import java.security.spec.InvalidKeySpecException;
  import java.security.spec.PKCS8EncodedKeySpec;
  import java.security.spec.X509EncodedKeySpec;
  import javax.crypto.Cipher;
  import javax.crypto.CipherInputStream;
  import javax.crypto.CipherOutputStream;
  import javax.crypto.KeyGenerator;
  import javax.crypto.NoSuchPaddingException;
  import javax.crypto.SecretKey;
  import javax.crypto.spec.SecretKeySpec;
  import upl.core.File;
  import upl.cipher.exceptions.CryptoException;
  import upl.cipher.exceptions.DecryptException;
  import upl.cipher.exceptions.EncryptException;
  
  public class PKE extends upl.cipher.CipherInputStream {
    
    // Based on http://www.macs.hw.ac.uk/~ml355/lore/pkencryption.htm
    
    public static final int keySize = 256;
    
    protected Cipher pkCipher, aesCipher;
    protected byte[] aesKey;
    protected SecretKeySpec aeskeySpec;
    
    protected File privateKeyFile, publicKeyFile, keyFile;
    protected OutputStream outStream;
    
    public PKE (InputStream stream) throws CryptoException {
      
      super (stream);
      
      try {
        
        pkCipher = Cipher.getInstance ("RSA");
        aesCipher = Cipher.getInstance ("AES");
        
      } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
        throw new CryptoException (e);
      }
      
    }
    
    /**
     * Creates a new AES key
     */
    public void makeKey () throws CryptoException {
      
      try {
        
        KeyGenerator kgen = KeyGenerator.getInstance ("AES");
        kgen.init (keySize);
        
        SecretKey key = kgen.generateKey ();
        
        aesKey = key.getEncoded ();
        aeskeySpec = new SecretKeySpec (aesKey, "AES");
        
      } catch (NoSuchAlgorithmException e) {
        throw new CryptoException (e);
      }
      
    }
    
    public PKE setPrivateKeyFile (File file) {
      
      privateKeyFile = file;
      return this;
      
    }
    
    /**
     * Decrypts an AES key from a file using an RSA private key
     */
    public void loadKey () throws CryptoException {
      
      try {
        
        // read private key to be used to decrypt the AES key
        byte[] encodedKey = new byte[(int) privateKeyFile.length ()];
        new FileInputStream (privateKeyFile).read (encodedKey);
        
        // create private key
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec (encodedKey);
        KeyFactory kf = KeyFactory.getInstance ("RSA");
        PrivateKey pk = kf.generatePrivate (privateKeySpec);
        
        // read AES key
        pkCipher.init (Cipher.DECRYPT_MODE, pk);
        aesKey = new byte[keySize / 8];
        
        CipherInputStream is = new CipherInputStream (new FileInputStream (keyFile), pkCipher);
        is.read (aesKey);
        
        aeskeySpec = new SecretKeySpec (aesKey, "AES");
        
      } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException e) {
        throw new CryptoException (e);
      }
      
    }
    
    public PKE setPublicKeyFile (File file) {
      
      publicKeyFile = file;
      return this;
      
    }
    
    public PKE setKeyFile (File file) {
      
      keyFile = file;
      return this;
      
    }
    
    /**
     * Encrypts the AES key to a file using an RSA public key
     */
    public void saveKey () throws CryptoException {
      
      try {
        
        // read public key to be used to encrypt the AES key
        byte[] encodedKey = new byte[(int) publicKeyFile.length ()];
        new FileInputStream (publicKeyFile).read (encodedKey);
        
        // create public key
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec (encodedKey);
        KeyFactory kf = KeyFactory.getInstance ("RSA");
        PublicKey pk = kf.generatePublic (publicKeySpec);
        
        // write AES key
        pkCipher.init (Cipher.ENCRYPT_MODE, pk);
        CipherOutputStream os = new CipherOutputStream (new FileOutputStream (keyFile), pkCipher);
        
        os.write (aesKey);
        os.close ();
        
      } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException e) {
        throw new CryptoException (e);
      }
      
    }
    
    public PKE setOutputFile (File file) throws FileNotFoundException {
      return setOutputStream (new FileOutputStream (file));
    }
    
    public PKE setOutputStream (OutputStream stream) {
      
      outStream = stream;
      return this;
      
    }
    
    /**
     * Encrypts and then copies the contents of a given file.
     */
    
    @Override
    public byte[] encrypt () throws EncryptException {
      
      try {
        
        aesCipher.init (Cipher.ENCRYPT_MODE, aeskeySpec);
        
        CipherOutputStream os = new CipherOutputStream (outStream, aesCipher);
        
        copy (os);
        os.close ();
        
        return null;
        
      } catch (IOException | InvalidKeyException e) {
        throw new EncryptException (e);
      }
      
    }
    
    /**
     * Decrypts and then copies the contents of a given file.
     */
    
    @Override
    public byte[] decrypt () throws DecryptException {
      
      try {
        
        aesCipher.init (Cipher.DECRYPT_MODE, aeskeySpec);
        
        copy (outStream);
        
        //is.close ();
        //os.close ();
        
        return null;
        
      } catch (IOException | InvalidKeyException e) {
        throw new DecryptException (e);
      }
      
    }
    
  }