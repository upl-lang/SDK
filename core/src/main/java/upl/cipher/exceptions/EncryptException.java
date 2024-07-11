  package upl.cipher.exceptions;
  
  public class EncryptException extends Exception {
    
    public EncryptException (Exception e) {
      super (e);
    }
    
    @Override
    public Exception getCause () {
      return (Exception) super.getCause ();
    }
    
  }