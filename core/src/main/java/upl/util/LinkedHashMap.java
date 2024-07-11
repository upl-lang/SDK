  package upl.util;
  
  import java.util.Collection;
  import java.util.Set;
  import javax.annotation.NonNull;
  import org.jetbrains.annotations.NotNull;
  
  public class LinkedHashMap<K, V> extends Map<K, V> {
    
    private final java.util.Map<K, V> object = new java.util.LinkedHashMap<> ();
    
    @Override
    public int size () {
      return object.size ();
    }
    
    @Override
    public boolean isEmpty () {
      return object.isEmpty ();
    }
    
    @Override
    public boolean containsKey (Object key) {
      return object.containsKey (key);
    }
    
    @Override
    public boolean containsValue (Object value) {
      return object.containsValue (value);
    }
    
    @Override
    public V get (Object key) {
      return object.get (key);
    }
    
    @Override
    public V put (K key, V value) {
      return object.put (key, super.put (key, value));
    }
    
    @Override
    public V remove (Object key) {
      return object.remove (key);
    }
    
    @Override
    public void putAll (@NotNull java.util.Map<? extends K, ? extends V> m) {
      object.putAll (m);
    }
    
    @Override
    public void clear () {
      object.clear ();
    }
    
    @Override
    @NonNull
    public Set<K> keySet () {
      return object.keySet ();
    }
    
    @Override
    @NonNull
    public Collection<V> values () {
      return object.values ();
    }
    
    @Override
    @NonNull
    public Set<Map.Entry<K, V>> entrySet () {
      return object.entrySet ();
    }
    
    @Override
    public int hashCode () {
      return object.hashCode ();
    }
    
  }