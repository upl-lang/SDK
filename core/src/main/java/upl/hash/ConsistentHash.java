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
  
  package upl.hash;
  
  import java.util.Iterator;
  import java.util.SortedMap;
  import java.util.TreeMap;
  import upl.cipher.exceptions.EncryptException;
  import upl.core.Hash;
  import upl.exceptions.EmptyException;
  
  public class ConsistentHash<T extends ConsistentHash.Node> {
    
    public int vNodeCount;
    
    protected final SortedMap<Long, VirtualNode<T>> ring = new TreeMap<> ();
    protected Hash hashFunction;
    
    public ConsistentHash (int vNodeCount) {
      this.vNodeCount = vNodeCount;
    }
    
    public ConsistentHash setHash (Hash hash) {
      
      hashFunction = hash;
      return this;
      
    }
    
    public Hash getHash () {
      
      if (hashFunction == null)
        hashFunction = new Hash ();
      
      return hashFunction;
      
    }
    
    public void addNode (T pNode) throws EncryptException {
      addNode (pNode, vNodeCount);
    }
    
    /**
     * Add physical node to the hash ring with virtual nodes
     *
     * @param pNode      Physical node which added to hash ring
     * @param vNodeCount The number of virtual node of the physical node. Should be greater or equals to 0
     */
    public void addNode (T pNode, int vNodeCount) throws EncryptException {
      
      if (vNodeCount > 0) {
        
        int existingReplicas = getExistingReplicas (pNode);
        
        for (int i = 0; i < vNodeCount; i++) {
          
          VirtualNode<T> vNode = new VirtualNode<> (pNode, i + existingReplicas);
          ring.put (getHash ().process (vNode.getKey ()).toLong (), vNode);
          
        }
        
      } else throw new IllegalArgumentException ("Illegal virtual node number: " + vNodeCount);
      
    }
    
    /**
     * Remove the physical node from the hash ring
     *
     * @param pNode Physical node
     */
    public void removeNode (T pNode) {
      
      Iterator<Long> it = ring.keySet ().iterator ();
      
      while (it.hasNext ())
        if (ring.get (it.next ()).isVirtualNodeOf (pNode))
          it.remove ();
      
    }
    
    /**
     * Route the nearest Node instance in the current hash ring with a specified key
     *
     * @param key The key to find the nearest Node
     * @return Node
     */
    public T routeNode (String key) throws EncryptException {
      
      if (!ring.isEmpty ()) {
        
        Long hashVal = getHash ().process (key).toLong ();
        
        SortedMap<Long, VirtualNode<T>> tailMap = ring.tailMap (hashVal);
        
        Long nodeHashVal = (!tailMap.isEmpty () ? tailMap.firstKey () : ring.firstKey ());
        
        T node = ring.get (nodeHashVal).physicalNode;
        
        node.hashVal = hashVal;
        node.nodeHashVal = nodeHashVal;
        
        return node;
        
      } else throw new EmptyException ("Ring is empty");
      
    }
    
    public int getExistingReplicas (T pNode) {
      
      int replicas = 0;
      
      for (VirtualNode<T> vNode : ring.values ())
        if (vNode.isVirtualNodeOf (pNode))
          replicas++;
      
      return replicas;
      
    }
    
    public static abstract class Node {
      
      public Long hashVal, nodeHashVal;
      
      public abstract String getKey ();
      
      @Override
      public String toString () {
        return getKey ();
      }
      
    }
    
    protected static class VirtualNode<T extends Node> extends Node {
      
      public T physicalNode;
      public int replicaIndex;
      
      public VirtualNode (T physicalNode, int replicaIndex) {
        
        this.physicalNode = physicalNode;
        this.replicaIndex = replicaIndex;
        
      }
      
      @Override
      public String getKey () {
        return physicalNode.getKey () + "-" + replicaIndex;
      }
      
      public boolean isVirtualNodeOf (Node pNode) {
        return physicalNode.getKey ().equals (pNode.getKey ());
      }
      
    }
    
  }