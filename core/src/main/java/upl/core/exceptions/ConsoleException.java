  package upl.core.exceptions;
  
  import upl.util.List;
  
  public class ConsoleException extends Exception {
    
    public ConsoleException (Exception e) {
      super (e);
    }
    
    public ConsoleException (List<?> e) {
      this (e.implode ());
    }
    
    public ConsoleException (String e) {
      super (e);
    }
    
    @Override
    public Exception getCause () {
      return (Exception) super.getCause ();
    }
    
  }