  package upl.cipher.exceptions;
  
  public class CryptoException extends Exception {
    
    public CryptoException (Exception e) {
      super (e);
    }
    
    @Override
    public Exception getCause () {
      return (Exception) super.getCause ();
    }
    
  }