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
  
  package uplx.server;
  
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.OutputStream;
  import java.text.ParseException;
  import java.text.SimpleDateFormat;
  import java.util.Arrays;
  import java.util.Date;
  import java.util.Iterator;
  import java.util.LinkedHashMap;
  import java.util.Locale;
  import java.util.Map;
  import upl.util.List;
  
  /**
   * The {@code Headers} class encapsulates a collection of HTTP headers.
   * <p>
   * Header names are treated case-insensitively, although this class retains
   * their original case. Header insertion order is maintained as well.
   */
  public class Headers implements Iterable<Header> {
    
    // due to the requirements of case-insensitive name comparisons,
    // retaining the original case, and retaining header insertion order,
    // and due to the fact that the number of headers is generally
    // quite small (usually under 12 headers), we use a simple array with
    // linear access times, which proves to be more efficient and
    // straightforward than the alternatives
    protected Header[] headers = new Header[12];
    protected int count;
    
    public int maxHeaders = -1;
    
    private final Server server;
    
    /**
     * The SimpleDateFormat-compatible formats of dates which must be supported.
     * Note that all generated date fields must be in the RFC 1123 format only,
     * while the others are supported by recipients for backwards-compatibility.
     */
    public final String[] DATE_PATTERNS = {
      "EEE, dd MMM yyyy HH:mm:ss z", // RFC 822, updated by RFC 1123
      "EEEE, dd-MMM-yy HH:mm:ss z",  // RFC 850, obsoleted by RFC 1036
      "EEE MMM d HH:mm:ss yyyy"      // ANSI C's asctime() format
    };
    
    public Headers (Server server) {
      this.server = server;
    }
    
    /**
     * Reads headers from the given stream. Headers are read according to the
     * RFC, including folded headers, element lists, and multiple headers
     * (which are concatenated into a single element list header).
     * Leading and trailing whitespace is removed.
     *
     * @param in the stream from which the headers are read
     * @return the read headers (possibly empty, if none exist)
     * @throws ServerException if an IO error occurs or the headers are malformed
     *                         or there are more than 100 header lines
     */
    public Headers readHeaders (InputStream in) throws ServerException {
      
      try {
        
        String line;
        String prevLine = "";
        int count = 0;
        while ((line = Request.readLine (in)).length () > 0) {
          int start; // start of line data (after whitespace)
          for (start = 0; start < line.length () && Character.isWhitespace (line.charAt (start)); start++) ;
          if (start > 0) // unfold header continuation line
            line = prevLine + ' ' + line.substring (start);
          int separator = line.indexOf (':');
          if (separator < 0)
            throw new ServerException ("Invalid header: \"" + line + "\"");
          String name = line.substring (0, separator);
          String value = line.substring (separator + 1).trim (); // ignore LWS
          Header replaced = replace (name, value);
          // concatenate repeated headers (distinguishing repeated from folded)
          if (replaced != null && start == 0) {
            value = replaced.getValue () + ", " + value;
            line = name + ": " + value;
            replace (name, value);
          }
          prevLine = line;
          
          if (maxHeaders > 0 && ++count > maxHeaders)
            throw new ServerException ("Too many header lines");
          
        }
        
        return this;
        
      } catch (IOException e) {
        throw new ServerException (e);
      }
      
    }
    
    /**
     * Returns the number of added headers.
     *
     * @return the number of added headers
     */
    public int size () {
      return count;
    }
    
    /**
     * Returns the value of the first header with the given name.
     *
     * @param name the header name (case insensitive)
     * @return the header value, or null if none exists
     */
    public String get (String name) {
      for (int i = 0; i < count; i++)
        if (headers[i].getName ().equalsIgnoreCase (name))
          return headers[i].getValue ();
      return null;
    }
    
    /**
     * Returns the Date value of the header with the given name.
     *
     * @param name the header name (case insensitive)
     * @return the header value as a Date, or null if none exists
     * or if the value is not in any supported date format
     */
    public Date getDate (String name) {
      try {
        String header = get (name);
        return header == null ? null : parseDate (header);
      } catch (IllegalArgumentException iae) {
        return null;
      }
    }
    
    /**
     * Parses a date string in one of the supported {@link #DATE_PATTERNS}.
     * <p>
     * Received date header values must be in one of the following formats:
     * Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
     * Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
     * Sun Nov  6 08:49:37 1994       ; ANSI C's asctime() format
     *
     * @param time a string representation of a time value
     * @return the parsed date value
     * @throws IllegalArgumentException if the given string does not contain
     *                                  a valid date format in any of the supported formats
     */
    public Date parseDate (String time) {
      for (String pattern : DATE_PATTERNS) {
        try {
          SimpleDateFormat df = new SimpleDateFormat (pattern, Locale.US);
          df.setLenient (false);
          df.setTimeZone (Server.GMT);
          return df.parse (time);
        } catch (ParseException ignore) {
        }
      }
      throw new IllegalArgumentException ("invalid date format: " + time);
    }
    
    /**
     * Returns whether there exists a header with the given name.
     *
     * @param name the header name (case insensitive)
     * @return whether there exists a header with the given name
     */
    public boolean contains (String name) {
      return get (name) != null;
    }
    
    /**
     * Adds a header with the given name and value to the end of this
     * collection of headers. Leading and trailing whitespace are trimmed.
     *
     * @param name  the header name (case insensitive)
     * @param value the header value
     */
    public void add (String name, Object value) {
      Header header = new Header (name, value); // also validates
      // expand array if necessary
      if (count == headers.length) {
        Header[] expanded = new Header[2 * count];
        System.arraycopy (headers, 0, expanded, 0, count);
        headers = expanded;
      }
      headers[count++] = header; // inlining header would cause a bug!
    }
    
    /**
     * Adds all given headers to the end of this collection of headers,
     * in their original order.
     *
     * @param headers the headers to add
     */
    public void addAll (Headers headers) {
      for (Header header : headers)
        add (header.getName (), header.getValue ());
    }
    
    /**
     * Adds a header with the given name and value, replacing the first
     * existing header with the same name. If there is no existing header
     * with the same name, it is added as in {@link #add}.
     *
     * @param name  the header name (case insensitive)
     * @param value the header value
     * @return the replaced header, or null if none existed
     */
    public Header replace (String name, String value) {
      for (int i = 0; i < count; i++) {
        if (headers[i].getName ().equalsIgnoreCase (name)) {
          Header prev = headers[i];
          headers[i] = new Header (name, value);
          return prev;
        }
      }
      add (name, value);
      return null;
    }
    
    /**
     * Removes all headers with the given name (if any exist).
     *
     * @param name the header name (case insensitive)
     */
    public void remove (String name) {
      int j = 0;
      for (int i = 0; i < count; i++)
        if (!headers[i].getName ().equalsIgnoreCase (name))
          headers[j++] = headers[i];
      while (count > j)
        headers[--count] = null;
    }
    
    /**
     * Writes the headers to the given stream (including trailing CRLF).
     *
     * @param out the stream to write the headers to
     * @throws ServerException if an error occurs
     */
    public void writeTo (OutputStream out) throws ServerException {
      
      try {
        
        for (int i = 0; i < count; i++) {
          
          out.write (server.getBytes (headers[i].getName (), ": ", headers[i].getValue ()));
          out.write (Server.CRLF);
          
        }
        
        out.write (Server.CRLF); // ends header block
        
      } catch (IOException e) {
        throw new ServerException (e);
      }
      
    }
    
    /**
     * Returns a header's parameters. Parameter order is maintained,
     * and the first key (in iteration order) is the header's value
     * without the parameters.
     *
     * @param name the header name (case insensitive)
     * @return the header's parameter names and values
     */
    public Map<String, String> getParams (String name) {
      Map<String, String> params = new LinkedHashMap<> ();
      for (String param : server.split (get (name), ";", -1)) {
        List<String> pair = server.split (param, "=", 2);
        String val = pair.length () == 1 ? "" : server.trimLeft (server.trimRight (pair.get (1), '"'), '"');
        params.put (pair.get (0), val);
      }
      return params;
    }
    
    /**
     * Returns an iterator over the headers, in their insertion order.
     * If the headers collection is modified during iteration, the
     * iteration result is undefined. The remove operation is unsupported.
     *
     * @return an Iterator over the headers
     */
    public Iterator<Header> iterator () {
      // we use the built-in wrapper instead of a trivial custom implementation
      // since even a tiny anonymous class here compiles to a 1.5K class file
      return Arrays.asList (headers).subList (0, count).iterator ();
    }
    
  }