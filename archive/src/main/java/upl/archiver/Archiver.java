  package upl.archiver;
  /*
   Created by Acuna on 17.07.2017
  */
  
  import java.io.BufferedWriter;
  import java.io.FileInputStream;
  import java.io.FileOutputStream;
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.OutputStream;
  import java.io.OutputStreamWriter;
  import java.net.URL;
  
  import upl.archiver.adapters.ZIP;
  import upl.cipher.CipherInputStream;
  import upl.cipher.exceptions.DecryptException;
  import upl.core.Arrays;
  import upl.core.Console;
  import upl.core.File;
  import upl.http.HttpMethod;
  import upl.http.HttpRequest;
  import upl.core.Int;
  import upl.core.Net;
  import upl.core.System;
  import upl.cipher.exceptions.EncryptException;
  import upl.type.Strings;
  import upl.util.ArrayList;
  import upl.util.List;
  import upl.core.exceptions.ConsoleException;
  import upl.core.exceptions.HttpRequestException;
  import upl.core.exceptions.OutOfMemoryException;
  import upl.io.BufferedInputStream;
  
  public abstract class Archiver {
    
    public File file;
    protected File prefDestFolder, cmdsFile;
    protected String prefDestPath, cmdsFileName = "commands.sh", srcFolder = "", pathFolder = "", type;
    protected List<File> denyFolders = new ArrayList<> ();
    protected List<String> entries = new ArrayList<> (), cmds2 = new ArrayList<> ();
    protected String prefPassword, cryptoAlgoritm;
    protected long fileId = 0, total = 0;
    
    protected Console exec;
    
    protected BufferedWriter cmd;
    
    public String shell = "";
    
    protected InputStream inputStream;
    protected OutputStream outputStream;
    
    public abstract void doCreate () throws Archiver.CompressException;
    public abstract void addEntry (java.io.File file, String entryFile) throws Archiver.CompressException;
    public abstract boolean getNextEntry () throws Archiver.DecompressException;
    public abstract String getEntryName ();
    public abstract boolean isDirectory ();
    public abstract boolean setPermissions ();
    public abstract void closeEntry () throws Archiver.DecompressException;
    public abstract void closeStream () throws Archiver.DecompressException;
    public abstract void close () throws Archiver.CompressException;
    
    public static class CompressException extends Exception {
      
      protected String file;
      
      public CompressException (Exception e, String... file) {
        
        super (e);
        
        if (Int.size (file) > 0)
          this.file = Arrays.implode (", ", file);
        
      }
      
      @Override
      public Exception getCause () {
        return (Exception) super.getCause ();
      }
      
      public String getFile () {
        return file;
      }
      
    }
    
    public static class DecompressException extends Exception {
      
      protected String file;
      
      public DecompressException (Exception e, String... file) {
        
        super (e);
        
        if (Int.size (file) > 0)
          this.file = Arrays.implode (upl.core.File.DS, file);
        
      }
      
      public DecompressException (String msg) {
        super (msg);
      }
      
      @Override
      public Exception getCause () {
        return (Exception) super.getCause ();
      }
      
      public String getEntry () {
        return file;
      }
      
    }
    
    public BufferedInputStream getInputStream (String entryFile) throws Archiver.DecompressException {
      return new BufferedInputStream (inputStream);
    }
    
    public void open () throws Archiver.DecompressException {}
    
    public Archiver setShell (String shell) {
      
      this.shell = shell;
      return this;
      
    }
    
    public Archiver create () throws CompressException {
      
      try {
        
        pathFolder = "";
        
        open ();
        
        if (file != null) {
          
          prefDestPath = file.getParent ();
          new File (prefDestPath).makeDir ();
          
          type = file.getExtension ();
          
          if (setPermissions () && !shell.equals ("") && prefDestPath != null) {
            
            cmdsFile = new File (prefDestPath, cmdsFileName);
            
            FileOutputStream stream = new FileOutputStream (cmdsFile);
            cmd = new BufferedWriter (new OutputStreamWriter (stream));
            
            exec = new Console ();
            exec.shell (shell);
            
          }
          
        }
        
        doCreate ();
        
        return this;
        
      } catch (IOException | ConsoleException | DecompressException e) {
        throw new CompressException (e);
      }
      
    }
    
    public Archiver addEntry (String input) throws CompressException {
      return addEntry (input, null);
    }
    
    public Archiver addEntry (String input, File.ListListener listener) throws CompressException {
      return addEntry (new File (input), listener);
    }
    
    public Archiver addEntry (File file) throws CompressException {
      return addEntry (file, (File.ListListener) null);
    }
    
    public Archiver addEntry (File file, File.ListListener listener) throws CompressException {
      
      try {
        
        if (file.exists ()) {
          
          if (file.isDirectory ()) {
            
            java.io.File[] files = file.listFiles ();
            
            if (files != null) {
              
              if (listener != null)
                listener.onDirectory (file);
              
              folderId = 0;
              
              srcFolder = file.getAbsolutePath ();
              
              for (java.io.File file2 : files) {
                
                total++;
                
                File file3 = new File (file2);
                
                String entryFile = new Strings (!path.equals ("") ? path : prefDestPath).addEnd ("/");
                entryFile = String.valueOf (new Strings (entryFile).slice (srcFolder));
                
                if (!entryFile.equals (""))
                  _addEntry (file3, entryFile + "/"); // TODO
                
                addEntry (file3, listener);
                
                //addFolder (file, entryFile);
                
              }
              
            } else if (listener != null)
              listener.onError (file, new IOException ("Can't load folder"));
            
          } else {
            
            if (listener != null) listener.onFile (file);
            
            String entryFile = String.valueOf (new Strings (prefDestPath + "/").slice (file.getAbsolutePath ()));
            addFile (file, entryFile);
            
            total++;
            
          }
          
        } else throw new IOException (file + " is not exists");
        
      } catch (IOException | IllegalArgumentException | OutOfMemoryException e) {
        throw new CompressException (e);
      }
      
      return this;
      
    }
    
    public Archiver addEntry (InputStream stream) throws CompressException {
      return addEntry (new BufferedInputStream (stream));
    }
    
    public Archiver addEntry (BufferedInputStream stream) throws CompressException {
      
      addStream (stream, null, new Strings (file.getAbsolutePath ()).slice (prefDestPath + "/", true).toString ()); // TODO
      return this;
      
    }
    
    public Archiver setFolderPath (String folder) {
      
      pathFolder = folder;
      return this;
      
    }
    
    public Archiver setPassword (String password) {
      
      prefPassword = password;
      return this;
      
    }
    
    public Archiver setCryptoAlgoritm (String algo) {
      
      cryptoAlgoritm = algo;
      return this;
      
    }
    
    public void denyFolder (String input) { // TODO void
      denyFolder (new String[] {input});
    }
    
    public void denyFolder (String... input) {
      denyFolder (new File (Arrays.implode ("/", input)));
    }
    
    public void denyFolder (File input) {
      denyFolders.add (input);
    }
    
    public interface ArchievsListener {
      
      void onProgress (String file, long i, long total);
      
      void onError (String mess);
      
    }
    
    protected ArchievsListener listener;
    
    public Archiver addListener (ArchievsListener listener) {
      
      this.listener = listener;
      return this;
      
    }
    
    protected String path = "";
    
    public Archiver setPath (String path) {
      
      this.path = path;
      return this;
      
    }
    
    protected void _addEntry (java.io.File file, String entryFile) throws CompressException {
      
      if (!entries.contains (entryFile)) {
        
        addEntry (file, entryFile);
        setPermissions (file, entryFile);
        
      }
      
    }
    
    protected BufferedInputStream _getInputStream (String entryFile) throws DecompressException {
      
      BufferedInputStream stream;
      
      if (encryptedFile (entryFile))
        stream = getInputStream (entryFile + ".aes");
      else
        stream = getInputStream (entryFile);
      
      return stream;
      
    }
    
    public BufferedInputStream getEntryStream (String... file) throws DecompressException {
      
      try {
        
        String entryFile = ((Int.size (file) > 1 && file[0].equals ("")) ? file[1] : Arrays.implode ("/", file));
        BufferedInputStream inputFile = _getInputStream (entryFile);
        
        if (encryptedFile (entryFile)) {
          
          CipherInputStream cipher = new CipherInputStream (inputFile);
          
          cipher.setPassword (prefPassword);
          
          inputFile = new BufferedInputStream (cipher.decrypt ());
          
        }
        
        return inputFile;
        
      } catch (DecryptException e) {
        throw new DecompressException (e);
      }
      
    }
    
    public String getEntry (String... file) throws DecompressException {
      
      try {
        return getEntryStream (file).read ("");
      } catch (IOException | OutOfMemoryException e) {
        throw new DecompressException (e, file);
      }
      
    }
    
    protected int folderId = 0;
    
    protected Archiver addFolder (java.io.File folder, String entryFile) throws CompressException {
      
      if (!folder.isDirectory () || !denyFolders.contains (folder)) {
        
        try {
          
          if (folder.isDirectory ()) {
            
            java.io.File[] files = folder.listFiles ();
            
            if (files != null && Int.size (files) > 0) {
              
              setPermissions (folder, entryFile + "/"); // Самая верхняя папка
              
              ++folderId;
              
              for (java.io.File file : files)
                addFolder (file, (!entryFile.equals ("") ? entryFile + "/" : "") + file.getName ());
              
            } else if (!entryFile.equals (""))
              _addEntry (folder, entryFile + "/"); // Если папка пустая
            
          } else if (folder.isFile ())
            addFile (folder, entryFile);
          else if (!folder.exists ())
            throw new IOException ("Folder not exists: " + folder);
          else
            throw new IllegalArgumentException (folder.getAbsolutePath () + " not a folder, use addFile () method instead if you want to add a file");
          
        } catch (IOException | IllegalArgumentException | OutOfMemoryException e) {
          throw new CompressException (e);
        }
        
      }
      
      return this;
      
    }
    
    protected List<String> newEntries = new ArrayList<> ();
    
    public Archiver pathDenyEntry (String entry) {
      
      newEntries.add (entry);
      return this;
      
    }
    
    protected void setPermissions (java.io.File file, String entryFile) {
      
      if (shell.equals (Console.su)) {
        
        entries.add (entryFile);
        ++fileId;
        
        if (!pathFolder.equals ("") && !srcFolder.equals ("")) {
          
          entryFile = String.valueOf (new Strings (file.toString ()).trimStart (srcFolder + "/"));
          
          final System.DirData dirData = System.getDirData ();
          
          if (listener != null)
            listener.onProgress (pathFolder + (!entryFile.equals ("/") ? "/" + entryFile : ""), fileId, total);
          
          if (exec != null) {
            
            exec.addListener (new Console.Listener () {
              
              @Override
              public void onExecute (String line, int i) {
              }
              
              @Override
              public void onSuccess (String line, int i) {
                
                System.DirData data = dirData.matcher (line);
                
                if (!newEntries.contains (data.path) && data.chmod != null) {
                  
                  try {
                    
                    cmd.write ("chmod " + data.chmod + " " + "\"" + data.path + "\"");
                    cmd.newLine ();
                    
                    cmd.write ("chown " + data.uid + ":" + data.gid + " " + "\"" + data.path + "\"");
                    cmd.newLine ();
                    
                  } catch (IOException e) {
                    if (listener != null) listener.onError (e.getMessage ());
                  }
                  
                  newEntries.add (data.path);
                  
                }
                
              }
              
              @Override
              public void onError (String line, int i) {
                if (listener != null && !line.contains ("No such file")) listener.onError (line);
              }
              
            });
            
          }
          
        }
        
        file = new File (pathFolder, entryFile);
        cmds2 = System.getDirData ().shell (file, cmds2);
        
      }
      
    }
    
    protected Archiver addFile (java.io.File file, String entryFile) throws CompressException, IOException, OutOfMemoryException {
      return addStream (new BufferedInputStream (new FileInputStream (file)), file/* Костыль для библиотек, которые не поддерживают запись в потоки напрямую */, entryFile);
    }
    
    protected Archiver addStream (BufferedInputStream stream, java.io.File file, String entryFile) throws CompressException {
      
      try {
        
        if (file == null || file.isFile ()) {
          
          ++folderId;
          
          if (!encryptedFile (entryFile)) {
            
            _addEntry (file, entryFile); // Создаем файл
            
            new BufferedInputStream (stream).copy (outputStream);
            
          } else {
            
            _addEntry (file, entryFile + ".aes"); // Создаем файл
            
            CipherInputStream cipher = new CipherInputStream (stream);
            
            cipher.setPassword (prefPassword);
            cipher.encrypt (outputStream); // TODO algo?
            
          }
          
          stream.close ();
          
        } else throw new IllegalArgumentException (file.getAbsolutePath () + " not a file, use addFolder () method instead if you want to add a folder");
        
        return this;
        
      } catch (IOException | EncryptException | IllegalArgumentException e) {
        throw new CompressException (e);
      }
      
    }
    
    protected boolean encryptedFile (String entryFile) {
      return (prefPassword != null && !prefPassword.equals ("") && !entryFile.equals (cmdsFileName));
    }
    
    public Archiver unpack (String input) throws DecompressException {
      return unpack (new File (input));
    }
    
    public Archiver unpack (File destFolder) throws DecompressException {
      
      prefDestFolder = destFolder;
      
      try {
        
        if (prefDestFolder.isDirectory ()) {
          
          String dest = prefDestFolder.getAbsolutePath ();
          
          while (getNextEntry ()) {
            
            File file = new File (dest, getEntryName ());
            
            if (!isDirectory ()) {
              
              java.io.File parentDir = file.getParentFile ();
              
              if (parentDir != null && (parentDir.isDirectory () || parentDir.mkdirs ())) {
                
                BufferedInputStream stream = getInputStream (getEntryName ());
                
                if (stream != null) {
                  
                  if (!encryptedFile (getEntryName ()))
                    stream.copy (file);
                  //else // TODO
                  //  new upl.cipher.adapters.File (stream, prefPassword).decrypt (new File (dest, new Strings (getEntryName ()).trimEnd (".aes")));
                  
                }
                
                closeEntry ();
                
              } else throw new IOException ("Unable to create folder " + parentDir);
              
            }
            
          }
          
          closeStream ();
          
        } else throw new DecompressException ("Destination folder must be a folder");
        
      } catch (IOException e) {
        throw new DecompressException (e);
      }
      
      return this;
      
    }
    
    public List<String> getCommands () throws DecompressException {
      
      List<String> cmds = new ArrayList<> ();
      
      try {
        
        cmdsFile = new File (prefDestFolder.toString (), cmdsFileName);
        cmds = cmdsFile.read (cmds);
        
      } catch (IOException | OutOfMemoryException e) {
        throw new DecompressException (e);
      }
      
      return cmds;
      
    }
    
    public static BufferedInputStream unpack (URL url) throws DecompressException {
      return unpack (url, Net.getUserAgent ());
    }
    
    public static BufferedInputStream unpack (URL url, String userAgent) throws DecompressException {
      return unpack (url, userAgent, HttpRequest.defTimeout);
    }
    
    public static BufferedInputStream unpack (URL url, String userAgent, int timeout) throws DecompressException { // TODO
      
      try {
        
        HttpRequest request = new HttpRequest (HttpMethod.GET, url)
                                .setUserAgent (userAgent)
                                .setTimeout (timeout);
        
        BufferedInputStream is = request.getInputStream ();
        if (!request.isOK ()) throw new IOException (request.getMessageCode ());
        
        return is;
        
      } catch (IOException | HttpRequestException | OutOfMemoryException e) {
        throw new DecompressException (e);
      }
      
    }
    
    public void clear () throws DecompressException {
      
      try {
        
        if (cmdsFile != null) cmdsFile.delete ();
        entries.clear ();
        
      } catch (Exception e) {
        throw new DecompressException (e);
      }
      
    }
    
    public void _close () throws CompressException {
      
      try {
        
        if (cmd != null) {
          
          if (exec != null) exec.query (cmds2);
          
          cmd.flush ();
          
          if (cmdsFile != null)
            addFile (cmdsFile, cmdsFileName);
          
        }
        
        close (); // TODO
        
        clear ();
        
      } catch (DecompressException | ConsoleException | IOException | OutOfMemoryException e) {
        throw new CompressException (e);
      }
      
    }
    
    protected String test (File file, String folder, String srcDir) throws CompressException, DecompressException {
      
      Archiver archive = new ZIP (file);
      
      archive.doCreate ();
      
      archive.setFolderPath (srcDir);
      archive.addEntry (srcDir);
      
      archive.close ();
      
      //archive.open (file);
      
      String string = getEntry ("111") + "\n\n";
      
      unpack (folder);
      
      return string;
      
    }
    
    /*public String test (String srcDir, String destDir, String password) throws CompressException, DecompressException, OutOfMemoryException {
      
      StringBuilder string = new StringBuilder ();
      setShell (Console.su);
      
      for (Provider provider : adapters) {
        
        for (String type : provider.setFormats ()) {
          
          string.append (type).append (":\n\n");
          
          setPassword ("");
          
          File file = new File (destDir, "compress." + type);
          string.append (test (file, destDir + "/decompress/" + type, srcDir));
          
          setPassword (password);
          
          file = new File (destDir, "compress-encrypted." + type);
          string.append (test (file, destDir + "/decompress-encrypted/" + type, srcDir));
          
        }
        
      }
      
      clear ();
      
      return string.toString ();
      
    }*/
    
  }