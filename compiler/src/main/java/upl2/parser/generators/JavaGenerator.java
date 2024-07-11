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
  
  package upl2.parser.generators;
  
  import upl2.compiler.Generator;
  import upl2.compiler.GeneratorException;
  import upl2.compiler.Operator;
  import upl2.parser.Parser;
  import upl.json.JSONArray;
  import upl.json.JSONObject;
  import upl.type.Strings;
  
  public class JavaGenerator extends Generator {
    
    public static class Element extends upl2.compiler.Element {
      
      public static final String PACKAGE = "package";
      public static final String IMPORT = "import";
      
    }
    
    public static class Keyword extends upl2.compiler.Keyword {
      
      public static String PACKAGE = "package";
      public static String IMPORT = "import";
      
    }
    
    public JavaGenerator (Parser parser) {
      super (parser);
    }
    
    public JavaGenerator (JSONArray classes) {
      super (classes);
    }
    
    @Override
    protected void setElement (int level, String indent, int i, JSONObject element) {
      
      switch (element.getString (Element.TYPE)) {
        
        case Element.PACKAGE: setPackage (indent, element); break;
        case Element.IMPORT: setImport (indent, element); break;
        case Element.CLASS: setClass (level, indent, element); break;
        case Element.CLASS_CALL: setClassCall (indent, element); break;
        case Element.PROPERTY_CALL: setPropertyCall (element); break;
        case Element.METHOD: setMethod (level, indent, i, element); break;
        case Element.METHOD_CALL: setMethodCall (indent, i, element); break;
        case Element.ARGUMENTS: setArgument (i, element); break;
        case Element.RETURN: setReturn (level, indent, element); break;
        case Element.LITERAL: setLiteral (element); break;
        case Element.BLOCK: setBlock (level, i, indent, element); break;
        case Element.PROPERTY: setProperty (indent, i, element); break;
        case Element.VARIABLE: setVariable (element); break;
        
      }
      
    }
    
    protected void setPackage (String indent, JSONObject element) {
      
      if (element.has (Element.VALUE)) {
        
        builder.append (indent);
        
        builder.append (element.getString (Element.TYPE)).append (upl2.compiler.Element.Separator.SPACE);
        builder.append (element.getString (Element.VALUE));
        
        builder.append (upl2.compiler.Element.Separator.SEMICOLON);
        
        setBlock (indent);
        setBlock (indent);
        
      } else throw new GeneratorException ("Package must have value", element);
      
    }
    
    protected void setImport (String indent, JSONObject element) {
      
      if (element.has (Element.VALUE)) {
        
        JSONArray values = element.getJSONArray (Element.VALUE);
        
        for (int i = 0; i < values.length (); i++) {
          
          builder.append (indent);
          
          builder.append (element.getString (Element.TYPE)).append (upl2.compiler.Element.Separator.SPACE);
          builder.append (values.getString (i));
          
          builder.append (upl2.compiler.Element.Separator.SEMICOLON);
          
          setBlock (indent);
          
        }
        
        setBlock (indent);
        
      } else throw new GeneratorException ("Import must have value", element);
      
    }
    
    protected void setClass (int level, String indent, JSONObject element) {
      
      if (element.has (Element.VISIBILITY))
        builder.append (element.getString (Element.VISIBILITY)).append (upl2.compiler.Element.Separator.SPACE);
      
      if (element.has (Keyword.STATIC))
        throw new GeneratorException ("Only methods in class can be static", element);
      
      builder.append (Keyword.CLASS).append (upl2.compiler.Element.Separator.SPACE);
      
      setName (element, "Class must have name");
      
      builder.append (upl2.compiler.Element.Separator.SPACE).append (upl2.compiler.Element.Separator.OPEN_BRACE);
      
      if (element.has (Element.BODY)) {
        
        JSONArray body = element.getJSONArray (Element.BODY);
        
        if (body.length () > 0)
          setBlock (indent).append (this.indent).append (Strings.LS);
        
        process (body, (level + 1));
        
        if (body.length () > 0)
          builder.append (indent);
        
      } else throw new GeneratorException ("Class must have body", element);
      
      builder.append (upl2.compiler.Element.Separator.CLOSE_BRACE);
      
    }
    
    protected void setMethod (int level, String indent, int i, JSONObject element) {
      
      JSONArray body = new JSONArray ();
      
      if (element.has (Element.BODY))
        body = element.getJSONArray (Element.BODY);
      
      if (body.length () > 0)
        builder.append (indent);
      
      if (element.has (Element.VISIBILITY))
        builder.append (element.getString (Element.VISIBILITY)).append (upl2.compiler.Element.Separator.SPACE);
      
      if (isStatic)
        builder.append (Keyword.STATIC).append (upl2.compiler.Element.Separator.SPACE);
      
      if (element.has (Keyword.ABSTRACT))
        builder.append (Keyword.ABSTRACT).append (upl2.compiler.Element.Separator.SPACE);
      
      if (element.has (Element.RETURN_TYPE))
        builder.append (element.getString (Element.RETURN_TYPE));
      else
        builder.append (Keyword.VOID);
      
      builder.append (upl2.compiler.Element.Separator.SPACE);
      
      setName (element, "Method must have name");
      
      builder.append (upl2.compiler.Element.Separator.SPACE);
      builder.append (upl2.compiler.Element.Separator.OPEN_BRACKET);
      builder.append (upl2.compiler.Element.Separator.CLOSE_BRACKET);
      
      if (element.has (Element.BODY)) {
        
        if (isAbstract)
          throw new GeneratorException ("Abstract methods can't have body", element);
        
        builder.append (upl2.compiler.Element.Separator.SPACE).append (upl2.compiler.Element.Separator.OPEN_BRACE);
        
        if (body.length () > 0) {
          
          setBlock (indent).append (this.indent);
          
          process (body, (level + 1));
          
          setBlock (indent);
          
        }
        
        builder.append (upl2.compiler.Element.Separator.CLOSE_BRACE);
        
      } else if (isAbstract)
        builder.append (upl2.compiler.Element.Separator.SEMICOLON);
      else
        throw new GeneratorException ("Only abstract methods may not have body", element);
      
      setBlock (indent).append (Strings.LS);
      
    }
    
    protected void setClassCall (String indent, JSONObject element) {
      
      if (!isStatic)
        builder.append (Keyword.NEW).append (upl2.compiler.Element.Separator.SPACE);
      
      setName (element, "Class must have name");
      
      if (!isStatic) {
        
        builder.append (upl2.compiler.Element.Separator.SPACE);
        builder.append (upl2.compiler.Element.Separator.OPEN_BRACKET);
        builder.append (upl2.compiler.Element.Separator.CLOSE_BRACKET);
        
      }
      
      if (element.has (Element.VALUE)) {
        
        builder.append (Operator.CLASS_CONCAT);
        
        process (element.getJSONObject (Element.VALUE), 0);
        
      }
      
    }
    
    protected void setMethodCall (String indent, int i, JSONObject element) {
      
      setName (element, "Method must have name");
      
      builder.append (upl2.compiler.Element.Separator.SPACE);
      builder.append (upl2.compiler.Element.Separator.OPEN_BRACKET);
      
      if (element.has (Element.ARGUMENTS))
        process (element.getJSONArray (Element.ARGUMENTS), 0);
      
      builder.append (upl2.compiler.Element.Separator.CLOSE_BRACKET);
      
      if (element.has (Element.VALUE)) {
        
        builder.append (Operator.METHOD_CONCAT);
        
        process (element.getJSONObject (Element.VALUE), 0);
        
      }
      
    }
    
    protected void setProperty (String indent, int i, JSONObject element) {
      
      builder.append (indent);
      
      if (element.has (Element.VISIBILITY))
        builder.append (element.getString (Element.VISIBILITY)).append (upl2.compiler.Element.Separator.SPACE);
      
      if (isStatic)
        builder.append (Keyword.STATIC).append (upl2.compiler.Element.Separator.SPACE);
      
      if (isFinal)
        builder.append (Keyword.FINAL).append (upl2.compiler.Element.Separator.SPACE);
      
      if (element.has (Keyword.ABSTRACT))
        throw new GeneratorException ("Properties can't be abstract", element);
      
      if (element.has (Element.VALUE_TYPE))
        builder.append (element.getString (Element.VALUE_TYPE)).append (upl2.compiler.Element.Separator.SPACE);
      else
        throw new GeneratorException ("Property must have type", element);
      
      setName (element, "Property must have name");
      
      if (element.has (Element.VALUE)) {
        
        builder.append (upl2.compiler.Element.Separator.SPACE).append (Operator.SET).append (upl2.compiler.Element.Separator.SPACE);
        
        process (element.getJSONObject (Element.VALUE), 0);
        
      }
      
      builder.append (upl2.compiler.Element.Separator.SEMICOLON);
      builder.append (Strings.LS).append (indent).append (Strings.LS);
      
    }
    
    protected void setVariable (JSONObject element) {
      
      if (element.has (Element.VISIBILITY))
        throw new GeneratorException ("Local variables can't have visibility", element);
      
      if (element.has (Keyword.STATIC))
        throw new GeneratorException ("Local variables can't be static", element);
      
      if (isFinal)
        builder.append (Keyword.FINAL).append (upl2.compiler.Element.Separator.SPACE);
      
      if (element.has (Keyword.ABSTRACT))
        throw new GeneratorException ("Local variables can't be abstract", element);
      
      if (element.has (Element.VALUE_TYPE))
        builder.append (element.getString (Element.VALUE_TYPE)).append (upl2.compiler.Element.Separator.SPACE);
      else
        throw new GeneratorException ("Local variable must have type", element);
      
      setName (element, "Local variable must have name");
      
      if (element.has (Element.VALUE)) {
        
        if (element.has (Element.OPERATOR)) {
          
          String operator = element.getString (Element.OPERATOR);
          
          if (!operator.equals (Operator.SET) && element.has (Element.VALUE_TYPE))
            throw new GeneratorException ("You need to set variable value before changed it", element);
          else
            builder.append (upl2.compiler.Element.Separator.SPACE).append (operator).append (upl2.compiler.Element.Separator.SPACE);
          
        } else throw new GeneratorException ("Local variables must get an operator if it have value", element);
        
        process (element.getJSONObject (Element.VALUE), 0);
        
      }
      
      builder.append (upl2.compiler.Element.Separator.SEMICOLON);
      builder.append (Strings.LS).append (indent).append (Strings.LS);
      
    }
    
    protected void setPropertyCall (JSONObject element) {
      
      String operator = "";
      
      if (element.has (Element.OPERATOR))
        operator = element.getString (Element.OPERATOR);
      
      if (operator.equals (Operator.Arithmetic.PREINCREMENT))
        builder.append (Operator.INCREMENT);
      else if (operator.equals (Operator.Arithmetic.PREDECREMENT))
        builder.append (Operator.DECREMENT);
      
      setName (element, "Property must have name");
      
      if (operator.equals (Operator.Arithmetic.POSTINCREMENT) || operator.equals (Operator.Arithmetic.POSTDECREMENT)) {
        
        if (operator.equals (Operator.Arithmetic.POSTINCREMENT))
          operator = Operator.INCREMENT;
        else
          operator = Operator.DECREMENT;
        
        builder.append (operator);
        
      } else if (element.has (Element.VALUE)) {
        
        if (element.has (Element.OPERATOR)) {
          
          builder.append (upl2.compiler.Element.Separator.SPACE);
          builder.append (element.getString (Element.OPERATOR));
          builder.append (upl2.compiler.Element.Separator.SPACE);
          
        } else builder.append (Operator.METHOD_CONCAT);
        
        process (element.getJSONObject (Element.VALUE), 0);
        
      }
      
    }
    
    protected void setArgument (int i, JSONObject element) {
      
      if (i > 0) builder.append (upl2.compiler.Element.Separator.COMMA).append (upl2.compiler.Element.Separator.SPACE);
      
      process (element, 0);
      
    }
    
    protected void setBlock (int level, int i, String indent, JSONObject element) {
      
      setBlock (indent);
      
      if (element.has (Element.BODY))
        process (element.getJSONObject (Element.BODY), (level + 1));
      
      builder.append (upl2.compiler.Element.Separator.SEMICOLON);
      
      setBlock (indent);
      
    }
    
    protected void setReturn (int level, String indent, JSONObject element) {
      
      setBlock (indent);
      builder.append (Keyword.RETURN);
      
      if (element.has (Element.VALUE)) {
        
        builder.append (upl2.compiler.Element.Separator.SPACE);
        
        process (element.getJSONObject (Element.VALUE), 0);
        
      }
      
      builder.append (upl2.compiler.Element.Separator.SEMICOLON);
      
    }
    
    protected void setCondition (JSONObject element) {
      
      setBlock (indent);
      builder.append (Keyword.RETURN);
      
      if (element.has (Element.VALUE)) {
        
        builder.append (upl2.compiler.Element.Separator.SPACE);
        
        process (element.getJSONObject (Element.VALUE), 0);
        
      }
      
      builder.append (upl2.compiler.Element.Separator.SEMICOLON);
      
    }
    
    protected void setLiteral (JSONObject element) {
      
      if (element.has (Element.VALUE)) {
        
        boolean isString = element.get (Element.VALUE) instanceof String;
        
        if (isString) builder.append (upl2.compiler.Element.Separator.STRING_QUOTE);
        builder.append (element.getString (Element.VALUE));
        if (isString) builder.append (upl2.compiler.Element.Separator.STRING_QUOTE);
        
      } else throw new GeneratorException ("Literal must have value", element);
      
    }
    
  }