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
  
  package uplx.server.handlers;
  
  import java.io.EOFException;
  import java.io.File;
  import java.io.FileInputStream;
  import java.io.IOException;
  import java.io.InputStream;
  import java.net.URI;
  import java.net.URISyntaxException;
  import java.util.Date;
  import java.util.Formatter;
  import java.util.Locale;
  import java.util.Map;
  import java.util.Objects;
  import java.util.concurrent.ConcurrentHashMap;
  import upl.core.Log;
  import upl.http.HttpMethod;
  import upl.http.HttpStatus;
  import upl.type.StringTemplate;
  import uplx.server.Handler;
  import uplx.server.Headers;
  import uplx.server.Request;
  import uplx.server.Response;
  import uplx.server.ServerException;
  import upl.util.List;
  
  /**
   * The {@code FileHandler} services a context by mapping it
   * to a file or folder (recursively) on disk.
   */
  public class FileHandler extends Handler {
    
    protected final File base;
    
    /**
     * A mapping of path suffixes (e.g. file extensions) to their
     * corresponding MIME types.
     */
    protected final Map<String, String> contentTypes = new ConcurrentHashMap<> ();
    
    {
      
      // add some default common content types
      // see http://www.iana.org/assignments/media-types/ for full list
      
      addContentType ("application/font-woff", "woff");
      addContentType ("application/font-woff2", "woff2");
      addContentType ("application/java-archive", "jar");
      addContentType ("application/javascript", "js");
      addContentType ("application/json", "json");
      addContentType ("application/octet-stream", "exe");
      addContentType ("application/pdf", "pdf");
      addContentType ("application/x-7z-compressed", "7z");
      addContentType ("application/x-compressed", "tgz");
      addContentType ("application/x-gzip", "gz");
      addContentType ("application/x-tar", "tar");
      addContentType ("application/xhtml+xml", "xhtml");
      addContentType ("application/zip", "zip");
      addContentType ("audio/mpeg", "mp3");
      addContentType ("image/gif", "gif");
      addContentType ("image/jpeg", "jpg", "jpeg");
      addContentType ("image/png", "png");
      addContentType ("image/svg+xml", "svg");
      addContentType ("image/x-icon", "ico");
      addContentType ("text/css", "css");
      addContentType ("text/csv", "csv");
      addContentType ("text/html; charset=utf-8", "htm", "html");
      addContentType ("text/plain", "txt", "text", "log");
      addContentType ("text/xml", "xml");
      
    }
    
    public FileHandler (File dir) throws ServerException {
      
      try {
        this.base = dir.getCanonicalFile ();
      } catch (IOException e) {
        throw new ServerException (e);
      }
      
    }
    
    /**
     * Adds a Content-Type mapping for the given path suffixes.
     * If any of the path suffixes had a previous Content-Type associated
     * with it, it is replaced with the given one. Path suffixes are
     * considered case-insensitive, and contentType is converted to lowercase.
     *
     * @param contentType the content type (MIME type) to be associated with
     *                    the given path suffixes
     * @param suffixes    the path suffixes which will be associated with
     *                    the contentType, e.g. the file extensions of served files
     *                    (excluding the '.' character)
     */
    public void addContentType (String contentType, String... suffixes) {
      for (String suffix : suffixes)
        contentTypes.put (suffix.toLowerCase (Locale.US), contentType.toLowerCase (Locale.US));
    }
    
    /**
     * Adds Content-Type mappings from a standard mime.types file.
     *
     * @param in a stream containing a mime.types file
     * @throws ServerException           if an error occurs
     */
    public void addContentTypes (InputStream in) throws ServerException {
      
      try (in) {
        
        while (true) {
          
          String line = Request.readLine (in).trim (); // throws EOFException when done
          
          if (line.length () > 0 && line.charAt (0) != '#') {
            
            List<String> lexemes = server.split (line, " \t", -1);
            
            for (int i = 1; i < lexemes.length (); i++)
              addContentType (lexemes.get (0), lexemes.get (i));
            
          }
          
        }
        
      } catch (EOFException ignore) { // the end of file was reached - it's ok
      } catch (IOException e) {
        throw new ServerException (e);
      }
      
    }
    
    protected static class Handler extends TextHandler {
      
      protected File file;
      
      public Handler (File file) throws ServerException {
        this.file = file;
      }
      
      @Override
      public StringTemplate onContent (Request request, Response response) {
        return createIndex (file, request.getPath ());
      }
      
      /**
       * Serves the contents of a directory as an HTML file index.
       *
       * @param dir  the existing and readable directory whose contents are served
       * @param path the displayed base path corresponding to dir
       * @return an HTML string containing the file index for the directory
       */
      protected StringTemplate createIndex (File dir, String path) {
        
        if (!path.endsWith ("/"))
          path += "/";
        // calculate name column width
        int w = 21; // minimum width
        
        for (String name : Objects.requireNonNull (dir.list ()))
          if (name.length () > w)
            w = name.length ();
        
        w += 2; // with room for added slash and space
        
        // note: we use apache's format, for consistent user experience
        
        Formatter f = new Formatter (Locale.US);
        
        f.format ("<!DOCTYPE html>%n" +
                    "<html><head><title>Index of %s</title></head>%n" +
                    "<body><h1>Index of %s</h1>%n" +
                    "<pre> Name%" + (w - 5) + "s Last modified      Size<hr>",
          path, path, "");
        
        if (path.length () > 1) // add parent link if not root path
          f.format (" <a href=\"%s/\">Parent Directory</a>%"
                      + (w + 5) + "s-%n", server.getParentPath (path), "");
        
        for (File file : Objects.requireNonNull (dir.listFiles ())) {
          
          try {
            
            String name = file.getName () + (file.isDirectory () ? "/" : "");
            String size = file.isDirectory () ? "- " : toSizeApproxString (file.length ());
            
            // properly url-encode the link
            String link = new URI (null, path + name, null).toASCIIString ();
            
            if (!file.isHidden () && !name.startsWith ("."))
              f.format (" <a href=\"%s\">%s</a>%-" + (w - name.length ()) + "s&#8206;%td-%<tb-%<tY %<tR%6s%n", link, name, "", file.lastModified (), size);
            
          } catch (URISyntaxException ignore) {}
          
        }
        
        f.format ("</pre></body></html>");
        
        return new StringTemplate (f.toString ());
        
      }
      
      /**
       * Returns a human-friendly string approximating the given data size,
       * e.g. "316", "1.8K", "324M", etc.
       *
       * @param size the size to display
       * @return a human-friendly string approximating the given data size
       */
      protected String toSizeApproxString (long size) {
        
        final char[] units = {' ', 'K', 'M', 'G', 'T', 'P', 'E'};
        int u;
        double s;
        
        for (u = 0, s = size; s >= 1000; u++, s /= 1024);
        return String.format (s < 10 ? "%.1f%c" : "%.0f%c", s, units[u]);
        
      }
      
    }
    
    protected volatile boolean allowGeneratedIndex = false;
    
    /**
     * Sets whether auto-generated indices are allowed. If false, and a
     * directory resource is requested, an error will be returned instead.
     *
     * @param allowed specifies whether generated indices are allowed
     */
    public void setAllowGeneratedIndex (boolean allowed) {
      this.allowGeneratedIndex = allowed;
    }
    
    /**
     * Returns whether auto-generated indices are allowed.
     *
     * @return whether auto-generated indices are allowed
     */
    public boolean isAllowGeneratedIndex () {
      return allowGeneratedIndex;
    }
    
    /**
     * Serves a context's contents from a file based resource.
     * <p>
     * The file is located by stripping the given context prefix from
     * the request's path, and appending the result to the given base directory.
     * <p>
     * Missing, forbidden and otherwise invalid files return the appropriate
     * error response. Directories are served as an HTML index page if the
     * virtual host allows one, or a forbidden error otherwise. Files are
     * sent with their corresponding content types, and handle conditional
     * and partial retrievals according to the RFC.
     *
     * @param context the context which is mapped to the base directory
     * @param request     the request
     * @param response    the response into which the content is written
     * @return the HTTP status code to return, or 0 if a response was sent
     * @throws ServerException if an error occurs
     */
    protected HttpStatus serveFile (String context, Request request, Response response) throws ServerException {
      
      try {
        
        String relativePath = request.getPath ().substring (context.length ());
        File file = new File (base, relativePath).getCanonicalFile ();
        
        if (!file.exists () || file.isHidden () || file.getName ().startsWith ("."))
          return HttpStatus.CLIENT_ERROR_NOT_FOUND;
        else if (!file.canRead () || !file.getPath ().startsWith (base.getPath ())) // validate
          return HttpStatus.CLIENT_ERROR_FORBIDDEN;
        else if (file.isDirectory ()) {
          
          if (relativePath.endsWith ("/")) {
            
            if (!isAllowGeneratedIndex ())
              return HttpStatus.CLIENT_ERROR_FORBIDDEN;
            
            response.send (new Handler (file).onContent (request, response).toString ());
            
          } else // redirect to the normalized directory URL ending with '/'
            response.setStatus (HttpStatus.REDIRECT_MOVED_PERMANENTLY).redirect (request.getBaseURL () + request.getPath () + "/");
          
        } else if (relativePath.endsWith ("/"))
          return HttpStatus.CLIENT_ERROR_NOT_FOUND; // non-directory ending with slash (File constructor removed it)
        else
          serveFileContent (file, request, response);
        
        return null;
        
      } catch (IOException e) {
        throw new ServerException (e);
      }
      
    }
    
    /**
     * Serves the contents of a file, with its corresponding content type,
     * last modification time, etc. conditional and partial retrievals are
     * handled according to the RFC.
     *
     * @param file the existing and readable file whose contents are served
     * @param request  the request
     * @param response the response into which the content is written
     * @throws ServerException if an error occurs
     */
    protected void serveFileContent (File file, Request request, Response response) throws IOException, ServerException {
      
      long len = file.length ();
      long lastModified = file.lastModified ();
      
      String etag = "W/\"" + lastModified + "\""; // a weak tag based on date
      
      HttpStatus status = HttpStatus.SUCCESS_OK;
      
      // handle range or conditional request
      
      long[] range = request.getRange (len);
      
      if (range == null || len == 0) {
        status = getConditionalStatus (request, lastModified, etag);
      } else {
        String ifRange = request.getHeaders ().get ("If-Range");
        if (ifRange == null) {
          if (range[0] >= len)
            status = HttpStatus.CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE; // unsatisfiable range
          else
            status = getConditionalStatus (request, lastModified, etag);
        } else if (range[0] >= len) {
          // RFC2616#14.16, 10.4.17: invalid If-Range gets everything
          range = null;
        } else { // send either range or everything
          if (!ifRange.startsWith ("\"") && !ifRange.startsWith ("W/")) {
            Date date = request.getHeaders ().getDate ("If-Range");
            if (date != null && lastModified > date.getTime ())
              range = null; // modified - send everything
          } else if (!ifRange.equals (etag)) {
            range = null; // modified - send everything
          }
        }
      }
      
      switch (status) {
        
        case REDIRECT_NOT_MODIFIED: { // no other headers or body allowed
          
          response.getHeaders ().add ("ETag", etag);
          response.getHeaders ().add ("Vary", "Accept-Encoding");
          response.getHeaders ().add ("Last-Modified", response.formatDate (lastModified));
          
          response.setStatus (status).sendHeaders ();
          
          break;
          
        }
        
        case CLIENT_ERROR_PRECONDITION_FAILED:
          response.setStatus (status).sendHeaders ();
          break;
          
        case CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE: {
          
          response.getHeaders ().add ("Content-Range", "bytes */" + len);
          
          response.setStatus (status).sendHeaders ();
          
          break;
          
        }
        
        case SUCCESS_OK: { // send OK response
          
          response
            .setStatus (status)
            .sendHeaders (len, lastModified, etag, getContentType (file.getName (), "application/octet-stream"), range);
          
          try (InputStream in = new FileInputStream (file)) { // send body
            response.sendBody (in, len, range);
          }
          
          break;
          
        }
        
        default:
          response.setStatus (HttpStatus.SERVER_ERROR_INTERNAL).sendHeaders (); // should never happen
          break;
          
      }
      
    }
    
    /**
     * Returns the content type for the given path, according to its suffix,
     * or the given default content type if none can be determined.
     *
     * @param path the path whose content type is requested
     * @param def  a default content type which is returned if none can be
     *             determined
     * @return the content type for the given path, or the given default
     */
    protected String getContentType (String path, String def) {
      
      int dot = path.lastIndexOf ('.');
      
      String type = dot < 0 ? def : contentTypes.get (path.substring (dot + 1).toLowerCase (Locale.US));
      
      return type != null ? type : def;
      
    }
    
    /**
     * Matches the given ETag value against the given ETags. A match is found
     * if the given ETag is not null, and either the ETags contain a "*" value,
     * or one of them is identical to the given ETag. If strong comparison is
     * used, tags beginning with the weak ETag prefix "W/" never match.
     * See RFC2616#3.11, RFC2616#13.3.3.
     *
     * @param strong if true, strong comparison is used, otherwise weak
     *               comparison is used
     * @param etags  the ETags to match against
     * @param etag   the ETag to match
     * @return true if the ETag is matched, false otherwise
     */
    protected boolean match (boolean strong, List<String> etags, String etag) {
      
      if (etag == null || strong && etag.startsWith ("W/"))
        return false;
      
      for (String e : etags)
        if (e.equals ("*") || (e.equals (etag) && !(strong && (e.startsWith ("W/")))))
          return true;
      
      return false;
      
    }
    
    /**
     * Calculates the appropriate response status for the given request and
     * its resource's last-modified time and ETag, based on the conditional
     * headers present in the request.
     *
     * @param request          the request
     * @param lastModified the resource's last modified time
     * @param etag         the resource's ETag
     * @return the appropriate response status for the request
     */
    protected HttpStatus getConditionalStatus (Request request, long lastModified, String etag) {
      
      Headers headers = request.getHeaders ();
      
      // If-Match
      String header = headers.get ("If-Match");
      
      if (header != null && !match (true, server.split (header, false), etag))
        return HttpStatus.CLIENT_ERROR_PRECONDITION_FAILED;
      
      // If-Unmodified-Since
      Date date = headers.getDate ("If-Unmodified-Since");
      
      if (date != null && lastModified > date.getTime ())
        return HttpStatus.CLIENT_ERROR_PRECONDITION_FAILED;
      
      // If-Modified-Since
      HttpStatus status = HttpStatus.SUCCESS_OK;
      
      boolean force = false;
      
      date = headers.getDate ("If-Modified-Since");
      
      if (date != null && date.getTime () <= System.currentTimeMillis ()) {
        
        if (lastModified > date.getTime ())
          force = true;
        else
          status = HttpStatus.REDIRECT_NOT_MODIFIED;
        
      }
      
      // If-None-Match
      header = headers.get ("If-None-Match");
      
      if (header != null) {
        
        if (match (false, server.split (header, false), etag)) // RFC7232#3.2: use weak matching
          status = request.getMethod ().equals (HttpMethod.GET)
                     || request.getMethod ().equals (HttpMethod.HEAD) ? HttpStatus.REDIRECT_NOT_MODIFIED : HttpStatus.CLIENT_ERROR_PRECONDITION_FAILED;
        else
          force = true;
        
      }
      
      return force ? HttpStatus.SUCCESS_OK : status;
      
    }
    
    @Override
    public void serve (Request request, Response response) throws ServerException {
      response.setStatus (serveFile (request.getContext ().getPath (), request, response));
    }
    
  }