	package upl.regex2;
	
	import upl.core.Log;
	import upl.regex2.automata.DFA;
	
	/**
	 * Created on 2015/5/11.
	 */
	public class RegexMatcher {
		
		protected int[][] transitionTable;
		protected int is;
		protected int rs;
		protected boolean[] fs;
		
		DFA dfa;
		String regex;
		
		public RegexMatcher (String regex) {
			
			this.regex = regex;
			
			dfa = new DFA (regex);
			
			transitionTable = dfa.getTransitionTable ();
			
			is = dfa.getInitState ();
			fs = dfa.getFinalStates ();
			rs = dfa.getRejectedState ();
			
		}
		
		public boolean match (String str) {
			
			int s = is;
			
			for (int i = 0; i < str.length (); i++) {
				
				char ch = str.charAt (i);
				
				s = transitionTable[s][ch];
				
				if (s == rs)
					return false; // fast failed using rejected state
				
			}
			
			return fs[s];
			
		}
		
		/**
		 * Checks if the matching space of <code>regexp1</code> contains the matching space of <code>regexp1</code>.
		 */
		public boolean contains (String regex) {
			
			// by definition if the matching space of A is equal than the matching space of A|B then B is contained in A similarly, if DFA(A) is identical to DFA(A|B) then B is contained in A.
			
			return dfa.isEquivalentTo (new DFA (this.regex + "|" + regex));
			
		}
		
	}