	package upl.type;
	
	import upl.core.Arrays;
	import upl.core.Log;
	import upl.core.Logger;
	import upl.loggers.Console;
	import upl.util.ArrayList;
	import upl.util.List;
	
	public class Object {
		
		protected List<String> printItems = new ArrayList<> ();
		
		public void implode () {
			implode (" ");
		}
		
		public void implode (String sep) {
			
			print (sep, printItems);
			
			printItems = new ArrayList<> ();
			
		}
		
		public void append (java.lang.Object msg) {
			printItems.add (msg.toString ());
		}
		
		public static void println (java.lang.Object... msg) {
			System.out.println (Log.msg (msg));
		}
		
		public static void printr (java.lang.Object... msg) {
			print ("\r" + Arrays.implode (", ", msg));
		}
		
		public static void print (java.lang.Object text) {
			System.out.print (text);
		}
		
		public void print (String sep, List<?> items) {
			println (items.implode (sep));
		}
		
		protected void d (java.lang.Object... items) {
			Log.d (items);
		}
		
	}