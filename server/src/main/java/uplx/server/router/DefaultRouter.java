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
  
  import java.io.File;
  import java.io.FileInputStream;
  import java.io.FileNotFoundException;
  import java.util.Arrays;
  import uplx.server.Router;
  import uplx.server.ServerException;
  import uplx.server.handlers.FileHandler;
  
  public abstract class DefaultRouter extends Router {
    
    public DefaultRouter () throws ServerException {
      
      try {
        
        File dir = new File (".");
        
        if (!dir.canRead ())
          throw new FileNotFoundException (dir.getAbsolutePath ());
        
        FileHandler handler = new FileHandler (dir);
        
        handler.setAllowGeneratedIndex (true); // with directory index pages
        
        // set up server
        for (File f : Arrays.asList (new File ("/etc/mime.types"), new File (dir, ".mime.types")))
          if (f.exists ())
            handler.addContentTypes (new FileInputStream (f));
        
        //addRewriteRule (new RewriteRule ("/", handler));
        
      } catch (FileNotFoundException e) {
        throw new ServerException (e);
      }
      
    }
    
  }