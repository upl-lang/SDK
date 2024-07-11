  package upl.storage.adapters;
  
  import java.io.UnsupportedEncodingException;
  import java.net.MalformedURLException;
  import java.net.URL;
  
  import upl.http.HttpMethod;
  import upl.http.HttpStatus;
  import upl.storage.Adapter;
  import upl.storage.Item;
  import upl.storage.Storage;
  import upl.storage.StorageException;
  
  import upl.core.Arrays;
  import upl.core.Int;
  import upl.core.Net;
  import upl.core.exceptions.HttpRequestException;
  import upl.core.exceptions.OutOfMemoryException;
  import upl.core.File;
  import upl.core.Log;
  import upl.http.HttpRequest;
  import upl.io.BufferedInputStream;
  import upl.json.JSONArray;
  import upl.json.JSONException;
  import upl.json.JSONObject;
  import upl.util.ArrayList;
  import upl.util.HashMap;
  import upl.util.LinkedHashMap;
  import upl.util.List;
  import upl.util.Map;
  
  public class YandexDisk extends Adapter {
    
    public YandexDisk () {
    }
    
    private YandexDisk (Storage storage) throws StorageException {
      
      super (storage);
      setUseragent ("api-explorer-client");
      
    }
    
    @Override
    public String getApiUrl () {
      return "https://cloud-api.yandex.net/v1/disk";
    }
    
    @Override
    public Adapter newInstance (Storage storage) throws StorageException {
      return new YandexDisk (storage);
    }
    
    @Override
    public void renewToken () throws StorageException {
      
      try {
        
        String refToken = storage.getPref ("refresh_token");
        
        if (refToken != null && !refToken.equals ("")) {
          
          HttpRequest request = new HttpRequest (HttpMethod.POST, "https://oauth.yandex.ru/token");
          
          Map<String, Object> data = new LinkedHashMap<> ();
          
          data.add ("client_id", storage.config.getString ("client_id"));
          data.add ("client_secret", storage.config.getString ("client_secret"));
          data.add ("grant_type", "refresh_token");
          data.add ("refresh_token", refToken);
          
          request.send (data);
          
          authData = new JSONObject (request.getContent ());
          
        }
        
      } catch (JSONException | HttpRequestException | OutOfMemoryException e) {
        throw new StorageException (storage, e);
      }
      
    }
    
    @Override
    public String getName () {
      return "yadisk";
    }
    
    @Override
    public String getTitle () {
      return "Яндекс.Диск";
    }
    
    @Override
    public String getVersion () {
      return "1.3";
    }
    
    @Override
    public JSONObject getUserData (String token) throws StorageException {
      
      try {
        
        Map<String, Object> data = new LinkedHashMap<> ();
        
        data.add ("format", "json");
        data.add ("oauth_token", token);
        
        HttpRequest request = new HttpRequest (HttpMethod.GET, "https://login.yandex.ru/info").setParams (data);
        
        String content = request.getContent ();
        
        if (!content.equals ("")) {
          
          JSONObject result = new JSONObject (content);
          
          if (result.has ("login")) {
            
            JSONObject userData = new JSONObject ();
            
            userData.put (Storage.USER_NAME, result.getString ("login"));
            
            return userData;
            
          } else throw new StorageException (storage, result.toString ());
          
        } else throw new StorageException (storage, content);
        
      } catch (JSONException | HttpRequestException | OutOfMemoryException e) {
        throw new StorageException (storage, e);
      }
      
    }
    
    @Override
    public String getAuthUrl () throws StorageException {
      
      try {
        
        Map<String, Object> data = new LinkedHashMap<> ();
        
        data.add ("client_id", storage.config.getString ("client_id"));
        data.add ("response_type", "token");
        data.add ("force_confirm", true);
        
        return "https://oauth.yandex.ru/authorize" + Net.urlQueryEncode (data);
        
      } catch (JSONException | UnsupportedEncodingException e) {
        throw new StorageException (storage, e);
      }
      
    }
    
    @Override
    public String getRedirectUrl () throws StorageException {
      
      try {
        return storage.config.getString ("redirect_url");
      } catch (JSONException e) {
        throw new StorageException (storage, e);
      }
      
    }
    
    @Override
    public String getRootDir () throws StorageException {
      return "app:";
    }
    
    protected class FileItem extends Item {
      
      protected FileItem (Storage storage, String... remoteFile) {
        super (storage, remoteFile);
      }
      
      @Override
      public List<Item> list (int mode) throws StorageException, OutOfMemoryException {
        
        List<Item> files = new ArrayList<> ();
        
        try {
          
          JSONArray items = getInfo ().getJSONObject ("_embedded").getJSONArray ("items");
          
          for (int i = 0; i < Int.size (items); ++i) {
            
            JSONObject file = items.getJSONObject (i);
            
            Item item = getItem (file.getString ("path")).toItem (file);
            
            if (item.show (mode))
              files.add (item);
            
          }
          
        } catch (JSONException e) {
          throw new StorageException (this, e);
        }
        
        return files;
        
      }
      
      @Override
      public JSONObject getInfo () throws StorageException {
        
        try {
          
          Map<String, Object> params = new LinkedHashMap<> ();
          
          if (getShortFile ().equals (""))
            setShortFile ("app:/");
          
          params.add ("path", this);
          params.add ("sort", sort);
          params.add ("offset", storage.pagination.offset);
          params.add ("limit", storage.pagination.perPage);
          
          HttpRequest request = request (HttpMethod.GET, getApiUrl () + "/resources", params);
          
          JSONObject output = new JSONObject (request.getContent ());
          
          if (output.has ("resource_id") || output.has ("public_id"))
            return output;
          else
            return new JSONObject ();
          
        } catch (JSONException | HttpRequestException | OutOfMemoryException e) {
          throw new StorageException (this, e);
        }
        
      }
      
      /*@Override
      public long getSize () throws StorageException {
        
        try {
          
          long size = 0;
          JSONObject data = getInfo ();
          
          if (data.has ("_embedded")) {
            
            JSONArray items = data.getJSONObject ("_embedded").getJSONArray ("items");
            
            for (int i = 0; i < Int.size (items); ++i) {
              
              JSONObject item = items.getJSONObject (i);
              size += item.getLong ("size");
              
            }
            
          } else size = data.getLong ("size");
          
          return size;
          
        } catch (JSONException e) {
          throw new StorageException (this, e);
        }
        
      }*/
      
      @Override
      public URL getDirectLink () throws StorageException, OutOfMemoryException {
        
        try {
          
          if (directUrl == null) {
            
            Map<String, Object> params = new LinkedHashMap<> ();
            
            params.add ("path", this);
            
            directUrl = new URL (_getDirectLink ("resources/download", params));
            
          }
          
          return directUrl;
          
        } catch (MalformedURLException e) {
          throw new StorageException (storage, e);
        }
        
      }
      
      @Override
      public Item toItem (JSONObject file) throws StorageException {
        
        try {
          
          isExists = (file.length () > 0);
          
          if (isExists) {
            
            isDir (file.getString ("type").equals ("dir"));
            
            if (!isDir) {
              
              isImage (file.getString ("media_type").equals ("image"));
              setSize (file.getLong ("size"));
              setDirectLink (new URL (file.getString ("file")));
              
              if (file.has ("preview"))
                setThumbUrl (new URL (file.getString ("preview")));
              
            }
            
          }
          
          return this;
          
        } catch (JSONException | MalformedURLException e) {
          throw new StorageException (this, e);
        }
        
      }
      
      @Override
      public boolean makeDir (boolean force) throws StorageException {
        
        try {
          
          if (!isExists) {
            
            List<String> parts = Arrays.explode ("/", getShortFile ());
            
            String path = (Int.size (parts) > 2 ? "disk:" : getRootDir ());
            
            for (int i = 1; i < Int.size (parts); ++i) {
              
              path += "/" + parts.get (i);
              
              Item item = toItem (getItem (path).getInfo ());
              
              if (!item.isDir) {
                
                Map<String, Object> params = new HashMap<> ();
                
                params.add ("path", item);
                
                HttpRequest request = request (HttpMethod.PUT, getApiUrl () + "/resources", params);
                
                if (request.getStatus () != 201)
                  throw new StorageException (item, request);
                
              }
              
            }
            
            return 1;
            
          } else return 2;
          
        } catch (JSONException | HttpRequestException | OutOfMemoryException e) {
          throw new StorageException (this, e);
        }
        
      }
      
      @Override
      public Item put (Item remoteItem, boolean force, boolean makeDir) throws StorageException, OutOfMemoryException {
        
        try {
          
          remoteItem = remoteItem.toItem (remoteItem.getInfo ());
          
          if (remoteItem.force (force)) {
            
            if (makeDir) remoteItem.getParent ().makeDir ();
            
            Map<String, Object> params = new LinkedHashMap<> ();
            
            params.add ("path", this);
            params.add ("overwrite", force);
            
            String link = _getDirectLink ("resources/upload", params);
            HttpRequest request = request (HttpMethod.PUT, link, new LinkedHashMap<> ());
            
            request.send (remoteItem.getStream ());
            
            if (request.getStatus () != 201)
              throw new StorageException (remoteItem, request.getMessageCode ());
            
            //publish (remoteItem);
            
          }
          
          return remoteItem;
          
        } catch (HttpRequestException e) {
          throw new StorageException (remoteItem, e);
        }
        
      }
      
      @Override
      public void delete (boolean trash) throws StorageException {
        
        try {
          
          Map<String, Object> params = new LinkedHashMap<> ();
          
          params.add ("path", this);
          params.add ("permanently", !trash);
          
          HttpRequest request = request (HttpMethod.DELETE, getApiUrl () + "/resources", params);
          
          HttpStatus status = request.getStatus ();
          
          if (status != 202 && status != 204)
            throw new StorageException (storage, request);
          
        } catch (HttpRequestException | OutOfMemoryException e) {
          throw new StorageException (this, e);
        }
        
      }
      
      @Override
      public BufferedInputStream getThumbStream () throws StorageException, OutOfMemoryException {
        
        try {
          
          if (stream == null) {
            
            HttpRequest request = request (HttpMethod.GET, getThumbUrl ().toString (), new HashMap<> ());
            
            setStream (request.getInputStream ());
            setSize (request.getLength ());
            
          }
          
        } catch (HttpRequestException e) {
          throw new StorageException (this, e);
        }
        
        return stream;
        
      }
      
      private String _getDirectLink (String url, Map<String, Object> params) throws StorageException, OutOfMemoryException {
        
        try {
          
          HttpRequest request = request (HttpMethod.GET, getApiUrl () + "/" + url, params);
          
          JSONObject result = new JSONObject (request.getContent ());
          
          if (request.isOK ())
            return result.getString ("href");
          else
            throw new StorageException (this, result);
          
        } catch (HttpRequestException | JSONException e) {
          throw new StorageException (storage, e);
        }
        
      }
      
      @Override
      public void move (Item to) throws StorageException {
        
        List<Item> from = new ArrayList<> ();
        
        from.add (this);
        
        List<Item> to2 = new ArrayList<> ();
        
        to2.add (to);
        
        _move ("move", from, to2);
        
      }
      
    }
    
    private HttpRequest request (HttpMethod method, String url, Map<String, Object> params) throws StorageException {
      
      HttpRequest request = initRequest (new HttpRequest (method, url).setParams (params));
      
      request.setHeader ("Authorization", "OAuth " + storage.getPref ("access_token"));
      request.setUserAgent (storage.adapter.getUserAgent ());
      
      return request;
      
    }
    
    private void _move (String action, List<Item> from, List<Item> to) throws StorageException {
      
      for (int i = 0; i < Int.size (from); i++) {
        
        try {
          
          Map<String, Object> params = new LinkedHashMap<> ();
          
          params.add ("from", from.get (i));
          params.add ("path", prepRemoteFile (to.get (i) + "/" + new File (from.get (i).toString ()).getName (true)));
          params.add ("overwrite", false);
          
          HttpRequest request = request (HttpMethod.POST, getApiUrl () + "/resources/" + action, params);
          
          if (request.getStatus () > 400) {
            Log.w (request.getContent ());
            throw new StorageException (from.get (i), request);
          }
          
        } catch (HttpRequestException | OutOfMemoryException | JSONException e) {
          throw new StorageException (from.get (i), e);
        }
        
      }
      
    }
    
    @Override
    public void copy (List<Item> from, List<Item> to) throws StorageException {
      _move ("copy", from, to);
    }
    
    @Override
    public void move (List<Item> from, List<Item> to) throws StorageException {
      _move ("move", from, to);
    }
    
    @Override
    public String setError (JSONObject result) throws JSONException {
      return result.getString ("message");
    }
    
    @Override
    public Item getItem (String... remoteFile) {
      return new FileItem (storage, remoteFile);
    }
    
    @Override
    public boolean isAuthenticated () throws StorageException {
      return storage.hasPref ("access_token");
    }
    
  }