	package upl.storage;
	/*
	 Created by Acuna on 28.04.2018
	*/
	
	import upl.app.Plugin;
	import upl.json.JSONException;
	import upl.json.JSONObject;
	
	import upl.http.HttpRequest;
	import upl.core.Net;
	import upl.core.exceptions.OutOfMemoryException;
	import upl.http.HttpStatus;
	import upl.type.Strings;
	import upl.util.HashMap;
	import upl.util.List;
	import upl.util.Map;
	
	public abstract class Adapter extends Plugin {
		
		public static final String KEY_COOKIES = "cookies";
		
		public Storage storage;
		protected int id;
		protected JSONObject authData = new JSONObject ();
		
		protected static final String SORT_NAME = "name", SORT_PATH = "path", SORT_CREATED = "created", SORT_MODIFIED = "modified", SORT_SIZE = "size";
		protected String sort = SORT_NAME;
		
		public Adapter () { }
		
		protected Adapter (Storage storage) {
			this.storage = storage;
		}
		
		public abstract String getVersion ();
		public abstract Adapter newInstance (Storage storage) throws StorageException;
		public abstract Item getItem (String... item);
		
		public abstract void copy (List<Item> from, List<Item> to) throws StorageException, OutOfMemoryException;
		public abstract void move (List<Item> from, List<Item> to) throws StorageException, OutOfMemoryException;
		
		public JSONObject getUserData () throws StorageException {
			return getUserData ("");
		}
		
		public abstract JSONObject getUserData (String token) throws StorageException;
		
		public abstract String getTitle ();
		
		public abstract boolean isAuthenticated () throws StorageException;
		
		public String getApiUrl () {
			return null;
		}
		
		protected final String prepRemoteFile (String str) throws StorageException {
			
			if (!str.equals ("/"))
				str = new Strings (str).trim ("/").toString ();
			
			if (!getRootDir ().equals ("") && !getRootDir ().equals ("/") && !str.equals ("") && !str.equals ("/")) {
				
				if (!getRootDir ().equals (str)) // /storage != /storage
					str = getRootDir () + "/" + str;
				
			} else str = getRootDir () + str;
			
			return str;
			
		}
		
		public void renewToken () throws StorageException {
		}
		
		public Map<String, Object> setDefData (Map<String, Object> data) {
			return data;
		}
		
		public String[] getDecryptedKeys () {
			return new String[0];
		}
		
		public String getAuthUrl () throws StorageException {
			return null;
		}
		
		public String getRedirectUrl () throws StorageException {
			return null;
		}
		
		public String getRootDir () throws StorageException {
			return "";
		}
		
		public void processUrl () throws StorageException {
			processUrl (new HashMap<> (), new HashMap<> ());
		}
		
		protected void processUrl (Map<String, String> urlData, Map<String, String> redirectUrlData) throws StorageException {
			
			if (urlData.get (Net.URL_DOMAIN).equals (redirectUrlData.get (Net.URL_DOMAIN)))
				processUrl (Net.urlQueryDecode (urlData.get (Net.URL_ANCHOR)));
			
		}
		
		protected void processUrl (Map<String, Object> data) throws StorageException {
		
		}
		
		public long getTotalSize () throws StorageException {
			return 0;
		}
		
		protected String getAppsFolderTitle () {
			return "";
		}
		
		protected final HttpRequest initRequest (HttpRequest request) {
			
			request.setListener (new Net.ProgressListener () {
				
				@Override
				public void onStart (long size) {
					//if (storage.listener != null) storage.listener.onStart (size);
				}
				
				@Override
				public void onProgress (final long length, final long size) {
					if (storage.listener != null) storage.listener.onProgress (length, size);
				}
				
				@Override
				public void onError (HttpStatus code, String result) {
					if (storage.listener != null) storage.listener.onError (code, result);
				}
				
				@Override
				public void onFinish (HttpStatus code) {
					if (storage.listener != null) storage.listener.onFinish (code);
				}
				
			});
			
			return request;
			
		}
		
		public final void setCookies (String cookies) {
			
			storage.putPref (KEY_COOKIES, cookies);
			storage.applyPrefs ();
			
		}
		
		public Map<String, Object> getCookies () {
			return Net.explodeCookies (storage.getPref (KEY_COOKIES));
		}
		
		protected Adapter setUseragent (String useragent) throws StorageException {
			
			try {
				
				storage.config.put ("useragent", useragent);
				return this;
				
			} catch (JSONException e) {
				throw new StorageException (e);
			}
			
		}
		
		public final String getUserAgent () throws StorageException {
			
			try {
				return storage.config.getString ("useragent");
			} catch (JSONException e) {
				throw new StorageException (e);
			}
			
		}
		
		public String setError (JSONObject result) throws JSONException {
			return result.toString ();
		}
		
	}