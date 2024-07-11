  package upl.storage;
  /*
   Created by Acuna on 17.02.2019
  */

  import upl.http.HttpStatus;

  public interface StorageListener {
    
    void onAuthSuccess () throws StorageException;
    void onProgress (long i, long total);
    void onFinish (HttpStatus code);
    void onError (HttpStatus code, String result);
    
  }