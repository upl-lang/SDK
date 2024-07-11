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
  
  package upl.compiler.compilers.java;
  
  import upl.lexer.BaseLexer;
  import upl.lexer.Channel;
  import upl.parser.Parser;
  import upl.compiler.Regex;
  import upl.lexer.channels.DefaultChannel;
  import upl.lexer.channels.SkipChannel;
  
  public class JavaLexer extends BaseLexer {
    
    public JavaLexer (StringBuilder builder) {
      super (builder, JavaLexeme.values ());
    }
    
    public enum JavaLexeme implements Parser.Lexeme {
      
      /*IMPORT ("import"),
      DIGIT (JavaDigit () + "+"),
      STRING (JavaLetter () + "+"),
      LPAREN ("\\{"),
      RPAREN ("\\}"),
      WS ("[ \\t\\r\\n\\u000C]+", new SkipChannel ()),
      ;*/
      
      IMPORT ("import"),
      LPAREN ("{"),
      RPAREN ("}"),
      LBRACKET ("("),
      RBRACKET (")"),
      TWO ("=="),
      OPERATOR (new Regex ("[\\+\\-\\*/=;]")),
      DIGIT (JavaDigit ()),
      STRING (JavaLetter ()),
      WS (new Regex ("[ \\t\\r\\n\\u000C]+"), new SkipChannel ()),
      ;
      
      public final Object value;
      public final Channel channel;
      
      JavaLexeme (Object value) {
        this (value, new DefaultChannel ());
      }
      
      JavaLexeme (Object value, int flags) {
        this (value, new DefaultChannel (flags));
      }
      
      JavaLexeme (Object value, Channel channel) {
        
        this.value = value;
        this.channel = channel;
        
      }
      
      @Override
      public String getName () {
        return name ();
      }
      
      @Override
      public Object getValue () {
        return value;
      }
      
      @Override
      public Channel getChannel () {
        return channel;
      }
      
      private static Regex JavaDigit () {
        return new Regex ("[0-9$_]+");
      }
      
      private static Regex JavaLetter () {
        return new Regex ("[a-zA-Z$_]+");
      }
      
    }
    
  }