	/*
	 * Copyright (c) 2020 - 2024 UPL Foundation
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 * 	  http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */
	
	package upl.http;
	/*
	 Created by Acuna on 01.02.2019
	*/
	
	public class HttpRequestException extends Exception {
		
		private HttpStatus code;
		private String content, mess, query;
		
		public HttpRequestException (Exception e) {
			super (e);
		}
		
		public HttpRequestException (String e) {
			super (e);
		}
		
		public HttpRequestException (String mess, HttpStatus code) {
			
			super (mess);
		
			this.code = code;
			
		}
		
		public HttpRequestException (Exception e, String content, HttpStatus code, String mess, String query) {
			
			super (e);
			
			this.code = code;
			this.mess = mess;
			this.content = content;
			this.query = query;
			
		}
		
		@Override
		public String getMessage () {
			
			if (content == null || content.equals ("")) content = super.getMessage ();
			return content;
			
		}
		
		public HttpStatus getHTTPCode () {
			return code;
		}
		
		public String getHTTPMessage () {
			return mess;
		}
		
		public String getHTTPQuery () {
			return query;
		}
		
		@Override
		public Exception getCause () {
			return (Exception) super.getCause ();
		}
		
	}