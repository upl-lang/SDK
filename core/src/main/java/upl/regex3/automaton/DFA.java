	package upl.regex3.automaton;
	
	import upl.regex3.lang.OperatorsSet;
	import upl.regex3.lang.SymbolSet;
	import upl.regex3.regex.RegExp;
	import upl.regex3.syntaxtree.LeafNode;
	import upl.regex3.syntaxtree.SyntaxTree;
	
	import java.io.IOException;
	import java.io.PrintWriter;
	import java.nio.charset.StandardCharsets;
	import java.util.*;
	
	public class DFA {
		
		protected TransitionTable lambda = new TransitionTable ();
		protected List<DFAState> listStates = new ArrayList<> ();
		protected Set<String> alphabet = new HashSet<> ();
		protected SymbolSet symbolSet = new SymbolSet ();
		protected SyntaxTree sTree = null;
		protected RegExp regex = null;
		protected OperatorsSet opSet = new OperatorsSet ();
		
		public DFA (RegExp regex, SyntaxTree sTree) {
			
			this.regex = regex;
			this.sTree = sTree;
			
			generateAlphabet (regex.getInitialValue ());
			generateTransitions ();
			generateDFA ();
			
		}
		
		public DFA (RegExp regex) {
			
			this.regex = regex;
			sTree = new SyntaxTree (regex);
			
			this.generateAlphabet (regex.getInitialValue ());
			this.generateTransitions ();
			this.generateDFA ();
			
		}
		
		public DFA (String rString) {
			regex = new RegExp (rString);
			sTree = new SyntaxTree (regex);
			
			this.generateAlphabet (regex.getInitialValue ());
			this.generateTransitions ();
			this.generateDFA ();
		}
		
		public DFA () {
		}
		
		protected void generateAlphabet (String str) {
			// Flag which is true when there is something like: \( or \* or etc
			boolean isEscaped = false;
			for (int i = 0; i < str.length (); i++) {
				if (str.charAt (i) == '\\' && !isEscaped) {
					isEscaped = true;
					continue;
				}
				if (isSymbol (str.charAt (i)) || isEscaped) {
					if (isEscaped) {
						//create a node with "\{symbol}" symbol
						alphabet.add ("\\" + str.charAt (i));
					} else {
						alphabet.add (Character.toString (str.charAt (i)));
					}
					isEscaped = false;
				} else if (str.charAt (i) == '[') {
					i++;
					char tem = str.charAt (i);
					i = i + 2;
					char tem2 = str.charAt (i);
					while (tem <= tem2) {
						alphabet.add (Character.toString (tem));
						tem++;
					}
				}
			}
		}
		
		protected void generateTransitions () {
			DFAState start = new DFAState (sTree.getRoot ().getFirstPos ());
			start.setStart ();
			listStates.add (start);
			int i = 0;
			int size = listStates.size ();
			listStates.get (i).setId (i);
			while (i < size) {
				for (String sym : alphabet) {
					Set<Integer> temp = this.nextStateName (listStates.get (i).getName (), sym);
					DFAState ds = new DFAState (temp);
					if (!listStates.contains (ds)) {
						listStates.add (ds);
						listStates.get (listStates.size () - 1).setId (listStates.size () - 1);
						listStates.get (i).addTransition (ds, sym);
						Transition t = new Transition (listStates.get (i), sym, ds.asSet ());
						lambda.addTransition (t);
					} else {
						int index = listStates.indexOf (ds);
						ds.setId (index);
						listStates.get (i).addTransition (listStates.get (index), sym);
						Transition t = new Transition (listStates.get (i), sym, listStates.get (index).asSet ());
						lambda.addTransition (t);
					}
				}
				i++;
				size = listStates.size ();
			}
		}
		
		protected boolean isSymbol (char ch) {
			return symbolSet.contains (ch) && !opSet.contains (ch);
		}
		
		protected Set<Integer> nextStateName (Set<Integer> cur, String sym) {
			Set<Integer> temp = new HashSet<> ();
			for (int i : cur) {
				if (Objects.requireNonNull (this.getLeaf (i)).getSymbol ().equals (sym))
					temp.addAll (Objects.requireNonNull (this.getLeaf (i)).getFollowPos ());
			}
			return temp;
		}
		
		protected LeafNode getLeaf (int id) {
			for (LeafNode temp : sTree.getLeaves ()) {
				if (temp.getId () == id) return temp;
			}
			return null;
		}
		
		public void addToAlphabet (Collection<String> alpha) {
			alphabet.addAll (alpha);
		}
		
		public void addState (DFAState state) {
			this.getStates ().add (state);
		}
		
		public void printTable () {
			System.out.println ("---------------DFA TRANSITIONS TABLE-------------");
			for (DFAState ds : listStates) {
				System.out.println ("| ");
				if (ds.isStart ()) {
					System.out.print ("->");
				} else if (ds.isFinal ()) {
					System.out.print ("F_");
				}
				System.out.print ("Q" + ds.getId () + "(" + ds.getName () + "):\r\n");
				for (String sym : alphabet) {
					if (ds.existsTransitions (sym)) {
						System.out.println ("D(Q" + ds.getId () + "," + sym + ")= " + "Q" + ds.getTransition (sym).getId () + "(" + ds.getTransition (sym).getName () + ")");
					}
				}
			}
			System.out.println ("-----------------------------------------------");
		}
		
		public Map<Integer, StringBuilder> getGroups (String str) {
			Map<Integer, StringBuilder> result = new HashMap<> ();
			for (Integer groupId : sTree.getTmpGroups ().keySet ()) {
				result.put (groupId, new StringBuilder ());
			}
			int index = 0;
			for (int i = 0; i < str.length (); i++) {
				DFAState start = listStates.get (index);
				//System.out.println("CHAR: " + str.charAt(i));
				for (Integer groupId : sTree.getTmpGroups ().keySet ()) {
					Set<Integer> names = new HashSet<> ();
					for (LeafNode leaf : sTree.getLeaves ()) {
						if (sTree.getTmpGroups ().get (groupId).contains (leaf.getId ()) && start.getName ().contains (leaf.getId ()) && leaf.getSymbol ().equals (Character.toString (str.charAt (i)))) {
							names.add (leaf.getId ());
						}
					}
					//System.out.println("GROUP: " + groupId);
					//System.out.println(names);
					if (!names.isEmpty ()) {
						result.get (groupId).append (str.charAt (i));
					}
				}
				DFAState transition = start.getTransition (Character.toString (str.charAt (i)));
				index = listStates.indexOf (transition);
				//System.out.println("Q" + listStates.get(tmpIndex).getId() + "," + str.charAt(i) + " -> Q" + listStates.get(index).getId());
				if (i == str.length () - 1 && listStates.get (index).isFinal ()) {
					System.out.println ("STRING ACCEPTED");
				}
			}
			System.out.println ("STRING REJECTED");
			return result;
		}
		
		public void isValidString (String str) {
			int index = 0, i, tmpIndex;
			for (i = 0; i < str.length (); i++) {
				DFAState start = listStates.get (index);
				DFAState transition = start.getTransition (Character.toString (str.charAt (i)));
				tmpIndex = index;
				index = listStates.indexOf (transition);
				System.out.println ("Q" + listStates.get (tmpIndex).getId () + "," + str.charAt (i) + " -> Q" + listStates.get (index).getId ());
				if (i == str.length () - 1 && listStates.get (index).isFinal ()) {
					System.out.println ("STRING ACCEPTED");
					return;
				}
			}
			
		}
		
		public List<DFAState> getStates () {
			return listStates;
		}
		
		public boolean isEmpty () {
			return alphabet.isEmpty ();
		}
		
		public Set<String> getAlphabet () {
			return alphabet;
		}
		
		protected void generateDFA () {
			int last = sTree.getLeaves ().size ();
			for (DFAState ds : listStates) {
				if (ds.getName ().contains (last)) ds.setFinal ();
				int i = 0;
				for (String sym : alphabet) {
					if (ds.getTransition (sym).equals (ds)) i++;
				}
				if ((i == alphabet.size ()) && ds.isFinal ()) ds.setGood ();
				if ((i == alphabet.size ()) && !ds.isFinal ()) ds.setTrap ();
			}
		}
		
		public void writeLangToFile () throws IOException {
			StringBuilder sb = new StringBuilder ();
			Set<DFAState> finalStates = getFinalStates ();
			DFAState startState = getStartState ();
			sb.append ("S=").append ("Q").append (startState.getId ()).append ("\n");
			sb.append ("A=");
			for (DFAState finalState : finalStates) {
				sb.append ("Q").append (finalState.getId ()).append (",");
			}
			sb.deleteCharAt (sb.length () - 1);
			sb.append ("\n");
			sb.append ("E=");
			for (String sym : getAlphabet ()) {
				sb.append (sym).append (",");
			}
			sb.deleteCharAt (sb.length () - 1);
			sb.append ("\n");
			sb.append ("Q=");
			for (DFAState ds : listStates) {
				sb.append ("Q").append (ds.getId ()).append (",");
			}
			sb.deleteCharAt (sb.length () - 1);
			sb.append ("\n");
			for (DFAState ds : listStates) {
				for (String sym : alphabet) {
					sb.append ("Q").append (ds.getId ());
					if (ds.existsTransitions (sym)) {
						sb.append (",").append (sym).append ("=");
						sb.append ("Q").append (ds.getTransition (sym).getId ());
						sb.append ("\n");
					}
				}
			}
			PrintWriter writer = new PrintWriter ("DFA.txt", StandardCharsets.UTF_8);
			writer.print (sb);
			writer.close ();
		}
		
		public DFA minimize () {
			System.out.println ("[*] Building groups\n");
			
			List<Set<DFAState>> groups = buildGroups ();
			//System.out.println(groups);
			
			System.out.println ("[*] Groups built\n");
			System.out.println ("[*] Building DFA_min\n");
			DFA dfaMin = new DFA ();
			dfaMin.addToAlphabet (this.alphabet);
			
			System.out.println ("[*] Creating S_min\n");
			for (Set<DFAState> group : groups) {
				dfaMin.addState (DFAState.mergeStates (group));
			}
			
			System.out.println ("[*] Creating δ_min\n");
			dfaMin.lambda = createLambda (dfaMin.getStates ());
			System.out.println ("DEBUG");
			System.out.println (dfaMin.getStates ().size ());
			
			//System.out.println(dfaMin.lambda);
			//System.out.println(dfaMin.listStates);
			
			System.out.println ("[*] Renaming States\n");
			dfaMin.renameStates ();
			System.out.println (dfaMin.getStates ().size ());
			
			System.out.println ("[*] Done\n");
			dfaMin.sTree = sTree;
			dfaMin.regex = regex;
			return dfaMin;
		}
		
		public DFA getComplement (Set<String> universum) {
			if (!universum.containsAll (this.alphabet)) {
				System.out.println ("---Input Alphabet has been expanded with Automata's alphabet---");
				universum.addAll (this.alphabet);
				System.out.println ("Current alphabet: " + universum);
			}
			DFA newDFA = new DFA (regex, sTree).minimize ();
			
			newDFA.totalize (universum);
			for (DFAState p : newDFA.getStates ()) {
				p.reverseState ();
			}
			//System.out.println(newDFA.getStates());
			DFAState.resetIds ();
			newDFA.addToAlphabet (universum);
			return newDFA;
		}
		
		public DFA getTotal (Set<String> universum) {
			if (universum.equals (this.alphabet))
				return this;
			if (!universum.containsAll (this.alphabet)) {
				System.out.println ("---Input Alphabet has been expanded with Automata's alphabet---");
				universum.addAll (this.alphabet);
				System.out.println ("Current alphabet: " + universum);
			}
			DFA newDFA = new DFA (regex, sTree).minimize ();
			newDFA.totalize (universum);
			DFAState.resetIds ();
			newDFA.addToAlphabet (universum);
			return newDFA;
		}
		
		public void totalize (Set<String> universum) {
			if (universum.equals (this.alphabet)) return;
			DFAState s = new DFAState ("Q~", false, false, true, false, Set.of ());
			s.addIncludedState (s);
			for (String sym : universum) {
				lambda.addTransition (new Transition (s, sym, s.asSet ()));
				s.addTransition (s, sym);
			}
			for (DFAState p : getStates ()) {
				for (String sym : universum) {
					if (!p.existsTransitions (sym)) {
						p.setTrap (false);
						p.setGood (false);
						lambda.addTransition (new Transition (p, sym, s.asSet ()));
						p.addTransition (s, sym);
					}
				}
			}
			addState (s);
		}
		
		protected List<Set<DFAState>> buildGroups () {
			List<Set<DFAState>> groups = new ArrayList<> ();
			Set<DFAState> endStates = this.getFinalStates ();
			Set<DFAState> nonEndStates = new HashSet<> (this.getStates ());
			nonEndStates.removeAll (endStates);
			System.out.print ("NonEnd states: ");
			System.out.println (nonEndStates);
			groups.add (endStates);
			if (nonEndStates.size () > 0)
				groups.add (nonEndStates);
			
			boolean groupWasSplit;
			int i = 0;
			do {
				System.out.println ("\tΠ" + i++ + " = " + groups);
				List<Set<DFAState>> newPartition = new ArrayList<> ();
				groupWasSplit = false;
				for (Set<DFAState> group : groups) {
					if (group.size () == 1) {
						newPartition.add (group);
						continue;
					}
					Set<DFAState> oldGroup = new HashSet<> (group);
					Set<DFAState> newGroup = new HashSet<> ();
					
					boolean groupNeedsToBeSplit = false;
					for (String c : this.alphabet) {
						Set<Set<DFAState>> allGoals = new HashSet<> ();
						
						for (DFAState s : group) {
							Set<DFAState> goal = getGroupWhichIncludes (s, c, groups);
							assert (goal != null);
							if (allGoals.size () == 0) {
								allGoals.add (goal);
							} else if (!allGoals.contains (goal)) {
								if (!groupNeedsToBeSplit)
									System.out.println ("\n\t\tSplitting " + group);
								groupNeedsToBeSplit = true;
								
								newGroup.add (s);
								oldGroup.remove (s);
							}
						}
					}
					
					if (groupNeedsToBeSplit) {
						System.out.println (" into " + oldGroup + " and " + newGroup + "\n");
						groupWasSplit = true;
						newPartition.add (newGroup);
					}
					newPartition.add (oldGroup);
				}
				System.out.println ('\n');
				groups = newPartition;
				
			} while (groupWasSplit);
			return groups;
		}
		
		protected Set<DFAState> getFinalStates () {
			Set<DFAState> finalState = new HashSet<> ();
			for (DFAState s : listStates) {
				if (s.isFinal ())
					finalState.add (s);
			}
			return finalState;
		}
		
		protected Set<DFAState> getGroupWhichIncludes (DFAState state, String sym, List<Set<DFAState>> groups) {
			Set<DFAState> result = null;
			state = lambda.getGoalFromTransition (state, sym);
			for (Set<DFAState> group : groups) {
				if (group.contains (state)) {
					result = group;
					break;
				}
			}
			return result;
		}
		
		protected TransitionTable createLambda (List<DFAState> states) {
			TransitionTable result = new TransitionTable ();
			for (int i = 0; i < states.size (); ++i) {
				DFAState s = states.get (i);
				DFAState start = s.getIncludedStates ().iterator ().next ();
				for (String c : this.alphabet) {
					DFAState goal = findStateWithIncludedState (lambda.getGoalFromTransition (start, c), new HashSet<> (states));
					if (goal != null) {
						result.addTransition (new Transition (s, c, goal.asSet ()));
						s.addTransition (goal, c);
					}
				}
				//System.out.println(s.getTransitions());
			}
			return result;
		}
		
		public void addTransitionToTable (Transition t) {
			lambda.addTransition (t);
		}
		
		protected DFAState findStateWithIncludedState (DFAState s, Set<DFAState> states) {
			DFAState result = null;
			for (DFAState curr : states) {
				if (curr.getIncludedStates ().contains (s)) {
					result = curr;
					break;
				}
			}
			return result;
		}
		
		public Map<String, Set<DFAState>> getPossibleTransitions (DFAState state) {
			return lambda.getPossibleTransitions (state);
		}
		
		public void renameStates () {
			for (DFAState s : listStates)
				s.setMarked (false);
			
			String fmtString = "q%d";
			if (listStates.size () >= 100) {
				fmtString = "q%03d";
			} else if (listStates.size () >= 10) {
				fmtString = "q%02d";
			}
			
			Queue<DFAState> queue = new PriorityQueue<> (listStates.size (), Comparator.comparingLong (DFAState::getId));
			queue.add (this.getStartState ());
			int cnt = 1;
			while (!queue.isEmpty ()) {
				DFAState state = queue.remove ();
				if (state.isMarked ())
					continue;
				state.setMarked (true);
				state.setNickname (String.format (fmtString, cnt++));
				
				for (Set<DFAState> goals : this.getPossibleTransitions (state).values ())
					queue.addAll (goals);
			}
		}
		
		public DFAState getStartState () {
			DFAState result = null;
			for (DFAState s : listStates) {
				if (s.isStart ()) {
					result = s;
					break;
				}
			}
			return result;
		}
		
	}
