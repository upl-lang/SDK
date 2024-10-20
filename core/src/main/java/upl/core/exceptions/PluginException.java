	package upl.core.exceptions;
	
	public class PluginException extends Exception {
	 
		protected Class<?> clazz;
	
		public PluginException (Exception e) {
			super (e);
		}
		
		public PluginException (String mess, Class<?> clazz) {
			
			super (mess);
			this.clazz = clazz;
			
		}
		
		public Class<?> getPlugin () {
			return clazz;
		}
		
	}