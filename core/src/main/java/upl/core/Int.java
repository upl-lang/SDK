  package upl.core;
    /*
     Created by Acuna on 17.07.2017
    */
  
  import upl.json.JSONArray;
  import upl.json.JSONObject;
  import upl.type.Strings;
  
  import java.io.File;
  import java.io.IOException;
  import java.io.InputStream;
  import java.lang.Math;
  import java.text.DecimalFormat;
  import java.text.DecimalFormatSymbols;
  import java.text.NumberFormat;
  import java.util.Locale;
  import java.util.Map;
  import java.util.Set;
  import java.lang.String;
  import upl.util.ArrayList;
  import upl.util.List;
  
  public class Int {
    
    public static String mPrefix = "";
    private static String mFormat = mPrefix + "#,###";
    
    public static int size (CharSequence str) {
      return str.length ();
    }
    
    public static int size (Object[] items) {
      return items.length;
    }
    
    public static int size (int[] items) {
      return items.length;
    }
    
    public static int size (long[] items) {
      return items.length;
    }
    
    public static int size (float[] items) {
      return items.length;
    }
    
    public static int size (byte[] items) {
      return items.length;
    }
    
    public static int size (java.util.List<?> items) {
      return items.size ();
    }
    
    public static int size (Map<?, ?> items) {
      return items.size ();
    }
    
    public static int size (Set<?> items) {
      return items.size ();
    }
    
    public static long size (File file) {
      return file.length ();
    }
    
    public static int size (InputStream stream) throws IOException {
      return stream.available ();
    }
    
    public static int size (JSONArray items) {
      return items.length ();
    }
    
    public static int size (JSONObject items) {
      return items.length ();
    }
    
    public static int valueOf (Object str) {
      
      try {
        
        if (str != null) {
          
          Strings str2 = new Strings (str.toString ().trim ()).trimStart ("0");
          return Integer.parseInt (str2.toString ());
          
        } else return 0;
        
      } catch (NumberFormatException e) {
        return 0;
      }
      
    }
    
    public static String numberFormat (String number, int num) {
      return numberFormat (number, num, ",");
    }
    
    public static String numberFormat (double number, int num) {
      return numberFormat (number, num, ",");
    }
    
    public static double toDouble (String value) {
      return Double.parseDouble (value.replace (",", "."));
    }
    
    public static String numberFormat (String number, int num, String sep1) {
      return numberFormat (toDouble (number), num, sep1);
    }
    
    public static String numberFormat (double number, int num, String sep1) {
      
      DecimalFormat df = new DecimalFormat ("#" + sep1 + new Strings ("#").repeat (num));
      return df.format (number);
      
    }
    
    public static int correct (String num, String result) {
      return correct (num, Integer.parseInt (result));
    }
    
    public static int correct (String num, int result) {
      return correct (Integer.parseInt (num), result);
    }
    
    public static int correct (int num, int result) {
      return correct (num, result, 0);
    }
    
    public static int correct (int num, int result, int expect) {
      
      if (num <= expect) num = result;
      return num;
      
    }
    
    public static boolean isNumeric (String str) {
      
      try {
        
        Integer.parseInt (str);
        return true;
        
      } catch (NumberFormatException e) {
        return false;
      }
      
    }
    
    public static boolean isLong (Object str) {
      return (str instanceof Long);
    }
    
    public static boolean isLong (String str) {
      
      try {
        
        Long.parseLong (str);
        return true;
        
      } catch (NumberFormatException e) {
        return false;
      }
      
    }
    
    public static boolean isDouble (double d) {
      return (Math.abs (Math.floor (d) - d) < 1E-5);
    }
    
    public static int intval (char str) {
      return Character.getNumericValue (str);
    }
    
    public static String toIntString (String part) {
      return part.replaceAll ("[,\\s+]", "");
    }
    
    public static int toInt (String part) {
      
      try {
        return Integer.parseInt (part);
      } catch (NumberFormatException e) {
        return 0;
      }
      
    }
    
    public static long toLong (String part) {
      
      try {
        return Long.parseLong (part);
      } catch (NumberFormatException e) {
        return 0;
      }
      
    }
    
    public static int compare (int x, int y) {
      return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
    
    public static int compare (long x, long y) {
      return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
    
    public static int compareUnsigned (int x, int y) {
      return compare (x + Integer.MIN_VALUE, y + Integer.MIN_VALUE);
    }
    
    public static String addZero (String number) {
      return addZero (valueOf (number));
    }
    
    public static String addZero (int number) {
      return addZero (number, 1);
    }
    
    public static String addZero (String number, int num) {
      return addZero (valueOf (number), num);
    }
    
    public static String addZero (String number, int num, String prefix) {
      return addZero (valueOf (number), num, prefix);
    }
    
    public static String addZero (int number, int num) {
      return addZero (number, num, "0");
    }
    
    public static String addZero (int number, int num, String prefix) { // Добавляет ведущий нуль, если число number меньше, чем его num-значный эквивалент. Короче, нуль ведущий добавляет.
      
      String output = String.valueOf (number);
      if (size (output) <= num) output = prefix + output;
      
      return output;
      
    }
    
    public static int getRangeNum (int day, int total, int rangeNum) {
      
      int i;
      double range = Math.ceil ((double) total / rangeNum); // 6
      
      for (i = 0; i < rangeNum; ++i)
        if (range * (i + 1) > day)
          return i;
      
      return i;
      
    }
    
    public static List<List<Integer>> getRanges (int[] range, int num) {
      
      List<List<Integer>> ranges = new ArrayList<> ();
      
      float modulo = (range.length % num);
      int part = (int) ((range.length / num) - modulo);
      
      //(modulo > 1 ? 1 : 0)
      
      int i2 = 0;
      
      List<Integer> rrange = new ArrayList<> ();
      
      for (int i = 0; i < range.length; i++) {
        
        if (i2 == num) {
          
          ranges.put (rrange);
          
          rrange = new ArrayList<> ();
          i2 = 0;
          
        } else {
          
          rrange.put (range[i]);
          i2++;
          
        }
        
      }
      
      return ranges;
      
    }
    
    public static List<List<Integer>> getRanges (int[] range, int num, int num2) {
      
      List<List<Integer>> ranges = new ArrayList<> ();
      
      for (int i = 0; i < num; i++) {
        
        List<Integer> rrange = new ArrayList<> ();
        
        for (int r : range) {
          
          rrange.put (r);
          
        }
        
      }
      
      return ranges;
      
    }
    
    /**
     * It will return suitable pattern for format decimal
     * For example: 10.2 -> return 0 | 10.23 -> return 00, | 10.235 -> return 000
     */
    public static StringBuilder getDecimalPattern (String str) {
      
      int decimalCount = size (str) - str.indexOf (",") - 1;
      
      StringBuilder pattern = new StringBuilder ();
      
      for (int i = 0; i < decimalCount && i < 3; i++)
        pattern.append ("0");
      
      return pattern;
      
    }
    
    public static DecimalFormat getFormat (Locale locale) {
      
      DecimalFormatSymbols symbols = new DecimalFormatSymbols (locale);
      
      symbols.setDecimalSeparator (' ');
      symbols.setGroupingSeparator ('.');
      
      return new DecimalFormat ("#,##,###,####", symbols);
      
    }
    
    public static String format (double value) {
      return format (value, Locale.getDefault ());
    }
    
    public static String format (double value, Locale locale) {
      return getFormat (locale).format (value);
    }
    
    public static String mksize (float bytes) {
      return mksize ((long) bytes);
    }
    
    public static String mksize (long bytes) {
      return mksize (bytes, "");
    }
    
    public static String mksize (long bytes, String value) {
      return mksize (bytes, value, " ");
    }
    
    public static String mksize (long bytes, String value, String sep) {
      return mksize (bytes, value, sep, true);
    }
    
    public static String mksize (long bytes, String value, String sep, boolean si) {
      return mksize (bytes, value, sep, si, 3);
    }
    
    public static String mksize (long bytes, String value, String sep, boolean si, int num) {
      return mksize (bytes, value, sep, si, num, Locale.getDefault ());
    }
    
    public static String mksize (long bytes, String value, String sep, boolean si, int num, Locale locale) {
      return mksize (bytes, value, sep, si, num, locale, new String[] {"b", "kb", "Mb", "Gb", "Tb"});
    }
    
    public static String mksize (long bytes, String value, String sep, boolean si, int num, Locale locale, String[] lang) {
      
      String output = "";
      int unit = (si ? 1024 : 1000);
      
      if (!value.equals ("")) {
        
        for (int i = 0; i < size (lang); ++i)
          if (value.equals (lang[i]) || value.equals ("b")) {
            
            if (i > 0) {
              
              double pow = Math.pow (unit, i);
              output = String.valueOf (numberFormat (bytes / pow, num));
              
            } else output = String.valueOf (bytes);
            
          }
        
      } else {
        
        for (int i = 0; i < size (lang); ++i) {
          
          double pow = Math.pow (unit, i);
          
          if (bytes >= pow) {
            
            DecimalFormat format = (DecimalFormat) NumberFormat.getNumberInstance (locale);
            format.applyPattern ((i < 3) ? "#0" : "#0.00");
            
            output = format.format (bytes / pow) + sep + lang[i];
            
          }
          
        }
        
        if (output.equals ("")) output = "0 " + lang[0];
        
      }
      
      return output;
      
    }
    
    public static int ensureRange (int value, int min, int max) {
      return Math.min (Math.max (value, min), max);
    }
    
    public static boolean inRange (int value, int min, int max) {
      return (value >= min) && (value <= max);
    }
    
  }