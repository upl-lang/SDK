	package upl.regex3.regex;
	
	import upl.regex3.automaton.DFA;
	import upl.regex3.automaton.DFAState;
	
	import java.util.ArrayList;
	import java.util.List;
	import java.util.regex.MatchResult;
	
	public class Matcher implements MatchResult {
		
		public Matcher (CharSequence chars, final DFA automaton) {
			
			this.chars = chars;
			this.automaton = automaton;
			
		}
		
		protected DFA automaton;
		protected CharSequence chars;
		protected List<MatchGroup> groups = new ArrayList<> ();
		
		protected int matchStart = -1;
		protected int matchEnd = -1;
		
		public boolean find () { // TODO:::DESPERATELY NEEDS REFACTORING!!!
			
			int begin;
			
			switch (getMatchStart ()) {
				
				case -2:
					return false;
					
				case -1:
					begin = 0;
					break;
					
				default: {
					
					begin = getMatchEnd ();
					// This occurs when a previous find() call matched the empty string. This can happen when the pattern is a* for example.
					if (begin == getMatchStart ()) {
						
						begin += 1;
						
						if (begin > getChars ().length ()) {
							
							setMatch (-2, -2);
							return false;
							
						}
						
					}
					
				}
				
			}
			
			int match_start;
			int match_end;
			if (automaton.getStartState ().isFinal ()) {
				match_start = begin;
				match_end = begin;
			} else {
				match_start = -1;
				match_end = -1;
			}
			int l = getChars ().length ();
			while (begin < l) {
				int index = 0;
				//System.out.println("START:");
				for (int i = begin; i < l; i++) {
					DFAState new_state = automaton.getStates ().get (index).getTransition (Character.toString (getChars ().charAt (i)));
					if (new_state == null) {
						break;
					} else if (new_state.isFinal ()) {
						// found a match from begin to (i+1)
						match_start = begin;
						match_end = (i + 1);
					}
					index = automaton.getStates ().indexOf (new_state);
				}
				if (match_start != -1) {
					setMatch (match_start, match_end);
					return true;
				}
				begin += 1;
			}
			if (match_start != -1) {
				setMatch (match_start, match_end);
				groups.add (new MatchGroup (match_start, match_end, group ()));
				return true;
			} else {
				setMatch (-2, -2);
				return false;
			}
			
		}
		
		public boolean findAll () {
			
			boolean result = false;
			
			while (find ()) {
				
				groups.add (new MatchGroup (matchStart, matchEnd, group ()));
				result = true;
				
			}
			
			return result;
			
		}
		
		protected void setMatch (final int matchStart, final int matchEnd) throws IllegalArgumentException {
			if (matchStart > matchEnd) {
				throw new IllegalArgumentException ("Start must be less than or equal to end: " + matchStart + ", " + matchEnd);
			}
			this.matchStart = matchStart;
			this.matchEnd = matchEnd;
		}
		
		protected int getMatchStart () {
			return matchStart;
		}
		
		protected int getMatchEnd () {
			return matchEnd;
		}
		
		protected CharSequence getChars () {
			return chars;
		}
		
		public int end () {
			matchGood ();
			return matchEnd;
		}
		
		public int end (final int group) {
			return groups.get (group).matchEnd;
		}
		
		public String group () {
			matchGood ();
			return chars.subSequence (matchStart, matchEnd).toString ();
		}
		
		public String group (final int group) {
			return groups.get (group).value;
		}
		
		public int groupCount () {
			return groups.size ();
		}
		
		public int start () {
			matchGood ();
			return matchStart;
		}
		
		public int start (int group) {
			return groups.get (group).matchStart;
		}
		
		public MatchResult toMatchResult () {
			final Matcher match = new Matcher (chars, automaton);
			match.matchStart = this.matchStart;
			match.matchEnd = this.matchEnd;
			return match;
		}
		
		protected void matchGood () throws IllegalStateException {
			if ((matchStart < 0) || (matchEnd < 0)) {
				throw new IllegalStateException ("There was no available match.");
			}
		}
		
	}
