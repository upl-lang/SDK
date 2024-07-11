  /*
   * Copyright (c) 2020 - 2023 UPL Foundation
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *    http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */
  
  package upl.preferences;
  
  import java.io.IOException;
  import upl.core.File;
  import upl.core.exceptions.OutOfMemoryException;
  import upl.exceptions.PreferencesException;
  import upl.json.JSONException;
  import upl.json.JSONObject;
  import upl.json.JSONTokener;
  
  public class FilePreferences extends JSONObject {
    
    public FilePreferences (File file) throws PreferencesException {
      
      try {
        
        String data = file.read ();
        
        if (!data.equals (""))
          object = ((JSONObject) new JSONTokener (data).nextValue ()).object;
        
      } catch (IOException | OutOfMemoryException | JSONException e) {
        throw new PreferencesException (e);
      }
      
    }
    
  }