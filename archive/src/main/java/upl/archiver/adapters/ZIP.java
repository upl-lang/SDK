  package upl.archiver.adapters;
  /*
   Created by Acuna on 19.07.2018
  */
  
  import java.io.BufferedOutputStream;
  import java.io.FileOutputStream;
  import java.io.IOException;
  import java.util.zip.ZipEntry;
  import java.util.zip.ZipFile;
  import java.util.zip.ZipInputStream;
  import java.util.zip.ZipOutputStream;
  
  import upl.archiver.Archiver;
  import upl.core.File;
  import upl.io.BufferedInputStream;
  
  public class ZIP extends Archiver {
    
    private ZipEntry entry;
    private final ZipFile zipFile;
    
    public ZIP (File file) throws Archiver.CompressException {
      
      try {
        
        this.file = file;
        
        zipFile = new ZipFile (file);
        inputStream = new ZipInputStream (new BufferedInputStream (file));
        
      } catch (IOException e) {
        throw new Archiver.CompressException (e);
      }
      
    }
    
    @Override
    public boolean setPermissions () {
      return true;
    }
    
    @Override
    public void doCreate () throws Archiver.CompressException {
      
      try {
        outputStream = new ZipOutputStream (new BufferedOutputStream (new FileOutputStream (file)));
      } catch (IOException e) {
        throw new Archiver.CompressException (e);
      }
      
    }
    
    @Override
    public void open () throws Archiver.DecompressException {
    
    }
    
    @Override
    public BufferedInputStream getInputStream (String entryFile) throws Archiver.DecompressException {
      
      try {
        return new BufferedInputStream (zipFile.getInputStream (new ZipEntry (entryFile)));
      } catch (IOException e) {
        throw new Archiver.DecompressException (e);
      }
      
    }
    
    @Override
    public void addEntry (java.io.File file, String entryFile) throws Archiver.CompressException {
      
      try {
        ((ZipOutputStream) outputStream).putNextEntry (new ZipEntry (entryFile));
      } catch (IOException e) {
        throw new Archiver.CompressException (e);
      }
      
    }
    
    @Override
    public boolean getNextEntry () throws Archiver.DecompressException {
      
      try {
        return ((entry = ((ZipInputStream) inputStream).getNextEntry ()) != null);
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
      
      try {
        ((ZipInputStream) inputStream).closeEntry ();
      } catch (IOException e) {
        throw new Archiver.DecompressException (e);
      }
      
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