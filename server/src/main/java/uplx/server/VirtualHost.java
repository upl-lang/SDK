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
  
  import java.lang.reflect.Method;
  import java.util.Collections;
  import java.util.Map;
  import java.util.Set;
  import java.util.concurrent.ConcurrentHashMap;
  import java.util.concurrent.ConcurrentMap;
  import java.util.concurrent.CopyOnWriteArraySet;
  import upl.app.Application;
  import upl.core.Log;
  import upl.http.HttpMethod;
  import uplx.server.handlers.MethodHandler;
  import uplx.server.router.Context;
  import upl.util.ArrayList;
  import upl.util.List;
  
  /**
   * The {@code VirtualHost} class represents a virtual host in the server.
   */
  public class VirtualHost {
    
    public Application app;
    public Server server;
    
    public List<Object> values = new ArrayList<> (); // Test value
    
    /**
     * The {@code ContextInfo} class holds a single context's information.
     */
    public class ContextInfo {
      
      protected final String path;
      protected final Map<HttpMethod, Handler> handlers = new ConcurrentHashMap<> (2);
      
      /**
       * Constructs a ContextInfo with the given context path.
       *
       * @param path the context path (without trailing slash)
       */
      public ContextInfo (String path) {
        this.path = path;
      }
      
      /**
       * Returns the context path.
       *
       * @return the context path, or null if there is none
       */
      public String getPath () {
        return path;
      }
      
      /**
       * Returns the map of supported HTTP methods and their corresponding handlers.
       *
       * @return the map of supported HTTP methods and their corresponding handlers
       */
      public Map<HttpMethod, Handler> getHandlers () {
        return handlers;
      }
      
      /**
       * Adds (or replaces) a context handler for the given HTTP methods.
       *
       * @param handler the context handler
       * @param methods the HTTP methods supported by the handler (default is HttpMethod.GET)
       */
      public void addHandler (Handler handler, HttpMethod... methods) {
        
        if (methods.length == 0)
          methods = new HttpMethod[] {HttpMethod.GET};
        
        for (HttpMethod method : methods) {
          
          handlers.put (method, handler);
          VirtualHost.this.methods.add (method); // it's now supported by server
          
        }
        
      }
      
    }
    
    protected final String name;
    protected final Set<String> aliases = new CopyOnWriteArraySet<> ();
    protected final Set<HttpMethod> methods = new CopyOnWriteArraySet<> ();
    protected final ContextInfo emptyContext = new ContextInfo (null);
    protected final ConcurrentMap<String, ContextInfo> contexts = new ConcurrentHashMap<> ();
    
    /**
     * Constructs a VirtualHost with the given name.
     *
     * @param name the host's name, or null if it is the default host
     */
    public VirtualHost (Server server, String name) {
      this.server = server;
      this.name = name;
      contexts.put ("*", new ContextInfo (null)); // for "OPTIONS *"
    }
    
    /**
     * Returns this host's name.
     *
     * @return this host's name, or null if it is the default host
     */
    public String getName () {
      return name;
    }
    
    /**
     * Adds an alias for this host.
     *
     * @param alias the alias
     */
    public void addAlias (String alias) {
      aliases.add (alias);
    }
    
    /**
     * Returns this host's aliases.
     *
     * @return the (unmodifiable) set of aliases (which may be empty)
     */
    public Set<String> getAliases () {
      return Collections.unmodifiableSet (aliases);
    }
    
    /**
     * Returns all HTTP methods explicitly supported by at least one context
     * (this may or may not include the methods with required or built-in support).
     *
     * @return all HTTP methods explicitly supported by at least one context
     */
    public Set<HttpMethod> getMethods () {
      return methods;
    }
    
    /**
     * Returns the context handler for the given path.
     * <p>
     * If a context is not found for the given path, the search is repeated for
     * its parent path, and so on until a base context is found. If neither the
     * given path nor any of its parents has a context, an empty context is returned.
     *
     * @param path the context's path
     * @return the context info for the given path, or an empty context if none exists
     */
    public ContextInfo getContext (String path) {
      // all context paths are without trailing slash
      for (path = server.trimRight (path, '/'); path != null; path = server.getParentPath (path)) {
        ContextInfo info = contexts.get (path);
        if (info != null)
          return info;
      }
      return emptyContext;
    }
    
    /**
     * Adds a context and its corresponding context handler to this server.
     * Paths are normalized by removing trailing slashes (except the root).
     *
     * @param path    the context's path (must start with '/')
     * @param handler the context handler for the given path
     * @param methods the HTTP methods supported by the context handler (default is HttpMethod.GET)
     * @throws IllegalArgumentException if path is malformed
     */
    public void addContext (String path, Handler handler, HttpMethod... methods) {
      
      handler.app = app;
      handler.server = server;
      
      if (path == null || !path.startsWith ("/") && !path.equals ("*"))
        throw new IllegalArgumentException ("Invalid path: " + path);
      
      path = server.trimRight (path, '/'); // remove trailing slash
      
      ContextInfo info = new ContextInfo (path);
      ContextInfo existing = contexts.putIfAbsent (path, info);
      
      info = existing != null ? existing : info;
      
      info.addHandler (handler, methods);
      
    }
    
    /**
     * Adds contexts for all methods of the given object that
     * are annotated with the {@link Context} annotation.
     *
     * @param o the object whose annotated methods are added
     * @throws IllegalArgumentException if a Context-annotated
     *                                  method has an {@link Context invalid signature}
     */
    public void addContexts (Object o) throws IllegalArgumentException {
      
      for (Class<?> c = o.getClass (); c != null; c = c.getSuperclass ()) {
        
        // add to contexts those with @Context annotation
        
        for (Method m : c.getDeclaredMethods ()) {
          
          Context context = m.getAnnotation (Context.class);
          
          if (context != null) {
            
            m.setAccessible (true); // allow access to private method
            addContext (context.value (), new MethodHandler (m, o), context.methods ());
            
          }
          
        }
        
      }
      
    }
    
  }