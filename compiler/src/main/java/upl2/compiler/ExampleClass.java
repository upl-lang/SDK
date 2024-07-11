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
  
  package upl2.compiler;
  
  import upl.core.Log;
  import upl.core.Random;
  import upl.json.JSONException;
  import upl.json.JSONObject;
  import upl.util.ArrayList;
  import upl.util.HashMap;
  import upl.util.List;
  import upl.util.Map;
  
  public class ExampleClass {
    
    final StringBuilder str = new StringBuilder ();
    protected static int value = 0;
    
    enum Types { TYPE1, TYPE2 }
    
    public interface Listener {
      
      void onAction ();
      
    }
    
    public ExampleClass () {
      super ();
    }
    
    public void method (Object arg, String... args) throws JSONException {
      
      try {
        
        JSONObject data = new JSONObject ();
        
        if (value == 1)
          data.getString ("value1");
        else if (value == 2)
          data.getString ("value2");
        
        str.append (data.implode (", "));
        
      } catch (JSONException e) {
        Log.w (e.getMessage ());
      } finally {
        str.append ("Final");
      }
      
      Map<String, Integer> hashMap = new HashMap<> ();
      
      hashMap.put ("key", 123);
      
      str.append (hashMap.implode (", "));
      
      // Comment
      
      List<Integer> array = new ArrayList<> ();
      
      array.put (123);
      
      str.append (array.implode (", "));
      
      Random random = new Random (value == 1 ? Random.SUMB_LETTERS_LOW : Random.SUMB_LETTERS_UP, Random.SUMB_LETTERS_LOW);
      //String content = content2 = ""; // !!!
      
      boolean check = (random instanceof Random);
      
      int value = 0;
      
      do {
        value++;
      } while (value <= 3);
      
      value = 0;
      
      while (value < 3) {
        
        if (value == 2) continue;
        value++;
        
      }
      
      switch (value) {
        
        case 123: break;
        case 456: break;
        
        default:
          System.out.println (value);
          break;
        
      }
      
    }
    
    @Override
    public String toString () {
      return str.toString ();
    }
    
  }