	package upl.storage.adapters;
	
	import upl.http.HttpMethod;
	import upl.json.JSONArray;
	import upl.json.JSONException;
	import upl.json.JSONObject;
	
	import java.io.UnsupportedEncodingException;
	import java.net.MalformedURLException;
	import java.net.URL;
	
	import upl.util.ArrayList;
	import upl.util.LinkedHashMap;
	import upl.util.List;
	import upl.util.Map;
	
	import upl.http.HttpRequest;
	import upl.core.Int;
	import upl.core.Net;
	import upl.core.exceptions.HttpRequestException;
	import upl.core.exceptions.OutOfMemoryException;
	import upl.storage.Adapter;
	import upl.storage.Item;
	import upl.storage.Storage;
	import upl.storage.StorageException;
	
	public class Dropbox extends Adapter {
		
		public Dropbox () {
		}
		
		protected Dropbox (Storage storager) throws StorageException {
			
			super (storager);
			setUseragent ("api-explorer-client");
			
		}
		
		@Override
		public Adapter newInstance (Storage storager) throws StorageException {
			return new Dropbox (storager);
		}
		
		@Override
		public String getApiUrl () {
			return "https://api.dropboxapi.com/2";
		}
		
		@Override
		public String getName () {
			return "dropbox";
		}
		
		@Override
		public String getVersion () {
			return "1.3";
		}
		
		@Override
		public String getTitle () {
			return "Dropbox";
		}
		
		protected String token;
		
		protected HttpRequest request (String url) throws StorageException {
			return request (HttpMethod.POST, url, new LinkedHashMap<> ());
		}
		
		protected HttpRequest request (HttpMethod method, String url, Map<String, Object> params) throws StorageException {
			
			try {
				
				HttpRequest request = initRequest (new HttpRequest (method, url).setParams (params));
				
				request.setUserAgent (storage.adapter.getUserAgent ());
				
				HttpRequest.Bearer bearer = new HttpRequest.Bearer ();
				
				if (token == null) token = storage.getPref ("access_token");
				
				bearer.setToken (token);
				
				request.setAuth (bearer);
				
				request.isJSON (true);
				
				return request;
				
			} catch (HttpRequestException e) {
				throw new StorageException (storage, e);
			}
			
		}
		
		@Override
		public boolean isAuthenticated () throws StorageException {
			return storage.hasPref ("access_token");
		}
		
		@Override
		public String getAuthUrl () throws StorageException {
			
			try {
				
				Map<String, Object> data = new LinkedHashMap<> ();
				
				data.add ("response_type", "token");
				data.add ("client_id", storage.config.getString ("key"));
				data.add ("redirect_uri", storage.config.getString ("redirect_url"));
				data.add ("force_reapprove", true);
				
				return "https://www.dropbox.com/oauth2/authorize" + Net.urlQueryEncode (data);
				
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
			return "/";
		}
		
		protected class File extends Item {
			
			protected File (Storage storage, String... items) {
				super (storage, items);
			}
			
			@Override
			public long getSize () throws StorageException {
				
				try {
					return getInfo ().getLong ("size");
				} catch (JSONException e) {
					throw new StorageException (storage, e);
				}
				
			}
			
			@Override
			public List<Item> list (int mode) throws StorageException, OutOfMemoryException {
				
				if (files == null) {
					
					files = new ArrayList<> ();
					
					try {
						
						JSONArray items = _list ();
						
						for (int i = 0; i < Int.size (items); ++i) {
							
							JSONObject data = items.getJSONObject (i);
							
							Item item = getItem (data.getString ("path_display")).toItem (data);
							
							//.isImage (data.has ("media_info") && data.getJSONObject ("media_info").getJSONObject ("metadata").getString (".tag").equals ("photo")) Deprecated since 2th Dec 2019
							
							if (item.show (mode)) files.add (item);
							
						}
						
					} catch (JSONException e) {
						throw new StorageException (this, e);
					}
					
				}
				
				return files;
				
			}
			
			@Override
			public List<Item> thumbsList () throws StorageException, OutOfMemoryException {
				
				try {
					
					List<Item> output = new ArrayList<> ();
					
					HttpRequest request = request ("https://content.dropboxapi.com/2/files/get_thumbnail_batch");
					
					JSONArray entries = new JSONArray ();
					
					JSONArray items = _list ();
					
					int num = 0;
					
					for (int i = 0; i < Int.size (items); ++i) {
						
						JSONObject data = items.getJSONObject (i);
						
						if (!toItem (data).isDir) {
							
							num++;
							
							if (num == 1) {
								
								JSONObject data2 = new JSONObject ();
								
								data2.put ("path", data.getString ("path_display"));
								data2.put ("format", "jpeg");
								data2.put ("size", "w256h256");
								data2.put ("mode", "bestfit");
								
								entries.put (data2);
								
							} else break;
							
						}
						
					}
					
					JSONObject data = new JSONObject ();
					
					data.put ("entries", entries);
					
					request.send (data);
					
					String content = request.getContent ();
					
					if (content.startsWith ("Error"))
						throw new StorageException (this, content);
					else {
						
						data = new JSONObject (content);
						
						if (!data.has ("error_summary")) {
							
							entries = data.getJSONArray ("entries");
							
							for (int i = 0; i < Int.size (entries); i++) {
								
								data = entries.getJSONObject (i);
								
								if (data.has ("metadata")) {
									
									JSONObject metadata = data.getJSONObject ("metadata");
									
									output.add (getItem (metadata.getString ("path_display"))
																.setContent (data.getString ("thumbnail"))
																.setSize (metadata.getLong ("size")));
									
								}
								
							}
							
						} else throw new StorageException (this, setError (data));
						
					}
					
					return output;
					
				} catch (JSONException | HttpRequestException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			protected JSONObject dimen;
			
			protected JSONObject getDimen () throws StorageException, JSONException {
				
				if (dimen == null)
					dimen = getInfo ().getJSONObject ("media_info").getJSONObject ("metadata").getJSONObject ("dimensions");
				return dimen;
				
			}
			
			@Override
			public int getWidth () throws StorageException {
				
				try {
					return getDimen ().getInt ("width");
				} catch (JSONException e) {
					throw new StorageException (storage, e);
				}
				
			}
			
			@Override
			public int getHeight () throws StorageException {
				
				try {
					return getDimen ().getInt ("height");
				} catch (JSONException e) {
					throw new StorageException (storage, e);
				}
				
			}
			
			@Override
			public JSONObject getInfo () throws StorageException {
				
				try {
					
					if (info == null) {
						
						HttpRequest request = request (getApiUrl () + "/files/get_metadata");
						
						JSONObject data = new JSONObject ();
						
						if (toString ().equals ("")) setFile ();
						
						data.put ("path", this);
						data.put ("include_media_info", true);
						data.put ("include_deleted", false);
						data.put ("include_has_explicit_shared_members", false);
						
						request.send (data);
						
						String content = request.getContent ();
						
						if (content.startsWith ("Error"))
							throw new StorageException (this, content);
						else {
							
							info = new JSONObject (content);
							
							if (info.has ("error_summary"))
								throw new StorageException (this, setError (info));
							
						}
						
					}
					
					return info;
					
				} catch (JSONException | OutOfMemoryException | HttpRequestException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			@Override
			public URL getDirectLink () throws StorageException, OutOfMemoryException {
				
				try {
					
					if (directUrl == null) {
						
						HttpRequest request = request (getApiUrl () + "/files/get_temporary_link");
						
						JSONObject data = new JSONObject ();
						
						data.put ("path", this);
						
						request.send (data);
						
						String content = request.getContent ();
						
						if (content.startsWith ("Error"))
							throw new StorageException (this, content);
						else
							directUrl = new URL (new JSONObject (content).getString ("link"));
						
					}
					
					return directUrl;
					
				} catch (JSONException | HttpRequestException | MalformedURLException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			
			@Override
			public boolean makeDir (boolean force) throws StorageException {
				
				try {
					
					if (!isExists) {
						
						HttpRequest request = request (getApiUrl () + "/files/create_folder_v2");
						
						JSONObject data = new JSONObject ();
						
						data.put ("path", this);
						data.put ("autorename", false);
						
						request.send (data);
						
						JSONObject output = new JSONObject (request.getContent ());
						
						if (!output.has ("error_summary"))
							return 1;
						else
							throw new StorageException (storage, setError (output));
						
					} else return 2;
					
				} catch (JSONException | HttpRequestException | OutOfMemoryException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			@Override
			public Item put (Item remoteItem, boolean force, boolean makeDir) throws StorageException {
				
				try {
					
					HttpRequest request = request ("https://content.dropboxapi.com/2/files/upload");
					
					JSONObject data2 = new JSONObject ();
					
					data2.put ("path", this);
					data2.put ("autorename", false);
					data2.put ("mute", false);
					
					JSONObject mode = new JSONObject ();
					if (force) mode.put (".tag", "overwrite");
					
					data2.put ("mode", mode);
					
					request.setHeader ("Dropbox-API-Arg", data2);
					request.setContentType ("application/octet-stream");
					
					request.send (remoteItem.getStream ());
					
					JSONObject output = new JSONObject (request.getContent ());
					
					if (output.has ("error_summary"))
						throw new StorageException (remoteItem, setError (output));
					
					return remoteItem;
					
				} catch (JSONException | HttpRequestException | OutOfMemoryException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			protected JSONArray _list () throws StorageException, OutOfMemoryException {
				
				try {
					
					HttpRequest request = request (getApiUrl () + "/files/list_folder");
					
					if (toString ().equals ("")) setFile ();
					
					JSONObject data = new JSONObject ();
					
					data.put ("path", this);
					data.put ("recursive", false);
					data.put ("include_media_info", true);
					data.put ("include_deleted", false);
					data.put ("include_has_explicit_shared_members", false);
					data.put ("include_mounted_folders", true);
					
					request.send (data);
					
					if (request.getStatus () != 400) {
						
						String content = request.getContent ();
						
						if (content.startsWith ("Error"))
							throw new StorageException (this, content);
						else
							return new JSONObject (content).getJSONArray ("entries");
						
					} else throw new StorageException (this, request.getContent ());
					
				} catch (JSONException | HttpRequestException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			@Override
			public void delete (boolean trash) throws StorageException {
				
				try {
					
					if (isDir) {
						
						JSONArray items = _list ();
						
						for (int i = 0; i < Int.size (items); ++i) {
							
							JSONObject data = items.getJSONObject (i);
							Item item2 = getItem (data.getString ("path_display"));
							
							if (!data.getString (".tag").equals ("folder"))
								deleteFile (item2, trash);
							else
								item2.delete (trash);
							
						}
						
					}
					
					deleteFile (this, trash);
					
				} catch (JSONException | OutOfMemoryException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			protected void deleteFile (Item item, boolean trash) throws StorageException {
				
				try {
					
					if (item.toItem (item.getInfo ()).isExists) {
						
						HttpRequest request = request (getApiUrl () + "/files/delete_v2");
						
						JSONObject data = new JSONObject ();
						
						data.put ("path", item);
						
						request.send (data);
						
						JSONObject output = new JSONObject (request.getContent ());
						
						if (output.has ("error_summary"))
							throw new StorageException (storage, setError (output));
						
					}
					
				} catch (JSONException | HttpRequestException | OutOfMemoryException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			@Override
			public void move (Item to) throws StorageException, OutOfMemoryException {
				
				try {
					
					List<Item> from = new ArrayList<> ();
					
					from.add (this);
					
					List<Item> to2 = new ArrayList<> ();
					
					to2.add (to);
					
					JSONObject data = new JSONObject ();
					
					data.put ("allow_ownership_transfer", false);
					
					_move ("move_batch_v2", data, from, to2);
					
				} catch (JSONException | HttpRequestException | OutOfMemoryException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			@Override
			public Item toItem (JSONObject file) throws StorageException {
				
				try {
					
					isExists = file.has (".tag");
					
					if (isExists)
						isDir (file.getString (".tag").equals ("folder"));
					
					return this;
					
				} catch (JSONException e) {
					throw new StorageException (this, e);
				}
				
			}
			
		}
		
		@Override
		public JSONObject getUserData (String token) throws StorageException {
			
			try {
				
				this.token = token;
				
				HttpRequest request = request (getApiUrl () + "/users/get_current_account");
				
				request.send ((JSONObject) null);
				
				JSONObject output = new JSONObject (request.getContent ());
				
				if (!output.has ("error_summary")) {
					
					JSONObject user = output.getJSONObject ("name");
					
					JSONObject userData = new JSONObject ();
					
					userData.put (Storage.USER_NAME, output.getString ("email"));
					userData.put (Storage.USER_FULLNAME, user.getString ("display_name"));
					userData.put (Storage.USER_AVATAR, output.getString ("profile_photo_url"));
					
					return userData;
					
				} else throw new StorageException (storage, setError (output));
				
			} catch (JSONException | HttpRequestException | OutOfMemoryException e) {
				throw new StorageException (storage, e);
			}
			
		}
		
		protected void _move (String url, JSONObject data, List<Item> from, List<Item> to) throws StorageException, JSONException, HttpRequestException, OutOfMemoryException {
			
			HttpRequest request = request (getApiUrl () + "/files/" + url);
			
			JSONArray entries = new JSONArray ();
			
			for (int i = 0; i < Int.size (from); ++i) {
				
				JSONObject entry = new JSONObject ();
				
				Item fromItem = from.get (i), toItem = to.get (i);
				
				String path;
				
				if (toItem.isDir)
					path = prepRemoteFile (toItem + "/" + fromItem.getName ());
				else
					path = prepRemoteFile (toItem.toString ());
				
				entry.put ("from_path", fromItem);
				entry.put ("to_path", path);
				
				entries.put (entry);
				
			}
			
			data.put ("entries", entries);
			data.put ("autorename", false);
			
			request.send (data);
			
			JSONObject output = new JSONObject (request.getContent ());
			
			if (output.has ("error_summary"))
				throw new StorageException (storage, output);
			
		}
		
		@Override
		public void copy (List<Item> from, List<Item> to) throws StorageException, OutOfMemoryException {
			
			try {
				_move ("copy_batch_v2", new JSONObject (), from, to);
			} catch (JSONException | HttpRequestException | OutOfMemoryException e) {
				throw new StorageException (storage, e);
			}
			
		}
		
		@Override
		public void move (List<Item> from, List<Item> to) throws StorageException, OutOfMemoryException {
			
			try {
				
				JSONObject data = new JSONObject ();
				
				data.put ("allow_ownership_transfer", false);
				
				_move ("move_batch_v2", data, from, to);
				
			} catch (JSONException | HttpRequestException | OutOfMemoryException e) {
				throw new StorageException (from.get (0), e);
			}
			
		}
		
		@Override
		public String setError (JSONObject output) {
			
			try {
				
				return output.getString ("error_summary");
				//return output.toString ();
				
			} catch (JSONException e) {
				return e.getMessage ();
			}
			
		}
		
		@Override
		public Item getItem (String... remoteFile) {
			return new File (storage, remoteFile);
		}
		
	}