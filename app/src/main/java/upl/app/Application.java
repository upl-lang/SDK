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
  
  package upl.app;
  
  import upl.app.design.Design;
  import upl.app.device.Device;
  import upl.loggers.Console;
  import upl.app.packages.StubPackage;
  import upl.core.Logger;
  import upl.platform.Platform;
  import upl.platform.platforms.OtherPlatform;
  import upl.util.ArrayList;
  import upl.util.HashMap;
  import upl.util.List;
  import upl.util.Map;
  
  public abstract class Application extends upl.type.Object {
    
    public Params params;
    
    protected void setParams (Params params) {
      this.params = params;
    }
    
    protected Package getPackage () {
      return new StubPackage ();
    }
    
    protected Map<String, Localization> localizations = new HashMap<> ();
    
    public Application () {
      
      setLogger (new Console (getClass ()));
      
    }
    
    protected Manifest setManifest () {
      return new Manifest (this);
    }
    
    protected Manifest manifest;
    
    public Manifest getManifest () {
      
      if (manifest == null)
        manifest = setManifest ();
      
      return manifest;
      
    }
    
    public Application addLocalization (Localization local) {
      
      localizations.add (local.getCode (), local);
      
      return this;
      
    }
    
    @SuppressWarnings ("unchecked")
    public <C extends Localization> C getLocalization (String code) {
      
      C local = (C) localizations.get (code);
      
      if (local != null)
        return local;
      else
        throw new NullPointerException ("Localization file with code \"" + code + "\" not found. Use addLocalization method to add it.");
      
    }
    
    protected abstract Design setDesign ();
    
    protected Design design;
    
    public final Design getDesign () {
      
      if (design == null)
        design = setDesign ();
      
      return design;
      
    }
    
    protected void setLogger (Logger log) {
      this.log = log; // TODO
    }
    
    private final List<Platform> platforms = new ArrayList<> ();
    
    public void setPlatform (Platform platform) {
      platforms.put (platform);
    }
    
    protected Platform platform;
    
    public final Platform getPlatform () {
      
      if (platform == null) {
        
        for (Platform platform : platforms) {
          
          if (platform.consume ()) {
            
            for (Platform type : platform.types) {
              
              if (type.consume ()) {
                
                this.platform = type;
                return this.platform;
                
              }
              
            }
            
            this.platform = platform;
            
            return this.platform;
            
          }
          
        }
        
      }
      
      return new OtherPlatform (this);
      
    }
    
    protected Device device;
    
    protected Device setDevice () {
      return new Device (this);
    }
    
    public Device getDevice () {
      
      if (device == null)
        device = setDevice ();
      
      return device;
      
    }
    
  }