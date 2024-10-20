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
	
	import java.util.Iterator;
	import java.util.SortedMap;
	import java.util.TreeMap;
	import upl.core.Hash;
	import upl.core.Log;
	import upl.json.JSONObject;
	
	/**
	 * Distributed Hash Table (DHT) implementation.
	 *
	 * Named as DistributedHashMap because of based on TreeMap instead of Hashtable and to avoid names confusion.
	 */
	public class DistributedHashMap extends TreeMap<Long, DistributedHashMap.Node> {
		
		protected int replicationFactor;
		protected Hash hashFunction;
		
		public DistributedHashMap () {
			this (1);
		}
		
		public DistributedHashMap (int replicationFactor) {
			this.replicationFactor = replicationFactor;
		}
		
		public DistributedHashMap setHash (Hash hash) {
			
			hashFunction = hash;
			return this;
			
		}
		
		public void put (Node node) {
			
			for (int i = 1; i <= replicationFactor; i++) {
				
				node.replicaId = i;
				
				put (getHash (node.getKey () + "-" + node.replicaId), node);
				
			}
			
		}
		
		protected long getHash (String key) {
			
			if (hashFunction == null)
				hashFunction = new Hash ();
			
			return hashFunction.process (key).toLong ();
			
		}
		
		public Node get (Node node) {
			return get (node.getKey () + "-" + node.replicaId);
		}
		
		public Node get (String key) {
			
			long hash = getHash (key);
			
			if (isEmpty ()) return null;
			
			if (!containsKey (hash)) {
				
				SortedMap<Long, Node> tailMap = tailMap (hash);
				hash = tailMap.isEmpty () ? firstKey () : tailMap.firstKey ();
				
			}
			
			return get (hash);
			
		}
		
		/**
     * Remove the physical node from the hash ring
     *
     * @param pNode Physical node
     */
		public void remove (Node pNode) {
			
			Iterator<Long> it = keySet ().iterator ();
			
			while (it.hasNext ())
				if (get (it.next ()).equals (pNode))
					it.remove ();
			
		}
		
		public static abstract class Node extends JSONObject {
			
			protected int replicaId;
			
			public abstract String getKey ();
			
		}
		
	}