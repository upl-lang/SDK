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
  
  import upl.http.HttpStatus;
  import uplx.server.Handler;
  import uplx.server.Request;
  import uplx.server.Response;
  import uplx.server.ServerException;
  
  public class RedirectHandler extends Handler {
    
    String url;
    HttpStatus status;
    
    public RedirectHandler (String url) {
      this (url, HttpStatus.REDIRECT_FOUND);
    }
    
    public RedirectHandler (String url, HttpStatus status) {
      
      this.url = url;
      this.status = status;
      
    }
    
    @Override
    public void serve (Request request, Response response) throws ServerException {
      response.setRedirect (url, status);
    }
    
  }