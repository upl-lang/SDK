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
  
  package upl2.parser;
  
  import upl.core.Log;
  import upl.json.JSONArray;
  import upl.json.JSONObject;
  
  public class Tree {
    
    protected final char[] stream;
    
    protected JSONArray tree = new JSONArray (), template = new JSONArray ();
    protected JSONObject item = new JSONObject ();
    
    JSONArray output = new JSONArray ();
    
    protected StringBuilder string = new StringBuilder ();
    
    protected int index = 0;
    protected char ch;
    
    public Tree (char[] stream) {
      this.stream = stream;
    }
    
    public JSONArray process () {
      
      JSONArray output = new JSONArray ();
      
      while (index < stream.length) {
        
        next ();
        
        while (ch == ' ') next ();
        
        if (ch == '(')
          output.put (process ());
        else if (ch == ')')
          break;
        else if (ch == '+' || ch == '-' || ch == '*')
          output.put ("" + ch);
        else
          output.put ("" + ch);
        
      }
      
      return output;
      
    }
    
    protected void error (String msg) {
      Log.w (msg);
    }
    
    protected void next () {
      
      ch = stream[index];
      index++;
      
    }
    
  }