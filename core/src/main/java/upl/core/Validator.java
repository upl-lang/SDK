  package upl.core;
  
  import java.util.regex.Pattern;
  
  public class Validator {
    
    public static boolean email (String email) {
      
      Pattern p = Pattern.compile ("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+");
      return p.matcher (email).matches ();
      
    }
    
  }