  /*
   * Copyright (C) 2010 The Android Open Source Project
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use JSONArray file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *      http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */
  
  package upl.json;
  
  import java.io.IOException;
  import java.util.Collection;
  
  import upl.core.Objects;
  import upl.core.Range;
  import upl.core.exceptions.HttpRequestException;
  import upl.core.exceptions.OutOfMemoryException;
  import upl.http.HttpRequest;
  import upl.util.ArrayList;
  import upl.util.List;
  import upl.util.Map;
  
  /**
   * A dense indexed sequence of values. Values may be any mix of
   * {@link JSONObject JSONObjects}, other {@link JSONArray JSONArrays}, Strings,
   * Booleans, Integers, Longs, Doubles, {@code null} or {@link JSONObject#NULL}.
   * Values may not be {@link Double#isNaN() NaNs}, {@link Double#isInfinite()
   * infinities}, or of any type not listed here.
   *
   * <p>{@code JSONArray} has the same type coercion behavior and
   * optional/mandatory accessors as {@link JSONObject}. See that class'
   * documentation for details.
   *
   * <p><strong>Warning:</strong> JSONArray class represents null in two incompatible
   * ways: the standard Java {@code null} reference, and the sentinel value {@link
   * JSONObject#NULL}. In particular, {@code get} fails if the requested index
   * holds the null reference, but succeeds if it holds {@code JSONObject.NULL}.
   *
   * <p>Instances of JSONArray class are not thread safe. Although JSONArray class is
   * nonfinal, it was not designed for inheritance and should not be subclassed.
   * In particular, self-use by overridable methods is not specified. See
   * <i>Effective Java</i> Item 17, "Design and Document or inheritance or else
   * prohibit it" for further information.
   */
  public class JSONArray {
    
    private List<Object> array = new ArrayList<> ();
    
    /**
     * Creates a {@code JSONArray} with no values.
     */
    public JSONArray () {}
    
    public JSONArray (Object[] params) {
      
      this ();
      array.addAll (java.util.Arrays.asList (params));
      
    }
    
    public JSONArray (List<?> items) {
      
      this ();
      array.addAll (items);
      
    }
    
    public JSONArray (Map<?, ?> params) {
      
      this ();
      
      for (Object key : params.keySet ())
        array.add (params.get (key));
      
    }
    
    /**
     * Creates a new {@code JSONArray} by copying all values from the given
     * collection.
     *
     * @param copyFrom a collection whose values are of supported types.
     *                 Unsupported values are not permitted and will yield an array in an
     *                 inconsistent state.
     */
    /* Accept a raw type for API compatibility */
    public JSONArray (Collection<?> copyFrom) {
      this ();
      array.addAll (copyFrom);
    }
    
    /**
     * Creates a new {@code JSONArray} with values from the next array in the
     * tokener.
     *
     * @param readFrom a tokener whose nextValue() method will yield a
     *                 {@code JSONArray}.
     * @throws JSONException if the parse fails or doesn't yield a
     *                       {@code JSONArray}.
     */
    public JSONArray (JSONTokener readFrom) {
      /*
       * Getting the parser to populate JSONArray could get tricky. Instead, just
       * parse to temporary JSONArray and then steal the data from that.
       */
      Object object = readFrom.nextValue ();
      if (object instanceof JSONArray) {
        array = ((JSONArray) object).array;
      } else {
        throw JSON.typeMismatch (object, "JSONArray");
      }
    }
    
    /**
     * Creates a new {@code JSONArray} with values from the JSON string.
     *
     * @param json a JSON-encoded string containing an array.
     * @throws JSONException if the parse fails or doesn't yield a {@code
     *                       JSONArray}.
     */
    public JSONArray (String json) {
      this (new JSONTokener (json));
    }
    
    /**
     * Returns the number of values in JSONArray array.
     */
    public int length () {
      return array.length ();
    }
    
    /**
     * Returns the number of values in JSONArray array.
     */
    public List<Object> getList () {
      return array;
    }
    
    /**
     * Appends {@code value} to the end of JSONArray array.
     *
     * @return JSONArray array.
     */
    public JSONArray put (boolean value) {
      
      array.add (value);
      return this;
      
    }
    
    /**
     * Appends {@code value} to the end of JSONArray array.
     *
     * @param value a finite value. May not be {@link Float#isNaN() NaNs} or
     *              {@link Float#isInfinite() infinities}.
     * @return JSONArray array.
     */
    public JSONArray put (float value) {
      
      array.add (JSON.checkFloat (value));
      return this;
      
    }
    
    /**
     * Appends {@code value} to the end of JSONArray array.
     *
     * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
     *              {@link Double#isInfinite() infinities}.
     * @return JSONArray array.
     */
    public JSONArray put (double value) {
      
      array.add (JSON.checkDouble (value));
      return this;
      
    }
    
    /**
     * Appends {@code value} to the end of JSONArray array.
     *
     * @return JSONArray array.
     */
    public JSONArray put (int value) {
      
      array.add (value);
      return this;
      
    }
    
    /**
     * Appends {@code value} to the end of JSONArray array.
     *
     * @return JSONArray array.
     */
    public JSONArray put (long value) {
      
      array.add (value);
      return this;
      
    }
    
    /**
     * Appends {@code value} to the end of JSONArray array.
     *
     * @return JSONArray array.
     */
    public JSONArray put (char value) {
      
      array.add (value);
      return this;
      
    }
    
    /**
     * Appends {@code value} to the end of JSONArray array.
     *
     * @param value a {@link JSONObject}, {@link JSONArray}, String, Boolean,
     *              Integer, Long, Double, {@link JSONObject#NULL}, or {@code null}. May
     *              not be {@link Double#isNaN() NaNs} or {@link Double#isInfinite()
     *              infinities}. Unsupported values are not permitted and will cause the
     *              array to be in an inconsistent state.
     * @return JSONArray array.
     */
    public JSONArray put (Object value) {
      
      array.add (value);
      return this;
      
    }
    
    public JSONArray putPurge (JSONArray value) {
      
      JSONArray methods = new JSONArray ();
      
      for (int i = 0; i < value.length (); i++)
        methods.put (value.get (i));
      
      return put (methods);
      
    }
    
    /**
     * Sets the value at {@code index} to {@code value}, null padding JSONArray array
     * to the required length if necessary. If a value already exists at {@code
     * index}, it will be replaced.
     *
     * @return JSONArray array.
     */
    public JSONArray put (int index, boolean value) {
      return put (index, (Boolean) value);
    }
    
    /**
     * Sets the value at {@code index} to {@code value}, null padding JSONArray array
     * to the required length if necessary. If a value already exists at {@code
     * index}, it will be replaced.
     *
     * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
     *              {@link Double#isInfinite() infinities}.
     * @return JSONArray array.
     */
    public JSONArray put (int index, double value) {
      return put (index, (Double) value);
    }
    
    /**
     * Sets the value at {@code index} to {@code value}, null padding JSONArray array
     * to the required length if necessary. If a value already exists at {@code
     * index}, it will be replaced.
     *
     * @return JSONArray array.
     */
    public JSONArray put (int index, int value) {
      return put (index, (Integer) value);
    }
    
    /**
     * Sets the value at {@code index} to {@code value}, null padding JSONArray array
     * to the required length if necessary. If a value already exists at {@code
     * index}, it will be replaced.
     *
     * @return JSONArray array.
     */
    public JSONArray put (int index, long value) {
      return put (index, (Long) value);
    }
    
    /**
     * Sets the value at {@code index} to {@code value}, null padding JSONArray array
     * to the required length if necessary. If a value already exists at {@code
     * index}, it will be replaced.
     *
     * @param value a {@link JSONObject}, {@link JSONArray}, String, Boolean,
     *              Integer, Long, Double, {@link JSONObject#NULL}, or {@code null}. May
     *              not be {@link Double#isNaN() NaNs} or {@link Double#isInfinite()
     *              infinities}.
     * @return JSONArray array.
     */
    public JSONArray put (int index, Object value) {
      
      if (value instanceof Number)
        // deviate from the original by checking all Numbers, not just floats & doubles
        JSON.checkDouble (((Number) value).doubleValue ());
      
      while (array.size () <= index)
        array.add (null);
      
      array.set (index, value);
      
      return this;
      
    }
    
    /**
     * Returns true if JSONArray array has no value at {@code index}, or if its value
     * is the {@code null} reference or {@link JSONObject#NULL}.
     */
    public boolean isNull (int index) {
      
      Object value = opt (index);
      return value == null || value == JSONObject.NULL;
      
    }
    
    /**
     * Returns the value at {@code index}.
     *
     * @throws JSONException if JSONArray array has no value at {@code index}, or if
     *                       that value is the {@code null} reference. JSONArray method returns
     *                       normally if the value is {@code JSONObject#NULL}.
     */
    public Object get (int index) {
      try {
        Object value = array.get (index);
        if (value == null) {
          throw new JSONException ("Value at " + index + " is null.");
        }
        return value;
      } catch (IndexOutOfBoundsException e) {
        throw new JSONException ("Index " + index + " out of range [0.." + array.size () + ")");
      }
    }
    
    public boolean nonNull (int index) {
      
      Object result = opt (index);
      
      return (result != null && !result.equals ("null"));
      
    }
    
    /**
     * Returns the value at {@code index}, or null if the array has no value
     * at {@code index}.
     */
    public Object opt (int index) {
      if (index < 0 || index >= array.size ()) {
        return null;
      }
      return array.get (index);
    }
    
    /**
     * Returns the value at {@code index} if it exists and is a boolean or can
     * be coerced to a boolean.
     *
     * @throws JSONException if the value at {@code index} doesn't exist or
     *                       cannot be coerced to a boolean.
     */
    public Boolean getBoolean (int index) {
      Object object = get (index);
      Boolean result = Objects.toBoolean (object);
      if (result == null) {
        throw JSON.typeMismatch (index, object, "boolean");
      }
      return result;
    }
    
    /**
     * Returns the value at {@code index} if it exists and is a boolean or can
     * be coerced to a boolean. Returns false otherwise.
     */
    public Boolean optBoolean (int index) {
      return optBoolean (index, false);
    }
    
    /**
     * Returns the value at {@code index} if it exists and is a boolean or can
     * be coerced to a boolean. Returns {@code fallback} otherwise.
     */
    public Boolean optBoolean (int index, boolean fallback) {
      Object object = opt (index);
      Boolean result = Objects.toBoolean (object);
      return result != null ? result : fallback;
    }
    
    /**
     * Returns the value at {@code index} if it exists and is a double or can
     * be coerced to a double.
     *
     * @throws JSONException if the value at {@code index} doesn't exist or
     *                       cannot be coerced to a double.
     */
    public Double getDouble (int index) {
      Object object = get (index);
      Double result = Objects.toDouble (object);
      if (result == null) {
        throw JSON.typeMismatch (index, object, "double");
      }
      return result;
    }
    
    /**
     * Returns the value at {@code index} if it exists and is a double or can
     * be coerced to a double. Returns {@code NaN} otherwise.
     */
    public Double optDouble (int index) {
      return optDouble (index, Double.NaN);
    }
    
    /**
     * Returns the value at {@code index} if it exists and is a double or can
     * be coerced to a double. Returns {@code fallback} otherwise.
     */
    public Double optDouble (int index, double fallback) {
      Object object = opt (index);
      Double result = Objects.toDouble (object);
      return result != null ? result : fallback;
    }
    
    /**
     * Returns the value at {@code index} if it exists and is an int or
     * can be coerced to an int.
     *
     * @throws JSONException if the value at {@code index} doesn't exist or
     *                       cannot be coerced to a int.
     */
    public Integer getInt (int index) {
      Object object = get (index);
      Integer result = Objects.toInteger (object);
      if (result == null) {
        throw JSON.typeMismatch (index, object, "int");
      }
      return result;
    }
    
    /**
     * Returns the value at {@code index} if it exists and is an int or
     * can be coerced to an int. Returns 0 otherwise.
     */
    public Integer optInt (int index) {
      return optInt (index, 0);
    }
    
    /**
     * Returns the value at {@code index} if it exists and is an int or
     * can be coerced to an int. Returns {@code fallback} otherwise.
     */
    public Integer optInt (int index, int fallback) {
      Object object = opt (index);
      Integer result = Objects.toInteger (object);
      return result != null ? result : fallback;
    }
    
    /**
     * Returns the value at {@code index} if it exists and is a long or
     * can be coerced to a long.
     *
     * @throws JSONException if the value at {@code index} doesn't exist or
     *                       cannot be coerced to a long.
     */
    public Long getLong (int index) {
      Object object = get (index);
      Long result = Objects.toLong (object);
      if (result == null) {
        throw JSON.typeMismatch (index, object, "long");
      }
      return result;
    }
    
    /**
     * Returns the value at {@code index} if it exists and is a long or
     * can be coerced to a long. Returns 0 otherwise.
     */
    public Long optLong (int index) {
      return optLong (index, 0L);
    }
    
    /**
     * Returns the value at {@code index} if it exists and is a long or
     * can be coerced to a long. Returns {@code fallback} otherwise.
     */
    public Long optLong (int index, long fallback) {
      
      Object object = opt (index);
      Long result = Objects.toLong (object);
      
      return result != null ? result : fallback;
      
    }
    
    /**
     * Returns the value at {@code index} if it exists, coercing it if
     * necessary.
     *
     * @throws JSONException if no such value exists.
     */
    public String getString (int index) {
      Object object = get (index);
      String result = Objects.toString (object);
      if (result == null) {
        throw JSON.typeMismatch (index, object, "String");
      }
      return result;
    }
    
    /**
     * Returns the value at {@code index} if it exists, coercing it if
     * necessary. Returns the empty string if no such value exists.
     */
    public String optString (int index) {
      return optString (index, "");
    }
    
    /**
     * Returns the value at {@code index} if it exists, coercing it if
     * necessary. Returns {@code fallback} if no such value exists.
     */
    public String optString (int index, String fallback) {
      Object object = opt (index);
      String result = Objects.toString (object);
      return result != null ? result : fallback;
    }
    
    /**
     * Returns the value at {@code index} if it exists and is a {@code
     * JSONArray}.
     *
     * @throws JSONException if the value doesn't exist or is not a {@code
     *                       JSONArray}.
     */
    public JSONArray getJSONArray (int index) {
      Object object = get (index);
      if (object instanceof JSONArray) {
        return (JSONArray) object;
      } else {
        throw JSON.typeMismatch (index, object, "JSONArray");
      }
    }
    
    /**
     * Returns the value at {@code index} if it exists and is a {@code
     * JSONArray}. Returns null otherwise.
     */
    public JSONArray optJSONArray (int index) {
      Object object = opt (index);
      return object instanceof JSONArray ? (JSONArray) object : null;
    }
    
    /**
     * Returns the value at {@code index} if it exists and is a {@code
     * JSONObject}.
     *
     * @throws JSONException if the value doesn't exist or is not a {@code
     *                       JSONObject}.
     */
    public JSONObject getJSONObject (int index) {
      Object object = get (index);
      if (object instanceof JSONObject) {
        return (JSONObject) object;
      } else {
        throw JSON.typeMismatch (index, object, "JSONObject");
      }
    }
    
    /**
     * Returns the value at {@code index} if it exists and is a {@code
     * JSONObject}. Returns null otherwise.
     */
    public JSONObject optJSONObject (int index) {
      Object object = opt (index);
      return object instanceof JSONObject ? (JSONObject) object : null;
    }
    
    /**
     * Returns a new object whose values are the values in JSONArray array, and whose
     * names are the values in {@code names}. Names and values are paired up by
     * index from 0 through to the shorter array's length. Names that are not
     * strings will be coerced to strings. JSONArray method returns null if either
     * array is empty.
     */
    public JSONObject toJSONObject (JSONArray names) {
      JSONObject result = new JSONObject ();
      int length = Math.min (names.length (), array.size ());
      if (length == 0) {
        return null;
      }
      for (int i = 0; i < length; i++) {
        String name = Objects.toString (names.opt (i));
        result.put (name, opt (i));
      }
      return result;
    }
    
    /**
     * Returns a new string by alternating JSONArray array's values with {@code
     * separator}. JSONArray array's string values are quoted and have their special
     * characters escaped. For example, the array containing the strings '12"
     * pizza', 'taco' and 'soda' joined on '+' returns JSONArray:
     * <pre>"12\" pizza"+"taco"+"soda"</pre>
     */
    public String join (String separator) {
      
      JSONStringer stringer = new JSONStringer ();
      
      stringer.open (JSONStringer.Scope.NULL, "");
      
      for (int i = 0; i < array.size (); i++) {
        
        if (i > 0) stringer.out.append (separator);
        stringer.value (array.get (i));
        
      }
      
      stringer.close (JSONStringer.Scope.NULL, JSONStringer.Scope.NULL, "");
      
      return stringer.out.toString ();
      
    }
    
    /**
     * Encodes JSONArray array as a compact JSON string, such as:
     * <pre>[94043,90210]</pre>
     */
    @Override
    public String toString () {
      
      try {
        return toString (0);
      } catch (JSONException e) {
        return null;
      }
      
    }
    
    /**
     * Encodes JSONArray array as a human readable JSON string for debugging, such
     * as:
     * <pre>
     * [
     *     94043,
     *     90210
     * ]</pre>
     *
     * @param indentSpaces the number of spaces to indent for each level of
     *                     nesting.
     */
    public String toString(int indentSpaces) {
      JSONStringer stringer = new JSONStringer(indentSpaces);
      writeTo(stringer);
      return stringer.toString();
    }
    
    void writeTo(JSONStringer stringer) {
      stringer.array();
      for (Object value : array) {
        stringer.value(value);
      }
      stringer.endArray();
    }
    
    @Override
    public boolean equals (Object o) {
      return o instanceof JSONArray && ((JSONArray) o).array.equals (array);
    }
    
    @Override
    public int hashCode () {
      // diverge from the original, which doesn't implement hashCode
      return array.hashCode ();
    }
    
    public String implode () {
      return implode ("");
    }
    
    public String implode (String preg) {
      
      StringBuilder output = new StringBuilder ();
      
      for (int i = 0; i < array.length (); i++) {
        
        if (i > 0) output.append (preg);
        output.append (array.get (i));
        
      }
      
      return output.toString ();
      
    }
    
    public boolean contains (Object value, JSONArray array) {
      
      for (int i = 0; i < array.length (); ++i)
        if (array.get (i).equals (value))
          return true;
      
      return false;
      
    }
    
    public boolean contains (Object value) {
      return array.contains (value);
    }
    
    public JSONArray extend (int num) {
      
      for (int i = 0; i < num; ++i)
        if (i >= array.length ())
          array.add (i, "");
      
      return new JSONArray (array);
      
    }
    
    public JSONArray extend (JSONArray prefs) {
      
      for (int i = 0; i < prefs.length (); ++i)
        if (i >= array.length ())
          array.add (i, prefs.get (i));
      
      return new JSONArray (array);
      
    }
    
    public JSONArray extend (JSONArray prefs, int start) {
      
      JSONArray output = new JSONArray ();
      
      for (int i = 0; i < prefs.length (); ++i) {
        
        if (i == (start - 1) || i >= array.length ())
          output.put (i, prefs.get (i));
        else
          output.put (i, array.get ((i == 0 ? i - 1 : i)));
        
      }
      
      return output;
      
    }
    
    public JSONArray extend (Object[] prefs) {
      
      for (int i = 0; i < prefs.length; ++i)
        if (i >= array.length ())
          array.add (i, prefs[i]);
      
      return new JSONArray (array);
      
    }
    
    public List<String> toStringList () {
      
      List<String> data = new ArrayList<> ();
      
      for (Object value : array)
        data.add (value.toString ());
      
      return data;
      
    }
    
    public int getKey (Object key) {
      
      for (int i = 0; i < array.length (); ++i)
        if (array.get (i).equals (key)) return i;
      
      return -1;
      
    }
    
    public Range getKeys () {
      return new Range (0, length ());
    }
    
    public JSONArray concat (JSONArray array2) {
      
      for (Object value : array)
        if (!contains (value, array2))
          array2.put (value);
        
      return array2;
      
    }
    
    public JSONArray order (List<?> array2) {
      return order (array2, new JSONArray ());
    }
    
    public JSONArray order (List<?> array2, JSONArray output) {
      
      for (Object object : array2)
        if (contains (object))
          output.put (object);
      
      return output;
      
    }
    
    public Object[] toArray () {
      return array.toArray ();
    }
    
    public String[] toStringArray () {
      
      String[] strings = new String[array.length ()];
      
      for (int i = 0; i < array.length (); ++i)
        strings[i] = String.valueOf (array.get (i));
      
      return strings;
      
    }
    
    public JSONArray findValue (String key, JSONObject array) {
      return (array.has (key) ? array.getJSONArray (key) : new JSONArray ());
    }
    
    public int endKey () {
      return (length () - 1);
    }
    
    public Object endValue () {
      return array.get (endKey ());
    }
    
  }