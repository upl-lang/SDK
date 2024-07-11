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
  
  import upl.lexer.channels.DefaultChannel;
  import upl.lexer.rules.Rule;
  import upl.lexer.rules.RuleAnd;
  import upl.lexer.rules.RuleAny;
  import upl.lexer.rules.RuleNot;
  import upl.lexer.rules.RuleOr;
  import upl.lexer.rules.RuleQuestion;
  import upl.lexer.rules.RuleStar;
  import upl.compiler.Regex;
  import upl.parser.Parser;
  
  public abstract class BaseLexer extends Parser {
    
    public BaseLexer (StringBuilder content, Lexeme[]... lexemes) {
      super (content, lexemes);
    }
    
    public enum BaseLexeme implements Parser.Lexeme {
      
      EOF,
      WS;
      
      public String value;
      public Channel channel;
      
      BaseLexeme () {}
      
      BaseLexeme (String value) {
        this (value, new DefaultChannel ());
      }
      
      BaseLexeme (String value, int flags) {
        this (value, new DefaultChannel (flags));
      }
      
      BaseLexeme (String value, Channel channel) {
        
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
        return null;
      }
      
    }
    
    // ======================================================
    // Lexer fragments
    //
    // -----------------------------------
    // Whitespace & Comments
    
    public static Rule Ws () {
      return new RuleOr (
        Hws (),
        Vws ()
      );
    }
    
    public static Regex Hws () {
      return new Regex ("[ \t]");
    }
    
    public static Regex Vws () {
      return new Regex ("[\r\n\f]");
    }
    
    public static Rule BlockComment () {
      return new RuleAnd (
        "/*",
        new Regex (".*?"),
        new RuleOr ("*/", BaseLexeme.EOF)
      );
    }
    
    public static Rule DocComment () {
      return new RuleAnd ("/**", new Regex (".*?"), new RuleOr ("*/", BaseLexeme.EOF));
    }
    
    public static Rule LineComment () {
      return new RuleAnd (
        "//",
        new RuleNot (
          new RuleStar (
            new Regex ("[\r\n]")
          )
        )
      );
    }
    
    // -----------------------------------
    // Escapes
    // Any kind of escaped character that we can embed within ANTLR literal strings.
    
    public static Rule EscSeq () {
      return new RuleAnd (
        Esc (),
        new RuleOr (
          new Regex ("[btnfr\"'\\]"),
          UnicodeEsc (),
          new RuleAny (),
          BaseLexeme.EOF
        )
      );
    }
    
    public static Rule EscAny () {
      return new RuleAnd (
        Esc (),
        new RuleAny ()
      );
    }
    
    public static Rule UnicodeEsc () {
      return new RuleAnd (
        "u",
        new RuleQuestion (
          new RuleAnd (
            HexDigit (),
            new RuleQuestion (
              new RuleAnd (
                HexDigit (),
                new RuleQuestion (
                  new RuleAnd (
                    HexDigit (),
                    new RuleQuestion (HexDigit ())
                  )
                )
              )
            )
          )
        )
      );
    }
    
    // -----------------------------------
    // Numerals
    
    public static Rule DecimalNumeral () {
      return new RuleOr (
        "0",
        new RuleAnd (
          new Regex ("[1-9]"),
          new RuleStar (DecDigit ())
        )
      );
    }
    
    // -----------------------------------
    // Digits
    
    public static Regex HexDigit () {
      return new Regex ("[0-9a-fA-F]");
    }
    
    public static Regex DecDigit () {
      return new Regex ("[0-9]");
    }
    
    // -----------------------------------
    // Literals
    
    public static Rule BoolLiteral () {
      return new RuleOr ("true", "false");
    }
    
    public static Rule CharLiteral () {
      return new RuleAnd (
        SQuote (),
        new RuleOr (
          EscSeq (),
          new RuleNot (new Regex ("['\r\n\\]"))
        ),
        SQuote ()
      );
    }
    
    public static Rule SQuoteLiteral () {
      return new RuleAnd (
        SQuote (),
        new RuleStar (
          new RuleOr (
            EscSeq (),
            new RuleNot (new Regex ("['\r\n\\]"))
          )
        ),
        SQuote ()
      );
    }
    
    public static Rule DQuoteLiteral () {
      return new RuleAnd (
        DQuote (),
        new RuleStar (
          new RuleOr (
            EscSeq (),
            new RuleNot (new Regex ("[\"\r\n\\]"))
          )
        ),
        DQuote ()
      );
    }
    
    public static Rule USQuoteLiteral () {
      return new RuleAnd (
        SQuote (),
        new RuleStar (
          new RuleOr (
            EscSeq (),
            new RuleOr (new Regex ("['\r\n\\]"))
          )
        )
      );
    }
    
    // -----------------------------------
    // Character ranges
    
    public static Rule NameChar () {
      return new RuleOr (
        NameStartChar (),
        new Regex ("[0-9]"),
        Underscore (),
        "\u00B7",
        new Regex ("[\u0300-\u036F]"),
        new Regex ("[\u203F-\u2040]")
      );
    }
    
    public static Rule NameStartChar () {
      return new RuleOr (
        new Regex ("[A-Z]"),
        new Regex ("[a-z]"),
        new Regex ("[\u00C0-\u00D6]"),
        new Regex ("[\u00D8-\u00F6]"),
        new Regex ("[\u00F8-\u02FF]"),
        new Regex ("[\u0370-\u037D]"),
        new Regex ("[\u037F-\u1FFF]"),
        new Regex ("[\u200C-\u200D]"),
        new Regex ("[\u2070-\u218F]"),
        new Regex ("[\u2C00-\u2FEF]"),
        new Regex ("[\u3001-\uD7FF]"),
        new Regex ("[\uF900-\uFDCF]"),
        new Regex ("[\uFDF0-\uFFFD]")
        // ignores  new Regex ("['\u10000-'\uEFFFF]");
      );
    }
    
    // -----------------------------------
    // Types
    
    public static Rule Int () {
      return new Rule ("int");
    }
    
    // -----------------------------------
    // Symbols
    
    public static Rule Esc () {
      return new Rule ("\\");
    }
    
    public static Rule Colon () {
      return new Rule (":");
    }
    
    public static Rule DColon () {
      return new Rule ("::");
    }
    
    public static Rule SQuote () {
      return new Rule ("'");
    }
    
    public static Rule DQuote () {
      return new Rule ("\"");
    }
    
    public static Rule LParen () {
      return new Rule ("(");
    }
    
    public static Rule RParen () {
      return new Rule (")");
    }
    
    public static Rule LBrace () {
      return new Rule ("{");
    }
    
    public static Rule RBrace () {
      return new Rule ("}");
    }
    
    public static Rule LBrack () {
      return new Rule ("[");
    }
    
    public static Rule RBrack () {
      return new Rule ("]");
    }
    
    public static Rule RArrow () {
      return new Rule ("->");
    }
    
    public static Rule Lt () {
      return new Rule ("<");
    }
    
    public static Rule Gt () {
      return new Rule (">");
    }
    
    public static Rule Equal () {
      return new Rule ("=");
    }
    
    public static Rule Question () {
      return new Rule ("?");
    }
    
    public static Rule Star () {
      return new Rule ("*");
    }
    
    public static Rule Plus () {
      return new Rule ("+");
    }
    
    public static Rule PlusAssign () {
      return new Rule ("+=");
    }
    
    public static Rule Underscore () {
      return new Rule ("_");
    }
    
    public static Rule Pipe () {
      return new Rule ("|");
    }
    
    public static Rule Dollar () {
      return new Rule ("$");
    }
    
    public static Rule Comma () {
      return new Rule (",");
    }
    
    public static Rule Semi () {
      return new Rule (");");
    }
    
    public static Rule Dot () {
      return new Rule (".");
    }
    
    public static Rule Range () {
      return new Rule ("..");
    }
    
    public static Rule At () {
      return new Rule ("@");
    }
    
    public static Rule Pound () {
      return new Rule ("#");
    }
    
    public static Rule Tilde () {
      return new Rule ("~");
    }
    
  }