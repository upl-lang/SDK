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

	package upl.compiler;

	import upl.core.Log;
	import upl.lexer.Lexer;
	import upl.lexer.Token;
	import upl.util.ArrayList;
	import upl.util.List;

	public class Tree {
	
		protected Lexer parser;
	
		protected int index = 0;
		protected Token token;
	
		public Tree (Lexer parser) {
			this.parser = parser;
		}
	
		protected boolean startBlock () {
			return token.value.equals ("{");
		}
	
		protected boolean endBlock () {
			return token.value.equals ("}");
		}
	
		protected boolean startCondition () {
			return token.value.equals ("(");
		}
	
		protected boolean endCondition () {
			return token.value.equals (")");
		}
	
		protected boolean endStringBlock () {
			return token.value.equals (";");
		}
	
		public static class Node {
		
			protected List<Node> body;
			protected List<ConditionNode> condition;
			protected Token token;
			protected int level = 1;
		
			@Override
			public String toString () {
			
				if (body != null)
					return "--".repeat (level) + "{\n" + body.implode ("") + "--".repeat (level) + "}\n";
				else if (condition != null)
					return "(" + condition.implode ("") + ") ";
				else
					return "--".repeat (level) + token + "\n";
			
			}
		
		}
	
		public static class ConditionNode extends Node {
		
			@Override
			public String toString () {
			
				if (body != null)
					return "--".repeat (level) + "{\n" + body.implode ("") + "--".repeat (level) + "}";
				else if (condition != null)
					return "( " + condition.implode ("") + ") ";
				else
					return token.value + " ";
			
			}
		
		}
	
		public List<Node> build () {
			return block (1);
		}
	
		protected boolean endCondition = false, oneLineBlock = false;
		protected Token prevToken;
	
		protected List<Node> block (int level) {
		
			List<Node> output = new ArrayList<> ();
		
			if (prevToken != null) {
			
				Node node = new Node ();
			
				node.level = level;
			
				node.token = prevToken;
				prevToken = null;
			
				output.put (node);
			
			}
		
			while (nextToken ()) {
			
				Node node = new Node ();
			
				node.level = level;
			
				if (startBlock ()) {
				
					endCondition = false;
				
					node.body = block (level + 1);
				
				} else if (endCondition) {
				
					oneLineBlock = true;
					endCondition = false;
				
					prevToken = token;
				
					node.body = block (level + 1);
				
				} else if (endBlock ())
					break;
				else if (oneLineBlock && endStringBlock ()) {
				
					oneLineBlock = false;
					break;
				
				} else if (startCondition ())
					node.condition = condition (level + 1);
				else
					node.token = token;
			
				output.put (node);
			
			}
		
			return output;
		
		}
	
		protected List<ConditionNode> condition (int level) {
		
			List<ConditionNode> output = new ArrayList<> ();
		
			while (nextToken ()) {
			
				ConditionNode node = new ConditionNode ();
			
				node.level = level;
			
				if (startBlock ())
					node.body = block (level + 1);
				else if (endBlock ())
					break;
				else if (startCondition ())
					node.condition = condition (level + 1);
				else if (endCondition ()) {
				
					endCondition = true;
					break;
				
				} else node.token = token;
			
				output.put (node);
			
			}
		
			return output;
		
		}
	
		protected final boolean nextToken () {
		
			if (index < parser.tokens.length ()) {
			
				token = parser.tokens.get (index);
				index++;
			
				return true;
			
			}
		
			return false;
		
		}
	
		public void d () {
			Log.d (token);
		}
	
	}