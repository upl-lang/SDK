	package upl.storage.adapters;
	
	import upl.http.HttpMethod;
	import upl.json.JSONArray;
	import upl.json.JSONException;
	import upl.json.JSONObject;
	
	import java.io.IOException;
	import java.io.UnsupportedEncodingException;
	import java.net.MalformedURLException;
	import java.net.URL;
	
	import upl.util.ArrayList;
	import upl.util.LinkedHashMap;
	import upl.util.List;
	import upl.util.Map;
	
	import upl.core.Arrays;
	import upl.http.HttpRequest;
	import upl.core.Int;
	import upl.core.Log;
	import upl.core.Net;
	import upl.http.HttpRequestException;
	import upl.exceptions.OutOfMemoryException;
	import upl.io.BufferedInputStream;
	import upl.storage.Item;
	import upl.storage.Adapter;
	import upl.storage.Storage;
	import upl.storage.StorageException;
	
	public class GDrive extends Adapter {
		
		protected String[] scopes;
		protected String mimeFolder = "application/vnd.google-apps.folder";
		
		public GDrive () {
		}
		
		protected GDrive (Storage storage) throws StorageException {
			
			super (storage);
			setUseragent ("Google-API-Java-Client");
			
			scopes = new String[] {
				
				"email",
				getApiUrl () + "/auth/drive.file",
				getApiUrl () + "/auth/drive.metadata",
				
			};
			
		}
		
		@Override
		public String getVersion () {
			return "1.3";
		}
		
		@Override
		public String getApiUrl () {
			return "https://www.googleapis.com";
		}
		
		@Override
		public Adapter newInstance (Storage storage) throws StorageException {
			return new GDrive (storage);
		}
		
		@Override
		public void renewToken () throws StorageException {
			
			try {
				
				String token = storage.getPref ("refresh_token");
				
				if (!token.equals ("")) {
					
					Map<String, Object> data = new LinkedHashMap<> ();
					
					data.add ("client_id", storage.config.getString ("client_id"));
					data.add ("client_secret", storage.config.getString ("client_secret"));
					data.add ("grant_type", "refresh_token");
					data.add ("refresh_token", token);
					
					HttpRequest request = request (HttpMethod.POST, "oauth2/v4/token", new LinkedHashMap<String, Object> ());
					request.send (data);
					
					authData = new JSONObject (request.getContent ());
					
				}
				
			} catch (JSONException | HttpRequestException | StorageException | OutOfMemoryException e) {
				throw new StorageException (storage, e);
			}
			
		}
		
		@Override
		public String getName () {
			return "gdrive";
		}
		
		@Override
		public String getTitle () {
			return "Google Drive";
		}
		
		@Override
		public String getAuthUrl () throws StorageException {
			
			try {
				
				Map<String, Object> data = new LinkedHashMap<> ();
				
				data.add ("client_id", storage.config.getString ("client_id"));
				data.add ("response_type", "code");
				data.add ("scope", Arrays.implode (" ", scopes));
				data.add ("redirect_uri", getRedirectUrl ());
				data.add ("prompt", "consent");
				data.add ("access_type", "offline");
				
				return "https://accounts.google.com/o/oauth2/auth" + Net.urlQueryEncode (data);
				
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
			
			try {
				return getAppsFolderTitle () + "/" + storage.config.getString ("app_name");
			} catch (JSONException e) {
				throw new StorageException (storage, e);
			}
			
		}
		
		@Override
		public boolean isAuthenticated () throws StorageException {
			return storage.hasPref ("access_token");
		}
		
		@Override
		public JSONObject getUserData (String token) throws StorageException {
			
			try {
				
				Map<String, Object> params = new LinkedHashMap<> ();
				
				params.add ("access_token", token);
				
				HttpRequest request = new HttpRequest (HttpMethod.GET, getApiUrl () + "/oauth2/v3/userinfo").setParams (params);
				request.setUserAgent (storage.adapter.getUserAgent ());
				
				JSONObject data = new JSONObject (request.getContent ());
				
				if (request.isOK ()) {
					
					JSONObject userData = new JSONObject ();
					
					userData.put (Storage.USER_NAME, data.getString ("email"));
					
					return userData;
					
				} else throw new StorageException (storage, data);
				
			} catch (JSONException | HttpRequestException | OutOfMemoryException e) {
				throw new StorageException (storage, e);
			}
			
		}
		
		protected HttpRequest request (HttpMethod method, String url, Map<String, Object> params) throws StorageException {
			
			HttpRequest request = initRequest (new HttpRequest (method, getApiUrl () + "/" + url).setParams (params));
			
			request.setUserAgent (storage.adapter.getUserAgent ());
			request.setHeader ("Authorization", storage.getPref ("token_type") + " " + storage.getPref ("access_token"));
			request.isJSON (true);
			
			return request;
			
		}
		
		protected JSONArray fileMetadata (String remoteFile, String folderId) throws StorageException {
			
			List<String> query = new ArrayList<> ();
			
			query.add ("name = '" + remoteFile + "'");
			query.add ("mimeType != '" + mimeFolder + "'");
			
			return _list (query, folderId, "id, webContentLink");
			
		}
		
		protected JSONArray dirMetadata (String remoteFile, String folderId) throws StorageException {
			return dirMetadata (remoteFile, folderId, false);
		}
		
		protected JSONArray dirMetadata (String remoteFile, String folderId, boolean trash) throws StorageException {
			
			List<String> query = new ArrayList<> ();
			
			query.add ("name = '" + remoteFile + "'");
			query.add ("mimeType = '" + mimeFolder + "'");
			
			return _list (query, folderId, "id", trash);
			
		}
		
		protected JSONArray filesMetadata (String remoteFile, String folderId) throws StorageException {
			
			List<String> query = new ArrayList<> ();
			
			query.add ("name = '" + remoteFile + "'");
			
			return _list (query, folderId, "id, name, webContentLink");
			
		}
		
		protected JSONArray _list (List<String> query, String folderId, String fields) throws StorageException {
			return _list (query, folderId, fields, false);
		}
		
		protected JSONArray _list (List<String> query, String folderId, String fields, boolean trash) throws StorageException {
			
			if (!folderId.equals ("")) query.add ("'" + folderId + "' IN parents");
			if (trash) query.add ("trashed = false"); // Если удаляем в корзину, то ее не обходим при получении списка файлов на удаление
			
			try {
				
				Map<String, Object> params = new LinkedHashMap<> ();
				
				params.add ("fields", "files(" + fields + ")");
				params.add ("q", query.implode (" AND "));
				
				HttpRequest request = request (HttpMethod.GET, "drive/v3/files", params);
				JSONObject result = new JSONObject (request.getContent ());
				
				if (result.has ("error"))
					throw new StorageException (storage, result);
				else
					return result.getJSONArray ("files");
				
			} catch (JSONException | HttpRequestException | OutOfMemoryException e) {
				throw new StorageException (storage, e);
			}
			
		}
		
		protected class File extends Item {
			
			protected File (Storage storage, String... remoteFile) {
				super (storage, remoteFile);
			}
			
			@Override
			public List<Item> list (int mode) throws StorageException, OutOfMemoryException {
				
				try {
					
					renewToken ();
					
					String folderId = "";
					JSONArray filesMetadata;
					
					List<String> parts = Arrays.explode ("/", getFile ());
					
					for (String part : parts) {
						
						filesMetadata = dirMetadata (part, folderId);
						
						// из-за откровенно индусского кода Гугла можно с легкостью создавать файлы с одинаковыми именами, поэтому если найдено два одинаковых файла или папки - выбираем первый.
						
						if (Int.size (filesMetadata) > 0)
							folderId = filesMetadata.getJSONObject (0).getString ("id");
						
					}
					
					List<Item> files = new ArrayList<> ();
					
					if (!folderId.equals ("")) {
						
						filesMetadata = _list (new ArrayList<> (), folderId, "name, mimeType, webContentLink");
						
						for (int i = 0; i < Int.size (filesMetadata); ++i) {
							
							JSONObject file = filesMetadata.getJSONObject (i);
							
							Item item2 = getItem (file.getString ("name")).toItem (file);
							
							if (!item2.isDir)
								item2.setDirectLink (new URL (file.getString ("webContentLink")));
							
							if (item2.show (mode)) files.add (item2);
							
						}
						
					}
					
					return files;
					
				} catch (JSONException | MalformedURLException e) {
					throw new StorageException (storage, e);
				}
				
			}
			
			/*@Override
			public boolean isDir () throws StorageException {
				
				if (isDir == null) {
					
					List<String> parts = Arrays.explode ("/", getFile ());
					isDir = !_getId (parts, 0).equals ("");
					
				}
				
				return isDir;
				
			}*/
			
			@Override
			public JSONObject getInfo () throws StorageException {
				
				try {
					
					if (info == null) {
						
						List<String> parts = Arrays.explode ("/", getFile ());
						String folderId = _getId (parts, 1);
						
						JSONArray filesMetadata = new JSONArray ();
						
						if (!folderId.equals (""))
							filesMetadata = fileMetadata (parts.get (parts.endKey ()), folderId);
						
						if (Int.size (filesMetadata) > 0)
							info = filesMetadata.getJSONObject (0);
						else
							throw new StorageException ("File " + getFile () + " not found on server");
						
					}
					
					return info;
					
				} catch (JSONException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			@Override
			public URL getDirectLink () throws StorageException {
				
				try {
					
					if (directUrl == null) directUrl = new URL (getInfo ().getString ("webContentLink"));
					return directUrl;
					
				} catch (JSONException | MalformedURLException e) {
					throw new StorageException (storage, e);
				}
				
			}
			
			@Override
			public Item toItem (JSONObject file) throws StorageException {
				
				try {
					
					List<String> parts = Arrays.explode ("/", getFile ());
					String folderId = _getId (parts, 1);
					
					JSONArray filesMetadata = new JSONArray ();
					
					if (!folderId.equals (""))
						filesMetadata = filesMetadata (parts.get (parts.endKey ()), folderId);
					
					isExists = (filesMetadata.length () > 0);
					
					if (isExists)
						isDir (file.getString ("mimeType").equals (mimeFolder));
					
					return this;
					
				} catch (JSONException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			protected String _getId (List<String> parts, int offset) throws StorageException {
				
				try {
					
					String folderId = "";
					
					for (int i = 0; i < (Int.size (parts) - offset); ++i) {
						
						JSONArray filesMetadata = dirMetadata (parts.get (i), folderId);
						
						if (Int.size (filesMetadata) > 0)
							folderId = filesMetadata.getJSONObject (0).getString ("id");
						else
							folderId = "";
						
					}
					
					return folderId;
					
				} catch (JSONException e) {
					throw new StorageException (storage, e);
				}
				
			}
			
			protected String _makeDir (String name, String parentId) throws StorageException {
				
				try {
					
					JSONObject data = new JSONObject ();
					
					JSONArray folders = dirMetadata (name, parentId);
					HttpRequest request;
					
					Map<String, Object> params = new LinkedHashMap<> ();
					
					params.add ("uploadType", "multipart");
					
					if (Int.size (folders) > 0) {
						
						//params.put ("addParents", Arrays.implode (",", addParents));
						//params.put ("removeParents", Arrays.implode (",", removeParents));
						
						JSONObject folder = folders.getJSONObject (0);
						//file.setModifiedTime (Locales.date (0));
						
						params.add ("fileId", folder.getString ("id"));
						
						request = request (HttpMethod.PATCH, "drive/v3/files/" + folder.getString ("id"), params);
						
					} else {
						
						data.put ("name", name);
						data.put ("mimeType", mimeFolder);
						
						if (!parentId.equals (""))
							data.put ("parents", new JSONArray ().put (parentId));
						
						request = request (HttpMethod.POST, "drive/v3/files", params);
						
					}
					
					data.put ("fields", "id");
					
					request.send (data);
					
					String output = request.getContent ();
					
					if (request.isOK ())
						return new JSONObject (output).getString ("id");
					else
						throw new StorageException (storage, output);
					
				} catch (JSONException | HttpRequestException | OutOfMemoryException e) {
					throw new StorageException (storage, e);
				}
				
			}
			
			protected String _makeDir (Item item) throws StorageException {
				
				List<String> parts = Arrays.explode ("/", item.getFile ());
				
				String folderId = "";
				
				for (int i = 0; i < Int.size (parts); ++i)
					folderId = _makeDir (parts.get (i), folderId);
				
				return folderId;
				
			}
			
			@Override
			public boolean makeDir (boolean force) throws StorageException {
				
				if (!isExists) {
					
					_makeDir (this);
					return 1;
					
				} else return 2;
				
			}
			
			@Override
			public Item put (Item remoteItem, boolean force, boolean makeDir) throws StorageException {
				
				try {
					
					JSONObject data2 = new JSONObject ();
					String folderId = _makeDir (getItem (getParent ().toString ()));
					
					JSONArray fileMetadata = fileMetadata (remoteItem.getName (), folderId);
					boolean isExists = (Int.size (fileMetadata) > 0); // TODO
					
					if (!isExists || force) {
						
						if (isExists) {
							
							JSONObject file = fileMetadata.getJSONObject (0);
							//file.setModifiedTime (new DateTime (System.currentTimeMillis ()));
											
											/*gdriveApi.files ().update (file, new GDriveAPI.File (), stream)
													     .setFields ("id, modifiedTime")
													     .execute ();*/
							
						} else {
							
							data2.put ("name", remoteItem.getName ());
							
							if (!folderId.equals (""))
								data2.put ("parents", new JSONArray ().put (folderId));
							
							Map<String, Object> params = new LinkedHashMap<> ();
							
							params.add ("uploadType", "resumable");
							
							HttpRequest request = request (HttpMethod.POST, "upload/drive/v3/files", params);
							
							//request.setHeader ("X-Upload-Content-Type", data2.optString ("mimeType"));
							request.setHeader ("X-Upload-Content-Length", Int.size (getStream ())); // TODO
							//request.setHeader ("Content-Length", item.getSize ());
							
							request.send (data2);
							
							String output = request.getContent ();
							
							if (request.isOK ()) {
								
								String sessionUrl = request.getHeader ("Location");
								
								if (sessionUrl != null) {
									
									data2 = upload (getStream (), data2, sessionUrl, 0, Int.size (getStream ()));
									
									params = new LinkedHashMap<> ();
									
									params.add ("fileId", data2.getString ("id"));
									params.add ("transferOwnership", false);
									
									request = request (HttpMethod.POST, "drive/v3/files/" + data2.getString ("id") + "/permissions", params);
									
									data2 = new JSONObject ();
									
									data2.put ("role", "reader");
									data2.put ("type", "anyone");
									
									request.send (data2);
									
									if (!request.isOK ())
										throw new StorageException (storage, request.getContent ());
									
								} else throw new StorageException (storage, output);
								
							} else throw new StorageException (storage, output);
							
						}
						
					}
					
					return remoteItem;
					
				} catch (IOException | HttpRequestException | JSONException | OutOfMemoryException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			protected long uploadedBytes = (100 * 1024 * 1024);
			
			protected JSONObject upload (BufferedInputStream stream, JSONObject file, String sessionUrl, long chunkStart, long total) throws StorageException {
				
				JSONObject result = new JSONObject ();
				
				try {
					
					HttpRequest request = request (HttpMethod.PUT, sessionUrl, null);
					
					if ((chunkStart + uploadedBytes) > total)
						uploadedBytes = total - chunkStart;
					
					request.setHeader ("Content-Type", file.optString ("mimeType"));
					request.setHeader ("Content-Length", uploadedBytes);
					request.setHeader ("Content-Range", "bytes " + chunkStart + "-" + (chunkStart + uploadedBytes - 1) + "/" + total);
					Log.w ("bytes " + chunkStart + "-" + (chunkStart + uploadedBytes - 1) + "/" + total);
						/*if (stream instanceof FileInputStream) {
							
							byte[] buffer = new byte[(int) uploadedBytes];
							
							FileInputStream fileInputStream = (FileInputStream) stream;
							fileInputStream.getChannel ().position (chunkStart);
							
							if (fileInputStream.read (buffer, 0, (int) uploadedBytes) == -1) { }
							fileInputStream.close ();
							
							OutputStream outputStream = request.getOutputStream ();
							
							outputStream.write (buffer);
							outputStream.close ();
							
						} else */
					request.send (stream);
					
					if (request.getStatus () == 308) { // Продолжаем
						
						String range = request.getHeader ("Range");
						chunkStart = Long.parseLong (range.substring (range.lastIndexOf ("-") + 1, Int.size (range))) + 1;
						Log.w (range);
						result = upload (stream, file, sessionUrl, chunkStart, total);
						
					} else if (request.isOK ())
						result = new JSONObject (request.getContent ());
					else if (result.has ("error"))
						throw new StorageException (storage, result);
					else
						throw new StorageException (storage, request.getContent ());
					
				} catch (JSONException | HttpRequestException | OutOfMemoryException e) {
					throw new StorageException (storage, e);
				}
				
				return result;
				
			}
			
			@Override
			public void delete (boolean trash) throws StorageException {
				
				try {
					
					if (isDir) {
						
						String folderId = "";
						
						JSONArray filesMetadata;
						List<String> parts = Arrays.explode ("/", getFile ());
						
						for (int i = 0; i < Int.size (parts); ++i) {
							
							filesMetadata = dirMetadata (parts.get (i), folderId, trash);
							deleteObject (filesMetadata.getJSONObject (0).getString ("id"));
							
						}
						
					} else deleteObject (getInfo ().getString ("id"));
					
				} catch (JSONException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			protected void deleteObject (String id) throws StorageException {
				
				try {
					
					Map<String, Object> params = new LinkedHashMap<> ();
					
					params.add ("fileId", id);
					
					HttpRequest request = request (HttpMethod.DELETE, "drive/v3/files/" + id, params);
					
					String output = request.getContent ();
					
					if (!output.equals (""))
						throw new StorageException (storage, new JSONObject (output));
					
				} catch (JSONException | HttpRequestException | OutOfMemoryException e) {
					throw new StorageException (storage, e);
				}
				
			}
			
			@Override
			public void move (Item to) throws StorageException, OutOfMemoryException {
				//throw new NotImplementedException (); //TODO
			}
			
		}
		
		@Override
		public void copy (List<Item> from, List<Item> to) throws StorageException {
			//throw new NotImplementedException (); // TODO
		}
		
		@Override
		public void move (List<Item> from, List<Item> to) throws StorageException, OutOfMemoryException {
			//throw new NotImplementedException (); //TODO
		}
		
		@Override
		public String setError (JSONObject result) {
			
			try {
				return result.getJSONObject ("error").getString ("message");
			} catch (JSONException e) {
				return e.getMessage ();
			}
			
		}
		
		@Override
		public Item getItem (String... remoteFile) {
			return new File (storage, remoteFile);
		}
		
	}