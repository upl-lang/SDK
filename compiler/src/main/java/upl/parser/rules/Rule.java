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
  
  package upl.parser.rules;
  
  import upl.parser.Parser;
  import upl.compiler.StringLexeme;
  import upl.compiler.exceptions.UnexpectedTokenException;
  import upl.exceptions.EmptyException;
  
  public class Rule {
    
    protected int tokenIndex = 0;
    
    protected Parser.Lexeme expected;
    
    protected Rule[] rules;
    
    protected Rule (Parser.Lexeme expected) {
      this.expected = expected;
    }
    
    public Rule (Object... rules) {
      
      if (rules.length > 0) {
        
        this.rules = new Rule[rules.length];
        
        for (int i = 0; i < rules.length; i++) {
          
          Rule rule = toRule (rules[i]);
          
          //if (listener != null)
          //  listener.onInit (rule);
          
          this.rules[i] = rule;
          
        }
        
      } else throw new EmptyException ("Rules not found");
      
    }
    
    protected boolean getToken () {
      
      if (tokenIndex < Parser.tokens.length ()) {
        
        Parser.token = Parser.tokens.get (tokenIndex);
        return true;
        
      }
      
      return false;
      
    }
    
    protected Rule toRule (Object obj) {
      
      if (obj instanceof Rule)
        return (Rule) obj;
      else if (obj instanceof Parser.Lexeme)
        return new Rule ((Parser.Lexeme) obj);
      else if (obj instanceof String)
        return new Rule (new StringLexeme (obj.toString ()));
      else
        throw new IllegalArgumentException ();
      
    }
    
    protected final boolean checkToken () {
      return Parser.token.name.equals (expected.getName ());
    }
    
    public final boolean isToken () {
      return expected != null;
    }
    
    protected void setRule () {
      
      //if (listener != null)
      //  listener.process (token);
      //else
      Parser.tokens.add (Parser.token);
      
      tokenIndex++;
      
    }
    
    protected void process () {
      
      if (checkToken ())
        setRule ();
      else
        throw new UnexpectedTokenException (Parser.token, expected);
      
    }
    
    public final void start () {
      
      process ();
      
      //if ((tokenIndex - 1) < tokens.length ())
      //  throw new UnexpectedTokenException (tokens.get (tokenIndex - 1));
      
    }
    
    @Override
    public String toString () {
      return (isToken () ? expected.getName () : getClass ().getSimpleName ());
    }
    
  }