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
  
  import upl2.compiler.Element;
  import upl.core.Log;
  import upl.json.JSONArray;
  import upl.json.JSONObject;
  
  public class LLParser2 {
    
    protected final char[] stream;
    protected int column = 0;
    protected char ch;
    protected float integer = 0;
    protected StringBuilder string = new StringBuilder ();
    
    protected JSONArray tree = new JSONArray (), block = new JSONArray ();
    protected JSONObject item = new JSONObject ();
    
    public LLParser2 (char[] stream) {
      this.stream = stream;
    }
    
    public JSONArray process () {
      
      while (column < stream.length) {
        
        next ();
        
        while (ch == ' ') next ();
        
        if (ch == '(') {
          
          item = new JSONObject ();
          
        } else if (ch == ')') {
          
          item.put (Element.VALUE, string);
          string = new StringBuilder ();
          
          block.put (item);
          
          tree.put (block);
          
          block = new JSONArray ();
          
        } else string.append (ch);
        
      }
      
      return tree;
      
    }
    
    protected void error (String msg) {
      Log.w (msg);
    }
    
    protected void next () {
      
      ch = stream[column];
      column++;
      
    }
    
  }