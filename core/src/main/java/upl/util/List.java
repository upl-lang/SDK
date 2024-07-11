  package upl.util;
  
  import java.util.Collections;
  import upl.type.Strings;
  
  public abstract class List<V> implements java.util.List<V> {
    
    public List<V> put (V k) {
      
      add (k);
      return this;
      
    }
    
    public int length () {
      return size ();
    }
    
    public String implode () {
      return implode (true);
    }
    
    public String implode (boolean isEmpty) {
      return implode ("\n", isEmpty);
    }
    
    public String implode (String preg) {
      return implode (preg, 0);
    }
    
    public String implode (String preg, boolean isEmpty) {
      return implode (preg, 0, 0, isEmpty);
    }
    
    public String implode (String preg, int min, boolean isEmpty) {
      return implode (preg, min, 0, isEmpty);
    }
    
    public String implode (String preg, int min) {
      return implode (preg, min, 0);
    }
    
    public String implode (String preg, int min, int max) {
      return implode (preg, min, max, true);
    }
    
    public String implode (String preg, int min, int max, boolean isEmpty) {
      
      StringBuilder output = new StringBuilder ();
      
      int length = length ();
      
      if (min < 0) min = (length + min);
      
      if (max == 0)
        max = length;
      else if (max < 0)
        max = (length + max);
      
      if (length > 0) {
        
        for (int i = min; i < max; ++i) {
          
          V obj = get (i);
          
          if (isEmpty || !Strings.isEmpty (obj.toString ())) {
            
            if (i > min) output.append (preg);
            
            if (obj instanceof Exception)
              output.append (((Exception) obj).getMessage ());
            else
              output.append (obj);
            
          }
          
        }
        
      }
      
      return output.toString ();
      
    }
    
    public Object extend () {
      return (length () > 1 ? get (1) : "");
    }
    
    public int getKey (V key) {
      return indexOf (key);
    }
    
    public List<?> toList (Object array) {
      
      List<Object> result = new ArrayList<> ();
      
      if (!(array instanceof Object[])) {
        
        if (array instanceof int[])
          for (int value : (int[]) array) result.add (value);
        else if (array instanceof boolean[])
          for (boolean value : (boolean[]) array) result.add (value);
        else if (array instanceof long[])
          for (long value : (long[]) array) result.add (value);
        else if (array instanceof float[])
          for (float value : (float[]) array) result.add (value);
        else if (array instanceof double[])
          for (double value : (double[]) array) result.add (value);
        else if (array instanceof short[])
          for (short value : (short[]) array) result.add (value);
        else if (array instanceof byte[])
          for (byte value : (byte[]) array) result.add (value);
        else if (array instanceof char[])
          for (char value : (char[]) array) result.add (value);
        
      } else result = (List<Object>) java.util.Arrays.asList ((Object[]) array);
      
      return result;
      
    }
    
    public boolean hasKey (int key) {
      
      try {
        
        get (key);
        return true;
        
      } catch (ArrayIndexOutOfBoundsException e) {
        return false;
      }
      
    }
    
    public int startKey () {
      return 0;
    }
    
    public V startValue () {
      return get (startKey ());
    }
    
    public int endKey () {
      return (length () - 1);
    }
    
    public V endValue () {
      return get (endKey ());
    }
    
    protected int total = 0;
    
    public V getValue (int i) {
      
      int size = length ();
      
      try {
        return get ((i >= size) ? (i - total) : i);
      } catch (IndexOutOfBoundsException e) {
        
        total = i;
        return getValue (i);
        
      }
      
    }
    
    public List<?> order (List<?> array2) {
      return order (array2, new ArrayList<> ());
    }
    
    public List<?> order (List<?> array2, List<V> output) {
      
      int i = 0;
      
      for (Object object : array2) {
        
        if (!contains (object)) {
          
          output.add (get (i));
          ++i;
          
        } else output.add ((V) object);
        
      }
      
      return output;
      
    }
    
    public List<?> unique () {
      
      for (int i = 0; i < length (); i++) {
        
        V item = get (i);
        
        if (!contains (item))
          add (item);
        
      }
      
      return this;
      
    }
    
    public V getPrev (V uid) {
      
      int idx = getKey (uid);
      
      if (idx <= 0)
        return null;
      else
        return get (idx - 1);
      
    }
    
    public V getNext (V uid) {
      
      int idx = getKey (uid);
      
      if (idx < 0 || idx + 1 == length ())
        return null;
      else
        return get (idx + 1);
      
    }
    
    public List<?> rsort () {
      
      this.sort (Collections.reverseOrder ()); // TODO Android < 24 support
      return this;
      
    }
    
    public String[] toStringArray () {
      
      String[] strings = new String[length ()];
      
      for (int i = 0; i < length (); ++i)
        strings[i] = get (i).toString ();
      
      return strings;
      
    }
    
    @Override
    public String toString () {
      return implode (", ");
    }
    
    public Integer increment (int index) {
      
      Integer value = (Integer) get (index);
      
      value++;
      
      set (index, (V) value);
      
      return value;
      
    }
    
    public Integer decrement (int index) {
      
      Integer value = (Integer) get (index);
      
      value--;
      
      set (index, (V) value);
      
      return value;
      
    }
    
  }