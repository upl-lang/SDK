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
  
  package upl.app;
  
  import upl.json.JSONArray;
  import upl.util.HashMap;
  import upl.util.Map;
  
  public abstract class Params extends Preferences {
    
    protected Map<String, String> shortKeys = new HashMap<> ();
    
    protected Map<String, JSONArray> arrays = new HashMap<> ();
    
    public Params (Application app) {
      super (app);
    }
    
    public void process () {}
    
    @Override
    protected boolean checkValue (String key, Object defValue) {
      
      process ();
      
      return (defValue != null || object.get (key) != null);
      
    }
    
    @Override
    public String toString () {
      
      process ();
      
      return object.toString ();
      
    }
    
    public ArrayValues getArray (String key) {
      return (ArrayValues) opt (key, defValues.get (key));
    }
    
    public Preferences setShortValue (String shortKey, String longKey) {
      
      shortKeys.add (shortKey, longKey);
      
      return this;
      
    }
    
  }