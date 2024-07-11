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
  
  package upl.parser;
  
  import upl.compiler.Regex;
  import upl.compiler.exceptions.UnexpectedCharacterException;
  import upl.lexer.Channel;
  import upl.parser.rules.Rule;
  import upl.exceptions.EmptyException;
  import upl.util.ArrayList;
  import upl.util.List;
  
  public abstract class Parser {
    
    public static List<Token> tokens = new ArrayList<> ();
    
    public StringBuilder content;
    
    public int index = 0, line = 0;
    
    public static Token token;
    
    public Parser (StringBuilder content, Lexeme[]... lexemes) {
      
      if (lexemes.length == 0)
        throw new EmptyException ("Lexemes not found");
      
      this.content = content;
      
      setTokens (lexemes);
      
    }
    
    protected final void setTokens (Lexeme[]... lexemes) {
      
      while (content.length () > 0) {
        
        for (Lexeme[] lexemes2 : lexemes) {
          
          for (Lexeme lexeme : lexemes2) {
            
            token = new Token ();
            
            token.name = lexeme.getName ();
            token.value = lexeme.getValue ();
            
            if (lexeme instanceof Regex)
              token.regex = (Regex) lexeme;
            
            token.channel = lexeme.getChannel ();
            token.channel.parser = this;
            
            if (token.channel.process ()) {
              
              //Log.w (lexeme);
              tokens.add (token);
              index++;
              
            }
            
          }
          
        }
        
        if (index == 0)
          throw new UnexpectedCharacterException (content, this.index);
        
      }
      
    }
    
    public List<Token> getTokens () {
      return tokens;
    }
    
    public interface Lexeme {
      
      String getName ();
      Object getValue ();
      Channel getChannel ();
      
    }
    
    public Parser (StringBuilder builder) {
      this.content = builder;
    }
    
    public static abstract class Listener {
      
      public abstract void process (Token token);
      public abstract void onInit (Rule rule);
      
    }
    
    protected Listener listener;
    
    public void setListener (Listener listener) {
      this.listener = listener;
    }
    
  }