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
  
  package uplx.server;
  
  import java.net.URI;
  import java.util.regex.Matcher;
  import upl.app.Application;
  import upl.core.Log;
  import upl.util.ArrayList;
  import upl.util.List;
  import uplx.server.router.Condition;
  import uplx.server.router.RewriteRule;
  
  public abstract class Router {
    
    protected List<RewriteRule> rules = new ArrayList<> ();
    
    public Application app;
    
    /**
     * Add rewrite rule.
     *
     * @param rule RewriteRule rule
     */
    protected final void addRewriteRule (RewriteRule rule) {
      rules.put (rule);
    }
    
    protected abstract void setRules () throws ServerException;
    
    public final RewriteRule process (Request request, Response response) throws ServerException {
      
      setRules ();
      
      URI uri = request.getURI ();
      
      for (RewriteRule rule : rules) {
        
        if (!rule.checkFlag (RewriteRule.Flag.APPEND_QUERY))
          request.queryParams = new Params ();
        
        if (rule.condition != null) {
          
          if (rule.condition.name.equals (Condition.HTTP_REFERER)) {
            
            rule.regex = response.getHeaders ().get ("Referer");
            rule.url = uri.toString ();
            
          }
          
        } else rule.url = uri.getPath ();
        
        Matcher matcher = rule.getMatcher (rule.regex, rule.url);
        
        if (matcher.find ()) {
          
          rule.matcher = request.matcher = matcher;
          
          if (rule.processor != null)
            rule.handler = rule.processor.process (request, response);
          
          rule.handler.app = app;
          
          return rule;
          
        }
        
        //if (!rule.checkFlag (RewriteRule.Flag.CONTINUE))
        //  break;
        
      }
      
      return null;
      
    }
    
  }