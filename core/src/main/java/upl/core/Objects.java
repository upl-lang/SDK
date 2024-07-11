  package upl.core;
    /*
     Created by Acuna on 24.03.2018
    */
  
  import java.lang.Math;
  
  public class Objects {
    
    public static boolean isBool (String str) {
      return (str.equals ("true") || str.equals ("false"));
    }
    
    public static boolean equals (Object o1, Object o2) {
      return o1 == null ? o2 == null : o1.equals (o2);
    }
    
    public static int hashCode (Object o) {
      return o != null ? o.hashCode () : 0;
    }
    
    public static String getClassName (Object object) {
      return object.getClass ().getSimpleName ();
    }
    
    public static int materialsNum (int a, int h, Integer[]... walls) {
      
      int output = 0;
      
      for (Integer[] size : walls)
        output += (((1 / (double) a) * size[0]) * ((1 / (double) h) * size[1]));
      
      return output;
      
    }
    
    public static int cbmNum (int a, int b, int h) {
      return cbmNum (a, b, h, 1);
    }
    
    public static int cbmNum (int a, int b, int h, int quality) {
      return cbmNum (a, b, h, quality, 3);
    }
    
    public static int cbmNum (int a, int b, int h, int quality, int power) {
      
      int output = (a * b * h);
      double output2 = ((1 / ((double) output / Math.pow (1000, power))));
      
      if (quality > 0) output2 = (quality / output2);
      
      return (int) Math.ceil (output2);
      
    }
    
    public static Boolean toBoolean (Object value) {
      
      if (value instanceof Boolean)
        return (Boolean) value;
      else if (value instanceof String)
        return Boolean.valueOf (((String) value));
      else
        return null;
      
    }
    
    public static Double toDouble (Object value) {
      
      if (value instanceof Double)
        return (Double) value;
      else if (value instanceof Number)
        return ((Number) value).doubleValue ();
      else if (value instanceof String) {
        
        try {
          return Double.valueOf ((String) value);
        } catch (NumberFormatException e) {
          // empty
        }
        
      }
      
      return null;
      
    }
    
    public static Integer toInteger (Object value) {
      
      if (value instanceof Integer)
        return (Integer) value;
      else if (value instanceof Number)
        return ((Number) value).intValue ();
      else if (value instanceof String) {
        
        try {
          return (int) Double.parseDouble ((String) value);
        } catch (NumberFormatException e) {
          // empty
        }
        
      }
      
      return null;
      
    }
    
    public static Long toLong (Object value) {
      
      if (value instanceof Long)
        return (Long) value;
      else if (value instanceof Number)
        return ((Number) value).longValue ();
      else if (value instanceof String) {
        
        try {
          return (long) Double.parseDouble ((String) value);
        } catch (NumberFormatException e) {
          // empty
        }
        
      }
      
      return null;
      
    }
    
    public static Float toFloat (Object value) {
      
      if (value instanceof Float)
        return (Float) value;
      else if (value instanceof Number)
        return ((Number) value).floatValue ();
      else if (value instanceof String) {
        
        try {
          return Float.parseFloat ((String) value);
        } catch (NumberFormatException e) {
          // empty
        }
        
      }
      
      return null;
      
    }
    
    public static String toString (Object value) {
      
      if (value instanceof String)
        return (String) value;
      else if (value != null)
        return String.valueOf (value);
      else
        return null;
      
    }
    
    public static boolean isEmpty (String obj) {
      return (obj == null || obj.equals (""));
    }
    
  }