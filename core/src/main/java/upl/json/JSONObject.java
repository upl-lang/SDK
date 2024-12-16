	/*
	 * Copyright (C) 2010 The Android Open Source Project
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 *			http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */
	
	package upl.json;
	
	import java.util.ArrayList;
	import java.util.Iterator;
	
	import upl.core.Struct;
	import upl.type.Strings;
	import upl.util.Map;
	
	/**
	 * A modifiable set of name/value mappings. Names are unique, non-null strings.
	 * Values may be any mix of {@link JSONObject JSONObjects}, {@link JSONArray
	 * JSONArrays}, Strings, Booleans, Integers, Longs, Doubles or {@link #NULL}.
	 * Values may not be {@code null}, {@link Double#isNaN() NaNs}, {@link
	 * Double#isInfinite() infinities}, or of any type not listed here.
	 *
	 * <p>This class can coerce values to another type when requested.
	 * <ul>
	 * 	 <li>When the requested type is a boolean, strings will be coerced
	 * 	     using {@link Boolean#valueOf(String)}.
	 * 	 <li>When the requested type is a double, other {@link Number} types will
	 * 	     be coerced using {@link Number#doubleValue() doubleValue}. Strings
	 * 	     that can be coerced using {@link Double#valueOf(String)} will be.
	 * 	 <li>When the requested type is an int, other {@link Number} types will
	 * 	     be coerced using {@link Number#intValue() intValue}. Strings
	 * 	     that can be coerced using {@link Double#valueOf(String)} will be,
	 * 	     and then cast to int.
	 * 	 <li>When the requested type is a long, other {@link Number} types will
	 * 	     be coerced using {@link Number#longValue() longValue}. Strings
	 * 	     that can be coerced using {@link Double#valueOf(String)} will be,
	 * 	     and then cast to long. This two-step conversion is lossy for very
	 * 	     large values. For example, the string "9223372036854775806" yields the
	 * 	     long 9223372036854775807.
	 * 	 <li>When the requested type is a String, other non-null values will be
	 * 	     coerced using {@link String#valueOf(Object)}. Although null cannot be
	 * 	     coerced, the sentinel value {@link JSONObject#NULL} is coerced to the
	 * 	     string "null".
	 * </ul>
	 *
	 * <p>This class can look up both mandatory and optional values:
	 * <ul>
	 * 	 <li>Use <code>get<i>Type</i>()</code> to retrieve a mandatory value. This
	 * 	     fails with a {@code JSONException} if the requested name has no value
	 * 	     or if the value cannot be coerced to the requested type.
	 * 	 <li>Use <code>opt<i>Type</i>()</code> to retrieve an optional value. This
	 * 	     returns a system- or user-supplied default if the requested name has no
	 * 	     value or if the value cannot be coerced to the requested type.
	 * </ul>
	 *
	 * <p><strong>Warning:</strong> this class represents null in two incompatible
	 * ways: the standard Java {@code null} reference, and the sentinel value {@link
	 * JSONObject#NULL}. In particular, calling {@code put(name, null)} removes the
	 * named entry from the object but {@code put(name, JSONObject.NULL)} stores an
	 * entry whose value is {@code JSONObject.NULL}.
	 *
	 * <p>Instances of this class are not thread safe. Although this class is
	 * nonfinal, it was not designed for inheritance and should not be subclassed.
	 * In particular, self-use by overridable methods is not specified. See
	 * <i>Effective Java</i> Item 17, "Design and Document or inheritance or else
	 * prohibit it" for further information.
	 */
	public class JSONObject extends Struct {
		
		private static final Double NEGATIVE_ZERO = -0d;
		
		/**
		 * A sentinel value used to explicitly define a name with no value. Unlike
		 * {@code null}, names with this value:
		 * <ul>
		 * 	 <li>show up in the {@link #keys} array
		 * 	 <li>show up in the {@link #keys} iterator
		 * 	 <li>return {@code true} for {@link #has(String)}
		 * 	 <li>do not throw on {@link #opt(String)}
		 * 	 <li>are included in the encoded JSON string.
		 * </ul>
		 *
		 * <p>This value violates the general contract of {@link Object#equals} by
		 * returning true when compared to {@code null}. Its {@link #toString}
		 * method returns "null".
		 */
		public static final Object NULL = new Object () {
			
			@Override
			public boolean equals (Object o) {
				return o == this || o == null; // API specifies this broken equals implementation
			}
			
			@Override
			public String toString () {
				return "null";
			}
			
		};
		
		/**
		 * Creates a {@code JSONObject} with no name/value mappings.
		 */
		public JSONObject () {
		
		}
		
		/**
		 * Creates a new {@code JSONObject} by copying all name/value mappings from
		 * the given map.
		 *
		 * @param copyFrom a map whose keys are of type {@link String} and whose
		 *                 values are of supported types.
		 *
		 * @throws NullPointerException if any of the map's keys are null.
		 */
		/* (accept a raw type for API compatibility) */
		public JSONObject (Map<?, ?> copyFrom) {
			
			this ();
			
			for (Map.Entry<?, ?> entry : copyFrom.entrySet ()) {
				
				/*
				 * Deviate from the original by checking that keys are non-null and
				 * of the proper type. (We still defer validating the values).
				 */
				String key = (String) entry.getKey ();
				
				if (key == null) {
					throw new NullPointerException ();
				}
				
				put (key, entry.getValue ());
				
			}
			
		}
		
		/**
		 * Creates a new {@code JSONObject} with name/value mappings from the next
		 * object in the tokener.
		 *
		 * @param readFrom a tokener whose nextValue() method will yield a
		 *                 {@code JSONObject}.
		 *
		 * @throws JSONException if the parse fails or doesn't yield a
		 *                       {@code JSONObject}.
		 */
		public JSONObject (JSONTokener readFrom) {
			/*
			 * Getting the parser to populate this could get tricky. Instead, just
			 * parse to temporary JSONObject and then steal the data from that.
			 */
			this (((JSONObject) readFrom.nextValue ()));
		}
		
		/**
		 * Creates a new {@code JSONObject} with name/value mappings from the JSON
		 * string.
		 *
		 * @param json a JSON-encoded string containing an
		 *
		 * @throws JSONException if the parse fails or doesn't yield a {@code JSONObject}.
		 */
		public JSONObject (String json) {
			this (new JSONTokener (json));
		}
		
		public JSONObject put (String key, String value) {
			
			if (value == null)
				assertNull (key, value);
			
			super.put (key, value);
			
			return this;
			
		}
		
		public JSONObject put (String key, Integer value) {
			
			if (value == null)
				assertNull (key, value);
			
			super.put (key, value);
			
			return this;
			
		}
		
		public JSONObject put (String key, Long value) {
			
			if (value == null)
				assertNull (key, value);
			
			super.put (key, value);
			
			return this;
			
		}
		
		public JSONObject put (String key, Float value) {
			
			if (value == null)
				assertNull (key, value);
			
			super.put (key, value);
			
			return this;
			
		}
		
		public JSONObject put (String key, Double value) {
			
			if (value == null)
				assertNull (key, value);
			
			super.put (key, value);
			
			return this;
			
		}
		
		public JSONObject put (String key, Boolean value) {
			
			if (value == null)
				assertNull (key, value);
			
			super.put (key, value);
			
			return this;
			
		}
		
		/**
		 * Returns the value mapped by {@code name}, or null if no such mapping exists.
		 */
		protected final Object opt (String key) {
			return get (key, defValues.get (key));
		}
		
		/**
		 * Creates a new {@code JSONObject} by copying mappings for the listed names from the given	Names that aren't present in {@code copyFrom} will be skipped.
		 */
		public JSONObject (JSONObject copyFrom, String[] names) {
			
			for (String name : names) {
				
				Object value = copyFrom.opt (name);
				
				if (value != null)
					put (name, value);
				
			}
			
		}
		
		public JSONObject putPurge (String name, JSONArray value) {
			
			JSONArray methods = new JSONArray ();
			
			for (int i = 0; i < value.length (); i++)
				methods.put (value.get (i));
			
			return put (name, methods);
			
		}
		
		public JSONObject set (String name, int value) {
			
			try {
				put (checkName (name), value);
			} catch (JSONException ignore) {
			}
			
			return this;
			
		}
		
		/**
		 * Maps {@code name} to {@code value}, clobbering any existing name/value
		 * mapping with the same name. If the value is {@code null}, any existing
		 * mapping for {@code name} is removed.
		 *
		 * @param value a {@link JSONObject}, {@link JSONArray}, String, Boolean,
		 *              Integer, Long, Double, {@link #NULL}, or {@code null}. May not be
		 *              {@link Double#isNaN() NaNs} or {@link Double#isInfinite()
		 *              infinities}.
		 *
		 * @return this
		 */
		public JSONObject put (String name, Object value) {
			if (value == null) {
				remove (name);
				return this;
			}
			if (value instanceof Number) {
				// deviate from the original by checking all Numbers, not just floats & doubles
				JSONArray.checkDouble (((Number) value).doubleValue ());
			}
			super.put (checkName (name), value);
			return this;
		}
		
		/**
		 * Equivalent to {@code put(name, value)} when both parameters are non-null;
		 * does nothing otherwise.
		 */
		public JSONObject putOpt (String name, Object value) {
			if (name == null || value == null) {
				return this;
			}
			return put (name, value);
		}
		
		/**
		 * Appends {@code value} to the array already mapped to {@code name}. If
		 * this object has no mapping for {@code name}, this inserts a new mapping.
		 * If the mapping exists but its value is not an array, the existing
		 * and new values are inserted in order into a new array which is itself
		 * mapped to {@code name}. In aggregate, this allows values to be added to a
		 * mapping one at a time.
		 *
		 * @param value a {@link JSONObject}, {@link JSONArray}, String, Boolean,
		 *              Integer, Long, Double, {@link #NULL} or null. May not be {@link
		 *              Double#isNaN() NaNs} or {@link Double#isInfinite() infinities}.
		 */
		public JSONObject accumulate (String name, Object value) {
			
			Object current = get (checkName (name));
			
			if (current == null)
				return put (name, value);
			
			// check in accumulate, since array.put(Object) doesn't do any checking
			if (value instanceof Number)
				JSONArray.checkDouble (((Number) value).doubleValue ());
			
			if (current instanceof JSONArray) {
				JSONArray array = (JSONArray) current;
				array.put (value);
			} else {
				JSONArray array = new JSONArray ();
				array.put (current);
				array.put (value);
				put (name, array);
			}
			return this;
		}
		
		String checkName (String name) {
			if (name == null) {
				throw new JSONException ("Names must be non-null");
			}
			return name;
		}
		
		/**
		 * Returns true if this object has no mapping for {@code name} or if it has
		 * a mapping whose value is {@link #NULL}.
		 */
		public boolean isNull (String name) {
			Object value = get (name);
			return value == null || value == NULL;
		}
		
		/**
		 * Returns true if this object has a mapping for {@code name}. The mapping
		 * may be {@link #NULL}.
		 */
		public boolean has (String name) { // TODO to containsKey
			return containsKey (name);
		}
		
		public boolean nonNull (String name) {
			
			Object result = optString (name);
			
			return (!result.equals ("null"));
			
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is a {@code JSONArray}.
		 *
		 * @throws JSONException if the mapping doesn't exist or is not a {@code JSONArray}.
		 */
		public JSONArray getJSONArray (String name) {
			
			Object object = get (name);
			
			if (object instanceof JSONArray)
				return (JSONArray) object;
			else
				throw typeMismatch (name, object, "JSONArray");
			
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is a {@code JSONArray}.
		 * Returns null otherwise.
		 */
		public JSONArray optJSONArray (String name) {
			Object object = opt (name);
			return object instanceof JSONArray ? (JSONArray) object : null;
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is a {@code JSONObject}.
		 *
		 * @throws JSONException if the mapping doesn't exist or is not a {@code JSONObject}.
		 */
		public JSONObject getJSONObject (String name) {
			
			Object object = get (name);
			
			if (object instanceof JSONObject)
				return (JSONObject) object;
			else
				throw typeMismatch (name, object, "JSONObject");
			
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is a {@code JSONObject}.
		 * Returns null otherwise.
		 */
		public JSONObject optJSONObject (String name) {
			Object object = opt (name);
			return object instanceof JSONObject ? (JSONObject) object : null;
		}
		
		/**
		 * Returns an array with the values corresponding to {@code names}. The array contains
		 * null for names that aren't mapped. This method returns null if {@code names} is either
		 * null or empty.
		 */
		public JSONArray toJSONArray (JSONArray names) {
			JSONArray result = new JSONArray ();
			if (names == null) {
				return null;
			}
			int length = names.length ();
			if (length == 0) {
				return null;
			}
			for (int i = 0; i < length; i++) {
				String name = Struct.toString (names.opt (i));
				result.put (opt (name));
			}
			return result;
		}
		
		@Override
		protected void assertNull (String key, Object value) {
			throw new JSONException ("Value with key " + key + " is null");
		}
		
		@Override
		protected RuntimeException typeMismatch (Object indexOrName, Object actual, String requiredType) {
			throw new JSONException (actual == null ? "Value at " + indexOrName + " is null." : "Value " + actual + " at " + indexOrName + " of type " + actual.getClass ().getName () + " cannot be converted to " + requiredType + ".");
		}
		
		/**
		 * Returns an iterator of the {@code String} names in this	The
		 * returned iterator supports {@link Iterator#remove() remove}, which will remove the
		 * corresponding mapping from this	If this object is modified after the iterator is
		 * returned, the iterator's behavior is undefined. The order of the keys is undefined.
		 */
		/* Return a raw type for API compatibility */
		/*public Iterator<?> getKeys () {
			return keySet ().iterator ();
		}*/
		
		/**
		 * Returns an array containing the string names in this	This method returns null if this
		 * object contains no mappings.
		 */
		public JSONArray keys () { // TODO
			return isEmpty () ? null : new JSONArray (new ArrayList<> (keySet ()));
		}
		
		/**
		 * Encodes this object as a compact JSON string, such as:
		 * <pre>{"query":"Pizza","locations":[94043,90210]}</pre>
		 */
		@Override
		public String toString () {
			
			try {
				
				JSONStringer stringer = new JSONStringer ();
				writeTo (stringer);
				
				return stringer.toString ();
				
			} catch (JSONException e) {
				return null;
			}
			
		}
		
		/**
		 * Encodes this object as a human readable JSON string for debugging, such
		 * as:
		 * <pre>
		 * {
		 *     "query": "Pizza",
		 *     "locations": [
		 * 		     94043,
		 * 		     90210
		 *     ]
		 * }</pre>
		 *
		 * @param indentSpaces the number of spaces to indent for each level of
		 *                     nesting.
		 */
		public String toString (int indentSpaces) {
			
			JSONStringer stringer = new JSONStringer (indentSpaces);
			writeTo (stringer);
			
			return stringer.toString ();
			
		}
		
		/**
		 * Encodes this object as a human readable JSON string for debugging, such
		 * as:
		 * <pre>
		 * {
		 *     "query": "Pizza",
		 *     "locations": [
		 * 		     94043,
		 * 		     90210
		 *     ]
		 * }</pre>
		 *
		 * @param indentSpaces the number of spaces to indent for each level of
		 *                     nesting.
		 */
		public String toString (boolean indentSpaces) {
			return toString (indentSpaces ? 2 : 0);
		}
		
		void writeTo (JSONStringer stringer) {
			
			stringer.object ();
			
			for (Map.Entry<String, Object> entry : entrySet ())
				stringer.key (entry.getKey ()).value (entry.getValue ());
			
			stringer.endObject ();
			
		}
		
		/**
		 * Encodes the number as a JSON string.
		 *
		 * @param number a finite value. May not be {@link Double#isNaN() NaNs} or
		 *               {@link Double#isInfinite() infinities}.
		 */
		public static String numberToString (Number number) {
			if (number == null) {
				throw new JSONException ("Number must be non-null");
			}
			
			double doubleValue = number.doubleValue ();
			JSONArray.checkDouble (doubleValue);
			
			// the original returns "-0" instead of "-0.0" for negative zero
			if (number.equals (NEGATIVE_ZERO)) {
				return "-0";
			}
			
			long longValue = number.longValue ();
			if (doubleValue == (double) longValue) {
				return Long.toString (longValue);
			}
			
			return number.toString ();
		}
		
		/**
		 * Encodes {@code data} as a JSON string. This applies quotes and any
		 * necessary character escaping.
		 *
		 * @param data the string to encode. Null will be interpreted as an empty
		 *             string.
		 */
		public static String quote (String data) {
			if (data == null) {
				return "\"\"";
			}
			try {
				JSONStringer stringer = new JSONStringer ();
				stringer.open (JSONStringer.Scope.NULL, "");
				stringer.value (data);
				stringer.close (JSONStringer.Scope.NULL, JSONStringer.Scope.NULL, "");
				return stringer.toString ();
			} catch (JSONException e) {
				throw new AssertionError ();
			}
		}
		
		public String implode (String sep1) {
			return implode (sep1, ": ");
		}
		
		public JSONObject extend (JSONObject prefs) {
			
			JSONObject output = new JSONObject ();
			
			for (String key : keySet ()) {
				
				if (!containsKey (key))
					output.put (key, prefs.get (key));
				else
					output.put (key, get (key));
				
			}
			
			return output;
			
		}
		
		public JSONObject extend (Map<?, ?> prefs) {
			
			for (Object key : prefs.keySet ()) {
				
				if (!containsKey (key.toString ()))
					put (key.toString (), prefs.get (key));
				
			}
			
			return this;
			
		}
		
		public String[] toStringArray (JSONObject defs) {
			
			JSONArray keys = defs.keys ();
			String[] strings = new String[keys.length ()];
			
			for (int i = 0; i < keys.length (); ++i)
				strings[i] = String.valueOf (get (keys.getString (i)));
			
			return strings;
			
		}
		
		public String[] toStringArray (JSONArray keys) {
			
			String[] strings = new String[keys.length ()];
			
			for (int i = 0; i < keys.length (); ++i)
				strings[i] = String.valueOf (get (keys.getString (i)));
			
			return strings;
			
		}
		
		public JSONArray toJSONArray () {
			return toJSONArray (new JSONArray ());
		}
		
		public JSONObject toJSONObject (String[] prefs) {
			
			JSONObject output = new JSONObject ();
			
			for (int i = 0; i < prefs.length; ++i)
				output.put (prefs[i], get (i));
			
			return output;
			
		}
		
		/*public Set<String> getKeys () {
			return keySet ();
		}*/
		
		/*public Collection<?> getValues () {
			return values ();
		}*/
		
		public JSONObject extend (JSONObject... arrays) {
			
			for (JSONObject array : arrays)
				for (String key : array.getKeys ())
					put (key, array.get (key));
			
			return this;
			
		}
		
		protected Strings str;
		
		public JSONObject (Strings str) {
			this.str = str;
		}
		
		public JSONObject explode (String sep1, String sep2) {
			return explode (sep1, sep2, new JSONObject ());
		}
		
		public JSONObject explode (String sep1, String sep2, JSONObject output) {
			
			for (String item : str.explode (sep1)) {
				
				String[] values = item.split (sep2);
				
				if (!item.equals (""))
					output.put (values[0].trim (), (values.length > 1 ? values[1] : ""));
				
			}
			
			return output;
			
		}
		
	}