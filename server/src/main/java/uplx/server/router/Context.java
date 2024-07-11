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
  
  package uplx.server.router;
  
  import java.lang.annotation.ElementType;
  import java.lang.annotation.Retention;
  import java.lang.annotation.RetentionPolicy;
  import java.lang.annotation.Target;
  import upl.http.HttpMethod;
  import uplx.server.VirtualHost;
  import uplx.server.Handler;
  
  /**
   * The {@code Context} annotation decorates methods which are mapped
   * to a context (path) within the server, and provide its contents.
   * <p>
   * The annotated methods must have the same signature and contract
   * as {@link Handler#serve}, but can have arbitrary names.
   *
   * @see VirtualHost#addContexts(Object)
   */
  @Retention (RetentionPolicy.RUNTIME)
  @Target (ElementType.METHOD)
  public @interface Context {
    
    /**
     * The context (path) that this field maps to (must begin with '/').
     *
     * @return the context (path) that this field maps to
     */
    String value ();
    
    /**
     * The HTTP methods supported by this context handler (default is HttpMethod.GET).
     *
     * @return the HTTP methods supported by this context handler
     */
    HttpMethod[] methods () default HttpMethod.GET;
    
  }