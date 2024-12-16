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
	
	package upl2.compiler;
	
	import upl2.parser.generators.JavaGenerator;
	import upl.json.JSONArray;
	import upl.json.JSONObject;
	import upl2.parser.Parser;
	import upl.type.Strings;
	
	public abstract class Generator {
		
		protected JSONArray elements;
		protected StringBuilder builder = new StringBuilder ();
		
		protected String indent = "\t";
		
		public int level = 1;
		
		public Generator (Parser parser) {
			//this (parser.getTree ());
		}
		
		public Generator (JSONArray elements) {
			this.elements = elements;
		}
		
		public Generator setIndent (String indent) {
			
			this.indent = indent;
			return this;
			
		}
		
		protected StringBuilder setBlock (String indent) {
			
			builder.append (Strings.LS).append (indent);
			
			return builder;
			
		}
		
		public final void process () {
			process (elements, level);
		}
		
		protected void process (JSONObject element, int level) {
			
			JSONArray body = new JSONArray ();
			
			body.put (element);
			
			process (body, level);
			
		}
		
		protected boolean hasName, isStatic, isAbstract, isFinal;
		
		protected void process (JSONArray elements, int level) {
			
			String indent = this.indent.repeat (level);
			
			for (int i = 0; i < elements.length (); i++) {
				
				JSONObject element = elements.getJSONObject (i);
				
				hasName = !element.isNull (JavaGenerator.Element.NAME) && !element.getString (JavaGenerator.Element.NAME).equals ("");
				isStatic = element.has (JavaGenerator.Keyword.STATIC) && element.getBool (JavaGenerator.Keyword.STATIC);
				isAbstract = element.has (JavaGenerator.Keyword.ABSTRACT) && element.getBool (JavaGenerator.Keyword.ABSTRACT);
				isFinal = element.has (JavaGenerator.Keyword.FINAL) && element.getBool (JavaGenerator.Keyword.FINAL);
				
				setElement (level, indent, i, element);
				
			}
			
		}
		
		protected void setName (JSONObject element, String mess) {
			
			if (hasName)
				builder.append (element.getString (JavaGenerator.Element.NAME));
			else
				throw new GeneratorException (mess, element);
			
		}
		
		protected void setElement (int level, String indent, int i, JSONObject element) {}
		
		@Override
		public String toString () {
			return builder.toString ();
		}
		
	}