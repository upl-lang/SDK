  package upl.core;
      /*
       Created by Acuna on 17.07.2017
      */
  
  import upl.type.Strings;
  import upl.util.ArrayList;
  import upl.util.List;
  
  import java.io.ByteArrayOutputStream;
  import java.io.IOException;
  
  public class Arrays {
    
    public static final String[] brArray = new String[] {"<br />", "<br>"};
    
    public static java.util.List<?> toList (String[] string) {
      return java.util.Arrays.asList (string);
    }
    
    public static List<String> explode (String text, int indexStart, int indexEnd, List<String> output) {
      
      if (indexEnd < 0) indexEnd = Int.size (text);
      String word = text.substring (indexStart, indexEnd);
      
      output.add (word);
      
      return output;
      
    }
    
    public static List<String> explode (String symb, File file) {
      return explode (symb, file.getAbsolutePath ());
    }
    
    public static List<String> explode (String symb, String text) {
      
      List<String> output = new ArrayList<> ();
      String str = text;
      
      int sLength = 1;
      int indexStart = 0;
      int indexEnd = str.indexOf (symb);
      
      explode (text, indexStart, indexEnd, output);
      
      while (indexEnd >= 0) {
        
        indexStart = indexEnd + sLength;
        indexEnd = str.indexOf (symb, indexEnd + 1);
        
        explode (text, indexStart, indexEnd, output);
        
      }
      
      return output;
      
    }
    
    public static String implode (String sep, Object[] string) {
      return implode (sep, string, true);
    }
    
    public static String implode (String sep, Object[] string, boolean isEmpty) {
      return implode (sep, string, 0, 0, isEmpty);
    }
    
    public static String implode (String sep, Object[] string, int min) {
      return implode (sep, string, min, 0, true);
    }
    
    public static String implode (String sep, Object[] string, int min, boolean isEmpty) {
      return implode (sep, string, min, 0, isEmpty);
    }
    
    public static String implode (String sep, Object[] string, int min, int max) {
      return implode (sep, string, min, max, true);
    }
    
    public static String implode (String sep, Object[] string, int min, int max, boolean isEmpty) {
      
      StringBuilder output = new StringBuilder ();
      
      int length = Int.size (string);
      
      if (min < 0) min = (length + min);
      
      if (max == 0)
        max = length;
      else if (max < 0)
        max = (length + max);
      
      if (length > 0) {
        
        //Log.w (string[0]);
        //Log.w (min);
        //Log.w (min);
        
        for (int i = min; i < max; ++i) {
          
          String value = String.valueOf (string[i]);
          
          if (isEmpty || !Strings.isEmpty (value)) {
            
            if (i > min) output.append (sep);
            
            if (!value.equals (sep))
              output.append (value);
            
          }
          
        }
        
      }
      
      return output.toString ();
      
    }
    
    public static String implode (String sep, int[] string) {
      
      StringBuilder str = new StringBuilder ();
      
      for (int i = 0; i < string.length; ++i) {
        
        if (i > 0) str.append (sep);
        str.append (string[i]);
        
      }
      
      return str.toString ();
      
    }
    
    public static String implode (String sep, char[] string) {
      
      StringBuilder str = new StringBuilder ();
      
      for (int i = 0; i < string.length; ++i) {
        
        if (i > 0) str.append (sep);
        str.append (string[i]);
        
      }
      
      return str.toString ();
      
    }
    
    /*public static String implode (String sep, java.util.List<?> items) {
      
      StringBuilder str = new StringBuilder ();
      
      for (int i = 0; i < items.size (); ++i) {
        
        if (i > 0) str.append (sep);
        str.append (items.get (i));
        
      }
      
      return str.toString ();
      
    }*/
    
    public static boolean contains (Object value, Object[] items) {
      
      if (items != null)
        for (Object item : items)
          if (item.equals (value))
            return true;
      
      return false;
      
    }
    
    public static boolean contains (int value, int[] items) {
      
      if (items != null)
        for (int item : items)
          if (item == value)
            return true;
      
      return false;
      
    }
    
    public static int search (String value, String[] arr) {
      return java.util.Arrays.binarySearch (arr, value);
    }
    
    public static String[] concat (String[] first, String[]... rest) {
      
      int totalLength = Int.size (first);
      for (String[] array : rest) totalLength += array.length;
      
      String[] result = java.util.Arrays.copyOf (first, totalLength);
      int offset = Int.size (first);
      
      for (String[] array : rest) {
        
        java.lang.System.arraycopy (array, 0, result, offset, array.length);
        offset += array.length;
        
      }
      
      return result;
    }
    
    public static String extend (String[] params) {
      return ((Int.size (params) > 1) ? params[1] : "");
    }
    
    public static String extend (String params) {
      
      if (params == null) params = "";
      return params;
      
    }
          
          /*public static String[] extend (String[] params) {
            return extend (params, Int.size (params));
          }*/
    
    public static String[] extend (String[] params, int num) {
      
      String[] output = new String[num];
      
      for (int i = 0; i < num; ++i) {
        
        if (i >= Int.size (params) || params[i] == null)
          output[i] = "";
        else
          output[i] = params[i];
        
      }
      
      return output;
      
    }
    
    public static List<?> toList (String item) {
      
      List<Object> list = new ArrayList<> ();
      list.add (item);
      
      return list;
      
    }
    
    public static int getKey (Object key, Object[] array) {
      
      for (int i = 0; i < array.length; ++i)
        if (array[i].equals (key)) return i;
      
      return -1;
      
    }
        
        /*public static int getKey (int key, int[] array) {
          
          for (int i = 0; i < array.length; ++i)
            if (array[i] == key) return i;
          
          return -1;
          
        }*/
    
    public static String[] strSplit (String str) {
      
      int length = Int.size (str);
      String[] output = new String[length];
      
      for (int i = 0; i < length; ++i)
        if ((i + 1) < length)
          output[i] = str.substring (i, (i + 1));
        else
          output[i] = str.substring (i);
      
      return output;
      
    }
    
    public static String[] toStringArray (String item) {
      return new String[] {item};
    }
    
    public static String rand (String[] array) {
      return array[randKey (array)];
    }
    
    public static String[] rand (String[] array, int num) {
      
      String[] output = new String[num];
      
      for (int i = 0; i < num; ++i)
        output[i] = rand (array);
      
      return output;
      
    }
    
    public static int randKey (String[] array) {
      
      int count = array.length;
      if (count > 0) count = (count - 1);
      
      return new Random ().generate (0, count);
      
    }
    
    public static Integer[] randKey (String[] array, int num) {
      
      Integer[] output = new Integer[num];
      for (int i = 0; i < num; ++i)
        output[i] = randKey (array);
      
      return output;
      
    }
    
    public static byte[] append (byte[]... arrays) throws IOException {
      
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream ();
      
      for (byte[] array : arrays)
        outputStream.write (array);
      
      return outputStream.toByteArray ();
      
    }
    
    public static String contains (String key, String[] array, String defValue) {
      
      if (!contains (key, array)) key = defValue;
      return key;
      
    }
    
    public static int contains (int key, Integer[] array, int defValue) {
      
      if (!contains (key, array)) key = defValue;
      return key;
      
    }
    
    public static int startKey (int key) {
      return Int.correct (key, 0);
    }
    
    public static int prevKey (int key) {
      return startKey (key - 1);
    }
    
    public static int nextKey (int key, int count) {
      
      key = key + 1;
      if (key >= count) key = prevKey (count);
      
      return key;
      
    }
    
    public static int endKey (int[] array) {
      return (array.length - 1);
    }
    
    public static int endValue (int[] array) {
      return array[endKey (array)];
    }
    
    public static int endKey (long[] array) {
      return (array.length - 1);
    }
    
    public static long endValue (long[] array) {
      return array[endKey (array)];
    }
    
    public static int endKey (Object[] array) {
      return (array.length - 1);
    }
    
    public static String endValue (String[] array) {
      return array[endKey (array)];
    }
    
    public static char[] hexArray = "0123456789ABCDEF".toCharArray ();
    
    public static String toString (byte[] bytes) {
      
      char[] hexChars = new char[bytes.length * 2];
      
      for (int j = 0; j < bytes.length; ++j) {
        
        int v = bytes[j] & 0xFF;
        
        hexChars[j * 2] = hexArray[v >>> 4];
        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        
      }
      
      return new String (hexChars);
      
    }
    
    public static Object[] range (Object[] array, int start, int end) {
      
      Object[] output = new Object[end - start];
      
      for (int i = 0; i < end - start; i++)
        output[i] = array[i + start];
      
      return output;
      
    }
    
    public static Object[] range (Object[] array, int start) {
      
      Object[] output = new Object[array.length];
      
      int i = start, i2 = 0;
      
      while (i2 < array.length) {
        
        if (i >= array.length) i = 0;
        output[i2] = array[i];
        
        i++;
        i2++;
        
      }
      
      return output;
      
    }
    
    public Object[] reverse (Object[] array) {
      
      Object[] output = new Object[array.length];
      int count = array.length - 1;
      
      for (Object num : array) {
        
        output[count] = num;
        count--;
        
      }
      
      return output;
      
    }
    
  }