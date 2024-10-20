	/*
	 * Licensed to the Apache Software Foundation (ASF) under one or more
	 * contributor license agreements.  See the NOTICE file distributed with
	 * this work for additional information regarding copyright ownership.
	 * The ASF licenses this file to You under the Apache License, Version 2.0
	 * (the "License"); you may not use this file except in compliance with
	 * the License.  You may obtain a copy of the License at
	 *
	 *     http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */
	
	package upl.hash;
	
	import java.util.Iterator;
	import java.util.SortedMap;
	import java.util.TreeMap;
	import upl.core.Hash;
	import upl.json.JSONObject;
	
	/**
	 * @author linjunjie1103@gmail.com
	 * <p>
	 * To hash Node objects to a hash ring with a certain amount of virtual node.
	 * Method routeNode will return a Node instance which the object key should be allocated to according to consistent hash algorithm
	 */
	public class ConsistentHash extends TreeMap<Long, ConsistentHash.VirtualNode> {
		
		protected Hash hashFunction;
		protected int vNodeCount;
		
		public ConsistentHash () {
			this (10);
		}
		
		/**
     * @param vNodeCount   amounts of virtual nodes
     */
		public ConsistentHash (int vNodeCount) {
			this.vNodeCount = vNodeCount;
		}
		
		/**
     * add physical node to the hash ring with some virtual nodes
     *
     * @param pNode      physical node needs added to hash ring
     */
		public void put (Node pNode) {
			
			int existingReplicas = getExistingReplicas (pNode);
			
			for (int i = 0; i < vNodeCount; i++) {
				
				VirtualNode vNode = new VirtualNode (pNode);
				
				vNode.replicaIndex = i + existingReplicas;
				
				put (getHash (vNode.getKey ()), vNode);
				
			}
			
		}
		
		/**
     * remove the physical node from the hash ring
     *
     * @param pNode      physical node needs to be removed from hash ring
     */
		@SuppressWarnings ("Java8CollectionRemoveIf")
		public void remove (Node pNode) {
			
			Iterator<Long> it = keySet ().iterator ();
			
			while (it.hasNext ())
				if (get (it.next ()).isVirtualNodeOf (pNode))
					it.remove ();
			
		}
		
		public ConsistentHash setHash (Hash hash) {
			
			hashFunction = hash;
			return this;
			
		}
		
		protected long getHash (String key) {
			
			if (hashFunction == null)
				hashFunction = new Hash ();
			
			return hashFunction.process (key).toLong ();
			
		}
		
		/**
     * with a specified key, route the nearest Node instance in the current hash ring
     *
     * @param objectKey the object key to find a nearest Node
     */
		public Node get (String objectKey) {
			
			if (!isEmpty ()) {
				
				SortedMap<Long, VirtualNode> tailMap = tailMap (getHash (objectKey));
				
				Long nodeHashVal = !tailMap.isEmpty () ? tailMap.firstKey () : firstKey ();
				
				return get (nodeHashVal).getPhysicalNode ();
				
			} else return null;
			
		}
		
		public int getExistingReplicas (Node pNode) {
			
			int replicas = 0;
			
			for (VirtualNode vNode : values ())
				if (vNode.isVirtualNodeOf (pNode))
					replicas++;
			
			return replicas;
			
		}
		
		public static abstract class Node extends JSONObject {
			
			public abstract String getKey ();
			
		}
		
		protected static class VirtualNode extends Node {
			
			protected int replicaIndex;
			
			protected Node physicalNode;
			
			protected VirtualNode (Node physicalNode) {
				this.physicalNode = physicalNode;
			}
			
			@Override
			public String getKey () {
				return physicalNode.getKey () + "-" + replicaIndex;
			}
			
			protected boolean isVirtualNodeOf (Node pNode) {
				return physicalNode.getKey ().equals (pNode.getKey ());
			}
			
			protected Node getPhysicalNode () {
				return physicalNode;
			}
			
		}
		
	}