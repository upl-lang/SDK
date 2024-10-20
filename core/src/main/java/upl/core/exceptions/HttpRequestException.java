	package upl.core.exceptions;
	/*
	 Created by Acuna on 01.02.2019
	*/

	import upl.http.HttpStatus;

	public class HttpRequestException extends Exception { // TODO to http
		
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