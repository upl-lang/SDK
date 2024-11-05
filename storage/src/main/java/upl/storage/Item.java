	package upl.storage;
	/*
	 Created by Acuna on 18.12.2018
	*/
	
	import java.io.FileNotFoundException;
	import java.io.InputStream;
	import java.net.URL;
	
	import upl.core.Arrays;
	import upl.core.File;
	import upl.http.HttpMethod;
	import upl.http.HttpRequest;
	import upl.core.Int;
	import upl.http.HttpRequestException;
	import upl.exceptions.OutOfMemoryException;
	import upl.io.BufferedInputStream;
	import upl.json.JSONObject;
	import upl.util.ArrayList;
	import upl.util.List;
	
	public abstract class Item {
		
		public Storage storage;
		
		private int width = -1, height = -1;
		private long size = -1;
		private JSONObject id = new JSONObject ();
		public URL directUrl, thumbUrl;
		public boolean isExists = true, isDir = false;
		public boolean isImage;
		public BufferedInputStream stream;
		private String[] descr = {};
		
		protected List<Item> files;
		
		private String file = "", shortFile = "";
		public File fileObj;
		public String[] remoteFile;
		public String content;
		public JSONObject info;
		
		public Item (Storage storage, String... file) {
			
			this.storage = storage;
			remoteFile = file;
			
			if (Int.size (file) > 0) {
				
				//if (file[0] == null) file[0] = "";
				shortFile = Arrays.implode ("/", file); // /storage
				
			}
			
			setFile ();
			
		}
		
		public final Item setStream (InputStream stream) {
			return setStream (new BufferedInputStream (stream));
		}
		
		public final Item setStream (BufferedInputStream stream) {
			
			this.stream = stream;
			
			return this;
			
		}
		
		public abstract List<Item> list (int mode) throws StorageException, OutOfMemoryException;
		
		public JSONObject getInfo () throws StorageException {
			return new JSONObject ();
		}
		
		public final Item setSize (long size) {
			
			this.size = size;
			return this;
			
		}
		
		public long getSize () throws StorageException {
			return size;
		}
		
		public final Item isDir (boolean isDir) {
			
			this.isDir = isDir;
			return this;
			
		}
		
		public final Item isImage (boolean isImage) {
			
			this.isImage = isImage;
			return this;
			
		}
		
		public final boolean isImage () {
			return isImage;
		}
		
		public final Item setDirectLink (URL url) {
			
			this.directUrl = url;
			return this;
			
		}
		
		public URL getDirectLink () throws StorageException, OutOfMemoryException {
			return directUrl;
		}
		
		public final Item setThumbUrl (URL url) {
			
			this.thumbUrl = url;
			return this;
			
		}
		
		public URL getThumbUrl () throws StorageException, OutOfMemoryException {
			
			if (thumbUrl == null) thumbUrl = getDirectLink ();
			return thumbUrl;
			
		}
		
		public final Item setWidth (int size) {
			
			this.width = size;
			return this;
			
		}
		
		public final Item setHeight (int size) {
			
			this.height = size;
			return this;
			
		}
		
		public int getWidth () throws StorageException {
			return width;
		}
		
		public int getHeight () throws StorageException {
			return height;
		}
		
		public final Item setData (JSONObject data) {
			
			this.id = data;
			return this;
			
		}
		
		public JSONObject getId () {
			return id;
		}
		
		public final boolean show (int mode) throws StorageException {
			
			return (
				(mode == 1 && !isDir) || // Только файлы
					(mode == 2 && isDir) || // Только папки
					mode == 0 // Все
			);
			
		}
		
		public final void setShortFile (String file) {
			shortFile = file;
		}
		
		public void setFile () {
			
			try {
				
				file = storage.adapter.prepRemoteFile (shortFile);
				fileObj = new File (file);
				
			} catch (StorageException e) {
				// empty
			}
			
		}
		
		public final String getFile () {
			return file;
		}
		
		public final String getShortFile () {
			return shortFile;
		}
		
		public final String getName () {
			return fileObj.getName (); // TODO
		}
		
		@Override
		public String toString () {
			return getShortFile ();
		}
		
		public final Item getParent () {
			return storage.adapter.getItem (fileObj.getParent ()).isDir (true); // TODO
		}
		
		public List<Item> list () throws StorageException, OutOfMemoryException {
			return list (0);
		}
		
		public List<Item> thumbsList () throws StorageException, OutOfMemoryException {
			return list ();
		}
		
		public BufferedInputStream getThumbStream () throws StorageException, OutOfMemoryException {
			
			try {
				
				if (stream == null) {
					
					HttpRequest request = new HttpRequest (HttpMethod.GET, getThumbUrl ()).setUserAgent (storage.adapter.getUserAgent ());
					
					setStream (request.getInputStream ());
					setSize (request.getLength ());
					
				}
				
			} catch (HttpRequestException e) {
				throw new StorageException (this, e);
			}
			
			return stream;
			
		}
		
		public BufferedInputStream getStream (String type) throws StorageException, OutOfMemoryException {
			
			try {
				
				if (stream == null) {
					
					HttpRequest request = new HttpRequest (HttpMethod.GET, getDirectLink ()).setUserAgent (storage.adapter.getUserAgent ());
					
					setStream (request.getInputStream (type));
					setSize (request.getLength ());
					
				}
				
				return stream;
				
			} catch (HttpRequestException e) {
				throw new StorageException (this, e);
			}
			
		}
		
		public final Item setContent (String content) {
			
			this.content = content;
			return this;
			
		}
		
		public String getContent () {
			return content; // TODO
		}
		
		public Item setDescr (String... descr) {
			
			this.descr = descr;
			return this;
			
		}
		
		public String[] getDescr () {
			return descr;
		}
		
		public Item toItem (JSONObject data) throws StorageException {
			return this;
		}
		
		public BufferedInputStream getStream () throws StorageException, OutOfMemoryException {
			return getStream ((directUrl != null ? new File (directUrl.toString ()) : fileObj).getExtension ());
		}
		
		public List<Item> sizeList () throws StorageException, OutOfMemoryException {
			return sizeList (0);
		}
		
		public List<Item> sizeList (int size) throws StorageException, OutOfMemoryException {
			return sizeList (size, new ArrayList<> ());
		}
		
		public List<Item> sizeList (int size, List<Item> output) throws StorageException, OutOfMemoryException {
			
			for (Item file : list ()) {
				
				Item item = file.toItem (file.getInfo ());
				
				if (item.isDir)
					item.sizeList (size, output);
				else if (size == 0 || item.getSize () == size)
					output.add (file);
				
			}
			
			return output;
			
		}
		
		public abstract boolean makeDir (boolean force) throws StorageException;
		public abstract Item put (Item remoteItem, boolean force, boolean makeDir) throws StorageException, OutOfMemoryException;
		public abstract void delete (boolean trash) throws StorageException;
		public abstract void move (Item to) throws StorageException, OutOfMemoryException;
		
		public void delete () throws StorageException {
			delete (true);
		}
		
		public boolean makeDir () throws StorageException {
			return makeDir (false);
		}
		
		public final boolean force (boolean force) throws StorageException {
			return (force || !isExists);
		}
		
		public Item put (Item remoteFile) throws StorageException, OutOfMemoryException {
			return put (remoteFile, true);
		}
		
		public Item put (Item remoteFile, boolean force) throws StorageException, OutOfMemoryException {
			return put (remoteFile, force, true);
		}
		
		public Item put (URL url) throws StorageException, OutOfMemoryException {
			return put (setDirectLink (url));
		}
		
		public Item put (BufferedInputStream stream) throws StorageException, OutOfMemoryException {
			return put (setStream (stream));
		}
		
		public Item put (List<?> items) throws StorageException, OutOfMemoryException {
			return put (items.implode ()); // TODO
		}
		
		public Item put (String text) throws StorageException, OutOfMemoryException {
			return put (text, true);
		}
		
		public Item put (String text, boolean force) throws StorageException, OutOfMemoryException {
			return put (text, force, true);
		}
		
		public Item put (String text, boolean force, boolean makeDir) throws StorageException, OutOfMemoryException {
			return put (setStream (new BufferedInputStream (text.getBytes ())), force, makeDir);
		}
		
		public void put (File localFile) throws StorageException, OutOfMemoryException { // TODO makeDir
			
			try {
				put (new BufferedInputStream (localFile));
			} catch (FileNotFoundException e) {
				throw new StorageException (this, e);
			}
			
		}
		
		public void chmod (int chmod) throws StorageException {
		}
		
		public void close () throws StorageException {
		}
		
	}