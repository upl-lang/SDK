	package upl.os;
	
	import upl.core.Arrays;
	import upl.util.LinkedHashMap;
	import upl.util.Map;
	
	public class Crontab {
		
		protected Map<Part, String> pattern = new LinkedHashMap<> ();
		protected Map<Part, int[]> values = new LinkedHashMap<> ();
		
		public enum Type { EVERY, EXACT }
		
		public enum Part {
			
			MINUTES,
			HOURS,
			DAYS,
			MONTHS,
			WEEKDAYS,
			
		}
		
		public Crontab () {
			
			pattern.add (Part.MINUTES, "*");
			pattern.add (Part.HOURS, "*");
			pattern.add (Part.DAYS, "*");
			pattern.add (Part.MONTHS, "*");
			pattern.add (Part.WEEKDAYS, "*");
			
			values.add (Part.MINUTES, new int[] {0, 59});
			values.add (Part.HOURS, new int[] {0, 23});
			values.add (Part.DAYS, new int[] {1, 31});
			values.add (Part.MONTHS, new int[] {1, 12});
			values.add (Part.WEEKDAYS, new int[] {0, 6});
			
		}
		
		public Crontab set (Type type, Part part, int value) {
			
			int[] range = values.get (part);
			
			if (value >= range[0] && value <= range[1]) {
				
				switch (type) {
					
					case EVERY: {
						
						pattern.add (part, "*/" + value);
						break;
						
					}
					
					case EXACT: {
						
						pattern.add (part, String.valueOf (value));
						break;
						
					}
					
				}
				
			} else throw new IndexOutOfBoundsException ("Value " + value + " out of range [" + range[0] + "..." + range[1] + "]");
			
			return this;
			
		}
		
		public Crontab set (Type type, Part part, int from, int to) {
			
			int[] range = values.get (part);
			
			if (from >= range[0] && from <= range[1]) {
				
				if (to >= range[0] && to <= range[1]) {
					
					switch (type) {
						
						case EXACT: {
							
							pattern.add (part, from + "-" + to);
							break;
							
						}
						
						default:
							throw new IllegalArgumentException ("Type must be EXACT with this values");
						
					}
					
				} else throw new IndexOutOfBoundsException ("Value " + to + " out of range [" + range[0] + "..." + range[1] + "]");
				
			} else throw new IndexOutOfBoundsException ("Value " + from + " out of range [" + range[0] + "..." + range[1] + "]");
			
			return this;
			
		}
		
		public Crontab set (Type type, Part part, Integer... values) {
			
			int[] range = this.values.get (part);
			
			for (int value : values)
				if (value < range[0] || value > range[1])
					throw new IndexOutOfBoundsException ("Value " + value + " out of range [" + range[0] + "..." + range[1] + "]");
				
			switch (type) {
				
				case EXACT: {
					
					pattern.add (part, Arrays.implode (",", values));
					break;
					
				}
				
				default:
					throw new IllegalArgumentException ("Type must be EXACT with this values");
				
			}
			
			return this;
			
		}
		
		public String get (Part part) {
			return pattern.get (part);
		}
		
		@Override
		public String toString () {
			
			StringBuilder str = new StringBuilder ();
			
			int i = 0;
			
			for (Part part : pattern.keySet ()) {
				
				if (i > 0) str.append (" ");
				str.append (pattern.get (part));
				
				i++;
				
			}
			
			return str.toString ();
			
		}
		
	}