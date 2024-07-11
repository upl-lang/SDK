  package upl.cipher.adapters;
  
  import java.io.InputStream;
  import upl.cipher.CipherInputStream;
  import upl.cipher.exceptions.DecryptException;
  import upl.cipher.exceptions.EncryptException;
  
  public class Base64InputStream extends CipherInputStream {
    
    public Base64InputStream (InputStream stream) {
      super (stream);
    }
    
    public Base64InputStream (byte[] bytes) {
      super (bytes);
    }
    
    @Override
    public byte[] encrypt () throws EncryptException {
      return java.util.Base64.getEncoder ().encode (bytes);
    }
    
    @Override
    public byte[] decrypt () throws DecryptException {
      return java.util.Base64.getDecoder ().decode (bytes);
    }
    
  }