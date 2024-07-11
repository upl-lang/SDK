  package upl.util;
  
  public class RecursionLimiter {
    
    public RecursionLimiter (int maxLevel) {
      
      if (maxLevel > 0) {
        
        try {
          throw new IllegalStateException ("Too deep, emerging");
        } catch (IllegalStateException e) {
          if (e.getStackTrace ().length > maxLevel + 1) throw e;
        }
        
      }
      
    }
    
  }