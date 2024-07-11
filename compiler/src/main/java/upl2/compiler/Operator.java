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
  
  public class Operator {
    
    public static class Assignment {
      
      public static String CONCAT = "+=";
      //public static String NULL_COALESCE = "??=";
      
    }
    
    public static String STRING_CONCAT = "+";
    public static String CLASS_CONCAT = ".";
    public static String METHOD_CONCAT = ".";
    public static String SET = "=";
    //public static String NULL_COALESCE = "??";
    
    public static class Comparison {
      
      public static String EQUAL = "==";
      public static String IDENTICAL = "===";
      public static String NOT_EQUAL = "!=";
      public static String NOT_IDENTICAL = "!==";
      public static String LESS_THAN = "<";
      public static String GREATER_THAN = ">";
      public static String LESS_THAN_EQUAL = "<=";
      public static String GREATER_THAN_EQUAL = ">=";
      //public static String SPACESHIP = "<=>";
      
    }
    
    public static class Arithmetic {
      
      public static String ADDITION = "+";
      public static String SUBTRACTION = "-";
      public static String MULTIPLICATION = "*";
      public static String DIVISION = "/";
      public static String MODULUS = "%";
      //public static String EXPONENTIATION = "**";
      public static String PREINCREMENT = "++|";
      public static String PREDECREMENT = "--|";
      public static String POSTINCREMENT = "|++";
      public static String POSTDECREMENT = "|--";
      
    }
    
    public static String INCREMENT = "++";
    public static String DECREMENT = "--";
    
    public static class ArithmeticAssignment {
      
      public static String ADDITION = "+=";
      public static String SUBTRACTION = "-=";
      public static String MULTIPLICATION = "*=";
      public static String DIVISION = "/=";
      public static String MODULUS = "%=";
      //public static String EXPONENTIATION = "**=";
      
    }
    
    public static class Logical {
      
      public static String AND = "&&";
      public static String OR = "||";
      public static String XOR = "^^";
      public static String NOT = "!";
      
    }
    
    public static class Bitwise {
      
      public static String AND = "&";
      public static String OR = "|";
      public static String XOR = "^";
      //public static String NOT = "~";
      public static String LEFT_SHIFT = "<<";
      public static String RIGHT_SHIFT = ">>";
      
    }
    
    public static class BitwiseAssignment {
      
      public static String AND = "&=";
      public static String OR = "|=";
      public static String XOR = "^=";
      public static String LEFT_SHIFT = "<<=";
      public static String RIGHT_SHIFT = ">>=";
      
    }
    
  }