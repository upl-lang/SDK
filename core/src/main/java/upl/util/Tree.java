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
  
  package upl.util;
  
  import java.util.Collection;
  
  public class Tree<T> {
    
    protected T head;
    
    protected List<Tree<T>> leafs = new ArrayList<> ();
    
    protected Tree<T> parent = null;
    
    protected Map<T, Tree<T>> locate = new HashMap<> ();
    
    public Tree (T head) {
      
      this.head = head;
      locate.put (head, this);
      
    }
    
    public void addLeaf (T root, T leaf) {
      
      if (locate.containsKey (root))
        locate.get (root).addLeaf (leaf);
      else
        addLeaf (root).addLeaf (leaf);
      
    }
    
    public Tree<T> addLeaf (T leaf) {
      
      Tree<T> tree = new Tree<> (leaf);
      
      leafs.add (tree);
      
      tree.parent = this;
      tree.locate = this.locate;
      
      locate.put (leaf, tree);
      
      return tree;
      
    }
    
    public Tree<T> setAsParent (T parentRoot) {
      
      Tree<T> tree = new Tree<> (parentRoot);
      
      tree.leafs.add (this);
      
      this.parent = tree;
      
      tree.locate = this.locate;
      tree.locate.put (head, this);
      tree.locate.put (parentRoot, tree);
      
      return tree;
      
    }
    
    public T getHead () {
      return head;
    }
    
    public Tree<T> getTree (T element) {
      return locate.get (element);
    }
    
    public Tree<T> getParent () {
      return parent;
    }
    
    public Collection<T> getSuccessors (T root) {
      
      Collection<T> successors = new ArrayList<> ();
      
      Tree<T> tree = getTree (root);
      
      if (null != tree)
        for (Tree<T> leaf : tree.leafs)
          successors.add (leaf.head);
      
      return successors;
      
    }
    
    public Collection<Tree<T>> getSubTrees () {
      return leafs;
    }
    
    public static <T> Collection<T> getSuccessors (T of, Collection<Tree<T>> in) {
      
      for (Tree<T> tree : in)
        if (tree.locate.containsKey (of))
          return tree.getSuccessors (of);
      
      return new ArrayList<> ();
      
    }
    
    @Override
    public String toString () {
      return printTree (0);
    }
    
    public int indent = 2;
    
    protected String printTree (int increment) {
      
      StringBuilder s = new StringBuilder (" ".repeat (Math.max (0, increment)) + head);
      
      for (Tree<T> child : leafs)
        s.append ("\n").append (child.printTree (increment + indent));
      
      return s.toString ();
      
    }
    
  }