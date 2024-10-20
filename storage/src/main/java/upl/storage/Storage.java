	package upl.storage;
	/*
	 Created by Acuna on 10.05.2017
	*/
	
	import java.io.IOException;
	
	import upl.app.PluginService;
	import upl.core.File;
	import upl.json.JSONArray;
	import upl.json.JSONException;
	import upl.json.JSONObject;
	
	import upl.core.Int;
	import upl.core.Net;
	import upl.core.exceptions.OutOfMemoryException;
	import upl.storage.adapters.Dropbox;
	import upl.storage.adapters.GDrive;
	import upl.storage.adapters.SDCard;
	import upl.storage.adapters.SFTP;
	import upl.storage.adapters.YandexDisk;
	import upl.util.ArrayList;
	import upl.util.HashMap;
	import upl.util.LinkedHashMap;
	import upl.util.List;
	import upl.util.Map;
	
	public class Storage {
		
		protected static final String NAME = "storage";
		public static final String KEY_COOKIES = "cookies";
		
		protected boolean refreshToken = true, auth = true;
		public JSONObject configs = new JSONObject (), config = new JSONObject ();
		protected int accountId = -1, providerId = 0;
		
		protected String type;
		
		public Adapter adapter;
		public Pagination pagination = new Pagination ();
		
		public JSONObject data = new JSONObject ();
		
		public static final String ITEM_TITLE = "title";
		public static final String ITEM_ICON = "icon";
		public static final String ITEM_LAYOUT = "layout";
		
		public static final String USER_NAME = "name";
		public static final String USER_FULLNAME = "fullname";
		public static final String USER_AVATAR = "avatar";
		
		protected static final String PREF_ACCOUNTS = "accounts";
		protected static final String PREF_PROVIDER_ID = "provider_id";
		
		protected PluginService<Adapter> service = new PluginService<> (PluginService.Options.ADD_AS_NAME);
		
		public Storage () {
			
			addAdapter (new SDCard ());
			addAdapter (new SFTP ());
			addAdapter (new Dropbox ());
			addAdapter (new GDrive ());
			addAdapter (new YandexDisk ());
			
		}
		
		public String getVersion () {
			return "3.0";
		}
		
		public Storage addAdapter (Adapter adapter) {
			
			service.add (adapter);
			
			return this;
			
		}
		
		public Adapter getProvider (String name) {
			
			/*items.put (TYPE_S3, new Object[] { "Amazon S3" });
			items.put (TYPE_ONEDRIVE, new Object[] { "OneDrive" });
			items.put (TYPE_MEGA, new Object[] { "Mega" });
			
			keys.put (TYPE_S3, new String[] { "key", "secret", "bucket", "region" });*/
			
			try {
				
				setAdapter (service.get (name));
				
				return adapter;
				
			} catch (StorageException e) {
				return null;
			}
			
		}
		
		protected Net.ProgressListener streamListener;
		
		public Storage setListener (Net.ProgressListener listener) {
			
			streamListener = listener;
			return this;
			
		}
		
		protected Map<String, JSONObject> items = new HashMap<> ();
		
		public final Storage setProviderItem (String type, JSONObject data) {
			
			items.add (type, data);
			return this;
			
		}
		
		public Map<String, Object> getDefData () throws StorageException {
			
			Map<String, Object> data = new LinkedHashMap<> ();
			
			data.add ("type", type);
			
			return adapter.setDefData (data);
			
		}
		
		public Storage setConfigs (JSONObject configs) {
			
			this.configs = configs;
			return this;
			
		}
		
		protected JSONObject defaultData (JSONArray data) throws StorageException {
			
			try {
				
				JSONObject data2 = new JSONObject ();
				
				int i = 0;
				
				for (String key : getDefData ().keySet ()) {
					
					data2.put (key, data.get (i));
					++i;
					
				}
				
				return data2;
				
			} catch (JSONException e) {
				throw new StorageException (this, e);
			}
			
		}
		
		protected void init (Adapter mProvider) throws JSONException {
			
			type = mProvider.getName ();
			config = configs.getJSONObject (type);
			if (accountId == -1) accountId = getAccountId ();
			
		}
		
		protected void refreshToken () throws JSONException {
			
			if (refreshToken && Int.size (adapter.authData) > 0) { // Обновляем токен
				
				JSONArray keys = adapter.authData.keys ();
				
				for (int i = 0; i < Int.size (keys); ++i) {
					
					String key = keys.getString (i);
					putPref (key, adapter.authData.get (key));
					
				}
				
				applyPrefs ();
				
				refreshToken = false;
				
			}
			
		}
		
		protected void setAdapter (Adapter mProvider) throws StorageException {
			
			try {
				
				init (mProvider);
				adapter = mProvider.newInstance (this);
				refreshToken ();
				
			} catch (JSONException e) {
				throw new StorageException (e);
			}
			
		}
		
		public final void setAccountId (int id) {
			
			putPref (type + "_account", id, true);
			accountId = id;
			
		}
		
		protected int getAccountId () {
			return getPref (type + "_account", accountId);
		}
		
		public final JSONObject defaultData () throws StorageException {
			
			try {
				
				//setAccountId ();
				
				Map<String, Object> defData = getDefData ();
				
				JSONObject data = new JSONObject ();
				
				for (String key : defData.keySet ()) {
					
					Object obj = defData.get (key);
					
					if (obj instanceof Integer)
						data.put (key, getPref (prefKey (key), (int) obj));
					else
						data.put (key, getPref (prefKey (key), obj.toString ()));
					
				}
				
				return data;
				
			} catch (JSONException e) {
				throw new StorageException (this, e);
			}
			
		}
		
		public final Storage makeAuth (boolean auth) {
			
			this.auth = auth;
			return this;
			
		}
		
		protected void applyPrefs () {
		
		}
		
		public final String toString () {
			return adapter.getName ();
		}
		
		public StorageListener listener;
		
		public final Storage setListener (StorageListener listener) {
			
			this.listener = listener;
			return this;
			
		}
		
		public final void setPrefs (JSONObject userData, JSONObject data) throws StorageException {
			
			JSONArray keys = data.keys ();
			
			try {
				
				addUser (userData);
				
				for (int i = 0; i < Int.size (keys); ++i) {
					
					String key = keys.getString (i);
					putPref (key, data.get (key));
					
				}
				
				applyPrefs ();
				
			} catch (JSONException e) {
				throw new StorageException (this, e);
			}
			
			if (listener != null) listener.onAuthSuccess ();
			
		}
		
		public final void setPrefs (JSONObject userData, Map<String, Object> data) throws StorageException {
			
			try {
				
				addUser (userData);
				
				for (String key : data.keySet ())
					putPref (key, data.get (key));
				
				applyPrefs ();
				
			} catch (JSONException e) {
				throw new StorageException (this, e);
			}
			
			if (listener != null) listener.onAuthSuccess ();
			
		}
		
		protected String prefKey (int id, String key) {
			return type + "_" + id + "_" + key;
		}
		
		protected String prefKey (String key) {
			return prefKey (accountId, key);
		}
		
		public void putPref (String key, Object value) {
			putPref (key, value, false);
		}
		
		public void putPref (String key, Object value, boolean isKey) {
		}
		
		protected void addUser (JSONObject userData) throws JSONException, StorageException {
			
			JSONObject accounts = getAccounts ();
			JSONArray users = new JSONArray (), getUsers = new JSONArray ();
			
			if (accounts.has (type)) {
				
				getUsers = accounts.getJSONArray (type);
				
				for (int i = 0; i < Int.size (getUsers); ++i)
					users.put (getUsers.getJSONObject (i).getString (USER_NAME));
				
			}
			
			if (userData != null && userData.length () > 0) {
				
				if (!users.contains (userData.getString (USER_NAME))) {
					
					accountId = Int.size (getUsers);
					
					getUsers.put (accountId, userData);
					
					accounts.put (type, getUsers);
					
					putPref (PREF_ACCOUNTS, accounts.toString (), true);
					setAccountId (accountId);
					
				}
				
			}
			
		}
		
		public String getPref (String key) {
			return getPref (prefKey (key), "");
		}
		
		public int getPref (String key, int value) {
			return value;
		}
		
		public String getPref (String key, String value) {
			return value;
		}
		
		protected JSONObject getPref (String key, JSONObject value) throws StorageException {
			return value;
		}
		
		protected Object getPref (String key, Map<String, Object> data) {
			
			Object value = data.get (key);
			
			if (value instanceof Integer)
				return getPref (getPref (key), (int) value);
			else
				return getPref (getPref (key), value.toString ());
			
		}
		
		public final JSONObject getAccounts () throws StorageException {
			return getPref (PREF_ACCOUNTS, new JSONObject ());
		}
		
		public final boolean hasPref (String key) {
			return (!getPref (key).equals (""));
		}
		
		public final JSONArray getAccounts (String type) throws StorageException {
			
			try {
				
				JSONObject accounts = getAccounts ();
				return (accounts.has (type) ? accounts.getJSONArray (type) : new JSONArray ());
				
			} catch (JSONException e) {
				throw new StorageException (this, e);
			}
			
		}
		
		public final JSONObject getAccount () throws StorageException {
			
			try {
				
				JSONArray accounts = getAccounts (type);
				return (Int.size (accounts) > 0 ? accounts.getJSONObject (getAccountId ()) : new JSONObject ());
				
			} catch (JSONException e) {
				throw new StorageException (this, e);
			}
			
		}
		
		public final int getAccountId (String user) throws StorageException {
			
			try {
				
				JSONArray account = getAccounts (type);
				
				for (int i = 0; i < Int.size (account); ++i) {
					
					if (account.getJSONObject (i).getString (USER_NAME).equals (user))
						return i;
					
				}
				
				return -1;
				
			} catch (JSONException e) {
				throw new StorageException (this, e);
			}
			
		}
		
		public final String test (Adapter provider, String file, String file2) throws StorageException, OutOfMemoryException, IOException {
			return test (provider, file, file2, true);
		}
		
		public final String test (Adapter provider, String file, String file2, boolean delete) throws StorageException, OutOfMemoryException, IOException {
			return test (provider, file, file2, delete, "");
		}
		
		public final String test (Adapter adapter, String file, String file2, boolean delete, String output) throws StorageException, OutOfMemoryException, IOException {
			
			if (data.has (adapter.getName ())) {
				
				setAdapter (adapter);
				
				output += adapter.getName () + "\n"; // TODO
				output += "\n";
				
				Item item = adapter.getItem (file);
				
				Item item2 = adapter.getItem ("123/456/ttt.txt");
				
				item.put (new File (file));
				item2.put (type);
				item.put (new File (file2));
				
				output += "list:\n\n";
				output += item.list (0).implode () + "\n\n";
				
				output += "read: " + item2.getStream ().read ("") + "\n\n";
				
				item2 = adapter.getItem (file2);
				
				if (delete) {
					
					if (item2.isExists)
						item2.delete ();
					//else
					//	throw new StorageException (type + ": File " + prepRemoteFile (file2) + " not found");
					
					//if (isDir ("123"))
					adapter.getItem ("123").delete ();
					//else
					//	throw new StorageException (type + ": Folder " + prepRemoteFile ("123") + " not found");
					
				}
				
				item.close ();
				
			}
			
			return output;
			
		}
		
		protected int findProviderId () {
			
			int i = 0;
			
			for (Adapter adapter : service.getPlugins ()) {
				
				if (adapter.getName ().equals (type))
					return i;
				
				i++;
				
			}
			
			return providerId;
			
		}
		
		public void setProviderId (int id) {
			putPref (PREF_PROVIDER_ID, id, true);
		}
		
		public int getProviderId () {
			return getPref (PREF_PROVIDER_ID, providerId);
		}
		
		public static final class Pagination {
			
			public int perPage = 25, page = 0, offset = 0;
			
		}
		
		public static final class ProviderItem {
			
			protected String name, title, icon;
			
			public final String getTitle () {
				return title;
			}
			
			public final String getName () {
				return name;
			}
			
		}
		
		public final class ProvidersData {
			
			protected List<ProviderItem> items = new ArrayList<> ();
			public final String[] names, titles;
			
			ProvidersData () throws StorageException {
				
				int size = service.getPlugins ().size ();
				
				names = new String[size];
				titles = new String[size];
				
				int i = 0;
				
				for (Adapter adapter : service.getPlugins ()) {
					
					ProviderItem item2 = new ProviderItem ();
					
					item2.name = names[i] = adapter.getName ();
					item2.title = titles[i] = adapter.getTitle ();
					
					items.add (item2);
					
					++i;
					
				}
				
			}
			
			public final List<ProviderItem> getItems () {
				return items;
			}
			
		}
		
		public final ProvidersData getProvidersData () throws StorageException {
			return new ProvidersData ();
		}
		
		public Item getItem (String... file) throws StorageException {
			return adapter.getItem (file);
		}
		
		public final void setCookies (String cookies) {
			
			putPref (KEY_COOKIES, cookies);
			applyPrefs ();
			
		}
		
		public Map<String, Object> getCookies () {
			return Net.explodeCookies (getPref (KEY_COOKIES));
		}
		
		protected Storage setUserAgent (String useragent) throws StorageException {
			
			try {
				
				config.put ("useragent", useragent);
				return this;
				
			} catch (JSONException e) {
				throw new StorageException (e);
			}
			
		}
		
		public void copy (List<Item> from, List<Item> to) throws StorageException, OutOfMemoryException {
			adapter.copy (from, to);
		}
		
		public void move (List<Item> from, List<Item> to) throws StorageException, OutOfMemoryException {
			adapter.move (from, to);
		}
		
	}