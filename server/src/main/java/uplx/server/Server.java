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
  import java.net.InetSocketAddress;
  import java.net.ServerSocket;
  import java.util.Locale;
  import java.util.Map;
  import java.util.Set;
  import java.util.TimeZone;
  import java.util.concurrent.ConcurrentHashMap;
  import java.util.concurrent.ExecutorService;
  import java.util.concurrent.Executors;
  import javax.net.ServerSocketFactory;
  import javax.net.ssl.SSLServerSocketFactory;
  import upl.app.Application;
  import upl.util.List;
  import upl.util.ArrayList;
  
  /**
   * The {@code Server} class implements a light-weight HTTP server.
   * <p>
   * This server implements all functionality required by RFC 2616 ("Hypertext
   * Transfer Protocol -- HTTP/1.1"), as well as some of the optional
   * functionality (this is termed "conditionally compliant" in the RFC).
   * In fact, a couple of bugs in the RFC itself were discovered
   * (and fixed) during the development of this server.
   * <p>
   * <b>Feature Overview</b>
   * <ul>
   * <li>RFC compliant - correctness is not sacrificed for the sake of size</li>
   * <li>Virtual hosts - multiple domains and subdomains per server</li>
   * <li>File serving - built-in handler to serve files and folders from disk</li>
   * <li>Mime type mappings - configurable via API or a standard mime.types file</li>
   * <li>Directory index generation - enables browsing folder contents</li>
   * <li>Welcome files - configurable default filename (e.g. index.html)</li>
   * <li>All HTTP methods supported - GET/HEAD/OPTIONS/TRACE/POST/PUT/DELETE/custom</li>
   * <li>Conditional statuses - ETags and If-* header support</li>
   * <li>Chunked transfer encoding - for serving dynamically-generated data streams</li>
   * <li>Gzip/deflate compression - reduces bandwidth and download time</li>
   * <li>HTTPS - secures all server communications</li>
   * <li>Partial content - download continuation (a.k.a. byte range serving)</li>
   * <li>File upload - multipart/form-data handling as stream or iterator</li>
   * <li>Multiple context handlers - a different handler method per URL path</li>
   * <li>@Context annotations - auto-detection of context handler methods</li>
   * <li>Parameter parsing - from query string or x-www-form-urlencoded body</li>
   * <li>A single source file - super-easy to integrate into any application</li>
   * <li>Standalone - no dependencies other than the Java runtime</li>
   * <li>Small footprint - standard jar is ~50K, stripped jar is ~35K</li>
   * <li>Extensible design - easy to override, add or remove functionality</li>
   * <li>Reusable utility methods to simplify your custom code</li>
   * <li>Extensive documentation of API and implementation (&gt;40% of source lines)</li>
   * </ul>
   * <p>
   * <b>Use Cases</b>
   * <p>
   * Being a lightweight, standalone, easily embeddable and tiny-footprint
   * server, it is well-suited for
   * <ul>
   * <li>Resource-constrained environments such as embedded devices.
   *     For really extreme constraints, you can easily remove unneeded
   *     functionality to make it even smaller (and use the -Dstripped
   *     maven build option to strip away debug info, license, etc.)</li>
   * <li>Unit and integration tests - fast setup/teardown times, small overhead
   *     and simple context handler setup make it a great web server for testing
   *     client components under various server response conditions.</li>
   * <li>Embedding a web console into any headless application for
   *     administration, monitoring, or a full portable GUI.</li>
   * <li>A full-fledged standalone web server serving files,
   *     dynamically-generated content, REST APIs, pseudo-streaming, etc.</li>
   * <li>A good reference for learning how HTTP works under the hood.</li>
   * </ul>
   * <p>
   * <b>Implementation Notes</b>
   * <p>
   * The design and implementation of this server attempt to balance correctness,
   * compliance, readability, size, features, extensibility and performance,
   * and often prioritize them in this order, but some trade-offs must be made.
   * <p>
   * This server is multithreaded in its support for multiple concurrent HTTP
   * connections, however most of its constituent classes are not thread-safe and
   * require external synchronization if accessed by multiple threads concurrently.
   * <p>
   * <b>Source Structure and Documentation</b>
   * <p>
   * This server is intentionally written as a single source file, in order to make
   * it as easy as possible to integrate into any existing project - by simply adding
   * this single file to the project sources. It does, however, aim to maintain a
   * structured and flexible design. There are no external package dependencies.
   * <p>
   * This file contains extensive documentation of its classes and methods, as
   * well as implementation details and references to specific RFC sections
   * which clarify the logic behind the code. It is recommended that anyone
   * attempting to modify the protocol-level functionality become acquainted with
   * the RFC, in order to make sure that protocol compliance is not broken.
   * <p>
   * <b>Getting Started</b>
   * <p>
   * For an example and a good starting point for learning how to use the API,
   * follow the code into the API from there. Alternatively, you can just browse
   * through the classes and utility methods and read their documentation and code.
   *
   * @author Amichai Rothman
   * @since 2008-07-24
   */
  public class Server extends ServerSocket {
    
    public Application app;
    
    public static final String VERSION = "1.0";
    
    /**
     * A GMT (UTC) timezone instance.
     */
    protected static final TimeZone GMT = TimeZone.getTimeZone ("GMT");
    
    /**
     * A convenience array containing the carriage-return and line feed chars.
     */
    public static final byte[] CRLF = {0x0d, 0x0a};
    
    protected String address;
    protected volatile int port;
    protected volatile int socketTimeout = 10000;
    protected volatile ServerSocketFactory serverSocketFactory;
    protected volatile boolean secure;
    protected volatile ExecutorService executor;
    protected final Map<String, VirtualHost> hosts = new ConcurrentHashMap<> ();
    
    /**
     * Constructs an Server which can accept connections on the default HTTP port 80.
     * Note: the {@link #start()} method must be called to start accepting connections.
     */
    public Server (Application app) throws ServerException, IOException {
      this (app, "127.0.0.1");
    }
    
    /**
     * Constructs an Server which can accept connections on the default HTTP port 80.
     * Note: the {@link #start()} method must be called to start accepting connections.
     */
    public Server (Application app, String address) throws ServerException, IOException {
      this (app, address, 80);
    }
    
    /**
     * Constructs an Server which can accept connections on the given port.
     * Note: the {@link #start()} method must be called to start accepting
     * connections.
     *
     * @param port the port on which this server will accept connections
     */
    public Server (Application app, String address, int port) throws IOException {
      
      super ();
      
      this.app = app;
      this.address = address;
      
      setPort (port);
      addVirtualHost (new VirtualHost (this, null)); // add default virtual host
      
    }
    
    /**
     * Sets the port on which this server will accept connections.
     *
     * @param port the port on which this server will accept connections
     */
    public void setPort (int port) {
      this.port = port;
    }
    
    /**
     * Gets the port on which this server will accept connections.
     *
     * @return port the port on which this server will accept connections
     */
    public int getPort () {
      return port;
    }
    
    /**
     * Sets the factory used to create the server socket.
     * If null or not set, the default {@link ServerSocketFactory#getDefault()} is used.
     * For secure sockets (HTTPS), use an SSLServerSocketFactory instance.
     * The port should usually also be changed for HTTPS, e.g. port 443 instead of 80.
     * <p>
     * If using the default SSLServerSocketFactory returned by
     * {@link SSLServerSocketFactory#getDefault()}, the appropriate system properties
     * must be set to configure the default JSSE provider, such as
     * {@code javax.net.ssl.keyStore} and {@code javax.net.ssl.keyStorePassword}.
     *
     * @param factory the server socket factory to use
     */
    public void setServerSocketFactory (ServerSocketFactory factory) {
      
      this.serverSocketFactory = factory;
      this.secure = factory instanceof SSLServerSocketFactory;
      
    }
    
    /**
     * Sets the socket timeout for established connections.
     *
     * @param timeout the socket timeout in milliseconds
     */
    public void setSocketTimeout (int timeout) {
      this.socketTimeout = timeout;
    }
    
    /**
     * Sets the executor used in servicing HTTP connections.
     * If null, a default executor is used. The caller is responsible
     * for shutting down the provided executor when necessary.
     *
     * @param executor the executor to use
     */
    public void setExecutor (ExecutorService executor) {
      this.executor = executor;
    }
    
    /**
     * Returns the virtual host with the given name.
     *
     * @param name the name of the virtual host to return,
     *             or null for the default virtual host
     * @return the virtual host with the given name, or null if it doesn't exist
     */
    public VirtualHost getVirtualHost (String name) {
      return hosts.get (name == null ? "" : name);
    }
    
    /**
     * Returns all virtual hosts.
     *
     * @return all virtual hosts (as an unmodifiable set)
     */
    public Set<VirtualHost> getVirtualHosts () {
      return Set.copyOf (hosts.values ());
    }
    
    /**
     * Adds the given virtual host to the server.
     * If the host's name or aliases already exist, they are overwritten.
     *
     * @param host the virtual host to add
     */
    public void addVirtualHost (VirtualHost host) {
      
      host.app = app;
      
      String name = host.getName ();
      hosts.put (name == null ? "" : name, host);
      
    }
    
    public Router router;
    
    public Server setRouter (Router router) {
      
      this.router = router;
      this.router.app = app;
      
      return this;
      
    }
    
    /**
     * Splits the given element list string (comma-separated header value)
     * into its constituent non-empty trimmed elements.
     * (RFC2616#2.1: element lists are delimited by a comma and optional LWS,
     * and empty elements are ignored).
     *
     * @param list  the element list string
     * @param lower specifies whether the list elements should be lower-cased
     * @return the non-empty elements in the list, or an empty array
     */
    public List<String> split (String list, boolean lower) {
      return split (lower && list != null ? list.toLowerCase (Locale.US) : list, ",", -1);
    }
    
    /**
     * Splits the given string into its constituent non-empty trimmed elements,
     * which are delimited by any of the given delimiter characters.
     * This is a more direct and efficient implementation than using a regex
     * (e.g. String.split()), trimming the elements and removing empty ones.
     *
     * @param str        the string to split
     * @param delimiters the characters used as the delimiters between elements
     * @param limit      if positive, limits the returned array size (remaining of str in last element)
     * @return the non-empty elements in the string, or an empty array
     */
    public List<String> split (String str, String delimiters, int limit) {
      
      if (str == null)
        return new upl.util.ArrayList<> ();
      
      List<String> elements = new ArrayList<> ();
      
      int len = str.length ();
      int start = 0;
      int end;
      while (start < len) {
        for (end = --limit == 0 ? len : start;
             end < len && delimiters.indexOf (str.charAt (end)) < 0; end++)
          ;
        String element = str.substring (start, end).trim ();
        if (element.length () > 0)
          elements.add (element);
        start = end + 1;
      }
      
      return elements;
      
    }
    
    /**
     * Returns the parent of the given path.
     *
     * @param path the path whose parent is returned (must start with '/')
     * @return the parent of the given path (excluding trailing slash),
     * or null if given path is the root path
     */
    public String getParentPath (String path) {
      path = trimRight (path, '/'); // remove trailing slash
      int slash = path.lastIndexOf ('/');
      return slash < 0 ? null : path.substring (0, slash);
    }
    
    /**
     * Returns the given string with all occurrences of the given character
     * removed from its right side.
     *
     * @param s the string to trim
     * @param c the character to remove
     * @return the trimmed string
     */
    public String trimRight (String s, char c) {
      int len = s.length () - 1;
      int end;
      for (end = len; end >= 0 && s.charAt (end) == c; end--) ;
      return end == len ? s : s.substring (0, end + 1);
    }
    
    /**
     * Returns the given string with all occurrences of the given character
     * removed from its left side.
     *
     * @param s the string to trim
     * @param c the character to remove
     * @return the trimmed string
     */
    public String trimLeft (String s, char c) {
      int len = s.length ();
      int start;
      for (start = 0; start < len && s.charAt (start) == c; start++) ;
      return start == 0 ? s : s.substring (start);
    }
    
    /**
     * Converts strings to bytes by casting the chars to bytes.
     * This is a fast way to encode a string as ISO-8859-1/US-ASCII bytes.
     * If multiple strings are provided, their bytes are concatenated.
     *
     * @param strings the strings to convert (containing only ISO-8859-1 chars)
     * @return the byte array
     */
    public byte[] getBytes (String... strings) {
      int n = 0;
      for (String s : strings)
        n += s.length ();
      byte[] b = new byte[n];
      n = 0;
      for (String s : strings)
        for (int i = 0, len = s.length (); i < len; i++)
          b[n++] = (byte) s.charAt (i);
      return b;
    }
    
    protected SocketThread thread;
    
    public Server setThread (SocketThread thread) {
      
      this.thread = thread;
      
      return this;
      
    }
    
    /**
     * Starts this server. If it is already started, does nothing.
     * Note: Once the server is started, configuration-altering methods
     * of the server and its virtual hosts must not be used. To modify the
     * configuration, the server must first be stopped.
     *
     * @throws ServerException if the server cannot begin accepting connections
     */
    public void start () throws ServerException {
      
      try {
        
        if (System.getProperty ("javax.net.ssl.keyStore") != null) // enable SSL if configured
          setServerSocketFactory (SSLServerSocketFactory.getDefault ());
        
        //if (serv != null)
        //  return;
        
        if (serverSocketFactory == null) // assign default server socket factory if needed
          serverSocketFactory = ServerSocketFactory.getDefault (); // plain sockets
        
        serverSocketFactory.createServerSocket ();
        
        setReuseAddress (true);
        
        bind (new InetSocketAddress (address, port));
        
        if (executor == null) // assign default executor if needed
          executor = Executors.newCachedThreadPool (); // consumes no resources when idle
        
        // register all host aliases (which may have been modified)
        
        for (VirtualHost host : getVirtualHosts ())
          for (String alias : host.getAliases ())
            hosts.put (alias, host);
        
        if (thread == null)
          thread = new SocketThread (this);
        
        thread.start (); // start handling incoming connections
        
      } catch (IOException e) {
        throw new ServerException (e);
      }
      
    }
    
    /**
     * Stops this server. If it is already stopped, does nothing.
     * Note that if an {@link #setExecutor Executor} was set, it must be closed separately.
     */
    public synchronized void stop () {
      
      try {
        close ();
      } catch (IOException ignore) {
      }
      
    }
    
  }