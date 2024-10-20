	/*
	 * Copyright (c) 2022 UPL Foundation
	 */
	
	package upl.exceptions;
	
	public class ParameterNotFoundException extends RuntimeException {
		
		protected String key;
		
		public ParameterNotFoundException (String key) {
			
			super ("Parameter \"" + key + "\" is required");
			
			this.key = key;
			
		}
		
		public ParameterNotFoundException (String str, String key) {
			
			super (str);
			
			this.key = key;
			
		}
		
		public ParameterNotFoundException (Exception e) {
			super (e);
		}
		
		public String getParam () {
			return key;
		}
		
	}