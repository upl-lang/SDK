	package upl.core;
	
	import java.lang.System;
	import java.util.Scanner;
	
	public class Device {
		
		private static final File rootDir = new File (".");
		
		public static String waitKeyPress () {
			return new Scanner (System.in).nextLine ();
		}
		
		public static File getRootDir () {
			return rootDir;
		}
		
	}