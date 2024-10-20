	package upl.util.courutines;
	
	import java.util.concurrent.TimeUnit;
	
	public class Test extends CorRunThread<String, String> {
		
		public static class Fib extends CorRunThread<Integer, Integer> {
			
			@Override
			public Integer call () {
				
				try {
					
					Integer times = getReceiveValue ();
					
					do {
						
						try {
							
							int a = 1, b = 1;
							
							for (int i = 0; times != null && i < times; i++) {
								
								int temp = a + b;
								a = b;
								b = temp;
								
							}
							
							// A pythonic "yield", i.e., it returns `a` to the caller and waits `times` value from the next caller
							times = this.yield (a);
							
						} catch (InterruptedException e) {
							return 0;
						}
						
					} while (!isEnded ());
					
					setResult (Integer.MAX_VALUE);
					
					return getResult ();
					
				} catch (InterruptedException e) {
					return 0;
				}
				
			}
			
		}
		
		@Override
		public String call () {
			
			// The fib coroutine would be recycled by its parent
			// (no requirement to call its start () and stop () manually)
			// Otherwise, if you want to share its instance and start/stop it manually,
			// please start it before being called by yieldFrom () and stop it in the end.
			
			Fib fib = new Fib ();
			
			StringBuilder result = new StringBuilder ();
			Integer current;
			int times = 10;
			
			for (int i = 0; i < times; i++) {
				
				try {
					
					// A pythonic "yield from", i.e., it calls fib with `i` parameter and waits for returned value as `current`
					
					current = yieldFrom (fib, i);
					
					if (fib.getError () != null)
						throw new RuntimeException (fib.getError ());
					
					if (current == null)
						continue;
					
					if (i > 0) result.append (",");
					
					result.append (current);
					
				} catch (InterruptedException e) {
					return null;
				}
				
			}
			
			setResult (result.toString ());
			
			return result.toString ();
			
		}
		
		public static String test (Test mainRun) {
			
			// Run the entry coroutine
			mainRun.start ();
			
			// Wait for mainRun ending for 5 seconds
			long startTimestamp = System.currentTimeMillis ();
			
			while (!mainRun.isEnded ()) {
				
				if (System.currentTimeMillis () - startTimestamp > TimeUnit.SECONDS.toMillis (5))
					throw new RuntimeException ("Wait too much time");
				
			}
			
			// The result should be "1,1,2,3,5,8,13,21,34,55"
			
			return mainRun.getResult ();
			
		}
		
	}