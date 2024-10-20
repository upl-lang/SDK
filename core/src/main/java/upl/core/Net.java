	package upl.core;
		/*
     Created by Acuna on 17.07.2017
		*/
	
	import java.io.InputStream;
	import java.nio.charset.Charset;
	import java.nio.charset.StandardCharsets;
	import upl.http.HttpMethod;
	import upl.json.JSONArray;
	import org.jsoup.Connection;
	import org.jsoup.Jsoup;
	import org.jsoup.nodes.Document;
	import org.jsoup.nodes.Element;
	import org.jsoup.parser.Parser;
	
	import java.io.FileOutputStream;
	import java.io.IOException;
	import java.io.OutputStream;
	import java.io.UnsupportedEncodingException;
	import java.net.MalformedURLException;
	import java.net.URL;
	import java.net.URLEncoder;
	import java.text.ParseException;
	import java.util.Properties;
	
	import upl.core.exceptions.HttpRequestException;
	import upl.core.exceptions.OutOfMemoryException;
	
	import java.lang.String;
	import upl.io.BufferedInputStream;
	import upl.http.HttpRequest;
	import upl.http.HttpStatus;
	import upl.type.Strings;
	import upl.util.ArrayList;
	import upl.util.HashMap;
	import upl.util.List;
	import upl.util.Map;
	
	public class Net {
		
		public static final String URL_PROTOCOL = "protocol";
		public static final String URL_DOMAIN = "domain";
		public static final String URL_AUTHORITY = "authority";
		public static final String URL_PORT = "port";
		public static final String URL_PATH = "path";
		public static final String URL_CANONICAL = "canonical";
		public static final String URL_FILE = "file";
		public static final String URL_QUERY = "query";
		public static final String URL_ANCHOR = "ref";
		
		public static Map<String, String> parseUrl (String mUrl) throws MalformedURLException {
			
			Map<String, String> output = new HashMap<> ();
			
			URL url = new URL (mUrl); // http://example.com:80/docs/books/tutorial/index.html?name=networking#anchor
			
			output.add (URL_PROTOCOL, url.getProtocol ()); // http
			output.add (URL_DOMAIN, url.getHost ()); // example.com
			output.add (URL_AUTHORITY, url.getAuthority ()); // example.com:80
			output.add (URL_PORT, String.valueOf (url.getPort ())); // 80
			output.add (URL_PATH, url.getPath ()); // /docs/books/tutorial/index.html
			output.add (URL_CANONICAL, new File (output.get ("path")).getParent ()); // /docs/books/tutorial
			
			String str = url.getFile ();
			List<String> file = new Strings (str).explode ("?");
			
			if (Int.size (file) > 0) {
				
				int pos = file.get (0).lastIndexOf ("/");
				str = file.get (0).substring ((pos + 1));
				
			}
			
			output.add (URL_FILE, str); // index.html
			output.add (URL_QUERY, url.getQuery ()); // name=networking
			output.add (URL_ANCHOR, url.getRef ()); // anchor
			
			return output;
			
		}
		
		public static String urlQueryEncode (Map<String, ?> query) throws UnsupportedEncodingException {
			return urlQueryEncode (query, true);
		}
		
		public static String urlQueryEncode (Map<String, ?> query, boolean question) throws UnsupportedEncodingException {
			
			List<String> data = new ArrayList<> ();
			
			for (String key : query.keySet ()) {
				
				Object object = query.get (key);
				
				if (object instanceof String[]) {
					
					String[] values = (String[]) object;
					
					for (String value : values)
						data.add (key + "[]=" + urlEncode (value));
					
				} else if (object instanceof JSONArray) {
					
					JSONArray values = (JSONArray) object;
					
					for (int i = 0; i < Int.size (values); ++i)
						data.add (key + "[]=" + urlEncode (values.optString (i)));
					
				} else data.add (key + "=" + urlEncode (object.toString ()));
				
			}
			
			return ((question && query.length () > 0) ? "?" : "") + data.implode ("&");
			
		}
		
		public static String getUserAgent () {
			return getUserAgent ("JabaDaba");
		}
		
		private static String getUserAgent (String name) {
			
			Map<String, String> locale = Locales.getLocaleData ();
			Properties prop = java.lang.System.getProperties ();
			
			return name + "/" + System.version + " (" + prop.getProperty ("os.name") + " " + prop.getProperty ("os.version") + " (" + prop.getProperty ("os.arch") + "); " + locale.get (Locales.COUNTRY) + "; " + locale.get ("lang") + ") " + prop.getProperty ("java.vm.vendor") + " " + prop.getProperty ("java.vm.name") + " " + prop.getProperty ("java.vm.version") + "/" + prop.getProperty ("java.class.version");
			
		}
		
		public static BufferedInputStream getStream (String url) throws HttpRequestException, OutOfMemoryException, IOException {
			return getStream (url, getUserAgent ());
		}
		
		public static BufferedInputStream getStream (String url, String userAgent) throws HttpRequestException, OutOfMemoryException, IOException {
			return getStream (url, userAgent, "");
		}
		
		public static BufferedInputStream getStream (String url, String userAgent, String type) throws HttpRequestException, OutOfMemoryException {
			return new HttpRequest (HttpMethod.GET, url).setUserAgent (userAgent).getInputStream (type); // TODO
		}
		
		public static BufferedInputStream getStream (URL url, String userAgent, String type) throws HttpRequestException, OutOfMemoryException {
			return new HttpRequest (HttpMethod.GET, url).setUserAgent (userAgent).getInputStream (type);
		}
		
		public static String getContent (String url) throws HttpRequestException, OutOfMemoryException {
			return getContent (url, getUserAgent ());
		}
		
		public static String getContent (String url, String userAgent) throws HttpRequestException, OutOfMemoryException {
			return getContent (url, userAgent, "");
		}
		
		public static String getContent (String url, String userAgent, String type) throws HttpRequestException, OutOfMemoryException {
			return new HttpRequest (HttpMethod.GET, url).setUserAgent (userAgent).getContent (type);
		}
		
		public static java.util.List<String> getList (String url) throws HttpRequestException, OutOfMemoryException {
			return getList (url, getUserAgent ());
		}
		
		public static java.util.List<String> getList (String url, String userAgent) throws HttpRequestException, OutOfMemoryException {
			
			try {
				return getStream (url, userAgent).read (new ArrayList<> ());
			} catch (IOException e) {
				throw new HttpRequestException (e);
			}
			
		}
		
		public static boolean isUrl (String content) {
			return isUrl (content, "http|https|ftp");
		}
		
		public static boolean isUrl (String content, String protos) {
			return content.matches ("^(" + protos + ")://([\\w.\\-]+)([^\\s/?.#]+\\.?)+(/[^\\s]*)?");
		}
		
		public static Document toHTML (File file) throws IOException, OutOfMemoryException {
			return toHTML (file.read ());
		}
		
		public static Document toHTML (Element elem) {
			return toHTML (elem.html ());
		}
		
		public static Document toHTML (String html) {
			return Jsoup.parse (html, "", Parser.xmlParser ());
		}
		
		public static Document toHTML (InputStream html) throws IOException {
			return toHTML (new BufferedInputStream (html));
		}
		
		public static Document toHTML (BufferedInputStream html) throws IOException {
			return Jsoup.parse (html, Strings.DEF_CHARSET, "");
		}
		
		public static Document toHTML (URL url) throws IOException {
			return toHTML (url, getUserAgent ());
		}
		
		public static Document toHTML (URL url, String userAgent) throws IOException {
			return toHTML (url, userAgent, "");
		}
		
		public static Document toHTML (URL url, String userAgent, String referrer) throws IOException {
			
			Connection connect = Jsoup.connect (url.toString ());
			
			if (!userAgent.equals ("")) connect.userAgent (userAgent);
			if (!referrer.equals ("")) connect.referrer (referrer);
			
			Document doc = connect.get ();
			
			doc.outputSettings (new Document.OutputSettings ().prettyPrint (false));
			
			return doc;
			
		}
		
		public interface ProgressListener {
			
			void onStart (long size);
			void onProgress (long length, long size);
			void onError (HttpStatus code, String result);
			void onFinish (HttpStatus code);
			
		}
		
		public static void download (BufferedInputStream BufferedInputStream, OutputStream outputStream, ProgressListener listener) throws IOException {
			download (BufferedInputStream, outputStream, listener, -1);
		}
		
		public static void download (BufferedInputStream BufferedInputStream, OutputStream outputStream, ProgressListener listener, long length) throws IOException {
			
			if (length < 0) length = BufferedInputStream.available ();
			if (listener != null) listener.onStart (length);
			
			byte[] buffer = new byte[File.BUFFER_SIZE];
			
			long total = 0, percentDone = -1;
			int bytesRead;
			
			while ((bytesRead = BufferedInputStream.read (buffer)) > 0) {
				
				total += bytesRead;
				
				if (listener != null && percentDone != total) {
					
					percentDone = total;
					listener.onProgress (percentDone, length);
					
				}
				
				outputStream.write (buffer, 0, bytesRead);
				
			}
			
			if (listener != null) {
				
				if (total > 0)
					listener.onFinish (HttpStatus.SUCCESS_NO_CONTENT);
				else
					listener.onError (HttpStatus.SUCCESS_NO_CONTENT, "");
				
			}
			
		}
		
		public static void download (HttpRequest request, File fileName, ProgressListener listener, long length) throws HttpRequestException, OutOfMemoryException {
			
			try {
				
				HttpStatus responseCode = request.getStatus ();
				String result = responseCode.toString ();
				
				if (length < 0) length = request.getLength ();
				if (listener != null) listener.onStart (length);
				
				if (request.isOK ()) {
					
					new File (fileName.getParent ()).makeDir ();
					
					BufferedInputStream BufferedInputStream = request.getInputStream ();
					OutputStream outputStream = new FileOutputStream (fileName);
					
					byte[] buffer = new byte[File.BUFFER_SIZE];
					
					long total = 0, percentDone = -1;
					int bytesRead;
					
					while ((bytesRead = BufferedInputStream.read (buffer)) != -1) {
						
						total += bytesRead;
						
						if (listener != null && percentDone != total) {
							
							percentDone = total;
							listener.onProgress (percentDone, length);
							
						}
						
						outputStream.write (buffer, 0, bytesRead);
						
					}
					
					if (listener != null) {
						
						if (total > 0)
							listener.onFinish (responseCode);
						else
							listener.onError (responseCode, responseCode.getMessage ());
						
					}
					
					//BufferedInputStream.close ();
					//outputStream.close ();
					
				} else throw new IOException (request.getURL () + ": " + result);
				
				request.disconnect ();
				
			} catch (IOException e) {
				throw new HttpRequestException (e);
			}
			
		}
		
		public static String urlEncode (String url) {
			return urlEncode (url, StandardCharsets.UTF_8);
		}
		
		public static String urlEncode (String url, Charset ch) {
			return URLEncoder.encode (url, ch);
		}
		
		protected static Random random = new Random ();
		
		private static String[] randomUserAgentBrowserOS () {
			
			List<Integer[]> keys2 = new ArrayList<> ();
			
			List<List<String[]>> values1 = new ArrayList<> ();
			
			List<String[]> values2 = new ArrayList<> ();
			
			values2.add (new String[] {"chrome", "win"});
			values2.add (new String[] {"chrome", "mac"});
			values2.add (new String[] {"chrome", "linux"});
			
			keys2.add (new Integer[] {89, 9, 2});
			values1.add (values2);
			
			values2 = new ArrayList<> ();
			
			values2.add (new String[] {"iexplorer", "win"});
			
			keys2.add (new Integer[] {100});
			values1.add (values2);
			
			values2 = new ArrayList<> ();
			
			values2.add (new String[] {"firefox", "win"});
			values2.add (new String[] {"firefox", "mac"});
			values2.add (new String[] {"firefox", "linux"});
			
			keys2.add (new Integer[] {83, 16, 1});
			values1.add (values2);
			
			values2 = new ArrayList<> ();
			
			values2.add (new String[] {"safari", "win"});
			values2.add (new String[] {"safari", "mac"});
			values2.add (new String[] {"safari", "linux"});
			
			keys2.add (new Integer[] {95, 4, 1});
			values1.add (values2);
			
			values2 = new ArrayList<> ();
			
			values2.add (new String[] {"opera", "win"});
			values2.add (new String[] {"opera", "mac"});
			values2.add (new String[] {"opera", "linux"});
			
			keys2.add (new Integer[] {91, 6, 3});
			values1.add (values2);
			
			int sum = 0;
			
			int rand = random.generate (1, 100);
			
			Integer[] keys1 = {34, 32, 25, 7, 2};
			
			for (int i = 0; i < Int.size (keys1); ++i) {
				
				sum += keys1[i];
				
				if (rand <= sum) {
					
					sum = 0;
					rand = random.generate (1, 100);
					
					Integer[] keys = keys2.get (i);
					
					for (int i2 = 0; i2 < Int.size (keys); ++i2) {
						
						sum += keys[i2];
						if (rand <= sum) return values1.get (i).get (i2);
						
					}
					
				}
				
			}
			
			return values1.get (0).get (0);
			
		}
		
		public static final String TYPE_DESKTOP = "desktop";
		public static final String TYPE_MOBILE = "mobile";
		
		public static String getRandomUserAgent () {
			return getRandomUserAgent (TYPE_DESKTOP);
		}
		
		public static String getRandomUserAgent (String type) {
			return getRandomUserAgent (type, new String[] {"en-US"});
		}
		
		public static String getRandomUserAgent (String type, String[] lang) {
			
			String agent = "";
			
			switch (type) {
				
				case TYPE_DESKTOP: {
					
					String nt_version = random.generate (5, 6) + "." + random.generate (0, 1);
					String osx_version = "10_" + random.generate (5, 7) + "_" + random.generate (0, 9);
					
					String[] getBrowserOS = randomUserAgentBrowserOS ();
					
					String data = "", ver = "", version = "", version2 = "", extra;
					String browser = getBrowserOS[0];
					String os = getBrowserOS[1];
					
					Map<String, String[]> proc = new HashMap<> ();
					
					proc.add ("linux", new String[] {"i686", "x86_64"});
					proc.add ("mac", new String[] {"Intel", "PPC", "U; Intel", "U; PPC"});
					proc.add ("win", new String[] {"foo"});
					
					switch (browser) {
						
						default: {
							
							extra = Arrays.rand (new String[] {
								
								"",
								"; .NET CLR 1.1." + random.generate (4320, 4325),
								"; WOW64",
								
							});
							
							version = random.generate (7, 9) + ".0";
							version2 = random.generate (3, 5) + "." + random.generate (0, 1);
							
							data = "(compatible; MSIE " + version + "; Windows NT " + nt_version + "; Trident/" + version2 + ")";
							
							agent = "Mozilla/5.0 " + data;
							
							break;
							
						}
						
						case "firefox": {
							
							try {
								
								ver = Arrays.rand (new String[] {
									
									"Gecko/" + new Date ().toString ("yyyyMMdd", random.generate (new Date ().toTime ("2011-01-01", "yyyy-MM-dd"), new Date ().getTimeInMillis ())) + " Firefox/" + random.generate (5, 7) + ".0",
									"Gecko/" + new Date ().toString ("yyyyMMdd", random.generate (new Date ().toTime ("2011-01-01", "yyyy-MM-dd"), new Date ().getTimeInMillis ())) + " Firefox/" + random.generate (5, 7) + ".0.1",
									"Gecko/" + new Date ().toString ("yyyyMMdd", random.generate (new Date ().toTime ("2010-01-01", "yyyy-MM-dd"), new Date ().getTimeInMillis ())) + " Firefox/3.6." + random.generate (1, 20),
									"Gecko/" + new Date ().toString ("yyyyMMdd", random.generate (new Date ().toTime ("2010-01-01", "yyyy-MM-dd"), new Date ().getTimeInMillis ())) + " Firefox/3.8",
									
								});
								
							} catch (ParseException e) {
								// empty
							}
							
							switch (os) {
								
								default:
									data = "(Windows NT " + nt_version + "; " + Arrays.rand (lang) + "; rv:1.9." + random.generate (0, 2) + ".20) " + ver;
									break;
								
								case "linux":
									data = "(X11; Linux " + Arrays.rand ((String[]) proc.get (os)) + "; rv:" + random.generate (5, 7) + ".0) " + ver;
									break;
								
								case "mac":
									data = "(Macintosh; " + Arrays.rand ((String[]) proc.get (os)) + " Mac OS X " + osx_version + " rv:" + random.generate (2, 6) + ".0) " + ver;
									break;
								
							}
							
							agent = "Mozilla/5.0 " + data;
							
							break;
							
						}
						
						case "safari": {
							
							version = random.generate (531, 535) + "." + random.generate (1, 50) + "." + random.generate (1, 7);
							
							if (random.generate (0, 1) == 0)
								ver = random.generate (4, 5) + "." + random.generate (0, 1);
							else
								ver = random.generate (4, 5) + ".0." + random.generate (1, 5);
							
							switch (os) {
								
								default:
									data = "(Windows; U; Windows NT " + nt_version + ") AppleWebKit/" + version + " (KHTML, like Gecko) Version/" + ver + " Safari/" + version;
									break;
								
								case "mac":
									data = "(Macintosh; U; " + Arrays.rand ((String[]) proc.get (os)) + " Mac OS X " + osx_version + " rv:" + random.generate (2, 6) + ".0; " + Arrays.rand (lang) + ") AppleWebKit/" + version + " (KHTML, like Gecko) Version/" + ver + " Safari/" + version;
									break;
								
								case "iphone":
									data = "(iPod; U; CPU iPhone OS " + random.generate (3, 4) + "_" + random.generate (0, 3) + " like Mac OS X; " + Arrays.rand (lang) + ") AppleWebKit/" + version + " (KHTML, like Gecko) Version/" + random.generate (3, 4) + ".0.5 Mobile/8B" + random.generate (111, 119) + " Safari/6" + version;
									break;
								
							}
							
							agent = "Mozilla/5.0 " + data;
							
							break;
							
						}
						
						case "opera": {
							
							extra = Arrays.rand (new String[] {
								
								"",
								"; .NET CLR 1.1." + random.generate (4320, 4325) + ".0",
								"; WOW64",
								
							});
							
							version = "2.9." + random.generate (160, 190);
							version2 = random.generate (10, 12) + ".00";
							
							switch (os) {
								
								default:
									data = "(Windows NT " + nt_version + "; U; " + Arrays.rand (lang) + ") Presto/" + version + " Version/" + version2;
									break;
								
								case "linux":
									data = "(X11; Linux " + Arrays.rand ((String[]) proc.get (os)) + "; U; " + Arrays.rand (lang) + ") Presto/" + version + " Version/" + version2;
									break;
								
							}
							
							agent = "Opera/" + random.generate (8, 9) + "." + random.generate (10, 99) + " " + data;
							
							break;
							
						}
						
						case "chrome": {
							
							version = random.generate (531, 536) + random.generate (0, 2) + "";
							version2 = random.generate (13, 15) + ".0." + random.generate (800, 899) + ".0";
							
							switch (os) {
								
								default:
									data = "(Windows NT " + nt_version + ") AppleWebKit/" + version + " (KHTML, like Gecko) Chrome/" + version2 + " Safari/" + version;
									break;
								
								case "linux":
									data = "(X11; Linux " + Arrays.rand ((String[]) proc.get (os)) + ") AppleWebKit/" + version + " (KHTML, like Gecko) Chrome/" + version2 + " Safari/" + version;
									break;
								
								case "mac":
									data = "(Macintosh; U; " + Arrays.rand ((String[]) proc.get (os)) + " Mac OS X " + osx_version + ") AppleWebKit/" + version + " (KHTML, like Gecko) Chrome/" + version2 + " Safari/" + version;
									break;
								
							}
							
							agent = "Mozilla/5.0 " + data;
							
							break;
							
						}
						
					}
					
				}
				
				case TYPE_MOBILE: {
					
					String[] agents = {
						
						"Mozilla/5.0 (Linux; U; Android 4.4; en-us) AppleWebKit/999+ (KHTML, like Gecko) Safari/999.9",
						"Mozilla/5.0 (Linux; U; Android 6.0; zh-cn; HTC_IncredibleS_S710e Build/GRJ90) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
						"Mozilla/5.0 (Linux; U; Android 6.0; en-us; HTC Vision Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
						"Mozilla/5.0 (Linux; U; Android 7.0; fr-fr; HTC Desire Build/GRJ22) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
						"Mozilla/5.0 (Linux; U; Android 6.0; zh-tw; HTC_Pyramid Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
						"Mozilla/5.0 (Linux; U; Android 6.0; zh-tw; HTC_Pyramid Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari",
						"Mozilla/5.0 (Linux; U; Android 6.0; zh-tw; HTC Pyramid Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
						"Mozilla/5.0 (Linux; U; Android 6.0; ko-kr; LG-LU3000 Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
						"Mozilla/5.0 (Linux; U; Android 6.0; en-us; HTC_DesireS_S510e Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
						"Mozilla/5.0 (Linux; U; Android 6.0; en-us; HTC_DesireS_S510e Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile",
						"Mozilla/5.0 (Linux; U; Android 6.0; de-de; HTC Desire Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
						"Mozilla/5.0 (Linux; U; Android 6.0; de-ch; HTC Desire Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
						"Mozilla/5.0 (Linux; U; Android 7.1; fr-lu; HTC Legend Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
						"Mozilla/5.0 (Linux; U; Android 7.1; en-sa; HTC_DesireHD_A9191 Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
						"Mozilla/5.0 (Linux; U; Android 5.1; fr-fr; HTC_DesireZ_A7272 Build/FRG83D) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
						"Mozilla/5.0 (Linux; U; Android 5.1; en-gb; HTC_DesireZ_A7272 Build/FRG83D) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
						"Mozilla/5.0 (Linux; U; Android 5.1; en-ca; LG-P505R Build/FRG83) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
						
					};
					
					agent = Arrays.rand (agents);
					
				}
				
			}
			
			return agent;
			
		}
		
		public static boolean isUrlEmpty (String str) {
			return new Strings (str).hasEnd ("://");
		}
		
		public static String prepUrl (String url) {
			return new Strings (url).addStart ("https:");
		}
		
		public static String getRandomIp () {
			
			int[][] range = {
				
				{607649792,     608174079}, // 36.56.0.0	 - 36.63.255.255
				{1038614528,	 1039007743}, // 61.232.0.0	- 61.237.255.255
				{1783627776,	 1784676351}, // 106.80.0.0	- 106.95.255.255
				{2035023872,	 2035154943}, // 121.76.0.0	- 121.77.255.255
				{2078801920,	 2079064063}, // 123.232.0.0 - 123.235.255.255
				{-1950089216, -1948778497}, // 139.196.0.0 - 139.215.255.255
				{-1425539072, -1425014785}, // 171.8.0.0	 - 171.15.255.255
				{-1236271104, -1235419137}, // 182.80.0.0	- 182.92.255.255
				{-770113536,	 -768606209}, // 210.25.0.0	- 210.47.255.255
				{-569376768,	 -564133889}, // 222.16.0.0	- 222.95.255.255
				
			};
			
			int index = new Random ().nextInt (10);
			
			return num2ip (range[index][0] + new Random ().nextInt (range[index][1] - range[index][0]));
			
		}
		
		public static String num2ip (int ip) {
			return ((ip >> 24) & 0xff) + "." + ((ip >> 16) & 0xff) + "." + ((ip >> 8) & 0xff) + "." + (ip & 0xff);
		}
		
	}