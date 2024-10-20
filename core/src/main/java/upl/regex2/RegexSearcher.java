	package upl.regex2;
	
	import upl.regex2.automata.DFA;
	
	import java.util.Enumeration;
	
	/**
	 * Created on 5/25/15.
	 */
	public class RegexSearcher implements Enumeration<MatchedText> {
		
		protected int[][] transitionTable;
		protected int is;
		protected int rs;
		protected boolean[] fs;
		protected String str;
		
		protected int startPos;
		protected MatchedText text;
		
		public RegexSearcher (String regex) {
			compile (regex);
			str = null;
		}
		
		protected void compile (String regex) {
			
			DFA dfa = new DFA (regex);
			
			transitionTable = dfa.getTransitionTable ();
			
			is = dfa.getInitState ();
			fs = dfa.getFinalStates ();
			rs = dfa.getRejectedState ();
			
		}
		
		public RegexSearcher search (String str) {
			
			startPos = 0;
			text = null;
			this.str = str;
			
			return this;
			
		}
		
		@Override
		public boolean hasMoreElements () {
			while (startPos < str.length ()) {
				int s = is;
				for (int i = startPos; i < str.length (); i++) {
					char ch = str.charAt (i);
					s = transitionTable[s][ch];
					if (s == rs) {
						break;
					} else if (fs[s]) {
						text = new MatchedText (str.substring (startPos, i + 1), startPos);
						startPos = i + 1;
						return true;
					}
				}
				startPos++;
			}
			return false;
		}
		
		@Override
		public MatchedText nextElement () {
			return text;
		}
		
	}