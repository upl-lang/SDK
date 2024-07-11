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
  
  import upl.json.JSONObject;
  import upl.type.StringTemplate;
  import uplx.server.Request;
  import uplx.server.Response;
  import uplx.server.ServerException;
  
  public class TestHandler extends HTMLHandler {
    
    @Override
    public StringTemplate onContent (Request request, Response response) throws ServerException {
      
      JSONObject data = new JSONObject ();
      
      data.put ("content", loadTemplate (new JSONObject (), "content3"));
      
      return loadTemplate (data, "content2");
      
    }
    
    @Override
    public StringTemplate onHeader (Request request, Response response) throws ServerException {
      return new StringTemplate ("");
    }
    
    @Override
    public StringTemplate onFooter (Request request, Response response) throws ServerException {
      return new StringTemplate ("");
    }
    
  }