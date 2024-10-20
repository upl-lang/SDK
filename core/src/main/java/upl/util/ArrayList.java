	package upl.util;
	
	import java.util.Collection;
	import java.util.Iterator;
	import java.util.ListIterator;
	import javax.annotation.NonNull;
	
	public class ArrayList<V> extends List<V> {
		
		protected java.util.List<V> object = new java.util.ArrayList<> ();
		
		public ArrayList () {
			super ();
		}
		
		public ArrayList (int length) {
			object = new java.util.ArrayList<> (length);
		}
		
		@Override
		public int size () {
			return object.size ();
		}
		
		@Override
		public boolean isEmpty () {
			return object.isEmpty ();
		}
		
		@Override
		public boolean contains (Object o) {
			return object.contains (o);
		}
		
		@Override
		@NonNull
		public Iterator<V> iterator () {
			return object.iterator ();
		}
		
		@Override
		@NonNull
		public Object[] toArray () {
			return object.toArray ();
		}
		
		@Override
		@NonNull
		public <T> T[] toArray (@NonNull T[] a) {
			return object.toArray (a);
		}
		
		@Override
		public boolean add (V v) {
			return object.add (v);
		}
		
		public List<V> put (V v) {
			
			object.add (v);
			return this;
			
		}
		
		@Override
		public boolean remove (Object o) {
			return object.remove (o);
		}
		
		@Override
		public boolean containsAll (@NonNull Collection<?> c) {
			return object.containsAll (c);
		}
		
		@Override
		public boolean addAll (@NonNull Collection<? extends V> c) {
			return object.addAll (c);
		}
		
		@Override
		public boolean addAll (int index, Collection<? extends V> c) {
			return object.addAll (c);
		}
		
		@Override
		public boolean removeAll (@NonNull Collection<?> c) {
			return object.removeAll (c);
		}
		
		@Override
		public boolean retainAll (@NonNull Collection<?> c) {
			return object.retainAll (c);
		}
		
		@Override
		public void clear () {
			object.clear ();
		}
		
		@Override
		public V get (int index) {
			return object.get (index);
		}
		
		@Override
		public V set (int index, V element) {
			return object.set (index, element);
		}
		
		public void put (int index, V element) {
			object.add (index, element);
		}
		
		@Override
		@Deprecated
		public void add (int index, V element) {
			put (index, element);
		}
		
		@Override
		public V remove (int index) {
			return object.remove (index);
		}
		
		@Override
		public int indexOf (Object o) {
			return object.indexOf (o);
		}
		
		@Override
		public int lastIndexOf (Object o) {
			return object.lastIndexOf (o);
		}
		
		@Override
		@NonNull
		public ListIterator<V> listIterator () {
			return object.listIterator ();
		}
		
		@Override
		@NonNull
		public ListIterator<V> listIterator (int index) {
			return object.listIterator (index);
		}
		
		@Override
		@NonNull
		public java.util.List<V> subList (int fromIndex, int toIndex) {
			return object.subList (fromIndex, toIndex);
		}
		
	}