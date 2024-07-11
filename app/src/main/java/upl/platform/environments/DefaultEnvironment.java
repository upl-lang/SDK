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
  
  package upl.platform.environments;
  
  import upl.app.Application;
  import upl.platform.Environment;
  import upl.core.Arrays;
  import upl.core.File;
  
  public class DefaultEnvironment extends Environment {
    
    public DefaultEnvironment (Application app) {
      super (app);
    }
    
    public String getProjectAppPath () {
      return getProjectPath () + File.DS + "app";
    }
    
    public String getProjectAppSrcPath () {
      return getProjectAppPath () + File.DS + "src";
    }
    
    public String getProjectAppSrcMainPath () {
      return getProjectAppSrcPath () + File.DS + "main";
    }
    
    public String getProjectAppSrcMainAssetsPath () {
      return getProjectAppSrcMainPath () + File.DS + "assets";
    }
    
    public String getProjectAppSrcMainResPath () {
      return getProjectAppSrcMainPath () + File.DS + "res";
    }
    
    public String getProjectAppSrcMainLangPath () {
      return getProjectAppSrcMainPath () + File.DS + "upl";
    }
    
    public String getProjectAppSrcMainPrefsPath () {
      return getProjectAppSrcMainPath () + File.DS + "prefs";
    }
    
    @Override
    public File getAsset (String... name) {
      return new File (getProjectAppSrcMainAssetsPath () + File.DS + Arrays.implode (File.DS, name));
    }
    
    @Override
    public String getUserDataDir (String appName, String appVersion, String appAuthor, boolean roaming) {
      return null;
    }
    
    @Override
    public String getUserConfigDir (String appName, String appVersion, String appAuthor, boolean roaming) {
      return null;
    }
    
    @Override
    public String getUserCacheDir (String appName, String appVersion, String appAuthor) {
      return null;
    }
    
    @Override
    public String getSiteDataDir (String appName, String appVersion, String appAuthor, boolean multiPath) {
      return null;
    }
    
    @Override
    public String getSiteConfigDir (String appName, String appVersion, String appAuthor, boolean multiPath) {
      return null;
    }
    
    @Override
    public String getUserLogDir (String appName, String appVersion, String appAuthor) {
      return null;
    }
    
    @Override
    public String getSharedDir (String appName, String appVersion, String appAuthor) {
      return null;
    }
    
  }