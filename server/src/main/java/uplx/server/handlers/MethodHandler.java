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
  
  import java.lang.reflect.InvocationTargetException;
  import java.lang.reflect.Method;
  import upl.http.HttpStatus;
  import uplx.server.Handler;
  import uplx.server.Request;
  import uplx.server.Response;
  import uplx.server.ServerException;
  import uplx.server.VirtualHost;
  
  /**
   * The {@code MethodHandler} services a context
   * by invoking a handler method on a specified object.
   * <p>
   * The method must have the same signature and contract as
   * {@link Handler#serve}, but can have an arbitrary name.
   *
   * @see VirtualHost#addContexts(Object)
   */
  public class MethodHandler extends Handler {
    
    protected final Method m;
    protected final Object obj;
    
    public MethodHandler (Method m, Object obj) throws IllegalArgumentException {
      
      this.m = m;
      this.obj = obj;
      
      Class<?>[] params = m.getParameterTypes ();
      
      if (
        params.length != 2
          || !Request.class.isAssignableFrom (params[0])
          || !Response.class.isAssignableFrom (params[1])
          || !int.class.isAssignableFrom (m.getReturnType ())
      )
        throw new IllegalArgumentException ("Invalid method signature: " + m);
      
    }
    
    @Override
    public void serve (Request request, Response response) throws ServerException {
      
      try {
        response.setStatus (HttpStatus.get ((Integer) m.invoke (obj, request, response)));
      } catch (InvocationTargetException | IllegalAccessException ite) {
        throw new ServerException (ite.getCause ().getMessage ());
      }
      
    }
    
  }