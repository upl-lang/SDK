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
  
  public class Element {
    
    public static final String CLASS = "class";
    public static final String TYPE = "type";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String METHOD = "method";
    public static final String RETURN = "return";
    public static final String VISIBILITY = "visibility";
    public static final String BODY = "body";
    public static final String STATIC = "static";
    public static final String VALUE_TYPE = "valueType";
    public static final String RETURN_TYPE = "returnType";
    public static final String CLASS_CALL = "classCall";
    public static final String METHOD_CALL = "methodCall";
    public static final String PROPERTY_CALL = "propertyCall";
    public static final String LITERAL = "literal";
    public static final String PROPERTY = "property";
    public static final String CONDITION = "condition";
    public static final String OPERATOR = "operator";
    public static final String LEFT = "left";
    public static final String RIGHT = "right";
    public static final String BLOCK = "block";
    public static final String ARGUMENTS = "arguments";
    public static final String VARIABLE = "variable";
    public static final String EOF = "eof";
    
    public static class Separator {
      
      public static char SPACE = ' ';
      public static char SEMICOLON = ';';
      public static char OPEN_BRACKET = '(';
      public static char CLOSE_BRACKET = ')';
      public static char OPEN_BRACE = '{';
      public static char CLOSE_BRACE = '}';
      public static char STRING_QUOTE = '\"';
      public static char CHAR_QUOTE = '\'';
      public static char COMMA = ',';
      
    }
    
  }