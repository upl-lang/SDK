  package upl.cipher.exceptions;
  
  public class DecryptException extends Exception {
    
    public DecryptException (Exception e) {
      super (e);
    }
    
    @Override
    public Exception getCause () {
      return (Exception) super.getCause ();
    }
    
  }