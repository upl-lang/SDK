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
  
  package upl.type;
  
  import java.util.regex.Matcher;
  import java.util.regex.Pattern;
  
  import upl.json.JSONObject;
  
  public class StringTemplate extends Strings {
    
    public StringTemplate (String str) {
      super (str);
    }
    
    public StringTemplate (String str, JSONObject data) {
      
      super (str);
      
      Matcher matcher = Pattern.compile ("\\$([A-z0-9._]+)").matcher (str);
      
      while (matcher.find ())
        str = str.replace (matcher.group (0), prepData (matcher.group (1), data));
      
      matcher = Pattern.compile ("\\$\\{(.+?)\\}").matcher (str);
      
      while (matcher.find ())
        str = str.replace (matcher.group (0), prepData (matcher.group (1), data));
      
      this.str = str;
      
    }
    
    protected String prepData (String var, JSONObject data) {
      
      JSONObject data2 = data;
      
      String[] keys = var.split ("\\.");
      
      for (int i = 0; i < keys.length; i++) {
        
        if (i < (keys.length - 1))
          data2 = data2.getJSONObject (keys[i]);
        else
          return data2.get (keys[i]).toString ();
        
      }
      
      return null;
      
    }
    
  }