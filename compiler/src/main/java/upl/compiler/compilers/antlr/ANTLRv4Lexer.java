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
  
  package upl.compiler.compilers.antlr;
  
  import upl.lexer.BaseLexer;
  import upl.lexer.Channel;
  import upl.parser.Parser;
  import upl.compiler.Regex;
  import upl.lexer.channels.CommentChannel;
  import upl.lexer.channels.SkipChannel;
  import upl.lexer.rules.Rule;
  import upl.lexer.rules.RuleAnd;
  import upl.lexer.rules.RuleAny;
  import upl.lexer.rules.RuleNot;
  import upl.lexer.rules.RuleOr;
  import upl.lexer.rules.RulePlus;
  import upl.lexer.rules.RuleStar;
  
  public class ANTLRv4Lexer extends BaseLexer {
    
    public ANTLRv4Lexer (StringBuilder content) {
      super (content, ANTLRv4Lexeme.values ());
    }
    
    public enum ANTLRv4Lexeme implements Parser.Lexeme {
      
      // -------------------------
      // Comments
      DOC_COMMENT (DocComment ().setChannel (new CommentChannel ())),
      BLOCK_COMMENT (BlockComment ().setChannel (new CommentChannel ())),
      LINE_COMMENT (LineComment ().setChannel (new CommentChannel ())),
      
      // -------------------------
      // Integer
      
      INT (DecimalNumeral ()),
      STRING_LITERAL (SQuoteLiteral ()),
      UNTERMINATED_STRING_LITERAL (USQuoteLiteral ()),
      
      // -------------------------
      // Arguments
      //
      // Certain argument lists, such as those specifying call parameters
      // to a rule invocation, or input parameters to a rule specification
      // are contained within square brackets.
      //BEGIN_ARGUMENT (LBrack { this.handleBeginArgument(); }),
      // -------------------------
      // Target Language Actions
      BEGIN_ACTION (LBrace ().pushMode ("TargetLanguageAction")),
      // -------------------------
      // Keywords
      //
      // "options", "tokens", and "channels" are considered keywords
      // but only when followed by "{", and considered as a single token.
      // Otherwise, the symbols are tokenized as RULE_REF and allowed as
      // an identifier in a labeledElement.
      
      OPTIONS (new RuleAnd ("options", new RuleStar (WSNLCHARS ()), "{")),
      TOKENS (new RuleAnd ("tokens", new RuleStar (WSNLCHARS ()), "{")),
      CHANNELS (new RuleAnd ("channels", new RuleStar (WSNLCHARS ()), "{")),
      IMPORT ("import"),
      FRAGMENT ("fragment"),
      LEXER ("lexer"),
      PARSER ("parser"),
      GRAMMAR ("grammar"),
      PROTECTED ("protected"),
      PUBLIC ("public"),
      PRIVATE ("private"),
      RETURNS ("returns"),
      LOCALS ("locals"),
      THROWS ("throws"),
      CATCH ("catch"),
      FINALLY ("finally"),
      MODE ("mode"),
      
      // -------------------------
      // Punctuation
      
      COLON (Colon ()),
      COLONCOLON (DColon ()),
      COMMA (Comma ()),
      SEMI (Semi ()),
      LPAREN (LParen ()),
      RPAREN (RParen ()),
      LBRACE (LBrace ()),
      RBRACE (RBrace ()),
      RARROW (RArrow ()),
      LT (Lt ()),
      GT (Gt ()),
      ASSIGN (Equal ()),
      QUESTION (Question ()),
      STAR (Star ()),
      PLUS_ASSIGN (PlusAssign ()),
      PLUS (Plus ()),
      OR (Pipe ()),
      DOLLAR (Dollar ()),
      RANGE (Range ()),
      DOT (Dot ()),
      AT (At ()),
      POUND (Pound ()),
      NOT (Tilde ()),
      
      // -------------------------
      // Identifiers - allows unicode rule/token names
      ID (Id ()),
      
      // -------------------------
      // Whitespace
      WS (new RulePlus (Ws ()).setChannel (new SkipChannel ())),
      
      // -------------------------
      // Illegal Characters
      //
      // This is an illegal character trap which is always the last rule in the
      // lexer specification. It matches a single character of any value and being
      // the last rule in the file will match when no other rule knows what to do
      // about the character. It is reported as an error but is not passed on to the
      // parser. This means that the parser to deal with the gramamr file anyway
      // but we will not try to analyse or code generate from a file with lexical
      // errors.
      
      // Comment this rule out to allow the error to be propagated to the parser
      
      ERRCHAR (new RuleAny ().setChannel (new SkipChannel ())),
      
      // ======================================================
      // Lexer modes
      // -------------------------
      // Arguments
      //mode Argument;
      
      // E.g., [int x, List<String> a[]]
      NESTED_ARGUMENT (LBrack ().setType ("ARGUMENT_CONTENT").pushMode ("Argument")),
      ARGUMENT_ESCAPE (EscAny ().setType ("ARGUMENT_CONTENT")),
      ARGUMENT_STRING_LITERAL (DQuoteLiteral ().setType ("ARGUMENT_CONTENT")),
      ARGUMENT_CHAR_LITERAL (SQuoteLiteral ().setType ("ARGUMENT_CONTENT")),
      //END_ARGUMENT (RBrack { this.handleEndArgument(); }),
      // added this to return non-EOF token type here. EOF does something weird
      UNTERMINATED_ARGUMENT (new Rule (BaseLexeme.EOF).popMode ()),
      ARGUMENT_CONTENT (new RuleAny ()),
      
      // -------------------------
      //mode LexerCharSet;
      
      // TODO: This grammar and the one used in the Intellij Antlr4 plugin differ
      // for "actions". This needs to be resolved at some point.
      // The Intellij Antlr4 grammar is here:
      // https://github.com/antlr/intellij-plugin-v4/blob/1f36fde17f7fa63cb18d7eeb9cb213815ac658fb/src/main/antlr/org/antlr/intellij/plugin/parser/ANTLRv4Lexer.g4#L587
      
      // -------------------------
      // Target Language Actions
      //
      // Many language targets use {} as block delimiters and so we
      // must recursively match {} delimited blocks to balance the
      // braces. Additionally, we must make some assumptions about
      // literal string representation in the target language. We assume
      // that they are delimited by ' or " and so consume these
      // in their own alts so as not to inadvertantly match {}.
      
      //mode TargetLanguageAction;
      
      NESTED_ACTION (
        LBrace ()
          .setType ("ACTION_CONTENT")
          .pushMode ("TargetLanguageAction")
      ),
      
      ACTION_ESCAPE (
        EscAny ()
          .setType ("ACTION_CONTENT")
      ),
      
      ACTION_STRING_LITERAL (
        DQuoteLiteral ()
          .setType ("ACTION_CONTENT")),
      
      ACTION_CHAR_LITERAL (
        SQuoteLiteral ()
          .setType ("ACTION_CONTENT")
      ),
      
      ACTION_DOC_COMMENT (
        DocComment ()
          .setType ("ACTION_CONTENT")
      ),
      
      ACTION_BLOCK_COMMENT (
        BlockComment ()
          .setType ("ACTION_CONTENT")
      ),
      
      ACTION_LINE_COMMENT (
        LineComment ()
          .setType ("ACTION_CONTENT")
      ),
      
      //END_ACTION (RBrace { this.handleEndAction(); }),
      
      UNTERMINATED_ACTION (
        new Rule (BaseLexeme.EOF)
          .popMode ()
      ),
      
      ACTION_CONTENT (new RuleAny ()),
      
      LEXER_CHAR_SET_BODY (
        new RulePlus (
          new RuleOr (
            new RuleNot (new Regex ("[\\]\\]")),
            EscAny ()
          )
        )
      ),
      
      LEXER_CHAR_SET (RBrack ().popMode ()),
      UNTERMINATED_CHAR_SET (new Rule (BaseLexeme.EOF).popMode ());
      
      // ------------------------------------------------------------------------------
      // Grammar specific Keywords, Punctuation, etc.
      
      public static Rule Id () {
        return new RuleAnd (NameStartChar (), new RuleStar (NameChar ()));
      }
      
      public static Rule WSNLCHARS () {
        return new RuleOr (" ", "\t", "\f", "\n", "\r");
      }
      
      public final Object value;
      
      ANTLRv4Lexeme (Object value) {
        this.value = value;
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
    
  }