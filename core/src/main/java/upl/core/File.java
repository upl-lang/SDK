	package upl.core;
			/*
	     Created by Acuna on 17.07.2017
			*/
	
	import java.io.BufferedWriter;
	import java.io.FileNotFoundException;
	import java.io.FileOutputStream;
	import java.io.FileWriter;
	import java.io.IOException;
	import java.io.OutputStream;
	import java.io.OutputStreamWriter;
	import java.io.PrintWriter;
	
	import upl.cipher.exceptions.EncryptException;
	import upl.exceptions.ConsoleException;
	import upl.exceptions.OutOfMemoryException;
	import upl.exceptions.ZeroException;
	import upl.io.BufferedInputStream;
	import upl.type.Strings;
	import upl.util.ArrayList;
	import upl.util.HashMap;
	import upl.util.List;
	import upl.util.Map;
	
	public class File extends java.io.File {
		
		public BufferedInputStream stream;
		
		protected BufferedWriter bufferedWriter;
		public PrintWriter printWriter;
		
		public File (Object... fileName) {
			this (Arrays.implode (DS, fileName));
		}
		
		public File (List<String> fileName) {
			this (fileName.implode (DS, false));
		}
		
		public File (String file) {
			this (file, BUFFER_SIZE);
		}
		
		public File (java.io.File file) {
			super (file.toString ());
		}
		
		public File (String file, int buffer) { // TODO
			
			super (file);
			
			BUFFER_SIZE = buffer;
			
		}
		
		public static int BUFFER_SIZE = 4096;
		public static final byte[] BUFFER = new byte[BUFFER_SIZE];
		public static final String DS = separator;
		
		public String getExtension () {
			return getExtension (false);
		}
		
		public String getExtension (boolean dot) {
			
			String file = getName ();
			
			int i = file.lastIndexOf ('.');
			if (!dot) ++i;
			
			return file.substring (i);
			
		}
		
		public String getName (boolean ext) {
			
			String str = getName ();
			
			if (!ext) {
				
				int pos = str.lastIndexOf (".");
				if (pos > 0) str = str.substring (0, pos);
				
			}
			
			return str;
			
		}
		
		public BufferedInputStream getStream () throws FileNotFoundException {
			
			if (stream == null) stream = new BufferedInputStream (this);
			
			return stream;
			
		}
		
		public String read () throws IOException, OutOfMemoryException {
			return getStream ().read ("");
		}
		
		public StringBuilder read (StringBuilder str) throws IOException, OutOfMemoryException {
			return getStream ().read (str);
		}
		
		public List<String> read (List<String> output) throws IOException, OutOfMemoryException {
			return getStream ().read (output);
		}
		
		public Map<String, String> read (Map<String, String> output) throws IOException, OutOfMemoryException {
			return getStream ().read (output);
		}
		
		public String getLine () throws IOException {
			return getStream ().getLine ();
		}
		
		public boolean readLine () throws IOException {
			return getStream ().readLine ();
		}
		
		public FileOutputStream fos;
		
		public FileOutputStream getOutputStream () throws IOException {
			
			if (fos == null)
				fos = new FileOutputStream (this);
			
			return fos;
			
		}
		
		public BufferedWriter getBufferedWriter () throws IOException {
			
			if (bufferedWriter == null)
				bufferedWriter = new BufferedWriter (new OutputStreamWriter (getOutputStream ()));
			
			return bufferedWriter;
			
		}
		
		public PrintWriter getPrintWriter () throws IOException {
			
			if (printWriter == null) {
				
				if (bufferedWriter == null)
					bufferedWriter = new BufferedWriter (new FileWriter (this, true));
				
				printWriter = new PrintWriter (bufferedWriter);
				
			}
			
			return printWriter;
			
		}
		
		public enum Flag {
			
			CHECK_EXISTS,
			
		}
		
		public void write (String text) throws IOException {
			write (text, false);
		}
		
		public void write (String text, boolean newLine) throws IOException {
			
			try {
				
				getBufferedWriter ().write (text);
				
				if (newLine)
					bufferedWriter.newLine ();
				
			} finally {
				if (bufferedWriter != null) bufferedWriter.close ();
			}
			
		}
		
		public void write (byte[] bytes) throws IOException {
			getOutputStream ().write (bytes);
		}
		
		public void append (String text) throws IOException {
			append (text, false);
		}
		
		public void append (String text, boolean newLine) throws IOException {
			
			try {
				
				getPrintWriter ().println (text);
				
				if (newLine)
					printWriter.println (Strings.LS);
				
			} finally {
				if (printWriter != null) printWriter.flush ();
			}
			
		}
		
		public void write (List<?> items) throws IOException {
			
			if (Int.size (items) > 0) {
				
				try {
					
					//new File (getParent ()).makeDir ();
					
					for (java.lang.Object item : items) {
						
						getBufferedWriter ().write (item.toString ());
						getBufferedWriter ().newLine ();
						
					}
					
				} finally {
					if (bufferedWriter != null) bufferedWriter.flush ();
				}
				
			}
			
		}
		
		public void append (List<?> items) throws IOException {
			
			if (Int.size (items) > 0) {
				
				try {
					
					//new File (getParent ()).makeDir ();
					
					for (java.lang.Object item : items)
						getPrintWriter ().println (item);
					
				} finally {
					if (printWriter != null) printWriter.flush ();
				}
				
			}
			
		}
		
		public boolean makeDir () throws IOException {
			
			if (!exists ()) {
				
				if (mkdirs ())
					return true;
				else
					throw new IOException ("Can't create folder " + this);
				
			} else return false;
			
		}
		
		public String getPath2 () { // TODO
			
			List<String> parts = new Strings (toString ()).explode (DS);
			
			StringBuilder output = new StringBuilder ();
			
			if (Int.size (parts) > 1) {
				
				for (int i = 0; i < Int.size (parts) - 1; ++i) {
					
					if (i > 0) output.append (DS);
					output.append (parts.get (i));
					
				}
				
			} else output.append (getAbsolutePath ());
			
			return output.toString ();
			
		}
		
		public String getPath (int level) {
			
			String path = getAbsolutePath (), proto = "", sep = "://";
			List<String> parts = new Strings (path).explode (sep);
			
			if (Int.size (parts) > 1) {
				
				proto = parts.get (0) + sep;
				path = parts.get (1);
				
			} else path = parts.get (0);
			
			parts = new Strings (path).explode (DS);
			
			if (Int.size (parts) > 1)
				path = parts.implode (DS, 0, level);
			
			return proto + path;
			
		}
		
		protected String relativePath;
		
		public String getRelativePath (File file) {
			return getRelativePath (file.getPath ());
		}
		
		public String getRelativePath (String root) {
			
			if (relativePath == null) {
				
				relativePath = getAbsolutePath ();
				
				if (relativePath.length () > root.length ()) {
					
					relativePath = relativePath.substring (0, relativePath.length () - (getName ().length () + 1));
					
					if (relativePath.length () > root.length ())
						relativePath = relativePath.substring (root.length () + 1);
					else
						relativePath = "";
					
				} else relativePath = relativePath.substring (root.length ());
				
			}
			
			return relativePath;
			
		}
		
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		
		public static class ListListener {
			
			public void onFile (File file) throws IOException, OutOfMemoryException {
			}
			
			public void onDirectory (File file) throws IOException, OutOfMemoryException {
			}
			
			public void onError (File file, Exception e) {
			}
			
		}
		
		public static class CopyListener {
			
			public void onFile (File srcFile, File destFile) throws IOException, OutOfMemoryException {
			}
			
			public void onError (File srcFile, File destFile, Exception e) {
			}
			
		}
		
		public File list (boolean recurse, ListListener listener) throws IOException, OutOfMemoryException {
			return list (recurse, listener, false);
		}
		
		protected File list (boolean recurse, ListListener listener, boolean isFile) throws IOException, OutOfMemoryException {
			
			if (isDirectory () && !isFile) {
				
				java.io.File[] files = listFiles ();
				
				if (files != null) {
					
					listener.onDirectory (this);
					
					for (java.io.File file : files)
						new File (file).list (recurse, listener, !recurse);
					
				} else listener.onError (this, new IOException ("Can't load folder"));
				
			} else listener.onFile (this);
			
			return this;
			
		}
		
		public File copy (String destFile) throws IOException, OutOfMemoryException {
			return copy (destFile, null);
		}
		
		public File copy (File destFile) throws IOException, OutOfMemoryException {
			return copy (destFile, null);
		}
		
		public File copy (String destFile, ListListener listener) throws IOException, OutOfMemoryException {
			return copy (new File (destFile), listener);
		}
		
		public File copy (File destFile, ListListener listener) throws IOException, OutOfMemoryException {
			return copy (destFile, listener, false);
		}
		
		protected File copy (File destFile, ListListener listener, boolean delete) throws IOException, OutOfMemoryException {
			
			if (isDirectory ()) {
				
				String srcPath = getAbsolutePath ();
				String destPath = destFile.getAbsolutePath ();
				
				String[] files = list ();
				
				if (files != null) {
					
					for (String file : files)
						new File (srcPath, file)
							.copy (new File (destPath, file), listener, delete);
					
					if (delete) delete (listener);
					
				} else if (listener != null)
					listener.onError (this, new IOException ("Can't list folder"));
				
			} else {
				
				new File (destFile.getParent ()).makeDir ();
				
				OutputStream out = new FileOutputStream (destFile);
				
				getStream ().copy (out).close ();
				
				out.close ();
				
				if (delete) delete (listener);
				
				if (listener != null)
					listener.onFile (this);
				
			}
			
			return this;
			
		}
		
		public void create () throws IOException {
			createNewFile ();
		}
		
		public File move (String destFile) throws IOException, OutOfMemoryException {
			return move (destFile, null);
		}
		
		public File move (File destFile) throws IOException, OutOfMemoryException {
			return move (destFile, null);
		}
		
		public File move (String destFile, ListListener listener) throws IOException, OutOfMemoryException {
			return move (new File (destFile), listener);
		}
		
		public File move (File destFile, ListListener listener) throws IOException, OutOfMemoryException {
			return copy (new File (destFile), listener, true);
		}
		
		public File rename (String name) throws IOException, OutOfMemoryException {
			return rename (name, null);
		}
		
		public File rename (String name, ListListener listener) throws IOException, OutOfMemoryException {
			return copy (new File (getParent (), name), listener, true);
		}
		
		public boolean delete () {
			return delete (null);
		}
		
		public boolean delete (ListListener listener) {
			
			if (isDirectory ()) {
				
				java.io.File[] files = listFiles ();
				
				if (files != null) {
					
					for (java.io.File file : files)
						new File (file).delete (listener);
					
					if (!super.delete () && listener != null)
						listener.onError (this, new IOException ("Can't delete folder"));
					
				} else if (listener != null)
					listener.onError (this, new IOException ("Can't list folder"));
				
			} else if (!super.delete () && listener != null)
				listener.onError (this, new IOException ("Can't delete file"));
			
			return true;
			
		}
		
		public boolean deleteIfExists () {
			return (exists () && delete ());
		}
		
		public String mksize () {
			return mksize ("");
		}
		
		public String mksize (String value) {
			return Int.mksize (length (), value);
		}
		
		/**
     * Returns the parent of the given path.
     *
     * @param path the path whose parent is returned (must start with '/')
     * @return the parent of the given path (excluding trailing slash),
     * or null if given path is the root path
     */
		public static String getParent (String path) {
			path = new Strings (path).trimEnd (DS).toString (); // remove trailing slash
			int slash = path.lastIndexOf (DS);
			return slash < 0 ? null : path.substring (0, slash);
		}
		
		public static Map<String, String> mimeTypes () {
			
			Map<String, String> types = new HashMap<> ();
			
			types.add ("xls", "application/vnd.ms-excel");
			types.add ("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			types.add ("xml", "text/xml");
			types.add ("ods", "application/vnd.oasis.opendocument.spreadsheet");
			types.add ("csv", "text/plain");
			types.add ("tmpl", "text/plain");
			types.add ("pdf", "application/pdf");
			types.add ("php", "application/x-httpd-php");
			types.add ("jpg", "image/jpeg");
			types.add ("png", "image/png");
			types.add ("gif", "image/gif");
			types.add ("bmp", "image/bmp");
			types.add ("txt", "text/plain");
			types.add ("doc", "application/msword");
			types.add ("js", "text/js");
			types.add ("swf", "application/x-shockwave-flash");
			types.add ("apk", "application/vnd.android.package-archive");
			types.add ("jar", "application/java-archive");
			types.add ("mp3", "audio/mpeg");
			types.add ("zip", "application/zip");
			types.add ("rar", "application/rar");
			types.add ("tar", "application/tar");
			types.add ("arj", "application/arj");
			types.add ("cab", "application/cab");
			types.add ("html", "text/html");
			types.add ("htm", "text/html");
			types.add ("default", "application/octet-stream");
			
			return types;
			
		}
		
		public String getMimeType () {
			
			Map<String, String> types = mimeTypes ();
			
			String type = types.get (getExtension ()); // TODO
			if (type == null) type = types.get ("default");
			
			return type;
			
		}
		
		public void writeLog (Exception e) {
			writeLog (e, "");
		}
		
		public void writeLog (Throwable e) {
			writeLog (e, "");
		}
		
		public void writeLog (Throwable e, Object appName) {
			writeLog (new Exception (e), appName);
		}
		
		public static String logText (String mess) {
			return mess + ":\n\n" + Arrays.implode ("\n", Thread.currentThread ().getStackTrace ()) + "\n\n";
		}
		
		private String logText (Exception e, Object appName) {
			
			return e.toString () +
		           (!appName.equals ("") ? " [" + appName + "]" : "") +
		           (e.getCause () != null ? ":\n\n" + Arrays.implode ("\n", e.getCause ().getStackTrace ()) : "") + "\n\n";
			
		}
		
		public void writeLog (Exception e, Object appName) {
			writeLog (logText (e, appName));
		}
		
		public void writeLog (String mess) {
			
			try {
				append (new Date ().toString (6, false) + ": " + mess);
			} catch (IOException e) {
				// empty
			}
			
		}
		
		public boolean isSymlink () {
			
			try {
				return !(getCanonicalFile ().equals (this));
			} catch (IOException e) {
				return false;
			}
			
		}
		
		public String prepPath () {
			return prepPath (getAbsolutePath ());
		}
		
		public String prepPath (String path) {
			
			path = new Strings (path).addStart (DS);
			return new Strings (path).trimEnd (DS).toString ();
			
		}
		
		public interface CountListener {
			
			void onProgress (long length);
			void onFinish (long length);
			
		}
		
		public long getFolderSize () {
			return getFolderSize (1);
		}
		
		public long getFolderSize (long blockSize) {
			return getFolderSize (blockSize, null);
		}
		
		public long getFolderSize (long blockSize, CountListener listener) {
			return getFolderSize (blockSize, listener, 0);
		}
		
		private long getFolderSize (long blockSize, CountListener listener, long size) {
			
			if (isDirectory ()) {
				
				java.io.File[] files = listFiles ();
				
				if (files != null)
					for (java.io.File file : files) {
						
						File srcFile = new File (file.getAbsolutePath ()); // TODO
						
						if (isDirectory ())
							size = getFolderSize (blockSize, listener, size);
						else
							size += srcFile.getFileSize (blockSize, listener);
						
					}
				
			} else size += getFileSize (blockSize, listener);
			
			return size;
			
		}
		
		public long getFileSize (long blockSize, CountListener listener) {
			
			long size = (Int.size (this) / (blockSize > 1 ? (blockSize + 1) : 1)) * blockSize;
			if (listener != null) listener.onProgress (size);
			
			return size;
			
		}
		
		public List<java.io.File> ls (String folder) throws ConsoleException {
			return ls (folder, Console.su);
		}
		
		public List<java.io.File> ls (String folder, String shell) throws ConsoleException {
			return ls (new java.io.File (folder), shell);
		}
		
		public List<java.io.File> ls (java.io.File folder) throws ConsoleException {
			return ls (folder, Console.su);
		}
		
		public List<java.io.File> ls (final java.io.File folder, String shell) throws ConsoleException {
			
			final List<java.io.File> output = new ArrayList<> ();
			final List<String> errors = new ArrayList<> ();
			
			Console exec = new Console (new Console.Listener () {
				
				@Override
				public void onExecute (String line, int i) {
				}
				
				@Override
				public void onSuccess (String line, int i) {
					output.add (new java.io.File (folder, line));
				}
				
				@Override
				public void onError (String line, int i) {
					errors.add (line);
				}
				
			});
			
			exec.shell (shell);
			exec.query ("ls " + folder);
			
			if (Int.size (errors) > 0)
				throw new ConsoleException (errors);
			
			return output;
			
		}
		
		public String[] images = new String[] {"jpg", "jpeg", "png", "gif", "webm"};
		
		public boolean isImageByExt () {
			return Arrays.contains (getExtension (), images);
		}
		
		public String[] explodePath () {
			return getAbsolutePath ().split (DS);
		}
		
		public Map<String, String> parsePath () {
			
			Map<String, String> output = new HashMap<> ();
			
			output.add ("full", getAbsolutePath ());
			
			String[] path0 = getAbsolutePath ().split (":");
			String[] part1 = explodePath ();
			
			String file_name = Arrays.endValue (part1);
			
			output.add ("full_name", file_name);
			
			StringBuilder path = new StringBuilder ();
			
			for (int i = 0; i < Int.size (part1); i++) {
				
				if (i > 0) path.append (DS);
				
				if (i < Int.size (part1) - 1)
					path.append (part1[i]);
				
			}
			
			output.add ("path", path.toString ());
			
			String drive = "";
			if (Int.size (path0) > 0) drive = path0[0];
			
			output.add ("drive", drive);
			
			String[] name = file_name.split ("\\.");
			
			String type = "";
			
			if (Int.size (name) > 1) {
				
				type = Arrays.endValue (name);
				
				path = new StringBuilder ();
				
				for (int i = 0; i < Int.size (name); i++) {
					
					if (i > 0) path.append (".");
					
					if (i < Int.size (name) - 1)
						path.append (name[i]);
					
				}
				
				output.add ("name", path.toString ());
				
			} else output.add ("name", file_name);
			
			output.add ("path_name", output.get ("path") + DS + output.get ("name"));
			
			output.add ("type", type);
			
			return output;
			
		}
		
		public File close () throws IOException {
			
			getStream ().close ();
			return this;
			
		}
		
		protected byte[] bytes;
		
		public byte[] toByteArray () throws IOException {
			return getStream ().toByteArray ();
		}
		
		protected String hash;
		
		public String getHash () throws FileNotFoundException, EncryptException {
			return getHash (Hash.ALGORITHM);
		}
		
		public String getHash (String algo) throws FileNotFoundException, EncryptException {
			
			if (hash == null)
				hash = new Hash ()
					.setAlgorithm (algo)
					.process (getStream ())
					.toString ();
			
			return hash;
			
		}
		
		public static java.io.File max (java.io.File... files) {
			
			if (files.length > 0) {
				
				java.io.File max = files[0];
				long length = max.length ();
				
				for (int i = 1; i < files.length; i++)
					if (files[i].length () > length)
						max = files[i];
				
				return max;
				
			} else throw new ZeroException ("Array length cannot be 0");
			
		}
		
		public static java.io.File min (java.io.File... files) {
			
			if (files.length > 0) {
				
				java.io.File max = files[0];
				long length = max.length ();
				
				for (int i = 1; i < files.length; i++)
					if (files[i].length () < length)
						max = files[i];
				
				return max;
				
			} else throw new ZeroException ("Array length cannot be 0");
			
		}
		
	}