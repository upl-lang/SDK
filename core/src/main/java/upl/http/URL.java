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
  
  package upl.http;
  
  import java.io.UnsupportedEncodingException;
  import java.net.URLDecoder;
  import upl.type.Strings;
  import upl.util.HashMap;
  import upl.util.List;
  import upl.util.Map;
  
  public class URL {
    
    protected String url;
    protected Strings str;
    
    public URL (String url) {
      
      this.url = url;
      this.str = new Strings (url);
      
    }
    
    public Map<String, String> explodeQuery () {
      return str.explode ("&", "=");
    }
    
    public Map<String, String> decodeHTTPQuery () throws UnsupportedEncodingException {
      
      Map<String, String> output = new HashMap<> ();
      
      List<String> parts1 = new Strings (url.replace ("&amp;", "&")).explode ("&");
      
      for (String part : parts1) {
        
        List<String> parts2 = new Strings (part).explode ("=");
        output.add (parts2.get (0), URLDecoder.decode ((String) parts2.extend (), Strings.DEF_CHARSET));
        
      }
      
      return output;
      
    }
    
  }