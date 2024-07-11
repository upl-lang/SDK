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
  
  package upl.app.apps;
  
  import upl.app.Application;
  import upl.app.ApplicationException;
  import upl.app.design.Design;
  import upl.app.apps.cli.CLIDesign;
  import upl.app.params.CLIParams;
  import upl.platform.platforms.OSXPlatform;
  import upl.platform.platforms.WindowsPlatform;
  import upl.platform.platforms.windows.Windows10;
  import upl.platform.platforms.windows.Windows11;
  import upl.platform.platforms.windows.Windows7;
  
  public abstract class CLIApplication extends Application {
    
    public CLIApplication (String[] args) {
      
      setPlatform (
        new WindowsPlatform (this)
          .setType (
            new Windows7 (this),
            new Windows10 (this),
            new Windows11 (this)
          )
      );
      
      setPlatform (new OSXPlatform (this));
      
      setParams (new CLIParams (this, args));
      
    }
    
    public abstract void onShow () throws ApplicationException;
    
    public final void show () throws ApplicationException {
      onShow ();
    }
    
    @Override
    protected Design setDesign () {
      return new CLIDesign (this);
    }
    
  }