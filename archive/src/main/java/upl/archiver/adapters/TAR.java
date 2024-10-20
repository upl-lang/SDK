	package upl.archiver.adapters;
	/*
	 Created by Acuna on 19.07.2018
	*/
	
	import java.io.FileInputStream;
	import java.io.FileOutputStream;
	import java.io.IOException;
	import org.kamranzafar.jtar.TarEntry;
	import org.kamranzafar.jtar.TarInputStream;
	import org.kamranzafar.jtar.TarOutputStream;
	import upl.archiver.Archiver;
	import upl.core.File;
	import upl.io.BufferedInputStream;
	
	public class TAR extends Archiver {
		
		private TarEntry entry;
		
		@Override
		public boolean setPermissions () {
			return true;
		}
		
		@Override
		public void doCreate () throws Archiver.CompressException {
			
			try {
				outputStream = new TarOutputStream (new FileOutputStream (file));
			} catch (IOException e) {
				throw new Archiver.CompressException (e);
			}
			
		}
		
		public TAR (File file) throws CompressException {
			
			try {
				inputStream = new TarInputStream (new BufferedInputStream (new FileInputStream (file)));
			} catch (IOException e) {
				throw new Archiver.CompressException (e);
			}
			
		}
		
		@Override
		public BufferedInputStream getInputStream (String entryFile) throws Archiver.DecompressException { // TODO
			return new BufferedInputStream (inputStream);
		}
		
		@Override
		public void addEntry (java.io.File file, String entryFile) throws Archiver.CompressException {
			
			try {
				((TarOutputStream) outputStream).putNextEntry (new TarEntry (file, entryFile));
			} catch (IOException e) {
				throw new Archiver.CompressException (e);
			}
			
		}
		
		@Override
		public boolean getNextEntry () throws Archiver.DecompressException {
			
			try {
				return ((entry = ((TarInputStream) inputStream).getNextEntry ()) != null);
			} catch (IOException e) {
				throw new Archiver.DecompressException (e);
			}
			
		}
		
		@Override
		public String getEntryName () {
			return entry.getName ();
		}
		
		@Override
		public boolean isDirectory () {
			return entry.isDirectory ();
		}
		
		@Override
		public void closeEntry () throws Archiver.DecompressException {
		
		}
		
		@Override
		public void closeStream () throws Archiver.DecompressException {
			
			try {
				inputStream.close ();
			} catch (IOException e) {
				throw new Archiver.DecompressException (e);
			}
			
		}
		
		@Override
		public void close () throws Archiver.CompressException {
			
			try {
				
				outputStream.flush ();
				outputStream.close ();
				
			} catch (IOException e) {
				throw new Archiver.CompressException (e);
			}
			
		}
		
	}