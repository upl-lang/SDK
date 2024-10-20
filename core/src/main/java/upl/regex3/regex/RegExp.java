	package upl.regex3.regex;
	
	import upl.regex3.lang.OperatorsSet;
	import upl.regex3.lang.SymbolSet;
	
	public class RegExp {
		
		protected String initialValue;
		protected String preparedRegex;
		protected SymbolSet symbolSet = new SymbolSet ();
		protected OperatorsSet opSet = new OperatorsSet ();
		
		public RegExp (String s) {
			initialValue = s;
			preparedRegex = prepare (initialValue);
		}
		
		protected String prepare (String initialRegex) {
			StringBuilder newRegex = fillConcatenations (initialRegex);
			return newRegex.toString ();
		}
		
		protected StringBuilder fillConcatenations (String initialRegex) {
			StringBuilder modernRegex = new StringBuilder ();
			modernRegex.append ("(");
			boolean escapedSeq;
			int i = 0;
			for (; i < initialRegex.length () - 1; ++i) {
				escapedSeq = (opSet.contains (initialRegex.charAt (i))) && i > 0 && initialRegex.charAt (i - 1) == '\\';
				if (initialRegex.charAt (i) == '\\' && isSymbol (initialRegex.charAt (i + 1))) {
					modernRegex.append (initialRegex.charAt (i));
				} else if (initialRegex.charAt (i) == '\\' && initialRegex.charAt (i + 1) == '[') {
					modernRegex.append (initialRegex.charAt (i));
				} else if (initialRegex.charAt (i) == '\\' && initialRegex.charAt (i + 1) == '(') {
					modernRegex.append (initialRegex.charAt (i));
				} else if ((isSymbol (initialRegex.charAt (i)) || (escapedSeq)) && isSymbol (initialRegex.charAt (i + 1))) {
					modernRegex.append (initialRegex.charAt (i)).append ('&');
				} else if ((isSymbol (initialRegex.charAt (i)) || (escapedSeq)) && initialRegex.charAt (i + 1) == '(') {
					modernRegex.append (initialRegex.charAt (i)).append ('&');
				} else if (initialRegex.charAt (i) == ')' && isSymbol (initialRegex.charAt (i + 1))) {
					modernRegex.append (initialRegex.charAt (i)).append ('&');
				} else if (initialRegex.charAt (i) == '*' && isSymbol (initialRegex.charAt (i + 1))) {
					modernRegex.append (initialRegex.charAt (i)).append ('&');
				} else if (initialRegex.charAt (i) == '*' && initialRegex.charAt (i + 1) == '(') {
					modernRegex.append (initialRegex.charAt (i)).append ('&');
				} else if (initialRegex.charAt (i) == ')' && initialRegex.charAt (i + 1) == '(') {
					modernRegex.append (initialRegex.charAt (i)).append ('&');
				} else if (initialRegex.charAt (i) == '[' && !escapedSeq) {
					i++;
					int from = initialRegex.charAt (i);
					i = i + 2;
					int to = initialRegex.charAt (i);
					modernRegex.append ('(');
					modernRegex.append ((char) from++);
					while (from <= to) {
						modernRegex.append ('|').append ((char) from++);
					}
				} else if (initialRegex.charAt (i) == ']' && !escapedSeq) {
					if (isSymbol (initialRegex.charAt (i + 1)) || initialRegex.charAt (i + 1) == '[' || initialRegex.charAt (i + 1) == '(') {
						modernRegex.append (")&");
					} else {
						modernRegex.append (')');
					}
				} else {
					modernRegex.append (initialRegex.charAt (i));
				}
			}
			escapedSeq = (opSet.contains (initialRegex.charAt (i))) && i > 0 && initialRegex.charAt (i - 1) == '\\';
			if (initialRegex.charAt (initialRegex.length () - 1) == ']' && !escapedSeq) {
				modernRegex.append (')');
			} else {
				modernRegex.append (initialRegex.charAt (initialRegex.length () - 1));
			}
			modernRegex.append (")&#");
			System.out.println (initialRegex);
			System.out.println (modernRegex);
			return modernRegex;
		}
		
		protected boolean isSymbol (char ch) {
			return symbolSet.contains (ch) && !opSet.contains (ch);
		}
		
		@Override
		public String toString () {
			return preparedRegex;
		}
		
		public String getInitialValue () {
			return initialValue;
		}
		
	}