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
  
  package upl2.parser;
  
  import upl.core.Int;
  import upl.core.Log;
  
  public class RecursiveDescentParser {
    
    protected final char[] stream;
    protected int column = 0;
    protected char ch;
    protected float integer = 0;
    protected String string = "";
    protected int token = 0;
    
    protected final static int semicolon = 0;
    protected final static int period = 1;
    protected final static int plus = 2;
    protected final static int minus = 3;
    protected final static int times = 4;
    protected final static int divide = 5;
    protected final static int assign = 6;
    protected final static int lparen = 7;
    protected final static int rparen = 8;
    protected final static int number = 10;
    
    public RecursiveDescentParser (char[] stream) {
      this.stream = stream;
    }
    
    public int getTree () {
      
      next ();
      statement ();
      
      expression ();
      statement (); // flush ';'
      
      return token;
      
    }
    
    protected void statement () {
      
      while (ch == ' ') next ();
      
      if (Character.isDigit (ch)) {
        
        token = number;
        
        integer = 0;
        
        try {
          
          while (Character.isDigit (ch)) {
            
            integer = Int.intval (ch);
            next ();
            
          }
          
        } catch (ArrayIndexOutOfBoundsException e) {
          // empty
        }
        
      } else {
        
        switch (ch) {
          
          case ';':
            token = semicolon;
            next ();
            break;
          
          case '.':
            token = period;
            next ();
            break;
          
          case '+':
            token = plus;
            next ();
            break;
          
          case '-':
            token = minus;
            next ();
            break;
          
          case '*':
            token = times;
            next ();
            break;
          
          case '/':
            token = divide;
            next ();
            break;
          
          case '=':
            token = assign;
            next ();
            break;
          
          case '(':
            token = lparen;
            next ();
            break;
          
          case ')':
            token = rparen;
            next ();
            break;
          
          default:
            error ("Illegal character " + ch);
            break;
          
        }
        
      }
      
    }
    
    protected float factor () { // factor = number | '(' expression ')'
      
      float value = 0;
      
      switch (token) {
        
        case number: {
          
          value = integer;
          statement (); // flush number
          
          break;
          
        }
        
        case lparen: {
          
          statement ();
          value = expression ();
          
          if (token != rparen)
            error ("Missing ')'");
          
          statement (); // flush ')'
          
          break;
          
        }
        
        default:
          error ("Invalid token " + token + ". Expecting number or (");
          break;
        
      }
      
      return value;
      
    }
    
    protected float term () { // term = factor { ( '*' | '/' ) factor }
      
      float left = factor ();
      
      while (token == times || token == divide) {
        
        int saveToken = token;
        statement ();
        
        switch (saveToken) {
          
          case times:
            left *= factor ();
            break;
          
          case divide:
            left /= factor ();
            break;
          
        }
        
      }
      
      return left;
      
    }
    
    protected float expression () { // expression = term { ( '+' | '-' ) term }
      
      float left = term ();
      
      while (token == plus || token == minus) {
        
        int saveToken = token;
        statement ();
        
        switch (saveToken) {
          
          case plus:
            left += term ();
            break;
          
          case minus:
            left -= term ();
            break;
          
        }
        
      }
      
      return left;
      
    }
    
    protected void error (String msg) {
      Log.w (msg);
    }
    
    protected void next () {
      
      ch = stream[column];
      column++;
      
    }
    
  }