	package upl.regex4;
	
	import java.util.*;
	
	public class State implements Comparable<State> {
		
		/*
		 * Status id
		 */
		Set<Integer> id = new TreeSet<> ();
		
		/*
		 * Indicate the final state
		 */
		boolean isEnd;
		
		/*
		 * Match priority
		 */
		int Priority = 0;
		
		/*
		 * Capture start state
		 */
		boolean isCapstart = false;
		
		/*
		 * Capture end state
		 */
		boolean isCapend = false;
		
		/*
		 * Capture string start
		 */
		boolean isStrs = false;
		
		/*
		 * Capture string end
		 */
		boolean isStre = false;
		
		@Override
		public String toString () {
			
			StringBuilder re = new StringBuilder ();
			
			for (int i : id)
				re.append (i).append (".");
			
			re = new StringBuilder (re.substring (0, re.length () - 1));
			
			if (isStrs) re.insert (0, 'A');
			if (isStre) re.insert (0, 'B');
			
			return re.toString ();
			
		}
		
		@Override
		public boolean equals (Object obj) {
			
			State state = (State) obj;
			return this.id.equals (state.id);
			
		}
		
		@Override
		public int hashCode () {
			
			int hash = 0, k = 1;
			
			for (int i : id) {
				
				hash += (k * i);
				k *= 10;
				
			}
			
			return hash;
			
		}
		
		@Override
		public int compareTo (State o) {
			return Integer.compare (o.Priority, this.Priority);
		}
		
	}