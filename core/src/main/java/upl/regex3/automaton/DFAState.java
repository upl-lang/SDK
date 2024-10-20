	package upl.regex3.automaton;
	
	import java.util.HashMap;
	import java.util.HashSet;
	import java.util.Map;
	import java.util.Set;
	
	public class DFAState {
		
		protected long id;
		protected static int nextID = 0;
		protected String nickname = null;
		protected Set<Integer> name = null;
		protected Map<String, DFAState> transitions = new HashMap<> ();
		protected Set<DFAState> includedStates = new HashSet<> ();
		protected boolean isStartState = false;
		protected boolean isFinalState = false;
		protected boolean isTrap = false;
		protected boolean isGood = false;
		protected boolean marked = false;
		
		public DFAState (Set<Integer> name) {
			this.name = name;
		}
		
		public DFAState (String nickname, boolean isInitial, boolean isFinal, boolean isTrap, boolean isGood, Set<DFAState> includedStates) {
			this (nickname);
			this.isStartState = isInitial;
			this.isFinalState = isFinal;
			this.isTrap = isTrap;
			this.isGood = isGood;
			this.includedStates.addAll (includedStates);
		}
		
		public DFAState (String nickname) {
			this.nickname = nickname;
			this.id = getNextID ();
		}
		
		public Set<DFAState> getIncludedStates () {
			return includedStates;
		}
		
		public Set<Integer> getName () {
			return name;
		}
		
		public void addTransition (DFAState dState, String sym) {
			transitions.put (sym, dState);
		}
		
		public long getId () {
			return id;
		}
		
		public void setTrap (boolean isTrap) {
			this.isTrap = isTrap;
		}
		
		public void setId (int newId) {
			this.id = newId;
		}
		
		public static void resetIds () {
			nextID = 0;
		}
		
		public Set<DFAState> asSet () {
			Set<DFAState> result = new HashSet<> ();
			result.add (this);
			return result;
		}
		
		protected static int getNextID () {
			return nextID++;
		}
		
		public Map<String, DFAState> getTransitions () {
			return transitions;
		}
		
		public void addIncludedState (DFAState s) {
			includedStates.add (s);
		}
		
		public boolean isStart () {
			return isStartState;
		}
		
		public void reverseState () {
			isFinalState = !isFinalState;
			if (isTrap) {
				isTrap = false;
				isGood = true;
			} else if (isGood) {
				isTrap = true;
				isGood = false;
			}
		}
		
		public boolean isFinal () {
			return isFinalState;
		}
		
		public boolean isGood () {
			return isGood;
		}
		
		public boolean isMarked () {
			return marked;
		}
		
		public void setMarked (boolean marked) {
			this.marked = marked;
		}
		
		public void setGood () {
			isGood = true;
		}
		
		public void setGood (boolean isGood) {
			this.isGood = isGood;
		}
		
		public void setStart () {
			isStartState = true;
		}
		
		public void setFinal () {
			isFinalState = true;
		}
		
		public void setTrap () {
			isTrap = true;
		}
		
		public boolean isTrap () {
			return isTrap;
		}
		
		public void setNickname (String nickname) {
			this.nickname = nickname;
		}
		
		public boolean equals (Object o) {
			if (o == this) return true;
			else if (!(o instanceof DFAState)) return false;
			else {
				DFAState temp = (DFAState) o;
				if (this.name != null && temp.name != null) {
					return this.name.equals (temp.getName ());
				} else {
					return this.nickname != null && this.nickname.equals (temp.nickname);
				}
			}
		}
		
		public DFAState getTransition (String sym) {
			if (!transitions.containsKey (sym)) return null;
			return transitions.get (sym);
		}
		
		public boolean existsTransitions (String x) {
			return transitions.containsKey (x);
		}
		
		public static DFAState mergePair (StatePair states) {
			StringBuilder sb = new StringBuilder ();
			boolean newIsStart = false;
			boolean newIsFinal = false;
			boolean newIsTrap = false;
			boolean newIsGood = false;
			Set<DFAState> includedStates = new HashSet<> ();
			if (states.s1.isStart () && states.s2.isStart ()) {
				newIsStart = true;
			}
			if (states.s1.isFinal () && states.s2.isFinal ()) {
				newIsFinal = true;
			}
			if (states.s1.isTrap () || states.s2.isTrap ()) {
				newIsTrap = true;
			}
			if (states.s1.isGood () && states.s2.isGood ()) {
				newIsGood = true;
			}
			sb.append (states.s1);
			includedStates.add (states.s1);
			sb.append (states.s2);
			includedStates.add (states.s2);
			DFAState newState;
			newState = new DFAState (sb.toString (), newIsStart, newIsFinal, newIsTrap, newIsGood, includedStates);
			System.out.println (newState);
			return newState;
		}
		
		public static DFAState mergeStates (Set<DFAState> states) {
			StringBuilder sb = new StringBuilder ();
			boolean newIsStart = false;
			boolean newIsFinal = false;
			boolean newIsTrap = false;
			boolean newIsGood = false;
			Set<DFAState> includedStates = new HashSet<> ();
			for (DFAState state : states) {
				if (state.isStart ())
					newIsStart = true;
				if (state.isFinal ())
					newIsFinal = true;
				if (state.isTrap ())
					newIsTrap = true;
				if (state.isGood ())
					newIsGood = true;
				sb.append (state);
				includedStates.add (state);
			}
			
			DFAState newState;
			if (includedStates.size () > 0)
				newState = new DFAState (sb.toString (), newIsStart, newIsFinal, newIsTrap, newIsGood, includedStates);
			else
				newState = new DFAState (sb.toString ());
			System.out.println (newState);
			return newState;
		}
		
		@Override
		public String toString () {
			StringBuilder sb = new StringBuilder ();
			if (this.isStart ()) {
				sb.append ("->");
			} else if (this.isFinal ()) {
				sb.append ("F_");
			}
			sb.append ("Q").append (this.getId ()).append ("(").append (this.getName ()).append ("):");
			sb.append ("Nick: ").append (nickname);
			return sb.toString ();
		}
		
		public void print () {
			StringBuilder sb = new StringBuilder ();
			if (this.isStart ()) {
				sb.append ("->");
			} else if (this.isFinal ()) {
				sb.append ("F_");
			}
			sb.append ("Q").append (this.getId ()).append ("(").append (this.getName ()).append ("):");
			sb.append ("Nick: ").append (nickname);
		}
		
		@Override
		public int hashCode () {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (id ^ (id >>> 32));
			return result;
		}
		
	}
