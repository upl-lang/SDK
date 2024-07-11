  package upl.app;
  
  import upl.cipher.exceptions.DecryptException;
  
  public abstract class EncryptedPreferences extends Preferences {
    
    public EncryptedPreferences (Application app) {
      super (app);
    }
    
    public String get (String key, String defValue, boolean encrypted) throws DecryptException {
      return optString (key, defValue);
    }
    
    public int get (String key, int defValue, boolean encrypted) throws DecryptException {
      return optInt (key, defValue);
    }
    
    public final float get (String key, float defValue, boolean encrypted) throws DecryptException {
      return optFloat (key, defValue);
    }
    
    public final boolean get (String key, boolean defValue, boolean encrypted) throws DecryptException {
      return optBool (key, defValue);
    }
    
  }