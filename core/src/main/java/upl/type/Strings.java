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
	
	package upl.type;
	
	import java.util.regex.MatchResult;
	import java.util.regex.Matcher;
	import java.util.regex.Pattern;
	import java.lang.System;
	
	import upl.core.Date;
	import upl.util.ArrayList;
	import upl.core.Arrays;
	import upl.core.Int;
	import upl.util.HashMap;
	import upl.util.LinkedHashMap;
	import upl.util.List;
	import upl.util.Map;
	
	public class Strings {
		
		public static String DEF_CHARSET = "UTF-8";
		public static String LS = System.getProperty ("line.separator");
		
		protected String str;
		
		public Strings (String str) {
			this.str = str;
		}
		
		public boolean pregMatch (String pattern) {
			return str.matches (pattern);
		}
		
		public static String
			spaces = ""
						     + "\\u0009" // CHARACTER TABULATION
						     + "\\u000A" // LINE FEED (LF)
						     + "\\u000B" // LINE TABULATION
						     + "\\u000C" // FORM FEED (FF)
						     + "\\u000D" // CARRIAGE RETURN (CR)
						     + "\\u0020" // SPACE
						     + "\\u0085" // NEXT LINE (NEL)
						     + "\\u00A0" // NO-BREAK SPACE
						     + "\\u1680" // OGHAM SPACE MARK
						     + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
						     + "\\u2000" // EN QUAD
						     + "\\u2001" // EM QUAD
						     + "\\u2002" // EN SPACE
						     + "\\u2003" // EM SPACE
						     + "\\u2004" // THREE-PER-EM SPACE
						     + "\\u2005" // FOUR-PER-EM SPACE
						     + "\\u2006" // SIX-PER-EM SPACE
						     + "\\u2007" // FIGURE SPACE
						     + "\\u2008" // PUNCTUATION SPACE
						     + "\\u2009" // THIN SPACE
						     + "\\u200A" // HAIR SPACE
						     + "\\u2028" // LINE SEPARATOR
						     + "\\u2029" // PARAGRAPH SEPARATOR
						     + "\\u202F" // NARROW NO-BREAK SPACE
						     + "\\u205F" // MEDIUM MATHEMATICAL SPACE
						     + "\\u3000" // IDEOGRAPHIC SPACE
			;
		
		public String trim () {
			return str.replaceAll ("^[" + spaces + "]", "");
		}
		
		public Strings trimStart () {
			return trimStart (" ");
		}
		
		public Strings trimEnd () {
			return trimEnd (" ");
		}
		
		public Strings trimStart (String symb) {
			
			if (str != null && str.length () > 0) {
				
				int symbLength = symb.length ();
				
				if (symbLength > 0) {
					//Log.w (str, str.length (), symbLength);
					if (str.length () > symbLength && hasStart (symb)) // Символ вначале строки
						str = str.substring (symbLength, str.length ());
					else if (str.length () < symbLength)
						new Strings (symb).trimStart (str); // TODO
					else if (str.equals (symb) && str.length () == symbLength)
						str = "";
					
				}
				
			}
			
			return this;
			
		}
		
		public static String trimStart (String str, String symb) {
			
			if (str != null && str.length () > 0) {
				
				int symbLength = symb.length ();
				
				if (symbLength > 0) {
					//Log.w (str, str.length (), symbLength);
					if (str.length () > symbLength && hasStart (str, symb)) // Символ вначале строки
						str = str.substring (symbLength);
					else if (str.length () < symbLength)
						new Strings (symb).trimStart (str); // TODO
					else if (str.equals (symb))
						str = "";
					
				}
				
			}
			
			return str;
			
		}
		
		public Strings trimEnd (String symb) {
			
			if (str != null) {
				
				int length = (str.length () - symb.length ());
				
				if (str.length () > 0 && hasEnd (symb))
					getStart (length);
				
			}
			
			return this;
			
		}
		
		public static String trimEnd (String str, String symb) {
			
			if (str != null) {
				
				int length = (str.length () - symb.length ());
				
				if (str.length () > 0 && hasEnd (str, symb))
					str = getStart (str, length);
				
			}
			
			return str;
			
		}
		
		public Strings trim (String symb) {
			
			trimEnd (symb);
			trimStart (symb);
			
			return this;
			
		}
		
		public Strings getStart (int num) {
			
			str = str.substring (0, num);
			
			return this;
			
		}
		
		public static boolean hasStart (String str, String start) {
			return (getStart (str, start.length ()).equals (start));
		}
		
		public static String getStart (String str, int num) {
			return str.substring (0, num);
		}
		
		public boolean hasStart (String start) {
			return (getStart (start.length ()).toString ().equals (start));
		}
		
		public String getEnd (int num) {
			
			String str = this.str;
			
			int length = str.length ();
			if (length > num) str = str.substring (length - num);
			
			return str;
			
		}
		
		public static String getEnd (String str, int num) {
			
			int length = str.length ();
			if (length > num) str = str.substring (length - num);
			
			return str;
			
		}
		
		public boolean hasEnd (String end) {
			return (getEnd (end.length ()).equals (end));
		}
		
		public static boolean hasEnd (String str, String end) {
			return (getEnd (str, end.length ()).equals (end));
		}
		
		public String addStart (String symb) {
			
			StringBuilder output = new StringBuilder ();
			
			if (str.length () > 0) {
				
				if (!hasStart (symb)) {
					
					output.append (symb);
					output.append (str);
					
				}
				
			} else output.append (symb);
			
			return output.toString ();
			
		}
		
		public String addEnd (String symb) {
			
			StringBuilder output = new StringBuilder ();
			
			if (str.length () > 0) {
				
				if (!hasEnd (symb)) {
					
					output.append (str);
					output.append (symb);
					
				}
				
			} else output.append (symb);
			
			return output.toString ();
			
		}
		
		public String add (String symb) {
			
			str = addStart (symb);
			str = addEnd (symb);
			
			return str;
			
		}
		
		public String suffix (int num, String[] lang) {
			
			String str = String.valueOf (num);
			
			int num1 = (num <= 9 ? num : Integer.parseInt (str.substring (1)));
			int num2 = (num <= 99 ? num : Integer.parseInt (str.substring (2)));
			
			return suffix (num1, num2, lang);
			
		}
		
		private String suffix (int num1, int num2, String[] lang) {
			
			String word = "";
			
			if (Int.size (lang) == 3) {
				
				if (num2 >= 10 && num2 <= 20)
					word = lang[2]; // Малолетки и просто ппц какие старые (10-20 и 110-120) - лет
				else if (num1 == 1) word = lang[0]; // год
				else if (num1 >= 2 && num1 <= 4) word = lang[1]; // года
				else word = lang[2]; // Все остальные возрасты - лет
				
			} else if (Int.size (lang) == 2) {
				
				if (num1 == 1) word = lang[0];
				else word = lang[1];
				
			}
			
			return word;
			
		}
		
		public String prepBr () {
			return prepBr ("<br/>").replaceAll ("\\s*\\n", "");
		}
		
		public String prepBr (String replace) {
			
			for (String find : Arrays.brArray)
				str = str.replace (find, replace);
			
			return str;
			
		}
		
		public String br2nl () {
			return prepBr ("\n");
		}
		
		public String repeat (int num) {
			return String.valueOf (str).repeat (Math.max (0, num));
		}
		
		public String ucfirst () {
			
			if (str != null && !str.equals (""))
				str = Character.toUpperCase (str.charAt (0)) + str.substring (1);
			
			return str;
			
		}
		
		public boolean lcontains (String what, String where) {
			return (Int.size (where) >= Int.size (what) && where.substring (0, Int.size (what)).equals (what));
		}
		
		public String cut (String string, int length) {
			return cut (string, length, "");
		}
		
		public String cut (String string, int length, String end) { // Резалка строк (обрезает до length длины)
			
			if (length > 0 && Int.size (string) > length)
				string = string.substring (0, length) + end;
			
			return string;
			
		}
		
		public static Map<String, String> htmlSpecialChars () {
			
			Map<String, String> chars = new HashMap<> ();
			
			chars.put ("#34", "\"");
			chars.put ("#36", "$");
			chars.put ("#38", "&");
			chars.put ("#39", "'");
			chars.put ("#44", ",");
			chars.put ("#45", "-");
			chars.put ("#46", ".");
			chars.put ("#8218", "‚");
			chars.put ("#47", "/");
			chars.put ("#92", "\\");
			chars.put ("#60", "<");
			chars.put ("#62", ">");
			chars.put ("#64", "@");
			chars.put ("#96", "`");
			chars.put ("#40", "(");
			chars.put ("#41", ")");
			chars.put ("#123", "{");
			chars.put ("#125", "}");
			chars.put ("#124", "|");
			chars.put ("#8211", "–");
			chars.put ("#8212", "—");
			chars.put ("#160", " ");
			chars.put ("#171", "«");
			chars.put ("#187", "»");
			chars.put ("#8220", "“");
			chars.put ("#8221", "”");
			chars.put ("#8222", "„");
			chars.put ("#169", "©");
			chars.put ("#174", "®");
			chars.put ("#8482", "™");
			chars.put ("#8195", "		");
			chars.put ("#8226", "•");
			
			chars.put ("nbsp", chars.get ("#160"));
			chars.put ("amp", chars.get ("#38"));
			chars.put ("lt", chars.get ("#60"));
			chars.put ("gt", chars.get ("#62"));
			chars.put ("quot", chars.get ("#34"));
			chars.put ("laquo", chars.get ("#171"));
			chars.put ("raquo", chars.get ("#187"));
			chars.put ("copy", chars.get ("#169"));
			chars.put ("reg", chars.get ("#174"));
			chars.put ("emsp", chars.get ("#8195"));
			chars.put ("bull", chars.get ("#8226"));
			
			return chars;
			
		}
		
		public Strings (StringBuilder str) {
			this (str.toString ());
		}
		
		public static String htmlSpecialCharsDecode (String str) {
			
			Map<String, String> chars = htmlSpecialChars ();
			
			for (String key : new String[] {"amp", "#38"})
				str = str.replace ("&" + key + ";", chars.get (key));
			
			str = str.replaceAll ("&\\s*(#?)\\s*([0-9]+)\\s*;", "&$1$2;");
			
			for (String key : chars.keySet ())
				str = str.replace ("&" + key + ";", chars.get (key));
			
			str = str.replace (" \n", "\n");
			
			return str;
			
		}
		
		public String htmlSpecialCharsDecode () {
			
			Map<String, String> chars = htmlSpecialChars ();
			
			for (String key : new String[] {"amp", "#38"})
				str = str.replace ("&" + key + ";", chars.get (key));
			
			str = str.replaceAll ("&\\s*(#?)\\s*([0-9]+)\\s*;", "&$1$2;");
			
			for (String key : chars.keySet ())
				str = str.replace ("&" + key + ";", chars.get (key));
			
			str = str.replace (" \n", "\n");
			
			return str;
			
		}
		
		@Override
		public String toString () {
			return str;
		}
		
		public String yearCopyright (int year) {
			
			int newYear = Integer.parseInt (new Date ().toString ("yyyy"));
			
			String output = String.valueOf (year);
			if (newYear > year) output += " - " + newYear;
			
			return output;
			
		}
		
		public boolean isEmpty () {
			return isEmpty (str);
		}
		
		public static boolean isEmpty (String str) {
			return (str == null || str.isEmpty ());
		}
		
		public Strings slice (String str2) {
			return slice (str2, true);
		}
		
		public Strings slice (String str2, boolean revert) {
			
			int start = 0;
			boolean right = (Int.size (str2) > str.length ());
			
			for (int i = 0; i < Int.size ((right ? str : str2)); ++i) {
				
				String letter1 = str.substring (i, (i + 1));
				String letter2 = str2.substring (i, (i + 1));
				
				if (letter1.equals (letter2))
					++start;
				
			}
			
			if (start > 0) getStart (start);
			if (revert) new Strings (str2).trimStart (str);
			
			return this;
			
		}
		
		public interface Callback {
			String match (MatchResult matchResult);
			
		}
		
		public String replace (String regex, Callback callback) {
			
			StringBuffer resultString = new StringBuffer ();
			Matcher regexMatcher = Pattern.compile (regex).matcher (str);
			
			while (regexMatcher.find ()) {
				regexMatcher.appendReplacement (resultString, callback.match (regexMatcher));
			}
			
			regexMatcher.appendTail (resultString);
			
			return resultString.toString ();
			
		}
		
		public String translit () {
			
			Map<String, String> letters = new HashMap<> ();
			
			letters.put ("\u0410", "A");
			letters.put ("\u0411", "B");
			letters.put ("\u0412", "V");
			letters.put ("\u0413", "G");
			letters.put ("\u0414", "D");
			letters.put ("\u0415", "E");
			letters.put ("\u0401", "Yo");
			letters.put ("\u0416", "Zh");
			letters.put ("\u0417", "Z");
			letters.put ("\u0418", "I");
			letters.put ("\u0419", "J");
			letters.put ("\u041a", "K");
			letters.put ("\u041b", "L");
			letters.put ("\u041c", "M");
			letters.put ("\u041d", "N");
			letters.put ("\u041e", "O");
			letters.put ("\u041f", "P");
			letters.put ("\u0420", "R");
			letters.put ("\u0421", "S");
			letters.put ("\u0422", "T");
			letters.put ("\u0423", "U");
			letters.put ("\u0424", "F");
			letters.put ("\u0425", "H");
			letters.put ("\u0426", "C");
			letters.put ("\u0427", "Ch");
			letters.put ("\u0428", "Sh");
			letters.put ("\u0429", "Sch");
			letters.put ("\u042a", "");
			letters.put ("\u042b", "Y");
			letters.put ("\u042c", "");
			letters.put ("\u042d", "Ye");
			letters.put ("\u042e", "Yu");
			letters.put ("\u042f", "Ya");
			letters.put ("\u0407", "Yi");
			letters.put ("\u0404", "Ye");
			
			letters.put ("\u0430", "a");
			letters.put ("\u0431", "b");
			letters.put ("\u0432", "v");
			letters.put ("\u0433", "g");
			letters.put ("\u0434", "d");
			letters.put ("\u0435", "e");
			letters.put ("\u0451", "yo");
			letters.put ("\u0436", "zh");
			letters.put ("\u0437", "z");
			letters.put ("\u0438", "i");
			letters.put ("\u0439", "j");
			letters.put ("\u043a", "k");
			letters.put ("\u043b", "l");
			letters.put ("\u043c", "m");
			letters.put ("\u043d", "n");
			letters.put ("\u043e", "o");
			letters.put ("\u043f", "p");
			letters.put ("\u0440", "r");
			letters.put ("\u0441", "s");
			letters.put ("\u0442", "t");
			letters.put ("\u0443", "u");
			letters.put ("\u0444", "f");
			letters.put ("\u0445", "h");
			letters.put ("\u0446", "c");
			letters.put ("\u0447", "ch");
			letters.put ("\u0448", "sh");
			letters.put ("\u0449", "sch");
			letters.put ("\u044a", "");
			letters.put ("\u044b", "y");
			letters.put ("\u044c", "");
			letters.put ("\u044d", "ye");
			letters.put ("\u044e", "yu");
			letters.put ("\u044f", "ya");
			letters.put ("\u0457", "i");
			letters.put ("\u0454", "ie");
			
			for (String key : letters.keySet ())
				str = str.replace (key, letters.get (key));
			
			return str;
			
		}
		
		public String altName () {
			
			Map<String, String> chars = htmlSpecialChars ();
			
			for (String key : chars.keySet ())
				str = str.replace ("&" + chars.get (key) + ";", "");
			
			str = str.toLowerCase ().replaceAll ("\\s+", "-");
			
			return str;
			
		}
		
		public String toCamelCase () {
			
			char first = Character.toUpperCase (str.charAt (0));
			
			if (str.length () == 1)
				return Character.toString (first);
			else
				return first + str.substring (1).toLowerCase ();
			
		}
		
		public String toCamelCase (String split) { // TODO
			
			StringBuilder sb = new StringBuilder ();
			
			for (String s : str.split (split)) {
				
				if (sb.length () > 0)
					sb.append (split);
				
				sb.append (toCamelCase (s));
				
			}
			
			return sb.toString ();
			
		}
		
		public String replace (String find, String replace) {
			return str.replace (find, replace);
		}
		
		public String replace (String[] find, String[] replace) {
			
			for (int i = 0; i < Int.size (find); i++)
				str = replace (find[i], replace[i]);
			
			return str;
			
		}
		
		public String replace (String find, String replace, int index) { // TODO Need test!!!
			
			StringBuilder output = new StringBuilder ();
			
			index = str.indexOf (find, index);
			
			output.append (0, index);
			
			if (index < index + Int.size (replace))
				output.append (str.substring (index, index + Int.size (replace)).replace (find, replace));
			else
				output.append (str.substring (index).replace (find, replace));
			
			return output.toString ();
			
		}
		
		public String replace (String find, String replace, int[] indexes) { // TODO Need test!!!
			
			StringBuilder output = new StringBuilder ();
			
			int start = 0, index = 0;
			
			while (index > -1) {
				
				if (Arrays.contains (index, indexes)) {
					
					index = str.indexOf (find, index);
					
					output.append (start, index);
					
					if (index < index + Int.size (replace))
						output.append (str.substring (index, index + Int.size (replace)).replace (find, replace));
					else
						output.append (str.substring (index).replace (find, replace));
					
				} else output.append (start, index);
				
				start = index;
				
				index++;
				
			}
			
			return output.toString ();
			
		}
		
		public String[] pregExplode (String preg) {
			return ((str == null || str.equals ("")) ? new String[0] : str.split (preg));
		}
		
		public List<String> explode (int indexStart, int indexEnd, List<String> output) {
			
			if (indexEnd < 0) indexEnd = str.length ();
			
			String sequence = str.substring (indexStart, indexEnd);
			output.add (sequence);
			
			return output;
			
		}
		
		public List<String> explode (String symb) {
			return explode (symb, new ArrayList<> ());
		}
		
		public List<String> explode (String symb, List<String> output) {
			
			int sLength = symb.length ();
			int indexStart = 0;
			int index = str.indexOf (symb);
			
			explode (indexStart, index, output);
			
			while (index >= 0) {
				
				indexStart = index + sLength;
				index = str.indexOf (symb, index + 1);
				
				explode (indexStart, index, output);
				
			}
			
			return output;
			
		}
		
		public Map<String, String> explode (String sep1, String sep2) {
			return explode (sep1, sep2, new LinkedHashMap<> ());
		}
		
		public Map<String, String> explode (String sep1, String sep2, Map<String, String> output) {
			
			for (String item : explode (sep1)) {
				
				if (!item.equals ("")) {
					
					String[] values = item.split (sep2);
					output.add (values[0].trim (), (values.length > 1 ? values[1] : ""));
					
				}
				
			}
			
			return output;
			
		}
		
		public String[] explodeSent () {
			return pregExplode ("(?i)\\s*(\\.|!|\\?|\\n|<br.*?>)\\s*");
		}
		
		public String unHex (String arg, int radix) {
			
			StringBuilder str = new StringBuilder ();
			
			for (int i = 0; i < arg.length (); i += 2) {
				
				String s = arg.substring (i, (i + 2));
				int decimal = Integer.parseInt (s, radix);
				
				str.append ((char) decimal);
				
			}
			
			return str.toString ();
			
		}
		
		public String randomHex (int length) {
			
			String randomChars = "ABCDEF0123456789";
			
			StringBuilder result = new StringBuilder ();
			
			for (int i = 0; i < length; ++i)
				result.append (randomChars.charAt ((int) java.lang.Math.floor (java.lang.Math.random () * randomChars.length ())));
			
			return result.toString ();
			
		}
		
	}