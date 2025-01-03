<#setting number_format=0>
<#macro array a><#list a as i><#if i_index &gt; 0>,</#if>${i}</#list></#macro>
<#macro longintarray a>${a?stringresource()}</#macro>
<#macro intarray a>${a?javastring()}</#macro>
<#macro type a b c d>${a?type(b,c,d)}</#macro>
<#if code.fileheader?has_content>
${code.fileheader}
</#if>
<#if package?has_content>
package ${package};
</#if>

import java.io.IOException;
<#if unicode>
import java.io.InputStreamReader;
import java.io.Reader;
<#else>
import java.io.InputStream;
</#if>
import java.io.FileInputStream;

<#if parser?has_content>
import java.util.ArrayList;
import java.util.Collection;
</#if>
<#if parser?has_content>
import java.util.Stack;
</#if>

<#if code.classheader?has_content>
${code.classheader}
</#if>
<#if public?has_content && public?string == "true">public </#if><#if abstract?has_content && abstract?string == "true">abstract </#if>class ${ccclass}<#if extend?has_content> extends ${extend}</#if>
{
<#if parser?has_content && parser.lexemes?has_content>
	////////////////////////////////////////////////////////////////////////
	//
	// Terminal Definitions
	//
	////////////////////////////////////////////////////////////////////////
<#list parser.lexemes as i>
	protected final static int ${i.name} = ${i.value};
</#list>
</#if>

<#if parser?has_content>
<#if !parser?has_content && parser.lexemes?has_content>
	////////////////////////////////////////////////////////////////////////
	//
	// Terminal Definitions
	//
	////////////////////////////////////////////////////////////////////////
<#list parser.lexemes as i>
	protected final static int ${i.name} = ${i.value};
</#list>
</#if>

<#if parser.states?size gt 0>
	////////////////////////////////////////////////////////////////////////
	//
	// Lexer States
	//
	////////////////////////////////////////////////////////////////////////
<#list parser.states as i>
	protected final static int ${i} = ${parser.begins[i_index]};
</#list>
</#if>

	// an internal class for lazy initiation
	private final static class cc_lexer
	{
<@longintarray parser.accept/>
		private final static char[] accept = <@intarray parser.accept/>;
<#if parser.table == "ecs" || parser.table == "compressed">
<@longintarray parser.ecs/>
		private final static char[] ecs = <@intarray parser.ecs/>;
</#if>
<#if parser.table == "full" || parser.table == "ecs">
		private final static char[][] next = {<#list parser.dfa.table as i><#if i_index &gt; 0>,</#if><@intarray i/></#list>};
</#if>
<#if parser.table == "compressed">
<@longintarray parser.dfa.base/>
<@longintarray parser.dfa.next/>
<@longintarray parser.dfa.check/>
		private final static char[] base = <@intarray parser.dfa.base/>;
		private final static char[] next = <@intarray parser.dfa.next/>;
		private final static char[] check = <@intarray parser.dfa.check/>;
	<#if parser.dfa.default?has_content>
<@longintarray parser.dfa.default/>
		private final static char[] defaults = <@intarray parser.dfa.default/>;
	</#if>
	<#if parser.dfa.meta?has_content>
<@longintarray parser.dfa.meta/>
		private final static char[] meta = <@intarray parser.dfa.meta/>;
	</#if>
</#if>
	}
</#if>

<#if parser?has_content>
	// an internal class for lazy initiation
	private final static class cc_parser
	{
		private final static char[] rule = <@intarray parser.rules/>;
		private final static char[] ecs = <@intarray parser.ecs/>;
<#if parser.table == "ecs">
		private final static char[][] next = {<#list parser.dfa.table as i><#if i_index &gt; 0>,</#if><@intarray i/></#list>};
<#else>
		private final static char[] base = <@intarray parser.dfa.base/>;
		private final static char[] next = <@intarray parser.dfa.next/>;
		private final static char[] check = <@intarray parser.dfa.check/>;
		<#if parser.dfa.default?has_content>
		private final static char[] defaults = <@intarray parser.dfa.default/>;
		</#if>
		<#if parser.dfa.meta?has_content>
		private final static char[] meta = <@intarray parser.dfa.meta/>;
		</#if>
		<#if parser.dfa.gotoDefault?has_content>
		private final static char[] gotoDefault = <@intarray parser.dfa.gotoDefault/>;
		</#if>
</#if>
		private final static char[] lhs = <@intarray parser.lhs/>;
	}

	private final static class cc_parser_symbol
	{
		private final static String[] symbols =
		{
			<#list parser.symbols as s><#if s_index &gt; 0>,</#if>"${s}"</#list>
		};
	<#if debug>
		private final static char[] reverseECS = <@intarray parser.reverse/>;
	</#if>
	}

	private final static class YYParserState	// internal tracking tool
	{
		int token;			// the current token type
		Object value;		// the current value associated with token
		int state;			// the current scan state
<#if parser.captureList>
		ArrayList<Object[]>	captureList;	// for storing captured terminals
</#if>

		YYParserState ()	// EOF token construction
		{
			this (0, null, 0);
		}
		YYParserState (int token, Object value)
		{
			this (token, value, 0);
		}
		YYParserState (int token, Object value, int state)
		{
			this.token = token;
			this.value = value;
			this.state = state;
		}
	}

<#if parser.captureList>
	// for storing integer objects (so we do not create too many objects)
	private Integer[] _yySymbolArray;
</#if>
	// lookahead stack for the parser
	private final ArrayList<YYParserState> _yyLookaheadStack = new ArrayList<YYParserState> (512);
	// state stack for the parser
	private final ArrayList<YYParserState> _yyStateStack = new ArrayList<YYParserState> (512);

<#if parser.recovery>
	// flag that indicates error
	private boolean _yyInError;
</#if>
	// internal track of the argument start
	private int _yyArgStart;
</#if>
	// for passing value from parser to parser
	private Object _yyValue;

<#if parser?has_content>
<#if unicode>
	private Reader _yyIs = new InputStreamReader (System.in);
	private char[] _yyBuffer;
<#else>
	private InputStream _yyIs = System.in;
	private byte[] _yyBuffer;
</#if>
	private int _yyBufferSize = 4096;
	private int _yyMatchStart;
	private int _yyBufferEnd;

	private int _yyBaseState;

	private int _yyTextStart;
	private int _yyLength;

	private Stack<Integer> _yyLexerStack;
	private Stack<Object[]> _yyInputStack;

<#if parser.bol>
	// we need to track beginning of line (BOL) status
	private boolean _yyIsNextBOL = true;
	private boolean _yyBOL = true;
</#if>
<#if parser.lineMode>
	private int _yyLineNumber = 1;
</#if>
</#if>

<#if parser?has_content>
<#if !parser?has_content>
	/**
	 * Return the object associate with the token.  This function is only generated
	 * when the parser is not specified.
	 *
	 * @return	the object assoicated with the token.
	 */
	public Object yyValue ()
	{
		return _yyValue;
	}
</#if>
<#if unicode>
	/**
	 * Set the current input.
	 *
	 * @param	reader
	 *			the new input.
	 */
	public void setInput (Reader reader)
	{
		_yyIs = reader;
	}

	/**
	 * Obtain the current input.
	 *
	 * @return	the current input
	 */
	public Reader getInput ()
	{
		return _yyIs;
	}

	/**
	 * Switch the current input to the new input.  The old input and already
	 * buffered characters are pushed onto the stack.
	 *
	 * @param	is
	 * 			the new input
	 */
	public void yyPushInput (Reader is)
	{
		int len = _yyBufferEnd - _yyMatchStart;
		char[] leftOver = new char[len];
		System.arraycopy (_yyBuffer, _yyMatchStart, leftOver, 0, len);

		Object[] states = new Object[4];
		states[0] = _yyIs;
		states[1] = leftOver;

		if (_yyInputStack == null)
			_yyInputStack = new Stack<Object[]> ();
		_yyInputStack.push (states);

		_yyIs = is;
		_yyMatchStart = 0;
		_yyBufferEnd = 0;
	}

	/**
	 * Switch the current input to the old input on stack.  The current input
	 * and its buffered characters are all switch to the old ones.
	 */
	public void yyPopInput ()
	{
		Object[] states = _yyInputStack.pop ();
		_yyIs = (Reader)states[0];
		char[] leftOver = (char[])states[1];

		int curLen = _yyBufferEnd - _yyMatchStart;

		if ((leftOver.length + curLen) > _yyBuffer.length)
		{
			char[] newBuffer = new char[leftOver.length + curLen];
			System.arraycopy (_yyBuffer, _yyMatchStart, newBuffer, 0, curLen);
			System.arraycopy (leftOver, 0, newBuffer, curLen, leftOver.length);
			_yyBuffer = newBuffer;
			_yyMatchStart = 0;
			_yyBufferEnd = leftOver.length + curLen;
		}
		else
		{
			int start = _yyMatchStart;
			int end = _yyBufferEnd;
			char[] buffer = _yyBuffer;

			for (int i = 0; start < end; ++i, ++start)
				buffer[i] = buffer[start];
			System.arraycopy (leftOver, 0, buffer, curLen, leftOver.length);
			_yyMatchStart = 0;
			_yyBufferEnd = leftOver.length + curLen;
		}
	}
<#else>
	/**
	 * Set the current input.
	 *
	 * @param	is
	 *			the new input.
	 */
	public void setInput (InputStream is)
	{
		_yyIs = is;
	}

	/**
	 * Obtain the current input.
	 *
	 * @return	the current input
	 */
	public InputStream getInput ()
	{
		return _yyIs;
	}

	/**
	 * Switch the current input to the new input.  The old input and already
	 * buffered characters are pushed onto the stack.
	 *
	 * @param	is
	 * 			the new input
	 */
	public void yyPushInput (InputStream is)
	{
		int len = _yyBufferEnd - _yyMatchStart;
		byte[] leftOver = new byte[len];
		System.arraycopy (_yyBuffer, _yyMatchStart, leftOver, 0, len);

		Object[] states = new Object[4];
		states[0] = _yyIs;
		states[1] = leftOver;

		if (_yyInputStack == null)
			_yyInputStack = new Stack<Object[]> ();
		_yyInputStack.push (states);

		_yyIs = is;
		_yyMatchStart = 0;
		_yyBufferEnd = 0;
	}

	/**
	 * Switch the current input to the old input on stack.  The currently
	 * buffered characters are inserted infront of the old buffered characters.
	 */
	public void yyPopInput ()
	{
		Object[] states = _yyInputStack.pop ();

		_yyIs = (InputStream)states[0];
		byte[] leftOver = (byte[])states[1];

		int curLen = _yyBufferEnd - _yyMatchStart;

		if ((leftOver.length + curLen) > _yyBuffer.length)
		{
			byte[] newBuffer = new byte[leftOver.length + curLen];
			System.arraycopy (_yyBuffer, _yyMatchStart, newBuffer, 0, curLen);
			System.arraycopy (leftOver, 0, newBuffer, curLen, leftOver.length);
			_yyBuffer = newBuffer;
			_yyMatchStart = 0;
			_yyBufferEnd = leftOver.length + curLen;
		}
		else
		{
			int start = _yyMatchStart;
			int end = _yyBufferEnd;
			byte[] buffer = _yyBuffer;

			for (int i = 0; start < end; ++i, ++start)
				buffer[i] = buffer[start];
			System.arraycopy (leftOver, 0, buffer, curLen, leftOver.length);
			_yyMatchStart = 0;
			_yyBufferEnd = leftOver.length + curLen;
		}
	}
</#if>

	/**
	 * Obtain the number of input objects on the stack.
	 *
	 * @return	the number of input objects on the stack.
	 */
	public int yyInputStackSize ()
	{
		return _yyInputStack == null ? 0 : _yyInputStack.size ();
	}

<#if parser.bol>
	/**
	 * Check whether or not the current token at the beginning of the line.  This
	 * function is not accurate if the user does multi-line pattern matching or
	 * have trail contexts at the end of the line.
	 *
	 * @return	whether or not the current token is at the beginning of the line.
	 */
	public boolean isBOL ()
	{
		return _yyBOL;
	}

	/**
	 * Set whether or not the next token at the beginning of the line.
	 *
	 * @param	bol
	 *			the bol status
	 */
	public void setBOL (boolean bol)
	{
		_yyIsNextBOL = bol;
	}
</#if>
<#if parser.lineMode>
	/**
	 * Return the current line number.
	 * <p>
	 * This function is only available in line mode.
	 *
	 * @return	the current line number.
	 */
	public int getLineNumber ()
	{
		return _yyLineNumber;
	}
</#if>

	/**
	 * Get the current token text.
	 * <p>
	 * Avoid calling this function unless it is absolutely necessary since it creates
	 * a copy of the token string.  The string length can be found by reading _yyLength
	 * or calling yyLength () function.
	 *
	 * @return	the current text token.
	 */
	public String yyText ()
	{
		if (_yyMatchStart == _yyTextStart)		// this is the case when we have EOF
			return null;
		return new String (_yyBuffer, _yyTextStart, _yyMatchStart - _yyTextStart);
	}

	/**
	 * Get the current text token's length.  Actions specified in the CookCC file
	 * can directly access the variable _yyLength.
	 *
	 * @return	the string token length
	 */
	public int yyLength ()
	{
		return _yyLength;
	}

	/**
	 * Print the current string token to the standard output.
	 */
	public void echo ()
	{
		System.out.print (yyText ());
	}

	/**
	 * Put all but n characters back to the input stream.  Be aware that calling
	 * yyLess (0) is allowed, but be sure to change the state some how to avoid
	 * an endless loop.
	 *
	 * @param	n
	 * 			The number of characters.
	 */
	protected void yyLess (int n)
	{
		if (n < 0)
			throw new IllegalArgumentException ("yyLess function requires a non-zero value.");
		if (n > (_yyMatchStart - _yyTextStart))
			throw new IndexOutOfBoundsException ("yyLess function called with a too large index value " + n + ".");
		_yyMatchStart = _yyTextStart + n;
	}

	/**
	 * Set the parser's current state.
	 *
	 * @param	baseState
	 *			the base state index
	 */
	protected void begin (int baseState)
	{
		_yyBaseState = baseState;
	}

	/**
	 * Push the current state onto parser state onto stack and
	 * begin the new state specified by the user.
	 *
	 * @param	newState
	 *			the new state.
	 */
	protected void yyPushLexerState (int newState)
	{
		if (_yyLexerStack == null)
			_yyLexerStack = new Stack<Integer> ();

		_yyLexerStack.push (new Integer (_yyBaseState));
		begin (newState);
	}

	/**
	 * Restore the previous parser state.
	 */
	protected void yyPopLexerState ()
	{
		begin (_yyLexerStack.pop ());
	}

	<#if debug>
	protected boolean debugLexer (int baseState, int matchedState, int accept)
	{
		System.err.println ("parser: " + baseState + ", " + matchedState + ", " + accept + ": " + yyText ());
		return true;
	}

	protected boolean debugLexerBackup (int baseState, int backupState, String backupString)
	{
		System.err.println ("parser backup: " + baseState + ", " + backupState + ": " + backupString);
		return true;
	}
	</#if>

	// read more data from the input
	protected boolean yyRefreshBuffer () throws IOException
	{
		if (_yyBuffer == null)
			_yyBuffer = new <#if unicode>char<#else>byte</#if>[_yyBufferSize];
		if (_yyMatchStart > 0)
		{
			if (_yyBufferEnd > _yyMatchStart)
			{
				System.arraycopy (_yyBuffer, _yyMatchStart, _yyBuffer, 0, _yyBufferEnd - _yyMatchStart);
				_yyBufferEnd -= _yyMatchStart;
				_yyMatchStart = 0;
			}
			else
			{
				_yyMatchStart = 0;
				_yyBufferEnd = 0;
			}
		}
		else if (_yyBufferEnd == _yyBuffer.length)
		{
			<#if unicode>char<#else>byte</#if>[] newBuffer = new <#if unicode>char<#else>byte</#if>[_yyBuffer.length + _yyBuffer.length / 2];

			System.arraycopy (_yyBuffer, 0, newBuffer, 0, _yyBufferEnd);
			_yyBuffer = newBuffer;
		}

		int readSize = _yyIs.read (_yyBuffer, _yyBufferEnd, _yyBuffer.length - _yyBufferEnd);
		if (readSize > 0)
			_yyBufferEnd += readSize;
	<#if parser.yywrap>
		else if (readSize < 0 && !yyWrap ())		// since we are at EOF, call yyWrap ().  If the return value of yyWrap is false, refresh buffer again
			return yyRefreshBuffer ();
	</#if>
		return readSize >= 0;
	}

	/**
	 * Reset the internal buffer.
	 */
	public void yyResetBuffer ()
	{
		_yyMatchStart = 0;
		_yyBufferEnd = 0;
	}

	/**
	 * Set the internal buffer size.  This action can only be performed
	 * when the buffer is empty.  Having a large buffer is useful to read
	 * a whole file in to increase the performance sometimes.
	 *
	 * @param	bufferSize
	 *			the new buffer size.
	 */
	public void setBufferSize (int bufferSize)
	{
		if (_yyBufferEnd > _yyMatchStart)
			throw new IllegalArgumentException ("Cannot change parser buffer size at this moment.");
		_yyBufferSize = bufferSize;
		_yyMatchStart = 0;
		_yyBufferEnd = 0;
		if (_yyBuffer != null && bufferSize != _yyBuffer.length)
			_yyBuffer = new <#if unicode>char<#else>byte</#if>[bufferSize];
	}

	/**
	 * Reset the internal state to reuse the same parser.
	 * <p>
	 * Note, it does not change the buffer size, the input buffer, and the input stream.
	 * <p>
	 * Making this function protected so that it can be enabled only if the child class
	 * decides to make it public.
	 */
	protected void reset ()
	{
<#if parser?has_content>
		// reset parser state
		_yyLookaheadStack.clear ();
		_yyStateStack.clear ();
		_yyArgStart = 0;
		_yyValue = null;
<#if parser.recovery>
		_yyInError = false;
</#if>
</#if>

<#if parser?has_content>
		// reset parser state
		_yyMatchStart = 0;
		_yyBufferEnd = 0;
		_yyBaseState = 0;
		_yyTextStart = 0;
		_yyLength = 0;

		if (_yyLexerStack != null)
			_yyLexerStack.clear ();
		if (_yyInputStack != null)
			_yyInputStack.clear ();

<#if parser.bol>
		_yyIsNextBOL = true;
		_yyBOL = true;
</#if>
<#if parser.lineMode>
		_yyLineNumber = 1;
</#if>
</#if>
	}

	/**
	 * Call this function to start the scanning of the input.
	 *
	 * @return	a token or status value.
	 * @throws	IOException
	 *			in case of I/O error.
	 */
	<#if parser?has_content>protected<#else>public</#if> int yyLex () throws IOException
	{
<#if code.lexerprolog?has_content>
	${code.lexerprolog}
</#if>

<#if parser.table == "ecs" || parser.table == "compressed">
		char[] cc_ecs = cc_lexer.ecs;
</#if>
<#if parser.table == "ecs" || parser.table == "full">
		char[][] cc_next = cc_lexer.next;
<#elseif parser.table="compressed">
		char[] cc_next = cc_lexer.next;
		char[] cc_check = cc_lexer.check;
	<#if parser.dfa.base?has_content>
		char[] cc_base = cc_lexer.base;
	</#if>
	<#if parser.dfa.default?has_content>
		char[] cc_default = cc_lexer.defaults;
	</#if>
	<#if parser.dfa.meta?has_content>
		char[] cc_meta = cc_lexer.meta;
	</#if>
</#if>
		char[] cc_accept = cc_lexer.accept;

		<#if unicode>char<#else>byte</#if>[] buffer = _yyBuffer;

		while (true)
		{
			// initiate variables necessary for lookup
<#if parser.bol>
			_yyBOL = _yyIsNextBOL;
			_yyIsNextBOL = false;
	<#if parser.bolStates>
			int cc_matchedState = _yyBaseState + (_yyBOL ? 1 : 0);
	<#else>
			int cc_matchedState = _yyBaseState;
	</#if>
<#else>
			int cc_matchedState = _yyBaseState;
</#if>

			int matchedLength = 0;

			int internalBufferEnd = _yyBufferEnd;
			int lookahead = _yyMatchStart;

<#if parser.backup>
			int cc_backupMatchedState = cc_matchedState;
			int cc_backupMatchedLength = 0;
</#if>

			// the DFA lookup
			while (true)
			{
				// check buffer status
				if (lookahead < internalBufferEnd)
				{
					// now okay to process the character
					int cc_toState;
<#if parser.lineMode>
					<#if unicode>char<#else>byte</#if> ch = buffer[lookahead];
</#if>
<#if parser.table == "full">
					cc_toState = cc_next[cc_matchedState][<#if parser.lineMode>ch<#else>buffer[lookahead]</#if><#if !unicode> & 0xff</#if>];
<#elseif parser.table == "ecs">
					cc_toState = cc_next[cc_matchedState][cc_ecs[<#if parser.lineMode>ch<#else>buffer[lookahead]</#if><#if !unicode> & 0xff</#if>]];
<#else>
					int symbol = cc_ecs[<#if parser.lineMode>ch<#else>buffer[lookahead]</#if><#if !unicode> & 0xff</#if>];
					cc_toState = cc_matchedState;
	<#if !parser.dfa.default?has_content>
					if (cc_check[symbol + cc_base[cc_matchedState]] == cc_matchedState)
						cc_toState = cc_next[symbol + cc_base[cc_matchedState]];
					else
						cc_toState = 0;
	<#elseif !parser.dfa.error?has_content>
					if (cc_check[symbol + cc_base[cc_matchedState]] == cc_matchedState)
						cc_toState = cc_next[symbol + cc_base[cc_matchedState]];
					else
						cc_toState = cc_default[cc_matchedState];
	<#elseif !parser.dfa.meta?has_content>
					while (cc_check[symbol + cc_base[cc_toState]] != cc_toState)
					{
						cc_toState = cc_default[cc_toState];
						if (cc_toState >= ${parser.dfa.size})
							symbol = 0;
					}
					cc_toState = cc_next[symbol + cc_base[cc_toState]];
	<#else>
					while (cc_check[symbol + cc_base[cc_toState]] != cc_toState)
					{
						cc_toState = cc_default[cc_toState];
						if (cc_toState >= ${parser.dfa.size})
							symbol = cc_meta[symbol];
					}
					cc_toState = cc_next[symbol + cc_base[cc_toState]];
	</#if>
</#if>

<#if parser.backup>
					if (cc_toState == 0)
					{
	<#if debug>
						debugLexerBackup (_yyBaseState, cc_matchedState, new String (_yyBuffer, _yyMatchStart, matchedLength));
	</#if>
						cc_matchedState = cc_backupMatchedState;
						matchedLength = cc_backupMatchedLength;
						break;
					}
<#else>
					if (cc_toState == 0)
						break;
</#if>

					cc_matchedState = cc_toState;
					++lookahead;
					++matchedLength;

<#if parser.backup>
					if (cc_accept[cc_matchedState] > 0)
					{
						cc_backupMatchedState = cc_toState;
						cc_backupMatchedLength = matchedLength;
					}
</#if>
<#if parser.lineMode>
					if (ch == '\n')
					{
	<#if parser.backup>
						cc_matchedState = cc_backupMatchedState;
						matchedLength = cc_backupMatchedLength;
	</#if>
						break;
					}
</#if>
				}
				else
				{
					int lookPos = lookahead - _yyMatchStart;
					boolean refresh = yyRefreshBuffer ();
					buffer = _yyBuffer;
					internalBufferEnd = _yyBufferEnd;
					lookahead = _yyMatchStart + lookPos;
					if (! refresh)
					{
						// <<EOF>>
						int cc_toState;
<#if parser.table == "full">
						cc_toState = cc_next[cc_matchedState][${parser.eof}];
<#elseif parser.table == "ecs">
						cc_toState = cc_next[cc_matchedState][cc_ecs[${parser.eof}]];
<#elseif parser.table == "compressed">
						int symbol = cc_ecs[${parser.eof}];
	<#if !parser.dfa.default?has_content>
						if (cc_check[symbol + cc_base[cc_matchedState]] == cc_matchedState)
							cc_toState = cc_next[symbol + cc_base[cc_matchedState]];
						else
							cc_toState = 0;
	<#elseif !parser.dfa.error>
						if (cc_check[symbol + cc_base[cc_matchedState]] == cc_matchedState)
							cc_toState = cc_next[symbol + cc_base[cc_matchedState]];
						else
							cc_toState = cc_default[cc_matchedState];
	<#elseif !parser.dfa.meta?has_content>
						cc_toState = cc_matchedState;
						while (cc_check[symbol + cc_base[cc_toState]] != cc_toState)
						{
							cc_toState = cc_default[cc_toState];
							if (cc_toState >= ${parser.dfa.size})
								symbol = 0;
						}
						cc_toState = cc_next[symbol + cc_base[cc_toState]];
	<#else>
						cc_toState = cc_matchedState;
						while (cc_check[symbol + cc_base[cc_toState]] != cc_toState)
						{
							cc_toState = cc_default[cc_toState];
							if (cc_toState >= ${parser.dfa.size})
								symbol = cc_meta[symbol];
						}
						cc_toState = cc_next[symbol + cc_base[cc_toState]];
	</#if>

</#if>
						if (cc_toState != 0)
							cc_matchedState = cc_toState;
<#if parser.backup>
						else
						{
							cc_matchedState = cc_backupMatchedState;
							matchedLength = cc_backupMatchedLength;
						}
</#if>
						break;
					}
				}
			}

			_yyTextStart = _yyMatchStart;
			_yyMatchStart += matchedLength;
			_yyLength = matchedLength;

<#if debug>
			debugLexer (_yyBaseState, cc_matchedState, cc_accept[cc_matchedState]);
</#if>

			switch (cc_accept[cc_matchedState])
			{
<#list parser.cases as i>
	<#if !i.internal>
		<#list i.patterns as p>
				case ${p.caseValue}:	// ${p.pattern}
				{
			<#if p.trailContext != 0 && p.trailContext != 3>
				<#if (p.trailContext % 2) == 1>
					_yyLength = ${p.trailLength};
					_yyMatchStart = _yyTextStart + ${p.trailLength};
				<#else>
					_yyLength -= ${p.trailLength};
					_yyMatchStart -= ${p.trailLength};
				</#if>
			</#if>
					${i.action}
				}
				case ${p.caseValue + parser.caseCount + 1}: break;
		</#list>
	<#else>
		<#list i.patterns as p>
				case ${p.caseValue}:	// ${p.pattern}
				{
			<#if p.pattern == "<<EOF>>">
					return 0;			// default EOF action
			<#else>
					echo ();			// default character action
			</#if>
				}
				case ${p.caseValue + parser.caseCount + 1}: break;
		</#list>
	</#if>
</#list>
				default:
					throw new IOException ("Internal error in ${ccclass} parser.");
			}

<#if parser.bol || parser.lineMode>
			// check BOL here since '\n' may be unput back into the stream buffer

			// specifically used _yyBuffer since it could be changed by user
			if (_yyMatchStart > 0 && _yyBuffer[_yyMatchStart - 1] == '\n')
			{
	<#if parser.bol>
				_yyIsNextBOL = true;
	</#if>
	<#if parser.lineMode>
				++_yyLineNumber;
	</#if>
			}
</#if>
		}
	}
</#if>
<#if !parser?has_content>
	/**
	 * Override this function to start the scanning of the input.  This function
	 * is used by the parser to scan the lexemes.
	 *
	 * @return	a status value.
	 * @throws	IOException
	 *			in case of I/O error.
	 */
	protected int yyLex () throws IOException
	{
		return 0;
	}

	/**
	 * Return the object associate with the token.  This function is only generated
	 * when the parser is not specified.
	 *
	 * @return	the object associated with the token.
	 */
	protected Object yyValue ()
	{
		return null;
	}
</#if>

<#if parser?has_content>
	/**
	 * Obtain the string representation for a symbol, which includes terminals
	 * and non-terminals.
	 *
	 * @param	symbol
	 *			The integer value of a symbol
	 * @return	the string representation of the symbol
	 */
	protected String getSymbolName (int symbol)
	{
		if (symbol < 0 || symbol > (255 + cc_parser_symbol.symbols.length))
			return "Unknown symbol: " + symbol;
		switch (symbol)
		{
			case 0:
				return "$";
			case 1:
				return "error";
			case '\\':
				return "'\\\\'";
			default:
				if (symbol > 255)
					return cc_parser_symbol.symbols[symbol - 256];
				if (symbol < 32 || symbol >= 127)
					return "'\\x" + Integer.toHexString (symbol) + "'";
				return "'" + ((char)symbol) + "'";
		}
	}

	/**
	 * Get the debugging string that represent the current parsing stack.
	 *
	 * @param	states
	 *			the current stack
	 * @return	a string representation of the parsing stack.
	 */
	protected String getStateString (Collection<YYParserState> states)
	{
		StringBuffer buffer = new StringBuffer ();
		boolean first = true;
		for (YYParserState state : states)
		{
			if (!first)
				buffer.append (" ");
			if (state.token < 0)
				buffer.append (state.token);
			else
				buffer.append (getSymbolName (state.token));
			first = false;
		}
		return buffer.toString ();
	}

	<#if debug>
	protected boolean debugParser (int fromState, int toState, int reduceState, int symbol)
	{
		if (toState == 0)
			System.err.println ("parser: " + fromState + " -> " + toState + " on " + getSymbolName (symbol) + ", error");
		else if (toState < 0)
			System.err.println ("parser: " + fromState + " -> " + toState + " on " + getSymbolName (symbol) + ", reduce " + getSymbolName (cc_parser_symbol.reverseECS[reduceState]));
		else
			System.err.println ("parser: " + fromState + " -> " + toState + " on " + getSymbolName (symbol) + ", shift");
		return true;
	}
	</#if>

	/**
	 * Call this function to start parsing.
	 *
	 * @return	0 if everything is okay, or 1 if an error occurred.
	 * @throws	IOException
	 *			in case of error
	 */
<#if parser.getProperty("SuppressUnCheckWarning")?has_content && parser.getProperty("SuppressUnCheckWarning")?string == 'true'>
	@SuppressWarnings ("unchecked")
</#if>
	public int yyParse () throws IOException
	{
 <#if code.parserprolog?has_content>
		${code.parserprolog}

 </#if>
		char[] cc_ecs = cc_parser.ecs;
<#if parser.table == "ecs">
		char[][] cc_next = cc_parser.next;
<#else>
		char[] cc_next = cc_parser.next;
		char[] cc_check = cc_parser.check;
		char[] cc_base = cc_parser.base;
	<#if parser.dfa.default?has_content>
		char[] cc_default = cc_parser.defaults;
	</#if>
	<#if parser.dfa.meta?has_content>
		char[] cc_meta = cc_parser.meta;
	</#if>
	<#if parser.dfa.gotoDefault?has_content>
		char[] cc_gotoDefault = cc_parser.gotoDefault;
	</#if>
</#if>
		char[] cc_rule = cc_parser.rule;
		char[] cc_lhs = cc_parser.lhs;

		ArrayList<YYParserState> cc_lookaheadStack = _yyLookaheadStack;
		ArrayList<YYParserState> cc_stateStack = _yyStateStack;

		if (cc_stateStack.size () == 0)
			cc_stateStack.add (new YYParserState ());

		int cc_toState;

<#if parser.captureList>
		ArrayList<Object[]> captureList = null;
</#if>
		for (;;)
		{
			YYParserState cc_lookahead;

			int cc_fromState;
			char cc_ch;

			//
			// check if there are any lookahead lexemes on stack
			// if not, then call yyLex ()
			//
			if (cc_lookaheadStack.size () == 0)
			{
<#if parser?has_content>
				_yyValue = null;
				int val = yyLex ();
<#else>
				int val = yyLex ();
				_yyValue = yyValue ();
</#if>
				cc_ch = cc_ecs[val];
<#if parser.ignoreList>
				if (cc_ch == 3)	// Ignore List
				{
					continue;
				}
</#if>
<#if parser.captureList>
				else if (cc_ch == 4)	// Capture List
				{
					if (captureList == null)
					{
						captureList = new ArrayList<Object[]> ();
					}
					captureList.add (new Object[] { getInteger (val), _yyValue });
					continue;
				}
</#if>
				cc_lookahead = new YYParserState (val, _yyValue);
<#if parser.captureList>
				cc_lookahead.captureList = captureList;
				captureList = null;
</#if>
				cc_lookaheadStack.add (cc_lookahead);
			}
			else
			{
				cc_lookahead = cc_lookaheadStack.get (cc_lookaheadStack.size () - 1);
				cc_ch = cc_ecs[cc_lookahead.token];
			}

			cc_fromState = cc_stateStack.get (cc_stateStack.size () - 1).state;
<#if parser.table == "ecs">
			cc_toState = (short)cc_next[cc_fromState][cc_ch];
<#else>
	<#if !parser.dfa.default?has_content>
			if (cc_check[cc_ch + cc_base[cc_fromState]] == cc_fromState)
				cc_toState = (short)cc_next[cc_ch + cc_base[cc_fromState]];
			else
				cc_toState = 0;
	<#elseif !parser.dfa.error?has_content>
			if (cc_check[cc_ch + cc_base[cc_fromState]] == cc_fromState)
				cc_toState = (short)cc_next[cc_ch + cc_base[cc_fromState]];
			else
				cc_toState = (short)cc_default[cc_fromState];
	<#elseif !parser.dfa.meta?has_content>
			int cc_symbol = cc_ch;
			cc_toState = cc_fromState;
			while (cc_check[cc_symbol + cc_base[cc_toState]] != cc_toState)
			{
				cc_toState = cc_default[cc_toState];
				if (cc_toState >= ${parser.dfa.size})
					cc_symbol = 0;
			}
			cc_toState = (short)cc_next[cc_symbol + cc_base[cc_toState]];
	<#else>
			int cc_symbol = cc_ch;
			cc_toState = cc_fromState;
			while (cc_check[cc_symbol + cc_base[cc_toState]] != cc_toState)
			{
				cc_toState = cc_default[cc_toState];
				if (cc_toState >= ${parser.dfa.size})
					cc_symbol = cc_meta[cc_symbol];
			}
			cc_toState = (short)cc_next[cc_symbol + cc_base[cc_toState]];
	</#if>
</#if>

<#if debug>
			debugParser (cc_fromState, cc_toState, cc_toState < 0 ? cc_parser.lhs[-cc_toState] : 0, cc_lookahead.token);
</#if>

			//
			// check the value of toState and determine what to do
			// with it
			//
			if (cc_toState > 0)
			{
				// shift
				cc_lookahead.state = cc_toState;
				cc_stateStack.add (cc_lookahead);
				cc_lookaheadStack.remove (cc_lookaheadStack.size () - 1);
				continue;
			}
			else if (cc_toState == 0)
			{
<#if parser.recovery>
				// error
				if (_yyInError)
				{
					// first check if the error is at the lookahead
					if (cc_ch == 1)
					{
						// so we need to reduce the stack until a state with reduceable
						// action is found
						if (_yyStateStack.size () > 1)
							_yyStateStack.remove (_yyStateStack.size () - 1);
						else
							return 1;	// can't do much we exit the parser
					}
					else
					{
						// this means that we need to dump the lookahead.
						if (cc_ch == 0)		// can't do much with EOF;
							return 1;
						cc_lookaheadStack.remove (cc_lookaheadStack.size () - 1);
					}
					continue;
				}
				else
				{
					if (yyParseError (cc_lookahead.token))
						return 1;
	<#if debug>
					System.err.println ("parser: inject error token as lookahead");
	</#if>
					_yyLookaheadStack.add (new YYParserState (1, _yyValue));
					_yyInError = true;
					continue;
				}
<#else>
				return 1;
</#if>
			}
<#if parser.recovery>
			_yyInError = false;
</#if>
			// now the reduce action
			int cc_ruleState = -cc_toState;

			_yyArgStart = cc_stateStack.size () - cc_rule[cc_ruleState] - 1;
			//
			// find the state that said need this non-terminal
			//
			cc_fromState = cc_stateStack.get (_yyArgStart).state;

			//
			// find the state to goto after shifting the non-terminal
			// onto the stack.
			//
			if (cc_ruleState == 1)
				cc_toState = 0;			// reset the parser
			else
			{
<#if parser.table == "ecs">
				cc_toState = cc_next[cc_fromState][cc_lhs[cc_ruleState]];
<#else>
				cc_toState = cc_fromState + ${parser.dfa.baseAdd};
				int cc_tmpCh = cc_lhs[cc_ruleState] - ${parser.dfa.usedTerminalCount};
	<#if !parser.dfa.gotoDefault?has_content>
				if (cc_check[cc_tmpCh + cc_base[cc_toState]] == cc_toState)
					cc_toState = cc_next[cc_tmpCh + cc_base[cc_toState]];
				else
					cc_toState = 0;
	<#else>
				while (cc_check[cc_tmpCh + cc_base[cc_toState]] != cc_toState)
					cc_toState = cc_gotoDefault[cc_toState - ${parser.dfa.baseAdd}];
				cc_toState = cc_next[cc_tmpCh + cc_base[cc_toState]];
	</#if>
</#if>
			}

			_yyValue = null;

			switch (cc_ruleState)
			{
				case 1:					// accept
					return 0;
<#list parser.cases as i>
<#if i.type == 'n'>
<#list i.rhs as p>
				case ${p.caseValue}:	// ${i.rule} : ${p.terms}
<#if p.translatedTerms??>					// converted to ${i.rule} : ${p.translatedTerms} </#if>
				{
					<#list p.action?actioncode() as a><#if a_index % 2 == 0>${a}<#else><#if a == "$">_yyValue<#else><@type p a parser.formats "yyGetValue (" + a + ")"/></#if></#if></#list>
				}
				case ${p.caseValue + parser.caseCount}: break;
</#list>
<#elseif i.type == 'g'>
<#list i.rhs as p>
				// internally generated group rule
				case ${p.caseValue}:	// ${i.rule} : ${p.terms}
				{
					org.yuanheng.cookcc.ASTNode ast = new org.yuanheng.cookcc.ASTNode (${i.symbol}, "${i.rule}", ${p.caseValue});
					<#if p.termCount == 1>
					ast.add (yyGetValue (1));
					<#else>
					for (int i = 0; i < ${p.termCount}; ++i)
					{
						ast.add (yyGetValue (i + 1));
					}
					</#if>
					_yyValue = ast;
					break;
				}
</#list>
<#elseif i.type == '?'>
<#list i.rhs as p>
				// internally generated optional rule
				case ${p.caseValue}:	// ${i.rule} : ${p.terms}
				{
				<#if p_index == 0>
					_yyValue = null;
				<#else>
					<#if p.termCount == 1>
					_yyValue = yyGetValue (1);
					<#else>
					org.yuanheng.cookcc.ASTNode ast = new org.yuanheng.cookcc.ASTNode (${i.symbol}, "${i.rule}", ${p.caseValue});
					for (int i = 0; i < ${p.termCount}; ++i)
					{
						ast.add (yyGetValue (i + 1));
					}
					_yyValue = ast;
					</#if>
				</#if>
					break;
				}
</#list>
<#elseif i.type == '*'>
<#list i.rhs as p>
				// internally generated optional list rule
				case ${p.caseValue}:	// ${i.rule} : ${p.terms}
				{
				<#if p_index == 0>
					_yyValue = new org.yuanheng.cookcc.ASTListNode (${i.symbol}, "${i.rule}", ${p.caseValue});
				<#else>
					org.yuanheng.cookcc.ASTListNode ast = (org.yuanheng.cookcc.ASTListNode)yyGetValue (1);
					<#if p.termCount == 1>
					ast.add (yyGetValue (2));
					<#else>
					for (int i = 1; i < ${p.termCount}; ++i)
					{
						ast.add (yyGetValue (i + 1));
					}
					</#if>
					_yyValue = ast;
				</#if>
					break;
				}
</#list>
<#elseif i.type == '+'>
<#list i.rhs as p>
				// internally generated list rule
				case ${p.caseValue}:	// ${i.rule} : ${p.terms}
				{
				<#if p_index == 0>
					org.yuanheng.cookcc.ASTListNode ast = new org.yuanheng.cookcc.ASTListNode (${i.symbol}, "${i.rule}", ${p.caseValue});
					<#if p.termCount == 1>
					ast.add (yyGetValue (1));
					<#else>
					for (int i = 0; i < ${p.termCount}; ++i)
					{
						ast.add (yyGetValue (i + 1));
					}
					</#if>
				<#else>
					org.yuanheng.cookcc.ASTListNode ast = (org.yuanheng.cookcc.ASTListNode)yyGetValue (1);
					<#if p.termCount == 1>
					((org.yuanheng.cookcc.ASTListNode)_yyValue).add (yyGetValue (2));
					<#else>
					for (int i = 1; i < ${p.termCount}; ++i)
					{
						ast.add (yyGetValue (i + 1));
					}
					</#if>
				</#if>
					_yyValue = ast;
					break;
				}
</#list>
<#elseif i.type == '|'>
<#list i.rhs as p>
				// internally generated or rule
				case ${p.caseValue}:	// ${i.rule} : ${p.terms}
				{
					<#if p.termCount == 1>
					_yyValue = yyGetValue (1);
					<#else>
					org.yuanheng.cookcc.ASTNode ast = new org.yuanheng.cookcc.ASTNode (${i.symbol}, "${i.rule}", ${p.caseValue});
					for (int i = 0; i < ${p.termCount}; ++i)
					{
						ast.add (yyGetValue (i + 1));
					}
					_yyValue = ast;
					</#if>
					break;
				}
</#list>
</#if>
</#list>
				default:
					throw new IOException ("Internal error in ${ccclass} parser.");
			}

			YYParserState cc_reduced = new YYParserState (-cc_ruleState, _yyValue, cc_toState);
			_yyValue = null;
			cc_stateStack.subList (_yyArgStart + 1, cc_stateStack.size ()).clear ();
			cc_stateStack.add (cc_reduced);
		}
	}

<#if parser.recovery>
	/**
	 * This function is used by the error handling grammars to check the immediate
	 * lookahead token on the stack.
	 *
	 * @return	the top of lookahead stack.
	 */
	protected YYParserState yyPeekLookahead ()
	{
		return _yyLookaheadStack.get (_yyLookaheadStack.size () - 1);
	}

	/**
	 * This function is used by the error handling grammars to pop an unwantted
	 * token from the lookahead stack.
	 */
	protected void yyPopLookahead ()
	{
		_yyLookaheadStack.remove (_yyLookaheadStack.size () - 1);
	}

	/**
	 * Clear the error flag.  If this flag is present and the parser again sees
	 * another error transition, it would immediately calls yyParseError, which
	 * would by default exit the parser.
	 * <p>
	 * This function is used in error recovery.
	 */
	protected void yyClearError ()
	{
		_yyInError = false;
	}

	/**
	 * Check if the terminal is not handled by the parser.
	 *
	 * @param	terminal
	 *			terminal obtained from calling yyLex ()
	 * @return	true if the terminal is not handled by the parser.
	 * 			false otherwise.
	 */
	protected boolean isUnhandledTerminal (int terminal)
	{
		return cc_parser.ecs[terminal] == 2;
	}

<#if parser.parseError>
	/**
	 * This function reports error and return true if critical error occurred, or
	 * false if the error has been successfully recovered.  IOException is an optional
	 * choice of reporting error.
	 *
	 * @param	terminal
	 *			the terminal that caused the error.
	 * @return	true if irrecoverable error occurred.  Or simply throw an IOException.
	 *			false if the parsing can be continued to check for specific
	 *			error lexemes.
	 * @throws	IOException
	 *			in case of error.
	 */
	protected boolean yyParseError (int terminal) throws IOException
	{
<#if debug>
		System.err.println ("parser: fatal error");
</#if>
		if (isUnhandledTerminal (terminal))
			return true;
		return false;
	}
</#if>
</#if>

	/**
	 * Gets the object value associated with the symbol at the argument's position.
	 *
	 * @param	arg
	 *			the symbol position starting from 1.
	 * @return	the object value associated with symbol.
	 */
	protected Object yyGetValue (int arg)
	{
		return _yyStateStack.get (_yyArgStart + arg).value;
	}

	/**
	 * Set the object value for the current non-terminal being reduced.
	 *
	 * @param	value
	 * 			the object value for the current non-terminal.
	 */
	protected void yySetValue (Object value)
	{
		_yyValue = value;
	}

	/**
	 * Obtain the current list of captured terminals.
	 * <p>
	 * Each Object[] contains two values.  The first is the {@link Integer} value
	 * of the terminal.  The second value is the value associated with the terminal.
	 *
	 * @param	arg
	 *			the symbol position starting from 1.
	 * @return	the captured terminals associated with the symbol
	 */
	protected Collection<Object[]> getCapturedTerminals (int arg)
	{
<#if parser.captureList>
		return _yyStateStack.get (_yyArgStart + arg).captureList;
<#else>
		return null;
</#if>
	}

<#if parser.captureList>
	/**
	 * A small utility to avoid too many Integer object creations.
	 *
	 * @param	symbol
	 *			an integer value.  Usually it is a symbol.
	 * @return	an Integer value matching the symbol value passed in.
	 */
	private Integer getInteger (int symbol)
	{
		if (_yySymbolArray == null)
			_yySymbolArray = new Integer[${parser.maxTerminal} + ${parser.nonTerminalCount} + 1];
		if (symbol < 0 || symbol >= _yySymbolArray.length)
			return new Integer (symbol);
		if (_yySymbolArray[symbol] == null)
			_yySymbolArray[symbol] = new Integer (symbol);
		return _yySymbolArray[symbol];
	}
</#if>
</#if>

<#if code.default?has_content>
${code.default}
</#if>

<#if unicode>
	protected static Reader open (String file) throws IOException
	{
		return new InputStreamReader (new FileInputStream (file));
	}
<#else>
	protected static InputStream open (String file) throws IOException
	{
		return new FileInputStream (file);
	}
</#if>

<#if main?has_content && main?string == "true">
	/**
	 * This is a stub main function that either reads the file that user specified
	 * or from the standard input.
	 *
	 * @param	args
	 *			command line arguments.
	 *
	 * @throws	Exception
	 *			in case of any errors.
	 */
	public static void main (String[] args) throws Exception
	{
<#if parser?has_content>
		${ccclass} tmpParser = new ${ccclass} ();
	<#if parser?has_content>
		if (args.length > 0)
			tmpParser.setInput (open (args[0]));
	</#if>

		if (tmpParser.yyParse () > 0)
		{
			System.err.println ("parser: fatal error!");
			System.exit (1);
		}
<#else>
		${ccclass} tmpLexer = new ${ccclass} ();
		tmpLexer.setInput (open (args[0]));

		tmpLexer.yyLex ();
</#if>
	}
</#if>

/*
 * parser properties:
 * unicode = ${unicode?string}
<#if parser?has_content>
 * bol = ${parser.bol?string}
 * backup = ${parser.backup?string}
 * cases = ${parser.caseCount}
 * table = ${parser.table}
<#if parser.table == "ecs" || parser.table == "compressed">
 * ecs = ${parser.ecsGroupCount}
</#if>
 * states = ${parser.dfa.size}
 * max symbol value = ${parser.maxSymbol}
 *
 * memory usage:
 * full table = ${(parser.eof + 1) * parser.dfa.size}
<#if parser.table == "ecs" || parser.table == "compressed">
 * ecs table = ${parser.eof + 1 + parser.ecsGroupCount * parser.dfa.size}
</#if>
<#if parser.table == "compressed">
 * next = ${parser.dfa.next?size}
 * check = ${parser.dfa.check?size}
<#if !parser.dfa.default?has_content>
 * compressed table = ${parser.eof + 1 + parser.dfa.next?size + parser.dfa.next?size}
<#else>
 * default = ${parser.dfa.default?size}
<#if !parser.dfa.meta?has_content>
 * compressed table = ${parser.eof + 1 + parser.dfa.next?size + parser.dfa.next?size + parser.dfa.default?size}
<#else>
 * meta = ${parser.dfa.meta?size}
 * compressed table = ${parser.eof + 1 + parser.dfa.next?size + parser.dfa.next?size + parser.dfa.default?size + parser.dfa.meta?size}
</#if>
</#if>
</#if>
<#else>
</#if>
 *
<#if parser?has_content>
 * parser properties:
 * symbols = ${parser.symbols?size}
 * max terminal = ${parser.maxTerminal}
 * used terminals = ${parser.usedTerminalCount}
 * non-terminals = ${parser.nonTerminalCount}
 * rules = ${parser.rules?size - 1}
 * shift/reduce conflicts = ${parser.shiftConflict}
 * reduce/reduce conflicts = ${parser.reduceConflict}
 *
 * memory usage:
 * ecs table = ${(parser.ecs?size + (parser.usedTerminalCount + parser.nonTerminalCount) * parser.dfa.size)}
<#if parser.table == "compressed">
 * compressed table = ${parser.ecs?size + parser.dfa.totalSize}
</#if>
</#if>
 */
}
