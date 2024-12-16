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
 
	import java.io.BufferedOutputStream;
	import java.io.DataOutputStream;
	import java.io.FileInputStream;
	import java.io.InputStream;
	import java.io.IOException;
	import java.io.OutputStream;
	import java.io.OutputStreamWriter;
	import java.lang.Math;
	import java.net.Authenticator;
	import java.net.HttpURLConnection;
	import java.net.InetSocketAddress;
	import java.net.PasswordAuthentication;
	import java.net.ProtocolException;
	import java.net.Proxy;
	import java.net.URL;
	import java.net.URLEncoder;
	import java.nio.charset.Charset;
	import java.security.KeyManagementException;
	import java.security.NoSuchAlgorithmException;
	import java.security.SecureRandom;
	import java.security.cert.CertificateException;
	import java.security.cert.X509Certificate;
	import java.util.zip.GZIPInputStream;
	
	import javax.net.ssl.HostnameVerifier;
	import javax.net.ssl.HttpsURLConnection;
	import javax.net.ssl.SSLContext;
	import javax.net.ssl.SSLSession;
	import javax.net.ssl.TrustManager;
	import javax.net.ssl.X509TrustManager;
	import upl.core.Arrays;
	import upl.core.Base64;
	import upl.core.File;
	import upl.core.Int;
	import upl.core.Log;
	import upl.core.Net;
	import upl.core.Random;
	import upl.exceptions.OutOfMemoryException;
	import upl.io.BufferedInputStream;
	import upl.json.JSONArray;
	import upl.json.JSONObject;
	import upl.type.Strings;
	import upl.util.ArrayList;
	import upl.util.HashMap;
	import upl.util.List;
	import upl.util.Map;
	
	public class HttpRequest {
		
		protected HttpURLConnection conn;
		protected HttpStatus code;
		protected int timeout = -1;
		
		protected HttpMethod method;
		public static final int defTimeout = 30000;
		public URL url;
		public String mUrl;
		protected JSONObject params = new JSONObject ();
		
		public HttpMethod[] additionalMethods = new HttpMethod[] { HttpMethod.OPTIONS, HttpMethod.HEAD, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.TRACE };
		
		protected final JSONObject headers = new JSONObject ();
		
		public HttpRequest (HttpMethod method, String url) {
			this (method, url, new HashMap<> ());
		}
		
		public HttpRequest (HttpMethod method, URL url) {
			
			this (method, null, new HashMap<> ());
			
			this.url = url;
			
		}
		
		public HttpRequest (HttpMethod method, String url, Map<String, Object> params) {
			
			this.method = method;
			this.mUrl = url;
			
			setParams (params);
			
			headers.setDefValue ("User-Agent", Net.getUserAgent ());
			
		}
		
		protected void connect () throws HttpRequestException {
			
			try {
				
				if (conn == null) {
					
					if (url == null) url = new URL (mUrl + (params != null ? Net.urlQueryEncode (params) : ""));
					
					if (proxy == null)
						conn = (HttpURLConnection) url.openConnection ();
					else
						conn = (HttpURLConnection) url.openConnection (proxy);
					
					conn.setInstanceFollowRedirects (true); // TODO
					
					if (cookies != null)
						setHeader ("Cookie", cookies);
					
					setMethod (method);
					
					for (String key : headers.keySet ())
						_setHeader (key, headers.get (key));
					
					if (isJSON) {
						
						_setHeader ("Content-Type", "application/json");
						//_setHeader ("Accept", "application/json");
						
					}
					
					if (timeout >= 0) {
						
						conn.setReadTimeout (timeout);
						conn.setConnectTimeout (timeout);
						
					}
					
					if (proxy != null && Int.size (parts) > 1) { // С паролем
						
						Authenticator.setDefault (null); // TODO
						
						Authenticator.setDefault (new Authenticator () {
							
							@Override
							public PasswordAuthentication getPasswordAuthentication () {
								
								String[] cred = parts[0].split (":");
								return new PasswordAuthentication (cred[0], cred[1].toCharArray ());
								
							}
							
						});
						
						//setAuth (new UserPassword ().setName (cred[0]).setPassword (cred[1]));
						
					}
					
				}
				
			} catch (IOException e) {
				throw new HttpRequestException (e);
			}
			
		}
		
		public HttpRequest setParams (Map<String, Object> params) {
			
			for (String key : params.keySet ())
				setParam (key, params.get (key));
			
			return this;
			
		}
		
		public HttpRequest setParam (String key, Object value) {
			
			params.put (key, value);
			return this;
			
		}
		
		public String getUserAgent () {
			return headers.getString ("User-Agent");
		}
		
		protected String cookies;
		
		public HttpRequest setCookies (File file) throws HttpRequestException {
			
			try {
				return setCookies (file.read ().trim ());
			} catch (IOException | OutOfMemoryException e) {
				throw new HttpRequestException (e);
			}
			
		}
		
		public HttpRequest setCookies (String cookies) {
			
			this.cookies = cookies;
			
			return this;
			
		}
		
		public HttpRequest setCookies (Map<String, String> cookies) {
			return setCookies (cookies.implode ("; ", "="));
		}
		
		protected boolean isJSON = false;
		
		public HttpRequest isJSON (boolean yes) {
			
			isJSON = yes;
			return this;
			
		}
		
		public HttpRequest setTimeout (int timeout)	{
			
			this.timeout = timeout;
			return this;
			
		}
		
		public HttpRequest setReferrer (URL referrer) {
			return setReferrer (referrer.toString ());
		}
		
		public HttpRequest setReferrer (String referrer) {
			return setHeader ("Referer", referrer);
		}
		
		protected void setMethod (HttpMethod method) throws HttpRequestException {
			
			try {
				
				if (method != null) {
					
					if (method.equals (HttpMethod.POST))
						_setHeader ("Content-Type", "application/x-www-form-urlencoded");
					
					if (Arrays.contains (method, new HttpMethod[] { HttpMethod.POST, HttpMethod.PUT }))
						conn.setDoOutput (true);
					
					if (Arrays.contains (method, additionalMethods)) {
						
						_setHeader ("X-HTTP-Method-Override", method);
						method = HttpMethod.POST;
						
					}
					
					conn.setRequestMethod (method.name ());
					
				} else throw new HttpRequestException ("Wrong method, methods can be " + Arrays.implode (", ", HttpMethod.values ()));
				
			} catch (ProtocolException e) {
				throw new HttpRequestException (e);
			}
			
		}
		
		public boolean isOK () throws HttpRequestException {
			return (getStatus () == HttpStatus.SUCCESS_OK);
		}
		
		public URL getURL () throws HttpRequestException {
			
			connect ();
			
			return conn.getURL ();
			
		}
		
		public HttpStatus getStatus () throws HttpRequestException {
			
			if (code == null) {
				
				connect ();
				
				try {
					code = HttpStatus.get (conn.getResponseCode ());
				} catch (IOException e) {
					throw new HttpRequestException (e);
				}
				
			}
			
			return code;
			
		}
		
		public String getMessageCode () throws HttpRequestException {
			return getURL () + " " + getStatus ();
		}
		
		public HttpRequest setHeaders (Map<String, Object> data) {
			
			for (String key : data.keySet ())
				setHeader (key, data.get (key));
			
			return this;
			
		}
		
		public HttpRequest setHeader (String key, Object value) {
			
			headers.put (key, value);
			
			return this;
			
		}
		
		protected HttpRequest _setHeader (String key, Object value) {
			
			conn.setRequestProperty (key, value.toString ());
			
			return this;
			
		}
		
		public String getHeader (String key) {
			return conn.getHeaderField (key);
		}
		
		public HttpURLConnection getConnection () {
			return conn;
		}
		
		public String getContent () throws HttpRequestException, OutOfMemoryException {
			return getContent ("");
		}
		
		protected String content;
		
		public String getContent (String type) throws HttpRequestException, OutOfMemoryException {
			
			try {
				
				if (content == null)
					content = getInputStream (type).read ("");
				
				return content;
				
			} catch (IOException e) {
				throw new HttpRequestException (e);
			}
			
		}
		
		public BufferedInputStream getInputStream () throws HttpRequestException, OutOfMemoryException {
			return getInputStream ("");
		}
		
		public BufferedInputStream getInputStream (String type) throws HttpRequestException, OutOfMemoryException {
			
			try {
				
				connect ();
				
				InputStream is = conn.getInputStream ();
				
				if (type.equals (""))
					type = new File (url.toString ()).getExtension ();
				
				switch (type) {
					
					case "gz": {
						
						_setHeader ("Accept", "gzip, deflate");
						
						is = new GZIPInputStream (is);
						
						break;
						
					}
					
				}
				
				return new BufferedInputStream (is);
				
			} catch (IOException e) {
				
				try {
					
					if (conn.getErrorStream () != null)
						throw new HttpRequestException (new BufferedInputStream (conn.getErrorStream ()).read (""), HttpStatus.CLIENT_ERROR_BAD_REQUEST);
					else
						throw new HttpRequestException (e);
					
				} catch (IOException e2) {
					throw new HttpRequestException (e2);
				}
				
			} catch (OutOfMemoryError e) {
				throw new OutOfMemoryException (e);
			}
			
		}
		
		public String getMethod () {
			return method.name ();
		}
		
		public String debug () throws HttpRequestException {
			
			StringBuilder output = new StringBuilder ("curl -X " + getMethod () + " \"" + getURL () + "\"\n");
			
			for (String key : headers.keySet ())
				output.append ("		--header \"" + key + ": " + headers.get (key) + "\"\n");
			
			if (data != null)
				output.append ("		--data \"" + data.toString ().replace ("\"", "\\\"") + "\"\n");
			
			if (cookies != null)
				output.append ("		--cookie \"" + cookies.replace ("\"", "\\\"") + "\"");
			
			return output.toString ();
			
		}
		
		protected BufferedOutputStream setOutputStream () throws HttpRequestException {
			return new BufferedOutputStream (getOutputStream (), File.BUFFER_SIZE);
		}
		
		protected OutputStream outputStream;
		
		public OutputStream getOutputStream () throws HttpRequestException {
			
			try {
				
				if (outputStream == null) outputStream = conn.getOutputStream ();
				return outputStream;
				
			} catch (IOException e) {
				throw new HttpRequestException (e);
			}
			
		}
		
		protected Net.ProgressListener pListener;
		
		public HttpRequest setListener (Net.ProgressListener listener) {
			
			this.pListener = listener;
			return this;
			
		}
		
		public HttpRequest send (InputStream inputStream) throws HttpRequestException {
			return send (new BufferedInputStream (inputStream));
		}
		
		public HttpRequest send (BufferedInputStream inputStream) throws HttpRequestException {
			
			connect ();
			
			try {
				
				OutputStream outputStream = setOutputStream ();
				
				Net.download (inputStream, outputStream, new Net.ProgressListener () {
					
					@Override
					public void onStart (long size) {
						if (pListener != null) pListener.onStart (size);
					}
					
					@Override
					public void onProgress (long length, long size) {
						if (pListener != null) pListener.onProgress (length, size);
					}
					
					@Override
					public void onError (HttpStatus code, String result) {
						if (pListener != null) pListener.onError (code, result);
					}
					
					@Override
					public void onFinish (HttpStatus code) {
						if (pListener != null) pListener.onFinish (code);
					}
					
				});
				
				outputStream.flush ();
				
			} catch (IOException e) {
				throw new HttpRequestException (e);
			}
			
			return this;
			
		}
		
		public HttpRequest send (File file) throws HttpRequestException {
			
			connect ();
			
			try {
				
				_setHeader ("Connection", "Keep-Alive");
				_setHeader ("Content-Type", file.getMimeType ());
				_setHeader ("Content-Disposition", "attachment; filename=\"" + file + "\";");
				
				InputStream is = new FileInputStream (file);
				DataOutputStream dos = new DataOutputStream (getOutputStream ());
				
				int bytesAvailable = is.available ();
				int bufferSize = Math.min (bytesAvailable, File.BUFFER_SIZE);
				
				byte[] buffer = new byte[bufferSize];
				
				long bytesRead = is.read (buffer, 0, bufferSize);
				
				while (bytesRead > 0) {
					
					dos.write (buffer, 0, bufferSize);
					bytesAvailable = is.available ();
					
					bufferSize = Math.min (bytesAvailable, File.BUFFER_SIZE);
					bytesRead = is.read (buffer, 0, bufferSize);
					
				}
				
				dos.flush ();
				
			} catch (IOException e) {
				throw new HttpRequestException (e);
			}
			
			return this;
			
		}
		
		protected Object data;
		
		public HttpRequest send (Object value) throws HttpRequestException, OutOfMemoryException {
			
			isJSON (value instanceof JSONArray || value instanceof JSONObject);
			
			return send (String.valueOf (value).getBytes ());
			
		}
		
		public HttpRequest send (Object value, Charset charset) throws HttpRequestException, OutOfMemoryException { // TODO
			
			isJSON (value instanceof JSONArray || value instanceof JSONObject);
			
			return send (String.valueOf (value).getBytes (charset));
			
		}
		
		public HttpRequest send (byte[] data) throws HttpRequestException, OutOfMemoryException {
			
			try {
				
				//this.data = value;
				
				connect ();
				
				_setHeader ("Content-Length", data.length);
				
				getOutputStream ().write (data);
				
			} catch (IOException e) {
				throw new HttpRequestException (e);
			} catch (OutOfMemoryError e) {
				throw new OutOfMemoryException (e);
			}
			
			return this;
			
		}
		
		public HttpRequest send (Map<String, Object> data) throws HttpRequestException {
			
			isJSON (true);
			
			connect ();
			
			try {
				
				StringBuilder postData = new StringBuilder ();
				
				int i = 0;
				
				for (String key : data.keySet ()) {
					
					if (i > 0) postData.append ('&');
					
					postData.append (URLEncoder.encode (key, Strings.DEF_CHARSET));
					postData.append ('=');
					postData.append (URLEncoder.encode (String.valueOf (data.get (key)), Strings.DEF_CHARSET));
					
					i++;
					
				}
				
				_setHeader ("Content-Type", "application/x-www-form-urlencoded");
				_setHeader ("Content-Length", postData.length ());
				
				OutputStreamWriter writer = new OutputStreamWriter (getOutputStream ());
				
				writer.write (postData.toString ());
				writer.flush ();
				
				return this;
				
			} catch (IOException e) {
				throw new HttpRequestException (e);
			}
			
		}
		
		public HttpRequest setUserAgent (String userAgent) {
			return setHeader ("User-Agent", userAgent);
		}
		
		public HttpRequest setContentLength (long length) {
			return setHeader ("Content-Length", length);
		}
		
		public HttpRequest setContentType (String type) {
			return setHeader ("Content-Type", type);
		}
		
		public long getLength () throws HttpRequestException {
			
			connect ();
			return conn.getContentLength ();
			
		}
		
		public HttpRequest disconnect () throws HttpRequestException {
			
			conn.disconnect ();
			return this;
			
		}
		
		public JSONObject getRequestCookies () {
			return new JSONObject (new Strings (cookies)).explode (";", "=");
		}
		
		public static abstract class Auth {
			
			public abstract String toString (String charset) throws HttpRequestException;
			
		}
		
		public static class Bearer extends Auth {
			
			protected String token;
			
			public Bearer setToken (String token) {
				
				this.token = token;
				return this;
				
			}
			
			@Override
			public String toString (String charset) throws HttpRequestException {
				return "Bearer " + token;
			}
			
		}
		
		public static class UserPassword extends Auth {
			
			protected String username, password;
			
			public UserPassword setName (String name) {
				
				this.username = name;
				return this;
				
			}
			
			public UserPassword setPassword (String password) {
				
				this.password = password;
				return this;
				
			}
			
			@Override
			public String toString (String charset) throws HttpRequestException {
				return "Basic " + Base64.encodeToString (username + ":" + password).trim ();
			}
			
		}
		
		public HttpRequest setAuth (Auth auth) throws HttpRequestException {
			return setAuth (auth, Strings.DEF_CHARSET);
		}
		
		public HttpRequest setAuth (Auth auth, String charset) throws HttpRequestException {
			return setHeader ("Authorization", auth.toString (charset));
		}
		
		protected Proxy proxy;
		protected String[] parts;
		
		public HttpRequest setProxy (Proxy.Type type, String... proxies) {
			
			String proxy = new Random ().generate (proxies);
			
			parts = proxy.split ("@");
			String[] addr;
			
			if (Int.size (parts) == 1) // No password
				addr = parts[0].split (":");
			else
				addr = parts[1].split (":");
			
			return setProxy (new Proxy (type, new InetSocketAddress (addr[0], Integer.parseInt (addr[1]))));
			
		}
		
		public HttpRequest setProxy (Proxy proxy) {
			
			this.proxy = proxy;
			return this;
			
		}
		
		public long length () {
			return conn.getContentLength ();
		}
		
		public interface Listener {
			
			void onConnect (HttpRequest request);
			
		}
		
		protected List<HttpRequest> requests = new ArrayList<> ();
		
		public HttpRequest addRequest (HttpRequest request) {
			
			requests.add (request);
			
			return this;
			
		}
		
		public HttpRequest process (Listener listener) { // TODO
			
			for (HttpRequest request : requests) {
				
				new Thread (new Runnable () {
					
					@Override
					public void run () {
						listener.onConnect (request);
					}
					
				}).start ();
				
			}
			
			return this;
			
		}
		
		public HttpRequest disableSSLCertificateChecking () throws HttpRequestException {
			
			try {
				return disableSSLCertificateChecking (SSLContext.getInstance ("TLS"));
			} catch (NoSuchAlgorithmException e) {
				throw new HttpRequestException (e);
			}
			
		}
		
		public HttpRequest disableSSLCertificateChecking (SSLContext sc) throws HttpRequestException {
			
			try {
				
				connect ();
				
				sc.init (null, new TrustManager[] {new X509TrustManager () {
					
					public X509Certificate[] getAcceptedIssuers () {
						return null;
					}
					
					@Override
					@SuppressWarnings ("TrustAllX509TrustManager")
					public void checkClientTrusted (X509Certificate[] chain, String authType) throws CertificateException {
					}
					
					@Override
					@SuppressWarnings ("TrustAllX509TrustManager")
					public void checkServerTrusted (X509Certificate[] chain, String authType) throws CertificateException {
					}
					
				}}, new SecureRandom ());
				
				((HttpsURLConnection) conn).setSSLSocketFactory (sc.getSocketFactory ());
				
				((HttpsURLConnection) conn).setHostnameVerifier (new HostnameVerifier () {
					
					@Override
					@SuppressWarnings ("BadHostnameVerifier")
					public boolean verify (String hostname, SSLSession session) {
						return true;
					}
					
				});
				
				return this;
				
			} catch (KeyManagementException e) {
				throw new HttpRequestException (e);
			}
			
		}
		
		public java.util.Map<String, java.util.List<String>> getRequestHeaders () throws HttpRequestException {
			
			connect ();
			
			return conn.getRequestProperties ();
			
		}
		
		public java.util.Map<String, java.util.List<String>> getResponseHeaders () throws HttpRequestException {
			
			connect ();
			
			return conn.getHeaderFields ();
			
		}
		
	}