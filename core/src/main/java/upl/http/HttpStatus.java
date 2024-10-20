	/*
	 * Copyright (c) 2020 - 2023 UPL Foundation
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
	
	import upl.net.Status;
	
	public enum HttpStatus implements Status {
		
		// 1xx: Informational
		INFO_CONTINUE (100, "Continue"),
		INFO_SWITCHING_PROTOCOLS (101, "Switching Protocols"),
		INFO_PROCESSING (102, "Processing"),
		INFO_EARLY_HINTS (103, "Early Hints"),
		INFO_NAME_NOT_RESOLVED (105, "Name Not Resolved"), // OLD
		
		// 2xx: Success
		SUCCESS_OK (200, "OK"),
		SUCCESS_CREATED (201, "Created"),
		SUCCESS_ACCEPTED (202, "Accepted"),
		SUCCESS_NON_AUTHORITATIVE_INFORMATION (203, "Non-Authoritative Information"),
		SUCCESS_NO_CONTENT (204, "No Content"),
		SUCCESS_RESET_CONTENT (205, "Reset Content"),
		SUCCESS_PARTIAL_CONTENT (206, "Partial Content"),
		SUCCESS_MULTI_STATUS (207, "Multi-Status"),
		SUCCESS_ALREADY_REPORTED (208, "Already Reported"),
		SUCCESS_IM_USED (226, "IM Used"),
		
		/*
     * Currently not in RFC! This response status offers to mean that the parameters (URL or
     * data) of the request have doubtful semantics (perhaps client sent inconsistent data),
     * but nevertheless the request was accepted and processed.
     *
     * Status code     Based on Oleg Tinkov year of birth (1967).
     * Status message	Translation of Oleg's phrase from the interview with Yury Dud
     * "Сомнительно, но окэй" to English.
     */
		SUCCESS_DOUBTFUL_BUT_OKAY (267, "Doubtful But Okay"),
		
		// 3xx: Redirection
		REDIRECT_MULTIPLE_CHOICES (300, "Multiple Choice"),
		REDIRECT_MOVED_PERMANENTLY (301, "Moved Permanently"),
		REDIRECT_FOUND (302, "Found"),
		REDIRECT_SEE_OTHER (303, "See Other"),
		REDIRECT_NOT_MODIFIED (304, "Not Modified"),
		REDIRECT_USE_PROXY (305, "Use Proxy"),
		REDIRECT_TEMPORARY (307, "Temporary Redirect"),
		REDIRECT_PERMANENT (308, "Permanent Redirect"),
		
		// 4xx: Client Error
		CLIENT_ERROR_BAD_REQUEST (400, "Bad Request"),
		CLIENT_ERROR_UNAUTHORIZED (401, "Unauthorized"),
		CLIENT_ERROR_PAYMENT_REQUIRED (402, "Payment Required"),
		CLIENT_ERROR_FORBIDDEN (403, "Forbidden"),
		CLIENT_ERROR_NOT_FOUND (404, "Not Found"),
		CLIENT_ERROR_METHOD_NOT_ALLOWED (405, "Method Not Allowed"),
		CLIENT_ERROR_NOT_ACCEPTABLE (406, "Not Acceptable"),
		CLIENT_ERROR_PROXY_AUTHENTICATION_REQUIRED (407, "Proxy Authentication Required"),
		CLIENT_ERROR_REQUEST_TIMEOUT (408, "Request Timeout"),
		CLIENT_ERROR_CONFLICT (409, "Conflict"),
		CLIENT_ERROR_GONE (410, "Gone"),
		CLIENT_ERROR_LENGTH_REQUIRED (411, "Length Required"),
		CLIENT_ERROR_PRECONDITION_FAILED (412, "Precondition Failed"),
		CLIENT_ERROR_REQUEST_TOO_LONG (413, "Payload Too Large"),
		CLIENT_ERROR_REQUEST_URI_TOO_LONG (414, "URI Too Long"),
		CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE (415, "Unsupported Media Type"),
		CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE (416, "Range Not Satisfiable"),
		CLIENT_ERROR_EXPECTATION_FAILED (417, "Expectation Failed"),
		CLIENT_ERROR_MISDIRECTED_REQUEST (421, "Misdirected Request"),
		CLIENT_ERROR_UNPROCESSABLE_ENTITY (422, "Unprocessable Entity"),
		CLIENT_ERROR_LOCKED (423, "Locked"),
		CLIENT_ERROR_FAILED_DEPENDENCY (424, "Failed Dependency"),
		CLIENT_ERROR_TOO_EARLY (425, "Too Early"),
		CLIENT_ERROR_UPGRADE_REQUIRED (426, "Upgrade Required"),
		CLIENT_ERROR_PRECONDITION_REQUIRED (428, "Precondition Required"),
		CLIENT_ERROR_TOO_MANY_REQUESTS (429, "Too Many Requests"),
		CLIENT_ERROR_REQUEST_HEADER_FIELDS_TOO_LARGE (431, "Request Header Fields Too Large"),
		CLIENT_ERROR_REQUESTED_HOST_UNAVAILABLE (434, "Requested host unavailable"),
		CLIENT_ERROR_RETRY_WITH (449, "Retry With"),
		CLIENT_ERROR_UNAVAILABLE_FOR_LEGAL_REASONS (451, "Unavailable For Legal Reasons"),
		CLIENT_ERROR_UNRECOVERABLE_ERROR (456, "Unrecoverable Error"),
		
		// 5xx: Server Error
		SERVER_ERROR_INTERNAL (500, "Internal Server Error"),
		SERVER_ERROR_NOT_IMPLEMENTED (501, "Not Implemented"),
		SERVER_ERROR_BAD_GATEWAY (502, "Bad Gateway"),
		SERVER_ERROR_SERVICE_UNAVAILABLE (503, "Service Unavailable"),
		SERVER_ERROR_GATEWAY_TIMEOUT (504, "Gateway Timeout"),
		SERVER_ERROR_VERSION_NOT_SUPPORTED (505, "Protocol Version Not Supported"),
		SERVER_ERROR_VARIANT_ALSO_NEGOTIATES (506, "Variant Also Negotiates"),
		SERVER_ERROR_INSUFFICIENT_STORAGE (507, "Insufficient Storage"),
		SERVER_ERROR_LOOP_DETECTED (508, "Loop Detected"),
		SERVER_ERROR_BADWIDTH_LIMIT_EXCEEDED (509, "Bandwidth Limit Exceeded"),
		SERVER_ERROR_NOT_EXTENDED (510, "Not Extended"),
		SERVER_ERROR_NETWORK_AUTHENTICATION_REQUIRED (511, "Network Authentication Required");
		
		private final int code;
		private final String message;
		
		HttpStatus (int code, String message) {
			
			this.code = code;
			this.message = message;
			
		}
		
		@Override
		public int getCode () {
			return code;
		}
		
		@Override
		public String getMessage () {
			return message;
		}
		
		public int getGroup () {
			return Integer.parseInt (Integer.toString (getCode ()).substring (0, 1));
		}
		
		@Override
		public boolean isSuccess () {
			return getGroup () == 2;
		}
		
		@Override
		public String toString () {
			return code + " " + message;
		}
		
		public static HttpStatus get (int value) {
			
			for (HttpStatus status : values ())
				if (status.code == value) return status;
			
			throw new IllegalArgumentException ("Invalid status code: " + value);
			
		}
		
	}