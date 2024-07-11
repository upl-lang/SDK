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
  
  import org.jsoup.nodes.Document;
  import uplx.server.Handler;
  import uplx.server.Request;
  import uplx.server.Response;
  import uplx.server.ServerException;
  
  public abstract class XMLHandler extends Handler {
    
    @Override
    public void serve (Request request, Response response) throws ServerException {
      
      response.getHeaders ().add ("Content-Type", "application/json; charset=utf-8");
      
      response.send (onContent (request, response).toString ());
      
    }
    
    protected abstract Document onContent (Request request, Response response) throws ServerException;
    
  }
  