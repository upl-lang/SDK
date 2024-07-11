  package upl.util;
  
  import java.util.Set;
  
  /**
   * The type Map.
   *
   * @param <K> the type parameter
   * @param <V> the type parameter
   */
  public abstract class Map<K, V> implements java.util.Map<K, V> {
    
    /**
     * The Required.
     */
    protected List<String> required;
    /**
     * The Values.
     */
    protected java.util.Map<String, List<String>> values;
    
    /**
     * Instantiates a new Map.
     */
    public Map () {
      
      required = getRequiredPairs ();
      values = getRequiredValues ();
      
    }
    
    /**
     * Instantiates a new Map.
     *
     * @param data the data
     */
    public Map (java.util.Map<K, V> data) {
      
      super ();
      
      for (K key : getPairs ().keySet ())
        add (key, get (key));
      
      for (K key : data.keySet ())
        add (key, data.get (key));
      
    }
    
    /**
     * Length int.
     *
     * @return the int
     */
    public int length () {
      return size ();
    }
    
    /**
     * Gets pairs.
     *
     * @return the pairs
     */
    protected Map<K, V> getPairs () {
      return new HashMap<> ();
    }
    
    /**
     * Gets required pairs.
     *
     * @return the required pairs
     */
    protected List<String> getRequiredPairs () {
      return new ArrayList<> ();
    }
    
    /**
     * Gets required values.
     *
     * @return the required values
     */
    protected java.util.Map<String, List<String>> getRequiredValues () {
      return new java.util.HashMap<> ();
    }
    
    /**
     * Set map.
     *
     * @param key   the key
     * @param value the value
     * @return the map
     */
    
    @Override
    public V put (K key, V value) {
      
      List<String> values = this.values.get (key);
      
      if (this.values.containsKey (key) && !values.contains (value))
        throw new IllegalArgumentException (key + " value must be " + values.implode (", "));
      else if (value == null)
        value = (V) "null";
      
      return value;
      
    }
    
    @Deprecated
    public V add (K key, V value) {
      return put (key, value);
    }
    
    /**
     * Get object.
     *
     * @param key    the key
     * @param defVal the def val
     * @return the object
     */
    protected V get (K key, V defVal) {
      
      V value = get (key);
      if (value == null) value = defVal;
      
      return value;
      
    }
    
    /**
     * Validate map.
     *
     * @return the map
     */
    public Map<K, V> validate () {
      
      List<?> missed = diff (keySet (), required);
      
      if (missed.size () > 0)
        throw new IllegalArgumentException ("Required keys missed: " + missed.implode (", "));
      
      return this;
      
    }
    
    /**
     * Diff list.
     *
     * @param where    the where
     * @param required the required
     * @return the list
     */
    protected List<?> diff (Set<K> where, List<?> required) {
      
      required.removeAll (where);
      return required;
      
    }
    
    public boolean isEmpty () {
      return (size () == 0);
    }
    
    /**
     * Gets keys.
     *
     * @return the keys
     */
    public Set<K> getKeys () {
      return keySet ();
    }
    
    public String implode () {
      return implode ("\n");
    }
    
    public String implode (String sep1) {
      return implode (sep1, ": ");
    }
    
    public String implode (String sep1, String sep2) {
      
      StringBuilder output = new StringBuilder ();
      
      int i = 0;
      
      for (K key : getKeys ()) {
        
        if (i > 0) output.append (sep1);
        
        output.append (key);
        output.append (sep2);
        output.append (get (key));
        
        i++;
        
      }
      
      return output.toString ();
      
    }
    
    public K getKey (int key) {
      
      int i = 0;
      
      for (K key2 : keySet ()) {
        
        if (i == key) return key2;
        ++i;
        
      }
      
      return null;
      
    }
    
    public K getKey (V value) {
      
      for (K key2 : keySet ()) {
        
        V value2 = get (key2);
        if (value2.equals (value)) return key2;
        
      }
      
      return null;
      
    }
    
    @Override
    public String toString () {
      return implode ();
    }
    
  }