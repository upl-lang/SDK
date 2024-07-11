  package upl.protocol;
  
  public class StubProtocol extends ProtocolAdapter {
    
    @Override
    public String getName () {
      return "Stub";
    }
    
    @Override
    public String getVersion () {
      return "1.0";
    }
    
    @Override
    public void process (ProtocolAdapter adapter) throws ProtocolException {
    
    }
    
  }