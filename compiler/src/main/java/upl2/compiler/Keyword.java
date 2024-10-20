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
	
	public class Keyword {
		
		// We need to make it as class, not enum, for extension and/or changing its values for several languages
		
		public static String CLASS = "class";
		public static String METHOD = "method";
		public static String RETURN = "return";
		public static String STATIC = "static";
		public static String ABSTRACT = "abstract";
		public static String VOID = "void";
		public static String NEW = "new";
		public static String FINAL = "final";
		
		public static class Visibility {
			
			public static String PUBLIC = "public";
			public static String PROTECTED = "protected";
			public static String PRIVATE = "private";
			
		}
		
	}