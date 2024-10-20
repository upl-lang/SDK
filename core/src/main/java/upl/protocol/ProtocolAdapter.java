	package upl.protocol;
	
	public abstract class ProtocolAdapter {
		
		public abstract String getName ();
		public abstract String getVersion ();
		
		public abstract void process (ProtocolAdapter adapter) throws ProtocolException;
		
	}