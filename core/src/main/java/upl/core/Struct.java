  /*
   * Copyright (c) 2020 - 2023 UPL Foundation
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *     http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
   * implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */
  
  package upl.core;
  
  import upl.util.HashMap;
  import upl.util.LinkedHashMap;
  import upl.util.Map;
  
  public abstract class Struct {
    
    public Map<String, Object> object = new LinkedHashMap<> ();
    
    public Map<String, Object> defValues = new HashMap<> ();
    
    protected Object defValue (String key, Object defVal) {
      
      Object value = defValues.get (key);
      if (value == null) value = defVal;
      
      return value;
      
    }
    
    public Struct setDefValue (String key, Object value) {
      
      defValues.add (key, value);
      
      return this;
      
    }
    
    /**
     Returns the value mapped by {@code name}, or null if no such mapping exists.
     */
    protected final Object opt (String key) {
      return opt (key, defValues.get (key));
    }
    
    protected final Object opt (String key, Object defValue) {
      
      Object value = object.get (key);
      
      if (value == null)
        return defValue;
      else
        return value;
      
    }
    
    /**
     Returns the value mapped by {@code name} if it exists, coercing it if necessary. Returns {@code fallback} if no such mapping exists.
     */
    public final String optString (String key, String defValue) {
      
      String result = Objects.toString (opt (key));
      return result != null ? result : defValue;
      
    }
    
    public final int optInt (String key, int defValue) {
      
      Integer result = Objects.toInteger (opt (key));
      return result != null ? result : defValue;
      
    }
    
    public final long optLong (String key, long defValue) {
      
      Long result = Objects.toLong (opt (key));
      return result != null ? result : defValue;
      
    }
    
    public final float optFloat (String key, float defValue) {
      
      Float result = Objects.toFloat (opt (key));
      return result != null ? result : defValue;
      
    }
    
    public final double optDouble (String key, double defValue) {
      
      Double result = Objects.toDouble (opt (key));
      return result != null ? result : defValue;
      
    }
    
    public final boolean optBool (String key, boolean defValue) {
      
      Boolean result = Objects.toBoolean (opt (key));
      return result != null ? result : defValue;
      
    }
    
    /**
     Returns the value mapped by {@code name} if it exists and is an int or can be coerced to an String. Returns {@code fallback} otherwise.
     */
    public final String optString (String key) {
      return optString (key, "");
    }
    
    /**
     Returns the value mapped by {@code name} if it exists and is an int or can be coerced to an int. Returns {@code fallback} otherwise.
     */
    public final int optInt (String key) {
      return optInt (key, 0);
    }
    
    /**
     Returns the value mapped by {@code name} if it exists and is a long or can be coerced to a long. Returns {@code fallback} otherwise.
     */
    public final long optLong (String key) {
      return optLong (key, 0L);
    }
    
    /**
     Returns the value mapped by {@code name} if it exists and is an int or can be coerced to an int. Returns {@code fallback} otherwise.
     */
    public final float optFloat (String key) {
      return optFloat (key, 0F);
    }
    
    /**
     Returns the value mapped by {@code name} if it exists and is a double or can be coerced to a double. Returns {@code fallback} otherwise.
     */
    public final double optDouble (String key) {
      return optDouble (key, 0D);
    }
    
    /**
     Returns the value mapped by {@code name} if it exists and is a boolean or can be coerced to a boolean. Returns {@code fallback} otherwise.
     */
    public final boolean optBool (String key) {
      return optBool (key, false);
    }
    
  }