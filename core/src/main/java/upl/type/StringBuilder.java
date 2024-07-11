  package upl.type;
  
  import java.lang.Object;
  
  public class StringBuilder {
    
    protected java.lang.StringBuilder str = new java.lang.StringBuilder ();
    
    public StringBuilder append (Object obj) {
      return append (obj, 0);
    }
    
    public StringBuilder append (Object obj, int repeat) {
      
      str.append (obj);
      
      if (repeat > 0)
        append ("  ".repeat (repeat));
      
      return this;
      
    }
    
    public StringBuilder appends (Object obj) {
      
      append (obj);
      append (" ");
      
      return this;
      
    }
    
    public void appendln () {
      appendln ("");
    }
    
    public void appendln (Object msg) {
      append (msg + Strings.LS);
    }
    
    public int length () {
      return str.length ();
    }
    
    @Override
    public java.lang.String toString () {
      return str.toString ();
    }
    
  }