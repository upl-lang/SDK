	package upl.storage.adapters;
	
	import java.io.FileNotFoundException;
	import upl.core.File;
	import upl.json.JSONException;
	import upl.json.JSONObject;
	
	import java.io.IOException;
	
	import java.net.MalformedURLException;
	import java.net.URL;
	
	import upl.io.BufferedInputStream;
	import upl.util.ArrayList;
	import upl.util.List;
	
	import upl.core.Int;
	import upl.exceptions.OutOfMemoryException;
	import upl.storage.Adapter;
	import upl.storage.Item;
	import upl.storage.Storage;
	import upl.storage.StorageException;
	
	public class SDCard extends Adapter {
		
		public SDCard () {
		}
		
		protected SDCard (Storage storager) throws StorageException {
			super (storager);
		}
		
		@Override
		public Adapter newInstance (Storage storager) throws StorageException {
			return new SDCard (storager);
		}
		
		@Override
		public String getName () {
			return "sdcard";
		}
		
		@Override
		public String getVersion () {
			return "1.0";
		}
		
		@Override
		public String getTitle () {
			return "SDCard";
		}
		
		@Override
		public boolean isAuthenticated () throws StorageException {
			return true;
		}
		
		@Override
		public String getRootDir () throws StorageException {
			
			try {
				return storage.config.getString ("folder");
			} catch (JSONException e) {
				throw new StorageException (storage, e);
			}
			
		}
		
		@Override
		public JSONObject getUserData (String token) throws StorageException {
			
			try {
				
				JSONObject userData = new JSONObject ();
				
				userData.put (Storage.USER_NAME, storage.adapter.getName ()); // TODO
				
				return userData;
				
			} catch (JSONException e) {
				throw new StorageException (storage, e);
			}
			
		}
		
		@Override
		public Item getItem (String... remoteFile) {
			return new FileItem (storage, remoteFile);
		}
		
		protected class FileItem extends Item {
			
			protected FileItem (Storage storage, String... remoteFile) {
				super (storage, remoteFile);
			}
			
			protected File destFile () {
				return new File (getShortFile ());
			}
			
			@Override
			public long getSize () throws StorageException {
				return destFile ().length ();
			}
			
			@Override
			public List<Item> list (int mode) throws StorageException, OutOfMemoryException {
				
				List<Item> output = new ArrayList<> ();
				
				String[] files = destFile ().list ();
				
				if (files != null) {
					
					for (String file : files) {
						
						Item item = getItem (getShortFile (), file);
						if (item.show (mode)) output.add (item);
						
					}
					
				}// else throw new StorageException (storage, getFile () + ": Not found or access denied", this);
				
				return output;
				
			}
			
			public JSONObject getInfo () throws StorageException {
				return new JSONObject ();
			}
			
			@Override
			public int getWidth () throws StorageException {
				
				try {
					return getInfo ().getInt ("width");
				} catch (JSONException e) {
					throw new StorageException (storage, e);
				}
				
			}
			
			@Override
			public int getHeight () throws StorageException {
				
				try {
					return getInfo ().getInt ("height");
				} catch (JSONException e) {
					throw new StorageException (storage, e);
				}
				
			}
			
			@Override
			public URL getDirectLink () throws StorageException {
				
				try {
					
					if (directUrl == null) directUrl = destFile ().toURI ().toURL ();
					return directUrl;
					
				} catch (MalformedURLException e) {
					throw new StorageException (storage, e);
				}
				
			}
			
			@Override
			public BufferedInputStream getStream (String type) throws StorageException {
				
				try {
					return (stream == null ? new BufferedInputStream (destFile ()) : stream);
				} catch (FileNotFoundException e) {
					throw new StorageException (storage, e);
				}
				
			}
			
			@Override
			public boolean makeDir (boolean force) throws StorageException {
				
				try {
					return destFile ().makeDir ();
				} catch (IOException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			@Override
			public Item put (Item remoteItem, boolean force, boolean makeDir) throws StorageException, OutOfMemoryException {
				
				remoteItem = remoteItem.toItem (remoteItem.getInfo ());
				
				try {
					if (remoteItem.force (force)) remoteItem.getStream ().copy (((FileItem) remoteItem).destFile ());
				} catch (IOException e) {
					throw new StorageException (this, e);
				}
				
				return remoteItem;
				
			}
			
			@Override
			public void delete (boolean trash) throws StorageException {
				destFile ().delete ();
			}
			
			@Override
			public void move (Item to) throws StorageException, OutOfMemoryException {
				
				put (to);
				delete (false);
				
			}
			
		}
		
		@Override
		public void copy (List<Item> from, List<Item> to) throws StorageException {
			
			for (int i = 0; i < Int.size (from); ++i) {
				
				try {
					from.get (i).put (to.get (i));
				} catch (OutOfMemoryException e) {
					throw new StorageException (from.get (0), e);
				}
				
			}
			
		}
		
		@Override
		public void move (List<Item> from, List<Item> to) throws StorageException, OutOfMemoryException {
			
			copy (from, to);
			
			for (int i = 0; i < Int.size (from); ++i)
				from.get (i).delete (false);
			
		}
		
	}