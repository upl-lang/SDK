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
  
  package upl.platform.platforms;
  
  import upl.app.Application;
  import upl.platform.Environment;
  import upl.core.File;
  import upl.platform.Platform;
  
  public class OSXPlatform extends Platform {
    
    public OSXPlatform (Application app) {
      super (app);
    }
    
    @Override
    public String getName () {
      return "OSX";
    }
    
    @Override
    public boolean consume () {
      return getOSName ().toLowerCase ().startsWith ("mac");
    }
    
    @Override
    protected Environment setEnvironment () {
      return new OSXEnvironment (app);
    }
    
    protected static class OSXEnvironment extends Environment {
      
      public OSXEnvironment (Application app) {
        super (app);
      }
      
      @Override
      public File getAsset (String... name) {
        return null;
      }
      
      @Override
      public String getUserDataDir (String appName, String appVersion, String appAuthor, boolean roaming) {
        return buildPath (home (), "/Library/Application Support", appName, appVersion);
      }
      
      @Override
      public String getUserConfigDir (String appName, String appVersion, String appAuthor, boolean roaming) {
        return buildPath (home (), "/Library/Preferences", appName, appVersion);
      }
      
      @Override
      public String getUserCacheDir (String appName, String appVersion, String appAuthor) {
        return buildPath (home (), "/Library/Caches", appName, appVersion);
      }
      
      @Override
      public String getSiteDataDir (String appName, String appVersion, String appAuthor, boolean multiPath) {
        return buildPath ("/Library/Application Support", appName, appVersion);
      }
      
      @Override
      public String getSiteConfigDir (String appName, String appVersion, String appAuthor, boolean multiPath) {
        return buildPath ("/Library/Preferences", appName, appVersion);
      }
      
      @Override
      public String getUserLogDir (String appName, String appVersion, String appAuthor) {
        return buildPath (home (), "/Library/Logs", appName, appVersion);
      }
      
      @Override
      public String getSharedDir (String appName, String appVersion, String appAuthor) {
        return buildPath ("/Users/Shared/Library/Application Support", appName, appVersion);
      }
      
    }
    
  }