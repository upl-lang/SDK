	package upl.archiver.adapters;
	/*
	 Created by Acuna on 19.07.2018
	*/
	
	import java.io.BufferedOutputStream;
	import java.io.File;
	import java.io.FileOutputStream;
	import java.io.IOException;
	import java.io.InputStream;
	import java.util.zip.GZIPInputStream;
	import java.util.zip.GZIPOutputStream;
	
	import upl.archiver.Archiver;
	
	public class GZIP extends Archiver {
		
		@Override
		public boolean setPermissions () {
			return false;
		}
		
		@Override
		public void doCreate () throws Archiver.CompressException {
			
			try {
				outputStream = new GZIPOutputStream (new BufferedOutputStream (new FileOutputStream (file))); // TODO
			} catch (IOException e) {
				throw new Archiver.CompressException (e);
			}
			
		}
		
		public GZIP (InputStream stream) throws Archiver.CompressException {
			
			try {
				inputStream = new GZIPInputStream (stream);
			} catch (IOException e) {
				throw new Archiver.CompressException (e);
			}
			
		}
		
		@Override
		public void addEntry (File file, String entryFile) throws Archiver.CompressException {
			// Не нужно
		}
		
		private int i = 0;
		
		@Override
		public boolean getNextEntry () throws Archiver.DecompressException {
			++i;
			
			return (i == 1);
			
		}
		
		@Override
		public void closeEntry () throws Archiver.DecompressException {}
		
		@Override
		public String getEntryName () {
			return file.getName ();
		}
		
		@Override
		public boolean isDirectory () {
			return false;
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
				
				//outputStream.finish ();
				outputStream.close ();
				
			} catch (IOException e) {
				throw new Archiver.CompressException (e);
			}
			
		}
		
	}