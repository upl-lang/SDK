	/*
	 * Copyright (c) 2018 - 2024 UPL Foundation
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 *     http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */
	
	package upl.core;
	
	import java.nio.charset.StandardCharsets;
	import java.text.SimpleDateFormat;
	import java.util.logging.ConsoleHandler;
	import java.util.logging.LogRecord;
	import upl.json.JSONArray;
	import upl.json.JSONException;
	import upl.json.JSONObject;
	
	import java.util.logging.Formatter;
	import java.util.logging.Logger;
	import java.util.logging.Level;
	
	import upl.util.List;
	import upl.util.Map;
	
	public class Log {
		
		protected static Logger logger = Logger.getLogger ("JabaDaba");
		
		public Log () {
			
			ConsoleHandler handler = new ConsoleHandler ();
			
			handler.setFormatter (new LogFormatter ());
			
			logger.addHandler (handler);
			
		}
					
					/*public Log (Application app) {
						logger = Logger.getLogger (app.getClass ().getSimpleName ());
					}*/
		
		protected static void log (Level level, Object msg, Exception e) {
			logger.log (level, msg (msg), e);
		}
		
		public static void log (Level level, Object... msg) {
			logger.log (level, msg (msg));
		}
		
		public static String msg (Object... items) {
			
			StringBuilder output = new StringBuilder ();
			
			if (items.length > 0) {
				
				for (int i = 0; i < items.length; i++) {
					
					if (i > 0) output.append (" - ");
					
					try {
						
						Object object = items[i];
						
						if (object instanceof JSONArray)
							output.append (((JSONArray) object).toString (2));
						else if (object instanceof JSONObject)
							output.append (((JSONObject) object).toString (2));
						else if (object instanceof byte[])
							output.append (new String ((byte[]) object, StandardCharsets.UTF_8));
						else if (object instanceof Object[])
							output.append (Arrays.implode (", ", (Object[]) object));
						else if (object instanceof List<?>)
							output.append (((List<?>) object).implode (" - "));
						else if (object instanceof Map<?, ?>)
							output.append (((Map<?, ?>) object).implode ());
						else
							output.append (object);
						
					} catch (JSONException e) {
						output.append (e.getMessage ());
					}
					
				}
				
			} else output.append (" ");
			
			return output.toString ();
			
		}
					
					/*protected static void log (Level level, Throwable err, String msg, Object... args) {
						logger.log (level, format (msg, args), err);
					}*/
		
		public static void test () {
			
			log (Level.ALL, "all");
			log (Level.SEVERE, "severe");
			log (Level.WARNING, "warn");
			log (Level.INFO, "info");
			log (Level.OFF, "off");
			log (Level.CONFIG, "config");
			log (Level.FINE, "fine");
			log (Level.FINER, "finer");
			log (Level.FINEST, "finest");
			
		}
		
		public static void w (Object... msg) {
			log (Level.WARNING, msg);
		}
		
		public static void d (Object... msg) {
			log (Level.INFO, msg (msg));
		}
		
		public static void d (char[] msg) {
			log (Level.INFO, Arrays.implode ("\n", msg));
		}
		
		public static void i (Object... msg) {
			log (Level.INFO, msg (msg));
		}
		
		public void i (Object msg, Exception e) {
			log (Level.INFO, msg, e);
		}
		
		public void i (Throwable err, String msg, Object... args) {
			log (Level.INFO, err, msg, args);
		}
		
		public static void d (int[] msg) {
			log (Level.WARNING, Arrays.implode (", ", msg));
		}
		
		public static void d (StackTraceElement[] msg) {
			log (Level.WARNING, Arrays.implode ("\n", msg));
		}
		
		public static void d (Object msg, Exception e) {
			log (Level.WARNING, msg, e);
		}
		
		public static void d (Throwable err, String msg, Object... args) {
			log (Level.WARNING, err, msg, args);
		}
		
		public void e (Object... msg) {
			log (Level.SEVERE, msg);
		}
		
		public void e (Object msg, Exception e) {
			log (Level.SEVERE, msg, e);
		}
		
		public void e (Throwable err, String msg, Object... args) {
			log (Level.SEVERE, err, msg, args);
		}
		
		public void v (Object... msg) {
			log (Level.OFF, msg);
		}
		
		public void v (Object msg, Exception e) {
			log (Level.OFF, msg, e);
		}
		
		protected static class LogFormatter extends Formatter {
			
			// ANSI escape code
			public static final String ANSI_RESET = "\u001B[0m";
			public static final String ANSI_BLACK = "\u001B[30m";
			public static final String ANSI_RED = "\u001B[31m";
			public static final String ANSI_GREEN = "\u001B[32m";
			public static final String ANSI_YELLOW = "\u001B[33m";
			public static final String ANSI_BLUE = "\u001B[34m";
			public static final String ANSI_PURPLE = "\u001B[35m";
			public static final String ANSI_CYAN = "\u001B[36m";
			public static final String ANSI_WHITE = "\u001B[37m";
			
			// Here you can configure the format of the output and
			// its color by using the ANSI escape codes defined above.
			
			// format is called for every console log message
			
			@Override
			public String format (LogRecord record) {
				
				// This example will print date/time, class, and log level in yellow,
				// followed by the log message and it's parameters in white .
				StringBuilder builder = new StringBuilder ();
				builder.append (ANSI_YELLOW);
				
				builder.append ("[");
				builder.append (calcDate (record.getMillis ()));
				builder.append ("]");
				
				builder.append (" [");
				builder.append (record.getSourceClassName ());
				builder.append ("]");
				
				builder.append (" [");
				builder.append (record.getLevel ().getName ());
				builder.append ("]");
				
				builder.append (ANSI_WHITE);
				builder.append (" - ");
				builder.append (record.getMessage ());
				
				Object[] params = record.getParameters ();
				
				if (params != null) {
					builder.append ("\t");
					for (int i = 0; i < params.length; i++) {
						builder.append (params[i]);
						if (i < params.length - 1)
							builder.append (", ");
					}
				}
				
				builder.append (ANSI_RESET);
				builder.append ("\n");
				
				return builder.toString ();
				
			}
			
			protected String calcDate (long millisecs) {
				
				SimpleDateFormat date_format = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
				return date_format.format (new Date (millisecs));
				
			}
			
		}
		
	}