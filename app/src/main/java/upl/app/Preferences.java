  /*
   * Copyright (c) 2023 UPL Foundation
   */
  
  package upl.app;
  
  import upl.core.Struct;
  import upl.exceptions.ParameterNotFoundException;
  import upl.json.JSONArray;
  import upl.json.JSONException;
  import upl.json.JSONObject;
  import upl.util.ArrayList;
  import upl.util.List;
  
  public abstract class Preferences extends Struct {
    
    protected Application app;
    
    public Preferences (Application app) {
      this.app = app;
    }
    
    protected boolean checkValue (String key, Object defValue) {
      return (defValue != null);
    }
    
    public void put (String key, Object value) {
      
      if (value instanceof Integer)
        put (key, (int) value);
      else if (value instanceof Long)
        put (key, (long) value);
      else if (value instanceof Float)
        put (key, (float) value);
      else if (value instanceof Double)
        put (key, Double.doubleToRawLongBits ((double) value));
      else if (value instanceof Boolean)
        put (key, (boolean) value);
      else
        put (key, String.valueOf (value));
      
    }
    
    public void put (String key, String value) {}
    public void put (String key, int value) {}
    public void put (String key, long value) {}
    public void put (String key, float value) {}
    public void put (String key, double value) {}
    public void put (String key, boolean value) {}
    
    public final String getString (String key) {
      
      String value = (String) opt (key);
      
      if (value != null && checkValue (key, value))
        return value;
      else
        throw new ParameterNotFoundException (key);
      
    }
    
    public final int getInt (String key) {
      
      Integer value = (Integer) opt (key);
      
      if (value != null && checkValue (key, value))
        return value;
      else
        throw new ParameterNotFoundException (key);
      
    }
    
    public final long getLong (String key) {
      
      Long value = (Long) opt (key);
      
      if (value != null && checkValue (key, value))
        return value;
      else
        throw new ParameterNotFoundException (key);
      
    }
    
    public final float getFloat (String key) {
      
      Float value = (Float) opt (key);
      
      if (value != null && checkValue (key, value))
        return value;
      else
        throw new ParameterNotFoundException (key);
      
    }
    
    public final double getDouble (String key) {
      
      Double value = (Double) opt (key);
      
      if (value != null && checkValue (key, value))
        return value;
      else
        throw new ParameterNotFoundException (key);
      
    }
    
    public final boolean getBool (String key) {
      
      Boolean value = (Boolean) opt (key);
      
      if (value != null && checkValue (key, value))
        return value;
      else
        throw new ParameterNotFoundException (key);
      
    }
    
    public final String[] getStringArray (String key) {
      return optString (key).split (",");
    }
    
    public final JSONArray get (String key, JSONArray value) throws JSONException {
      return new JSONArray (getString (key));
    }
    
    public final JSONObject get (String key, JSONObject value) throws JSONException {
      return new JSONObject (getString (key));
    }
    
    public void apply () {}
    
    public final void set (String key, Object value) {
      
      put (key, value);
      apply ();
      
    }
    
    public boolean contains (String string) {
      return false;
    }
    
    /*public String get (String key, String value, boolean encrypt) throws DecryptException {
      return get (key, value);
    }
    
    public abstract String get (String key, String defValue);
    public abstract int get (String key, int value);
    public abstract long get (String key, long value);
    public abstract float get (String key, float value);
    public abstract double get (String key, double defValue);
    public abstract boolean get (String key, boolean value);*/
    
    public static class ArrayValues {
      
      public List<Object> values = new ArrayList<> ();
      
      public String getString (int id) {
        return values.get (id).toString ();
      }
      
      public ArrayValues put (Object value) {
        
        values.add (value);
        
        return this;
        
      }
      
      public int getInt (int id) {
        return Integer.parseInt (getString (id));
      }
      
      public float getFloat (int id) {
        return Float.parseFloat (getString (id));
      }
      
      public boolean getBool (int id) {
        return Boolean.parseBoolean (getString (id));
      }
      
      @Override
      public String toString () {
        return values.toString ();
      }
      
    }
    
  }