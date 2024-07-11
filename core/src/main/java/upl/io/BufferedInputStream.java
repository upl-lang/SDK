  package upl.io;
  
  import java.io.BufferedReader;
  import java.io.ByteArrayInputStream;
  import java.io.ByteArrayOutputStream;
  import java.io.BufferedOutputStream;
  import java.io.File;
  import java.io.FileInputStream;
  import java.io.FileNotFoundException;
  import java.io.FileOutputStream;
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.InputStreamReader;
  import java.io.OutputStream;
  import upl.core.exceptions.OutOfMemoryException;
  import upl.type.Strings;
  import upl.util.List;
  import upl.util.Map;
  
  public class BufferedInputStream extends java.io.BufferedInputStream {
    
    protected String charset;
    protected int bufferLength;
    
    protected String line;
    protected byte[] bytes;
    
    @SuppressWarnings ("CopyConstructorMissesField")
    public BufferedInputStream (BufferedInputStream stream) {
      
      super (stream);
      throw new IllegalArgumentException ("BufferedInputStream is already implemented");
      
    }
    
    public BufferedInputStream (File file) throws FileNotFoundException {
      this (new FileInputStream (file));
    }
    
    public BufferedInputStream (InputStream stream) {
      this (stream, Strings.DEF_CHARSET);
    }
    
    public BufferedInputStream (InputStream stream, String charset) {
      this (stream, charset, upl.core.File.BUFFER_SIZE);
    }
    
    public BufferedInputStream (InputStream stream, String charset, int bufferLength) {
      
      super (stream, bufferLength);
      
      this.charset = charset;
      this.bufferLength = bufferLength;
      
      buffer = new byte[bufferLength];
      
    }
    
    public BufferedInputStream (byte[] bytes) {
      
      super (new ByteArrayInputStream (bytes));
      this.bytes = bytes;
      
    }
    
    /*public static OutputStream toOutputStream (File file) throws IOException {
      
      OutputStream stream = new FileOutputStream (file);
      return toOutputStream (stream);
      
    }
    
    public static OutputStream toOutputStream (OutputStream stream) {
      return new BufferedOutputStream (stream);
    }*/
    
    public int readLength;
    public byte[] buffer;
    
    public boolean isRead () throws IOException {
      return (readLength = read (buffer, 0, bufferLength)) > 0;
    }
    
    public File copy (File file) throws IOException {
      
      copy (new FileOutputStream (file));
      
      return file;
      
    }
    
    public BufferedInputStream copy (OutputStream out) throws IOException {
      return copy (new BufferedOutputStream (out));
    }
    
    public BufferedInputStream copy (OutputStream out, int size) throws IOException {
      return copy (new BufferedOutputStream (out, size));
    }
    
    public BufferedInputStream copy (BufferedOutputStream out) throws IOException {
      
      while (isRead ())
        out.write (buffer, 0, readLength);
      
      return this;
      
    }
    
    public byte[] getBytes () throws IOException {
      return getBytes (true);
    }
    
    public ByteArrayOutputStream baos;
    
    public byte[] getBytes (boolean flush) throws IOException {
      
      baos = new ByteArrayOutputStream ();
      
      copy (baos);
      
      byte[] out = baos.toByteArray ();
      
      if (flush) baos.flush ();
      
      return out;
      
    }
    
    protected BufferedReader br;
    
    protected void getReader () throws IOException {
      
      if (br == null)
        br = new BufferedReader (new InputStreamReader (this, charset), bufferLength);
      
    }
    
    public void close () throws IOException {
      if (br != null) br.close ();
    }
    
    public List<String> read (List<String> output) throws IOException {
      return read (output, true);
    }
    
    public boolean readLine () throws IOException {
      
      getReader ();
      
      return (line = br.readLine ()) != null;
      
    }
    
    public List<String> read (List<String> output, boolean close) throws IOException {
      
      while (readLine ())
        output.add (line);
      
      if (close) close ();
      
      return output;
      
    }
    
    public Map<String, String> read (Map<String, String> output) throws IOException {
      return read (output, "=");
    }
    
    public Map<String, String> read (Map<String, String> output, String sep) throws IOException {
      return read (output, sep, true);
    }
    
    public Map<String, String> read (Map<String, String> output, String sep, boolean close) throws IOException {
      
      while (readLine ()) {
        
        String[] parts = line.split (sep);
        output.add (new Strings (parts[0]).trim (), new Strings (parts[1]).trim ());
        
      }
      
      if (close) close ();
      
      return output;
      
    }
    
    public String read (String str) throws IOException, OutOfMemoryException {
      return read (new StringBuilder (str)).toString ();
    }
    
    public StringBuilder read (StringBuilder builder) throws IOException, OutOfMemoryException {
      
      try {
        
        while (readLine ()) {
          
          builder.append (line);
          builder.append (Strings.LS);
          
        }
        
        return builder;
        
      } catch (OutOfMemoryError e) {
        throw new OutOfMemoryException (e);
      }
      
    }
    
    public String getLine () {
      return line;
    }
    
    public byte[] read (int length) throws IOException {
      
      byte[] salt = new byte[length];
      read (salt);
      
      return salt;
      
    }
    
    public byte[] readBytes (int size) {
      
      byte[] iv = new byte[size];
      System.arraycopy (bytes, 0, iv, 0, iv.length);
      
      return iv;
      
    }
    
  }