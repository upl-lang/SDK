	package upl.regex4;
	
	import java.io.IOException;
	import java.util.*;
	
	public class Regex {
		
		public static final int DIGIT = 800;  // '\d' 匹配数字
		public static final int NODIGIT = 801;  // '\D' 匹配非数字
		public static final int LINEFEED = 802;  // '\n' 匹配换行符
		public static final int ENTER = 803;  // '\r' 匹配回车
		public static final int BLANK = 804;  // '\s' 匹配空白符
		public static final int NOBLANK = 805;  // '\S' 匹配非空白符
		public static final int CHARA = 806;  // '\w' 匹配字母、数字、下划线
		public static final int NOCHARA = 807;  // '\W' 匹配非字母、数字、下划线
		public static final int ANYONE = 808;  // '.'  匹配任何字符
		public static final int CHSET = 256;  // 只匹配 ascii 码
		
		public static final int CAPHEAD = 850; //捕获头边
		public static final int CAPEND = 851; //捕获尾边
		public static final int NOGREED = 852; //非贪婪边
		public static final int STRS = 853; //字符串起始
		public static final int STRE = 854; //字符串结尾
		public static final int NOCON = 855; //否定预查
		
		protected StateTable stateTable = new StateTable (); //DFA 表
		protected List<String> groups = new ArrayList<> (); //匹配结果组
		protected List<String> Tgroups = new ArrayList<> (); //临时匹配结果组
		protected LinkedList<StringBuilder> groupStack = new LinkedList<> (); //匹配结果栈
		
		public Regex (String regex) throws Exception {
			NFAGraph nfaGraph = regexToNFA (regex);
			nfaGraph.end.isEnd = true;
			showNFA (nfaGraph.start);
			Node.unLook ();
			productDFA (nfaGraph.start);
		}
		
		/**
		 * 下面四个函数都是 匹配字符串相关
		 *
		 * @param input 待匹配字符串
		 * @return 所有匹配结果
		 * @throws IOException
		 */
		public List<String> match (String input) throws IOException {
			return match (new GoodReader (input));
		}
		
		public List<String> match (GoodReader reader) throws IOException {
			
			groups.clear ();
			Tgroups.clear ();
			groupStack.clear ();
			
			while (reader.ready ()) {
				
				reader.mark (100);
				groupStack.push (new StringBuilder (""));
				
				if (match3 (reader, stateTable.getFirstY ())) {
					
					groups.add (String.valueOf (groupStack.pop ()));
					groups.addAll (Tgroups);
					
					Tgroups.clear ();
					groupStack.clear ();
					
				} else {
					
					Tgroups.clear ();
					groupStack.clear ();
					
					reader.reset ();
					reader.read ();
					
				}
				
			}
			
			return groups;
			
		}
		
		
		protected boolean match3 (GoodReader reader, State currentState) throws IOException {
			
			if (!reader.ready ())
				return stateTable.getState (currentState).isEnd;
			
			if (stateTable.getState (currentState).Priority == 1 && stateTable.getState (currentState).isEnd) {
				
				for (StringBuilder sb : groupStack)
					sb.deleteCharAt (sb.length () - 1);
				
				reader.unread ();
				
				return true;
				
			}
			
			// Read symbols and add to capture group
			char ch = (char) reader.read ();
			
			for (StringBuilder sb : groupStack)
				sb.append (ch);
			
			for (State state : stateTable.getStates (ch, currentState)) {
				
				if (state.isCapend && groupStack.size () > 1) { // Capture end
					
					for (StringBuilder sb : groupStack)
						sb.deleteCharAt (sb.length () - 1);
					
					reader.unread ();
					
					Tgroups.add (String.valueOf (groupStack.pop ()));
					
				}
				
				if (state.isCapstart) { // Capture start
					
					for (StringBuilder sb : groupStack)
						sb.deleteCharAt (sb.length () - 1);
					
					groupStack.push (new StringBuilder (""));
					
					reader.unread ();
					
				}
				
				if (state.isStrs) { // String start ^
					
					if (!reader.isHead ()) {
						
						for (StringBuilder sb : groupStack)
							if (sb.length () >= 1)
								sb.deleteCharAt (sb.length () - 1);
						
						reader.unread ();
						
						return false;
						
					}
					
				}
				
				if (state.isStre) { // String end $
					
					if (!reader.isEnd ()) {
						
						for (StringBuilder sb : groupStack)
							if (sb.length () >= 1)
								sb.deleteCharAt (sb.length () - 1);
						
						reader.unread ();
						
						return false;
						
					}
					
				}
				
				if (match3 (reader, state))
					return true;
				
				if (state.isCapstart)
					if (!groupStack.isEmpty ())
						groupStack.pop ();
				
			}
			
			
			if (stateTable.getState (currentState).isEnd) {
				for (StringBuilder sb : groupStack)
					sb.deleteCharAt (sb.length () - 1);
				reader.unread ();
				return true;
			}
			
			for (StringBuilder sb : groupStack)
				if (sb.length () >= 1)
					sb.deleteCharAt (sb.length () - 1);
			
			reader.unread ();
			
			return false;
			
		}
		
		
		/**
		 * 正则转 NFA
		 *
		 * @param regex2 正则表达式
		 * @return NFA 图
		 */
		protected NFAGraph regexToNFA (String regex2) throws Exception {
			
			GoodReader reader = new GoodReader (regex2);
			
			NFAGraph nfaGraph = null;
			
			while (reader.ready ()) {
				
				char ch = (char) reader.read ();
				if (ch > CHSET) throw new Exception ("正则中有非法字符");
				
				switch (ch) {
					
					case '(': {
						
						/* Capture groups names */
						
						String captureName = null;
						
						if (reader.peek () == ':') {
							
							captureName = "";
							
							stateTable.addAbscissa (CAPHEAD);
							stateTable.addAbscissa (CAPEND);
							
							reader.read ();
							
							if (reader.peek () == '<') {
								
								reader.read ();
								captureName = reader.readUntilCh ('>');
								
							}
							
						}
						
						if (reader.peek () == '!')
							reader.read ();
						
						/* Reading content in brackets recursively */
						NFAGraph nfaGraph0 = regexToNFA (reader.readContentInBracket ());
						
						/* Handling names */
						if (captureName != null)
							nfaGraph0.captureGraph (captureName);
						
						/* Handle duplicates */
						
						if (reader.peek () == '*') {
							
							nfaGraph0.repeatGraph ();
							reader.read ();
							
							if (reader.peek () == '?') {
								
								nfaGraph0.noGreedGraph ();
								reader.read ();
								
							}
							
						} else if (reader.peek () == '+') {
							
							nfaGraph0.repeatGraph0 ();
							reader.read ();
							
							if (reader.peek () == '?') {
								
								nfaGraph0.noGreedGraph ();
								reader.read ();
							}
							
						} else if (reader.peek () == '?') {
							
							nfaGraph0.repeatGraph1 ();
							reader.read ();
							
						} else if (reader.peek () == '{') {
							
							reader.read ();
							String tstr = reader.readUntilCh ('}');
							
							if (reader.peek () == '?') {
								
								braceHandler (nfaGraph0, tstr, true);
								reader.read ();
								
							} else braceHandler (nfaGraph0, tstr, false);
							
						}
						
						if (nfaGraph == null)
							nfaGraph = nfaGraph0;
						else
							nfaGraph.seriesGraph (nfaGraph0);
						
						break;
						
					}
					
					case '[':
						break;
					
					case '^': { // Match the beginning of the string
						
						if (nfaGraph == null) {
							
							Node start = new Node ();
							Node end = new Node ();
							
							start.addNextNode (STRS, end);
							
							nfaGraph = new NFAGraph (start, end);
							
						} else throw new Exception ("正则编译出错");
						
						break;
						
					}
					
					case '$': { // Match the end of the string
						
						if (nfaGraph == null) {
							throw new Exception ("正则编译出错");
						} else {
							
							Node start = new Node ();
							Node end = new Node ();
							start.addNextNode (STRE, end);
							NFAGraph graph0 = new NFAGraph (start, end);
							nfaGraph.seriesGraph (graph0);
							
						}
						
						break;
						
					}
					
					case '|': { // or
						
						NFAGraph nfaGraph2 = regexToNFA (reader.readUntilEnd ());
						nfaGraph.parallelGraph (nfaGraph2);
						
						return nfaGraph;
						
					}
					
					case '\\': { // Handle escaping
						
						Node start = new Node ();
						Node end = new Node ();
						
						char nextChar = (char) reader.read ();
						
						switch (nextChar) {
							
							case 'd':
								start.addNextNode (DIGIT, end);
								stateTable.addAbscissa (DIGIT);
								break;
								
							case 'D':
								start.addNextNode (NODIGIT, end);
								stateTable.addAbscissa (NODIGIT);
								break;
								
							case 'n':
								start.addNextNode (LINEFEED, end);
								stateTable.addAbscissa (LINEFEED);
								break;
								
							case 'r':
								start.addNextNode (ENTER, end);
								stateTable.addAbscissa (ENTER);
								break;
								
							case 's':
								start.addNextNode (BLANK, end);
								stateTable.addAbscissa (BLANK);
								break;
								
							case 'S':
								start.addNextNode (NOBLANK, end);
								stateTable.addAbscissa (NOBLANK);
								break;
								
							case 'w':
								start.addNextNode (CHARA, end);
								stateTable.addAbscissa (CHARA);
								break;
								
							case 'W':
								start.addNextNode (NOCHARA, end);
								stateTable.addAbscissa (NOCHARA);
								break;
								
							case '.':
								start.addNextNode (ANYONE, end);
								stateTable.addAbscissa (ANYONE);
								break;
								
							case '\\':
							case '*':
							case '+':
							case '?':
							case '{':
							case '}':
							case '(':
							case ')':
							case '[':
							case ']':
							case '=':
							case '|':
							case ',':
							case '^':
							case '$':
								start.addNextNode (nextChar, end);
								stateTable.addAbscissa (nextChar);
								break;
								
							default:
								throw new Exception ("正则编译错误 转义错误");
							
						}
						
						if (nfaGraph == null) {
							nfaGraph = new NFAGraph (start, end);
						} else {
							
							NFAGraph graph0 = new NFAGraph (start, end);
							nfaGraph.seriesGraph (graph0);
							
						}
						
						break;
						
					}
					
					default: { // Other characters
						
						Node start = new Node ();
						Node end = new Node ();
						
						start.addNextNode (ch, end);
						
						stateTable.addAbscissa (ch);
						
						if (nfaGraph == null) {
							nfaGraph = new NFAGraph (start, end);
						} else {
							
							NFAGraph graph0 = new NFAGraph (start, end);
							nfaGraph.seriesGraph (graph0);
							
						}
						
						break;
						
					}
					
				}
				
			}
			
			return nfaGraph;
			
		}
		
		/**
		 * 处理大括号 {2} {2,} {2,4} 指定次数重复
		 *
		 * @param nfaGraph    要重复的图
		 * @param repeatTimes 重复字符串
		 * @param noGreed     是否非贪婪
		 * @return 生成图
		 * @throws Exception 非法字符异常
		 */
		protected void braceHandler (NFAGraph nfaGraph, String repeatTimes, boolean noGreed) throws Exception {
			
			int i = 0;
			char nextChar = ' ';
			String minTimes = "";
			String maxTimes = "";
			
			for (; i < repeatTimes.length (); i++) {
				nextChar = repeatTimes.charAt (i);
				if (Character.isDigit (nextChar))
					minTimes += nextChar;
				else
					break;
			}
			
			if (nextChar == ',') {
				i++; //跳过逗号
				
				for (; i < repeatTimes.length (); i++) {
					nextChar = repeatTimes.charAt (i);
					if (Character.isDigit (nextChar))
						maxTimes += nextChar;
					else
						break;
				}
				
				if (i != repeatTimes.length ())
					throw new Exception ("正则编译失败 {}内有非法字符");
				
				nfaGraph.repeatGraph4 (Integer.parseInt (minTimes), Integer.parseInt (maxTimes), noGreed);
			} else {
				if (i != repeatTimes.length ())
					throw new Exception ("正则编译失败 {}内有非法字符");
				
				nfaGraph.repeatGraph3 (Integer.parseInt (minTimes));
			}
		}
		
		/**
		 * 下面的三个函数 用于产生 DFA
		 *
		 * @param start
		 */
		protected void productDFA (Node start) {
			State state = new State ();
			state.id.add (start.id);
			ArrayList<State> states = new ArrayList<> ();
			states.add (state);
			
			while (!states.isEmpty ()) {
				State state0 = states.remove (0);
				// 如果纵坐标中已经有了
				if (stateTable.containOrdinate (state0))
					continue;
				addLine (state0);
				stateTable.add (state0, states);
			}
			stateTable.showTable ();
		}
		
		protected void addLine (State state) {
			stateTable.addOrdinate (state);
			for (Node node : Node.allNodes) {
				for (int id : state.id) {
					if (id == node.getId ()) {
						Node.unLook ();
						bfs (node, state);
					}
				}
			}
		}
		
		protected void bfs (Node snode, State state) {
			ArrayList<Node> bfsNodes = new ArrayList<> (); //用于bfs
			bfsNodes.add (snode);
			while (!bfsNodes.isEmpty ()) {
				Node node = bfsNodes.remove (0);
				for (int i : node.nextNodes.keySet ()) {
					for (Node node0 : node.nextNodes.get (i)) {
						if (!node0.look) {
							node0.look = true;
							if (i == (int) ' '
								    || i == NOGREED
								    || i == STRS
								    || i >= NOCON
								    || i == STRE) {
								//向后传递
								if (i == NOGREED) node0.isNoGreed = true;
								if (i == STRS) node0.isStrs = true;
								if (i == STRE) node0.isStre = true;
								
								if (node.isNoGreed) node0.isNoGreed = true;
								if (node.isStrs) node0.isStrs = true;
								if (node.isStre) node0.isStre = true;
								
								//向前传递 触底前传
								if (node0.isEnd) {
									state.isEnd = true;
									if (node0.isNoGreed) state.Priority = 1;
									if (node0.isStre) state.isStre = true;
								}
								bfsNodes.add (node0);
							} else {
								//向后传递
								stateTable.addState ((char) i, state, node0, node);
							}
						}
					}
				}
			}
		}
		
		
		/**
		 * 输出 NFA 图
		 *
		 * @param snode 图的起始节点
		 */
		public static void showNFA (Node snode) {
			System.out.println ("-----------showNFA-----------");
			ArrayList<Node> bfsNodes = new ArrayList<> (); //用于bfs
			bfsNodes.add (snode);
			while (!bfsNodes.isEmpty ()) {
				Node node = bfsNodes.remove (0);
				
				System.out.println ("-----------");
				if (node.isEnd) System.out.println ("end");
				System.out.printf ("%-3d", node.id);
				
				for (int i : node.nextNodes.keySet ()) {
					for (Node node0 : node.nextNodes.get (i)) {
						System.out.printf ("%3d", node.id);
						
						if (i < 800)
							System.out.printf ("--%c-->", (char) i);
						else if (i == 850)
							System.out.print ("--caphead-->");
						else if (i == 851)
							System.out.print ("--capend-->");
						else if (i == 852)
							System.out.print ("--ungreed-->");
						else if (i == 800)
							System.out.print ("--\\d-->");
						else if (i == 801)
							System.out.print ("--\\D-->");
						else if (i == 802)
							System.out.print ("--\\n-->");
						else if (i == 803)
							System.out.print ("--\\r-->");
						else if (i == 804)
							System.out.print ("--\\s-->");
						else if (i == 805)
							System.out.print ("--\\S-->");
						else if (i == 806)
							System.out.print ("--\\w-->");
						else if (i == 807)
							System.out.print ("--\\W-->");
						else if (i == 808)
							System.out.print ("--.-->");
						else
							System.out.print ("--wrong-->");
						
						System.out.printf ("%-3d", node0.id);
						if (!node0.look) {
							node0.look = true;
							bfsNodes.add (node0);
						}
					}
				}
				
				System.out.println ();
			}
			System.out.println ("-----------showNFA-----------");
			Node.unLook ();
		}
		
	}