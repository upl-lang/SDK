	package upl.platform.plugins.demo;
	
	import upl.app.Plugin;
	
	public class DemoPlugin extends Plugin {
		
		public String demoMethod () {
			return "111";
		}
		
		@Override
		public String getName () {
			return null;
		}
		
	}