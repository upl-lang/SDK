  /*
 * Copyright (c) 2023 UPL Foundation
 */
  
  package upl.app.params;
  
  import upl.app.Params;
  import upl.app.Application;
  import upl.core.Int;
  import upl.util.ArrayList;
  import upl.util.List;
  
  public class CLIParams extends Params {
    
    protected String[] args;
    
    protected List<String> files = new ArrayList<> ();
    
    public CLIParams (Application app, String[] args) {
      
      super (app);
      
      this.args = args;
      
    }
    
    @Override
    public void process () {
      
      if (object.isEmpty ()) {
        
        int i = 0;
        boolean isFile = true;
        
        while (i < args.length) {
          
          String arg = args[i];
          
          if (arg.startsWith ("-")) {
            
            isFile = false;
            
            String key = arg.substring (1);
            
            if (arg.startsWith ("--"))
              key = arg.substring (2);
            else
              key = shortKeys.get (key);
            
            i++;
            
            if (i < args.length) {
              
              if (object.containsKey (key)) {
                
                Object value = object.get (key);
                
                ArrayValues params;
                
                if (value instanceof ArrayValues) {
                  
                  params = (ArrayValues) value;
                  
                  params.put (getValue (i));
                  
                } else {
                  
                  params = new ArrayValues ();
                  
                  params.put (value);
                  params.put (getValue (i));
                  
                }
                
                object.add (key, params);
                
              } else object.add (key, getValue (i));
              
              if (Int.isNumeric (args[i])) i++;
              
            }
            
          } else {
            
            if (isFile) files.add (args[i]);
            
            i++;
            
          }
          
        }
        
      }
      
    }
    
    protected Object getValue (int i) {
      return args[i].startsWith ("-") && !Int.isNumeric (args[i]) ? true : args[i];
    }
    
    public List<String> getFiles () {
      return files;
    }
    
  }