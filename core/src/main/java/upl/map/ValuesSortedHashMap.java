	/*
	 * Copyright (c) 2020 - 2024 UPL Foundation
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 *     http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */
	
	package upl.map;
	
	import java.util.Comparator;
	import java.util.LinkedList;
	import java.util.List;
	import java.util.Map;
	import upl.util.LinkedHashMap;
	
	public class ValuesSortedHashMap<K, V extends Comparable<V>> extends LinkedHashMap<K, V> {
		
		/**
     * Sorts current map by values in ascending order
     */
		public ValuesSortedHashMap<K, V> sort () {
			
			return sort (new Comparator<> () {
				
				@Override
				public int compare (Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
					return o1.getValue ().compareTo (o2.getValue ());
				}
				
			});
			
		}
		
		/**
     * Sorts current map by values in descending order
     */
		public ValuesSortedHashMap<K, V> rsort () {
			
			return sort (new Comparator<> () {
				
				@Override
				public int compare (Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
					return o2.getValue ().compareTo (o1.getValue ());
				}
				
			});
			
		}
		
		/**
     * Sorts current map by values with comparator
     */
		public ValuesSortedHashMap<K, V> sort (Comparator<Map.Entry<K, V>> comparator) {
			
			List<Map.Entry<K, V>> list = new LinkedList<> (entrySet ());
			
			list.sort (comparator);
			
			clear ();
			
			for (Map.Entry<K, V> entry : list)
				put (entry.getKey (), entry.getValue ());
			
			return this;
			
		}
		
		/**
     * Gets max value from map
     */
		public V max () {
			
			Map<K, V> map = rsort ();
			
			for (K key : map.keySet ())
				return map.get (key);
			
			return null;
			
		}
		
		/**
     * Gets min value from map
     */
		public V min () {
			
			Map<K, V> map = sort ();
			
			for (K key : map.keySet ())
				return map.get (key);
			
			return null;
			
		}
		
	}