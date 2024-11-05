	package upl.storage.adapters;
	/*
	 Created by Acuna on 15.05.2018
	*/
	
	import com.jcraft.jsch.ChannelSftp;
	import com.jcraft.jsch.JSch;
	import com.jcraft.jsch.JSchException;
	import com.jcraft.jsch.Session;
	import com.jcraft.jsch.SftpATTRS;
	import com.jcraft.jsch.SftpException;
	
	import upl.core.Arrays;
	import upl.core.Int;
	import upl.exceptions.OutOfMemoryException;
	import upl.json.JSONException;
	import upl.json.JSONObject;
	import upl.io.BufferedInputStream;
	import upl.util.ArrayList;
	import upl.util.List;
	import upl.util.Map;
	import upl.storage.Adapter;
	import upl.storage.Item;
	import upl.storage.Storage;
	import upl.storage.StorageException;
	
	import java.io.ByteArrayOutputStream;
	import java.io.IOException;
	import java.io.OutputStream;
	import java.io.PipedInputStream;
	import java.io.PipedOutputStream;
	import java.net.MalformedURLException;
	import java.net.URL;
	import java.util.Properties;
	import java.util.Vector;
	
	public class SFTP extends Adapter {
		
		private Session session;
		private ChannelSftp channel;
		
		final public static String SERVER = "server";
		final public static String PORT = "port";
		final public static String USER = "username";
		final public static String PASSWORD = "password";
		final public static String PATH = "remote_root_path";
		final public static String TIMEOUT = "timeout";
		
		public SFTP () {}
		
		private SFTP (Storage storager) throws StorageException {
			super (storager);
		}
		
		@Override
		public Adapter newInstance (Storage storager) throws StorageException {
			return new SFTP (storager);
		}
		
		@Override
		public String getName () {
			return "sftp";
		}
		
		@Override
		public String getVersion () {
			return "1.0";
		}
		
		@Override
		public String getTitle () {
			return "SFTP";
		}
		
		@Override
		public JSONObject getUserData (String token) throws StorageException {
			
			try {
				
				JSONObject userData = new JSONObject ();
				
				userData.put (Storage.USER_NAME, storage.config.getString (USER));
				
				return userData;
				
			} catch (JSONException e) {
				throw new StorageException (storage, e);
			}
			
		}
		
		@Override
		public void renewToken () throws StorageException {
			
			try {
				
				if (Int.size (storage.config) > 0) {
					
					Map<String, Object> defData = storage.getDefData ();
					
					if ((storage.config.getInt (PORT) <= 0) && (int) defData.get (PORT) > 0)
						storage.config.put (PORT, (int) defData.get (PORT));
					
					session = new JSch ().getSession (storage.config.getString (USER), storage.config.getString (SERVER), storage.config.getInt (PORT));
					
					if (storage.config.getString (PASSWORD) != null && !storage.config.getString (PASSWORD).equals (""))
						session.setPassword (storage.config.getString (PASSWORD));
					
					Properties properties = new Properties ();
					
					properties.put ("StrictHostKeyChecking", "no");
					properties.put ("PreferredAuthentications", "password");
					
					session.setConfig (properties);
					
					storage.config.put (TIMEOUT, Int.correct (storage.config.getInt (TIMEOUT), (int) defData.get (TIMEOUT)));
					
					session.connect (storage.config.getInt (TIMEOUT) * 1000);
					
					if (storage.config.getString (PATH) == null || storage.config.getString (PATH).equals (""))
						storage.config.put (PATH, "/");
					
					channel = (ChannelSftp) session.openChannel ("sftp");
					channel.connect ();
					
				}
				
			} catch (JSONException | JSchException e) {
				throw new StorageException (storage, e);
			}
			
		}
		
		@Override
		public boolean isAuthenticated () throws StorageException {
			return channel.isConnected ();
		}
		
		@Override
		public Map<String, Object> setDefData (Map<String, Object> data) {
			
			data.add (SERVER, "");
			data.add (PORT, 22);
			data.add (USER, "");
			data.add (PASSWORD, "");
			data.add (PATH, "");
			data.add (TIMEOUT, 10);
			
			return data;
			
		}
		
		@Override
		public String[] getDecryptedKeys () {
			return new String[] {USER};
		}
		
		@Override
		public String getRootDir () throws StorageException {
			
			try {
				return storage.config.getString (PATH);
			} catch (JSONException e) {
				throw new StorageException (storage, e);
			}
			
		}
		
		private class File extends Item {
			
			private File (Storage storage, String... remoteFile) {
				super (storage, remoteFile);
			}
			
			@Override
			public JSONObject getInfo () throws StorageException {
				return new JSONObject ();
			}
			
			@Override
			public long getSize () throws StorageException {
				return toItem (getInfo ()).getSize ();
			}
			
			@Override
			public List<Item> list (int mode) throws StorageException, OutOfMemoryException {
				
				List<Item> files = new ArrayList<> ();
				
				try {
					
					if (toItem (getInfo ()).isDir) {
						
						Vector<?> entries = channel.ls (getFile ());
						
						for (int i = 0; i < Int.size (entries); ++i) {
							
							ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) entries.get (i);
							
							String name = entry.getFilename ();
							
							if (!name.equals (".") && !name.equals ("..")) {
								
								Item item2 = getItem (getShortFile (), name)
													     .isDir (entry.getAttrs ().isDir ())
													     .setDirectLink (new URL (entry.toString ()));
								
								if (item2.show (mode)) files.add (item2);
								
							}
							
						}
						
					}
					
				} catch (SftpException | MalformedURLException e) {
					throw new StorageException (this, e);
				}
				
				return files;
				
			}
			
			@Override
			public URL getDirectLink () throws StorageException {
				
				try {
					
					if (directUrl == null) directUrl = new URL (getFile ()); // TODO
					return directUrl;
					
				} catch (MalformedURLException e) {
					throw new StorageException (storage, e);
				}
				
			}
			
			@Override
			public BufferedInputStream getStream (String type) throws StorageException {
				
				try {
					
					OutputStream outputStream = new ByteArrayOutputStream ();
					channel.get (getFile (), outputStream);
					
					//return new BufferedInputStream (outputStream).copy ();
					return null; // TODO
					
				} catch (SftpException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			@Override
			public Item toItem (JSONObject file) throws StorageException {
				
				try {
					
					isExists = (getAttrs (getFile ()) != null);
					
					if (isExists)
						isDir (getAttrs (getFile ()).isDir ());
					
				} catch (SftpException e) {
					
					if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE)
						throw new StorageException (this, e); // Вообще ничего не смогли получить
					
				}
				
				return this;
				
			}
			
			private SftpATTRS getAttrs (String remoteFile) throws SftpException {
				return channel.lstat (remoteFile);
			}
			
			@Override
			public void chmod (int chmod) throws StorageException {
				
				try {
					channel.chmod (chmod, toString ());
				} catch (SftpException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			@Override
			public boolean makeDir (boolean force) throws StorageException {
				
				try {
					
					if (!toItem (getInfo ()).isExists) {
						
						List<String> parts = Arrays.explode ("/", getFile ());
						
						String path = "";
						
						for (int i = 0; i < Int.size (parts); ++i) {
							
							if (i > 0) path += "/";
							path += parts.get (i);
							
							Item item = getItem (path);
							
							if (!item.toItem (item.getInfo ()).isDir)
								channel.mkdir (path);
							
						}
						
						return 1;
						
					} else return 2;
					
				} catch (SftpException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			@Override
			public Item put (Item remoteItem, boolean force, boolean makeDir) throws StorageException, OutOfMemoryException {
				
				try {
					
					remoteItem = remoteItem.toItem (remoteItem.getInfo ());
					
					if (remoteItem.force (force)) {
						
						if (makeDir) remoteItem.getParent ().makeDir ();
						channel.put (getStream (), remoteItem.getFile (), ChannelSftp.OVERWRITE);
						
					}
					
					return remoteItem;
					
				} catch (SftpException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			@Override
			public void close () throws StorageException {
				
				if (session != null) session.disconnect ();
				if (channel != null) channel.quit ();
				
			}
			
			@Override
			public void delete (boolean trash) throws StorageException {
				
				try {
					
					if (toItem (getInfo ()).isDir) {
						
						Vector<?> entries = channel.ls (getFile ());
						
						for (int i = 0; i < Int.size (entries); ++i) {
							
							ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) entries.get (i);
							
							String name = entry.getFilename ();
							
							if (!name.equals (".") && !name.equals ("..")) {
								
								Item item2 = getItem (getFile () + "/" + name);
								
								if (!item2.toItem (item2.getInfo ()).isDir)
									deleteFile (item2);
								else
									item2.delete (trash);
								
							}
							
						}
						
						channel.rmdir (getFile ());
						
					} else deleteFile (this);
					
				} catch (SftpException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			private void deleteFile (Item item) throws StorageException {
				
				try {
					channel.rm (item.getFile ());
				} catch (SftpException e) {
					throw new StorageException (this, e);
				}
				
			}
			
			@Override
			public void move (Item to) throws StorageException, OutOfMemoryException {
				//throw new NotImplementedException (); //TODO
			}
			
		}
		
		@Override
		public void copy (List<Item> from, List<Item> to) throws StorageException {
			_copy (from, to);
		}
		
		protected void _copy (List<Item> from, List<Item> to) throws StorageException {
			
			try {
				
				ChannelSftp wChannel = (ChannelSftp) session.openChannel (getName ());
				wChannel.connect ();
				
				for (int i = 0; i < Int.size (from); ++i) {
					
					PipedInputStream in = new PipedInputStream ();
					PipedOutputStream out = new PipedOutputStream (in);
					
					channel.get (from.get (i).toString (), out);
					wChannel.put (in, to.get (i).toString ());
					
				}
				
			} catch (SftpException | JSchException | IOException e) {
				throw new StorageException (from.get (0), e);
			}
			
		}
		
		@Override
		public void move (List<Item> from, List<Item> to) throws StorageException, OutOfMemoryException {
			//throw new NotImplementedException (); //TODO
		}
		
		@Override
		public Item getItem (String... remoteFile) {
			return new File (storage, remoteFile);
		}
		
	}