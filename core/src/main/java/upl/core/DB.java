  package upl.core;
  
  import upl.type.Object;
  
  public class DB extends Object {
    
    StringBuilder printItems = new StringBuilder ();
    
    public DB select () {
      
      printItems.append ("SELECT ");
      return this;
      
    }
    
    protected int colI = 0;
    
    public DB column (String key) {
      
      if (colI > 0) printItems.append (", ");
      printItems.append (key);
      
      colI++;
      
      return this;
      
    }
    
    public DB from (String table) {
      
      printItems.append ("\nFROM " + table);
      return this;
      
    }
    
    public DB where (String condition) {
      
      printItems.append ("\nWHERE " + condition);
      return this;
      
    }
    
    @Override
    public String toString () {
      return printItems.toString ();
    }
    
  }