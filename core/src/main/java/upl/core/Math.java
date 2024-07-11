  package upl.core;
  
  import java.math.BigDecimal;
  import java.math.RoundingMode;
  import upl.exceptions.ZeroException;
  
  public class Math {
    
    public static BigDecimal round (double value) {
      return round (value, 0);
    }
    
    public static BigDecimal round (double value, int precision) {
      return round (value, precision, RoundingMode.DOWN);
    }
    
    public static BigDecimal round (double value, int precision, RoundingMode mode) {
      return new BigDecimal (value).setScale (precision, mode);
    }
    
    public static int prop (int from, int to) {
      return prop (from, to, 100);
    }
    
    public static int prop (int from, int to, int divider) {
      
      int result = 0;
      if (to > 0) result = ((from * divider) / to);
      
      return result;
      
    }
    
    public static double prop (double from, double to) {
      return prop (from, to, 100);
    }
    
    public static double prop (double from, double to, double divider) {
      
      double result = 0;
      if (to > 0) result = ((from * divider) / to);
      
      return result;
      
    }
    
    public static long max (long... nums) {
      
      if (nums.length > 0) {
        
        long max = nums[0];
        
        for (int i = 1; i < nums.length; i++)
          if (nums[i] > max) max = nums[i];
        
        return max;
        
      } else throw new ZeroException ("Array length cannot be 0");
      
    }
    
    public static long min (long... nums) {
      
      if (nums.length > 0) {
        
        long max = nums[0];
        
        for (int i = 1; i < nums.length; i++)
          if (nums[i] < max) max = nums[i];
        
        return max;
        
      } else throw new ZeroException ("Array length cannot be 0");
      
    }
    
    public static long mod (long x, long y) {
      
      long result = x % y;
      if (result < 0) result += y;
      
      return result;
      
    }
    
  }