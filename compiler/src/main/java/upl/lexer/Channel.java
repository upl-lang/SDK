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
  
  package upl.lexer;
  
  import java.util.regex.Matcher;
  import java.util.regex.Pattern;
  import upl.parser.Parser;
  
  public abstract class Channel {
    
    public Parser parser;
    protected Matcher matcher;
    
    protected int flags;
    
    public Channel () {
      this (0);
    }
    
    public Channel (int flags) {
      this.flags = flags;
    }
    
    public abstract boolean process ();
    
    protected boolean find () {
      
      String regex;
      
      if (parser.token.regex != null)
        regex = parser.token.regex.toString ();
      else
        regex = parser.token.value.toString ().replaceAll ("[.\\\\+*?\\[\\]^$(){}=!<>|:\\-#~]", "\\\\$0");
      
      matcher = Pattern.compile ("^" + regex, flags).matcher (parser.content);
      
      return matcher.find ();
      
    }
    
    protected void setIndex () {
      
      parser.token.index = (parser.index + 1);
      
      parser.index += matcher.end ();
      
    }
    
    protected void setLine (int line) {
      parser.token.line = line;
    }
    
    protected void setValue (String value) {
      parser.token.value = value;
    }
    
  }