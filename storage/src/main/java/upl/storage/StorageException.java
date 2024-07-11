  package upl.storage;
  /*
   Created by Acuna on 31.07.2017
  */
  
  import upl.core.exceptions.HttpRequestException;
  import upl.core.exceptions.OutOfMemoryException;
  import upl.http.HttpRequest;
  import upl.json.JSONException;
  import upl.json.JSONObject;
  
  public class StorageException extends Exception {
    
    private Storage storage;
    private String remoteFile;
    
    public StorageException (String mess) {
      super (mess);
    }
    
    public StorageException (Exception e) {
      super (e);
    }
    
    public StorageException (Storage storage, Exception e) {
      this (storage, e.getMessage ());
    }
    
    public StorageException (Storage storage, HttpRequest request) throws HttpRequestException, OutOfMemoryException, JSONException {
      this (storage, new JSONObject (request.getContent ()));
    }
    
    public StorageException (Item item, HttpRequest request) throws HttpRequestException, OutOfMemoryException, JSONException {
      this (item, new JSONObject (request.getContent ()));
    }
  
    public StorageException (Item item, Exception e) {
      
      this (item.storage, e);
      this.remoteFile = item.getShortFile ();
      
    }
    
    public StorageException (Item item, String mess) {
      
      this (item.storage, mess);
      this.remoteFile = item.getShortFile ();
      
    }
    
    public StorageException (Storage storage, JSONObject result) throws JSONException {
      this (storage, storage.adapter.setError (result));
    }
    
    public StorageException (Item item, JSONObject result) throws JSONException {
      this (item, item.storage.adapter.setError (result));
    }
    
    public StorageException (Storage storage, String mess) {
      
      this (mess);
      this.storage = storage;
      
    }
    
    public final String getRemoteFile () {
      return remoteFile;
    }
    
    public final String getType () {
      return storage.adapter.getName ();
    }
    
  }