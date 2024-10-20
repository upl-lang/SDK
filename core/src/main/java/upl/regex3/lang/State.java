	package upl.regex3.lang;
	
	import java.util.HashMap;
	import java.util.Map;
	
	class State {
		
		static class Transition {
			String from;
			String to;
			String value;
			
			Transition (String from, String to, String value) {
				this.from = from;
				this.to = to;
				this.value = value;
			}
			
		}
		
		String label;
		Boolean isStart;
		Boolean isAccept;
		Map<String, Transition> inTransitions;
		Map<String, Transition> outTransitions;
		Transition selfLoop;
		
		State (String label, Boolean isAccept, Boolean isStart) {
			this.label = label;
			this.isAccept = isAccept;
			this.isStart = isStart;
			inTransitions = new HashMap<> ();
			outTransitions = new HashMap<> ();
		}
		
		public void addSelfLoop (String value) {
			if (selfLoop == null) {
				this.selfLoop = new Transition (label, label, value);
			} else {
				this.selfLoop.value = "(" + this.selfLoop.value + "|" + value + ")";
			}
		}
		
		public void addInTransition (String from, String value) {
			if (!inTransitions.containsKey (from)) {
				this.inTransitions.put (from, new Transition (from, label, value));
			} else {
				this.inTransitions.get (from).value = "(" + this.inTransitions.get (from).value + "|" + value + ")";
			}
		}
		
		public void addOutTransition (String to, String value) {
			if (!outTransitions.containsKey (to)) {
				this.outTransitions.put (to, new Transition (label, to, value));
			} else {
				this.outTransitions.get (to).value = "(" + value + "|" + this.outTransitions.get (to).value + ")";
			}
		}
		
		public void removeInTransition (String dest) {
			this.inTransitions.remove (dest);
		}
		
		public void removeOutTransition (String dest) {
			this.outTransitions.remove (dest);
		}
		
	}