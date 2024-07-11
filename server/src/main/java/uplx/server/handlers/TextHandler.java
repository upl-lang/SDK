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
  
  package uplx.server.handlers;
  
  import upl.type.StringTemplate;
  import uplx.server.Handler;
  import uplx.server.ServerException;
  import uplx.server.Request;
  import uplx.server.Response;
  
  public abstract class TextHandler extends Handler {
    
    @Override
    public void serve (Request request, Response response) throws ServerException {
      
      response.getHeaders ().add ("Content-Type", "text/plain");
      
      response.send (onContent (request, response).toString ());
      
    }
    
    protected abstract StringTemplate onContent (Request request, Response response) throws ServerException;
    
    /**
     * Returns an HTML-escaped version of the given string for safe display
     * within a web page. The characters '&amp;', '&gt;' and '&lt;' must always
     * be escaped, and single and double quotes must be escaped within
     * attribute values; this method escapes them always. This method can
     * be used for generating both HTML and XHTML valid content.
     *
     * @param s the string to escape
     * @return the escaped string
     * @see <a href="http://www.w3.org/International/questions/qa-escapes">The W3C FAQ</a>
     */
    public String escapeHTML (String s) {
      
      int len = s.length ();
      
      StringBuilder sb = new StringBuilder (len + 30);
      
      int start = 0;
      
      for (int i = 0; i < len; i++) {
        
        String ref = null;
        
        switch (s.charAt (i)) {
          
          case '&': ref = "&amp;";
            break;
          case '>': ref = "&gt;";
            break;
          case '<': ref = "&lt;";
            break;
          case '"': ref = "&quot;";
            break;
          case '\'': ref = "&#39;";
            break;
          
        }
        
        if (ref != null) {
          
          sb.append (s, start, i).append (ref);
          start = i + 1;
          
        }
        
      }
      
      return start == 0 ? s : sb.append (s.substring (start)).toString ();
      
    }
    
  }