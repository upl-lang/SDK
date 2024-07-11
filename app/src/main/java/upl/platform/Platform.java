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
  
  package upl.platform;
  
  import java.util.Arrays;
  import upl.app.Application;
  import upl.type.Object;
  import upl.util.ArrayList;
  import upl.util.List;
  
  public abstract class Platform extends Object {
    
    protected Application app;
    
    public Platform (Application app) {
      this.app = app;
    }
    
    public final List<Platform> types = new ArrayList<> ();
    
    public abstract String getName ();
    public abstract boolean consume ();
    
    protected String getOSName () {
      return System.getProperty ("os.name");
    }
    
    protected boolean versionCompare (float value) {
      
      try {
        return Float.parseFloat (System.getProperty ("os.version")) >= value;
      } catch (Exception e) { // TODO
        return false;
      }
      
    }
    
    protected abstract Environment setEnvironment ();
    
    protected Environment environment;
    
    public final Environment getEnvironment () {
      
      if (environment == null)
        environment = setEnvironment ();
      
      return environment;
      
    }
    
    public Platform setType (Platform... platforms) {
      
      types.addAll (Arrays.asList (platforms));
      
      return this;
      
    }
    
  }