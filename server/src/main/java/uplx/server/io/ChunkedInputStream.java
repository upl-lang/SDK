  /*
   * Copyright (c) 2020 - 2024 UPL Foundation
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *     http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */
  
  package uplx.server.io;
  
  import java.io.IOException;
  import java.io.InputStream;
  import upl.io.LimitedInputStream;
  import uplx.server.Headers;
  import uplx.server.Request;
  import uplx.server.ServerException;
  
  /**
   * The {@code ChunkedInputStream} decodes an InputStream whose data has the
   * "chunked" transfer encoding applied to it, providing the underlying data.
   */
  public class ChunkedInputStream extends LimitedInputStream {
    
    protected Headers headers;
    
    protected boolean initialized;
    
    /**
     * Constructs a ChunkedInputStream with the given underlying stream, and
     * a headers container to which the stream's trailing headers will be
     * added.
     *
     * @param in the underlying "chunked"-encoded input stream
     * @throws NullPointerException if the given stream is null
     */
    public ChunkedInputStream (InputStream in, Headers headers) {
      
      super (in, 0, true);
      
      this.headers = headers;
      
    }
    
    @Override
    public int read () throws IOException {
      return limit <= 0 && initChunk () < 0 ? -1 : super.read ();
    }
    
    @Override
    public int read (byte[] b, int off, int len) throws IOException {
      return limit <= 0 && initChunk () < 0 ? -1 : super.read (b, off, len);
    }
    
    /**
     * Initializes the next chunk. If the previous chunk has not yet
     * ended, or the end of stream has been reached, does nothing.
     *
     * @return the length of the chunk, or -1 if the end of stream
     * has been reached
     * @throws ServerException if an IO error occurs or the stream is corrupt
     */
    protected long initChunk () throws IOException {
      
      if (limit == 0) { // finished previous chunk
        // read chunk-terminating CRLF if it's not the first chunk
        
        if (initialized && Request.readLine (in).length () > 0)
          throw new IOException ("chunk data must end with CRLF");
        initialized = true;
        limit = parseChunkSize (Request.readLine (in)); // read next chunk size
        if (limit == 0) { // last chunk has size 0
          limit = -1; // mark end of stream
          // read trailing headers, if any
          Headers trailingHeaders = headers.readHeaders (in);
          if (headers != null)
            headers.addAll (trailingHeaders);
        }
      }
      
      return limit;
      
    }
    
    /**
     * Parses a chunk-size line.
     *
     * @param line the chunk-size line to parse
     * @return the chunk size
     * @throws IllegalArgumentException if the chunk-size line is invalid
     */
    protected long parseChunkSize (String line) throws IllegalArgumentException {
      
      int pos = line.indexOf (';');
      line = pos < 0 ? line : line.substring (0, pos); // ignore params, if any
      
      try {
        return Request.parseULong (line, 16); // throws NFE
      } catch (NumberFormatException nfe) {
        throw new IllegalArgumentException ("invalid chunk size line: \"" + line + "\"");
      }
      
    }
    
  }