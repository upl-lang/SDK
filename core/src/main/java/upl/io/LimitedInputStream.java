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
  
  package upl.io;
  
  import java.io.FilterInputStream;
  import java.io.IOException;
  import java.io.InputStream;
  
  /**
   * The {@code LimitedInputStream} provides access to a limited number
   * of consecutive bytes from the underlying InputStream, starting at its
   * current position. If this limit is reached, it behaves as though the end
   * of stream has been reached (although the underlying stream remains open
   * and may contain additional data).
   */
  public class LimitedInputStream extends FilterInputStream {
    
    protected long limit; // decremented when read, until it reaches zero
    protected boolean prematureEndException;
    
    /**
     * Constructs a LimitedInputStream with the given underlying
     * input stream and limit.
     *
     * @param in                    the underlying input stream
     * @param limit                 the maximum number of bytes that may be consumed from
     *                              the underlying stream before this stream ends. If zero or
     *                              negative, this stream will be at its end from initialization.
     * @param prematureEndException specifies the stream's behavior when
     *                              the underlying stream end is reached before the limit is
     *                              reached: if true, an exception is thrown, otherwise this
     *                              stream reaches its end as well (i.e. read() returns -1)
     * @throws NullPointerException if the given stream is null
     */
    public LimitedInputStream (InputStream in, long limit, boolean prematureEndException) {
      super (in);
      if (in == null)
        throw new NullPointerException ("input stream is null");
      this.limit = limit < 0 ? 0 : limit;
      this.prematureEndException = prematureEndException;
    }
    
    @Override
    public int read () throws IOException {
      int res = limit == 0 ? -1 : in.read ();
      if (res < 0 && limit > 0 && prematureEndException)
        throw new IOException ("unexpected end of stream");
      limit = res < 0 ? 0 : limit - 1;
      return res;
    }
    
    @Override
    public int read (byte[] b, int off, int len) throws IOException {
      int res = limit == 0 ? -1 : in.read (b, off, len > limit ? (int) limit : len);
      if (res < 0 && limit > 0 && prematureEndException)
        throw new IOException ("unexpected end of stream");
      limit = res < 0 ? 0 : limit - res;
      return res;
    }
    
    @Override
    public long skip (long len) throws IOException {
      long res = in.skip (len > limit ? limit : len);
      limit -= res;
      return res;
    }
    
    @Override
    public int available () throws IOException {
      int res = in.available ();
      return res > limit ? (int) limit : res;
    }
    
    @Override
    public boolean markSupported () {
      return false;
    }
    
    @Override
    public void close () {
      limit = 0; // end this stream, but don't close the underlying stream
    }
    
  }