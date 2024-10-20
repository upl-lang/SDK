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
	
	import upl.map.KeysSortedHashMap;
	import upl.util.HashMap;
	import upl.util.Map;
	
	public abstract class Struct extends KeysSortedHashMap<String, Object> {
		
		protected Map<String, Object> defValues = new HashMap<> ();
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is an int or can be coerced
		 * to a String.
		 * Returns {@code fallback} otherwise.
		 */
		public final String getString (String key) {
			return getString (key, (String) defValues.get (key));
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is a boolean or can be coerced
		 * to a boolean.
		 * Returns {@code fallback} otherwise.
		 */
		public final Boolean getBoolean (String key) {
			return getBoolean (key, (Boolean) defValues.get (key));
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is an int or can be coerced
		 * to an int.
		 * Returns {@code fallback} otherwise.
		 */
		public final Integer getInt (String key) {
			return getInt (key, (Integer) defValues.get (key));
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is a long or can be coerced
		 * to a long.
		 * Returns {@code fallback} otherwise.
		 */
		public final Long getLong (String key) {
			return getLong (key, (Long) defValues.get (key));
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is an int or can be coerced
		 * to an int.
		 * Returns {@code fallback} otherwise.
		 */
		public final Float getFloat (String key) {
			return getFloat (key, (Float) defValues.get (key));
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is a double or can be coerced
		 * to a double.
		 * Returns {@code fallback} otherwise.
		 */
		public final Double getDouble (String key) {
			return getDouble (key, (Double) defValues.get (key));
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists, coercing it if necessary.
		 */
		public final String getString (String name, String defValue) {
			
			Object object = get (name, defValue);
			String result = toString (object);
			
			if (result == null)
				throw typeMismatch (name, object, "String");
			
			return result;
			
		}
		
		/**
		 * Returns the value mapped by {@code key} if it exists and is a boolean or can be
		 * coerced to a boolean.
		 */
		public final Boolean getBoolean (String name, Boolean defValue) {
			
			Object object = get (name, defValue);
			Boolean result = toBoolean (object);
			
			if (result == null)
				throw typeMismatch (name, object, "Boolean");
			
			return result;
			
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is an int or can be coerced
		 * to an int.
		 */
		public final Integer getInt (String name, Integer defValue) {
			
			Object object = get (name, defValue);
			Integer result = toInteger (object);
			
			if (result == null)
				throw typeMismatch (name, object, "Integer");
			
			return result;
			
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is a long or can be coerced
		 * to a long.
		 */
		public final Long getLong (String name, Long defValue) {
			
			Object object = get (name, defValue);
			Long result = toLong (object);
			
			if (result == null)
				throw typeMismatch (name, object, "Long");
			
			return result;
			
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is an int or can be coerced
		 * to an int.
		 */
		public final Float getFloat (String name, Float defValue) {
			
			Object object = get (name, defValue);
			Float result = toFloat (object);
			
			if (result == null)
				throw typeMismatch (name, object, "Float");
			
			return result;
			
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is a double or can be coerced
		 * to a double.
		 */
		public final Double getDouble (String name, Double defValue) {
			
			Object object = get (name, defValue);
			Double result = toDouble (object);
			
			if (result == null)
				throw typeMismatch (name, object, "Double");
			
			return result;
			
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is an int or can be coerced
		 * to a String.
		 * Returns {@code fallback} otherwise.
		 */
		public final String optString (String key) {
			return getString (key, "");
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is a boolean or can be coerced
		 * to a boolean.
		 * Returns {@code fallback} otherwise.
		 */
		public final Boolean optBoolean (String key) {
			return getBoolean (key, false);
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is an int or can be coerced
		 * to an int.
		 * Returns {@code fallback} otherwise.
		 */
		public final Integer optInt (String key) {
			return getInt (key, 0);
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is a long or can be coerced
		 * to a long.
		 * Returns {@code fallback} otherwise.
		 */
		public final Long optLong (String key) {
			return getLong (key, 0L);
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is an int or can be coerced
		 * to an int.
		 * Returns {@code fallback} otherwise.
		 */
		public final Float optFloat (String key) {
			return getFloat (key, 0F);
		}
		
		/**
		 * Returns the value mapped by {@code name} if it exists and is a double or can be coerced
		 * to a double.
		 * Returns {@code fallback} otherwise.
		 */
		public final Double optDouble (String key) {
			return getDouble (key, 0D);
		}
		
		public Struct setDefValue (String key, Object value) {
			
			defValues.add (key, value);
			
			return this;
			
		}
		
		public static String toString (Object value) {
			
			if (value instanceof String)
				return (String) value;
			else if (value != null)
				return value.toString ();
			
			return null;
			
		}
		
		public static Boolean toBoolean (Object value) {
			
			if (value instanceof Boolean)
				return (Boolean) value;
			else if (value.equals ("true"))
				return true;
			else if (value.equals ("false"))
				return false;
			
			return null;
			
		}
		
		public static Integer toInteger (Object value) {
			
			if (value instanceof Integer)
				return (Integer) value;
			else if (value instanceof Number)
				return ((Number) value).intValue ();
			else if (value instanceof String) {
				
				try {
					return Integer.parseInt ((String) value);
				} catch (NumberFormatException ignored) {
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
					return Long.parseLong ((String) value);
				} catch (NumberFormatException ignored) {
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
				} catch (NumberFormatException ignored) {
				}
				
			}
			
			return null;
			
		}
		
		public static Double toDouble (Object value) {
			
			if (value instanceof Double)
				return (Double) value;
			else if (value instanceof Number)
				return ((Number) value).doubleValue ();
			else if (value instanceof String) {
				
				try {
					return Double.parseDouble ((String) value);
				} catch (NumberFormatException ignored) {
				}
				
			}
			
			return null;
			
		}
		
		protected RuntimeException typeMismatch (Object indexOrName, Object actual, String requiredType) {
			throw new RuntimeException (actual == null ? "Value at " + indexOrName + " is null." : "Value " + actual + " at " + indexOrName + " of type " + actual.getClass ().getName () + " cannot be converted to " + requiredType + ".");
		}
		
	}