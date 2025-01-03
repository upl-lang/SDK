	/*
	 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
	 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
	 *
	 * This code is free software; you can redistribute it and/or modify it
	 * under the terms of the GNU General Public License version 2 only, as
	 * published by the Free Software Foundation.  Oracle designates this
	 * particular file as subject to the "Classpath" exception as provided
	 * by Oracle in the LICENSE file that accompanied this code.
	 *
	 * This code is distributed in the hope that it will be useful, but WITHOUT
	 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
	 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
	 * version 2 for more details (a copy is included in the LICENSE file that
	 * accompanied this code).
	 *
	 * You should have received a copy of the GNU General Public License version
	 * 2 along with this work; if not, write to the Free Software Foundation,
	 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
	 *
	 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
	 * or visit www.oracle.com if you need additional information or have any
	 * questions.
	 */
	
	package com.florianingerl.util.regex;
	
	import com.florianingerl.util.regex.nodes.CustomNode;
	import java.util.Arrays;
	import java.util.HashMap;
	import java.util.Map;
	import java.util.Objects;
	import java.util.Stack;
	import java.util.Vector;
	import java.util.function.Function;
	
	/**
	 * An engine that performs match operations on a
	 * {@linkplain java.lang.CharSequence character sequence} by interpreting a
	 * {@link Pattern}.
	 *
	 * <p>
	 * A matcher is created from a pattern by invoking the pattern's
	 * {@link Pattern#matcher matcher} method. Once created, a matcher can be used
	 * to perform three different kinds of match operations:
	 *
	 * <ul>
	 *
	 * <li>
	 * <p>
	 * The {@link #matches matches} method attempts to match the entire input
	 * sequence against the pattern.
	 * </p>
	 * </li>
	 *
	 * <li>
	 * <p>
	 * The {@link #lookingAt lookingAt} method attempts to match the input sequence,
	 * starting at the beginning, against the pattern.
	 * </p>
	 * </li>
	 *
	 * <li>
	 * <p>
	 * The {@link #find find} method scans the input sequence looking for the next
	 * subsequence that matches the pattern.
	 * </p>
	 * </li>
	 *
	 * </ul>
	 *
	 * <p>
	 * Each of these methods returns a boolean indicating success or failure. More
	 * information about a successful match can be obtained by querying the state of
	 * the matcher.
	 *
	 * <p>
	 * A matcher finds matches in a subset of its input called the <i>region</i>. By
	 * default, the region contains all of the matcher's input. The region can be
	 * modified via the{@link #region region} method and queried via the
	 * {@link #regionStart regionStart} and {@link #regionEnd regionEnd} methods.
	 * The way that the region boundaries interact with some pattern constructs can
	 * be changed. See {@link #useAnchoringBounds useAnchoringBounds} and
	 * {@link #useTransparentBounds useTransparentBounds} for more details.
	 *
	 * <p>
	 * This class also defines methods for replacing matched subsequences with new
	 * strings whose contents can, if desired, be computed from the match result.
	 * The {@link #appendReplacement appendReplacement} and {@link #appendTail
	 * appendTail} methods can be used in tandem in order to collect the result into
	 * an existing string buffer, or the more convenient {@link #replaceAll
	 * replaceAll} method can be used to create a string in which every matching
	 * subsequence in the input sequence is replaced.
	 *
	 * <p>
	 * The explicit state of a matcher includes the start and end indices of the
	 * most recent successful match. It also includes the start and end indices of
	 * the input subsequence captured by each <a href="Pattern.html#cg">capturing
	 * group</a> in the pattern as well as a total count of such subsequences. As a
	 * convenience, methods are also provided for returning these captured
	 * subsequences in string form.
	 *
	 * <p>
	 * The explicit state of a matcher is initially undefined; attempting to query
	 * any part of it before a successful match will cause an
	 * {@link IllegalStateException} to be thrown. The explicit state of a matcher
	 * is recomputed by every match operation.
	 *
	 * <p>
	 * The implicit state of a matcher includes the input character sequence as well
	 * as the <i>append position</i>, which is initially zero and is updated by the
	 * {@link #appendReplacement appendReplacement} method.
	 *
	 * <p>
	 * A matcher may be reset explicitly by invoking its {@link #reset()} method or,
	 * if a new input sequence is desired, its {@link #reset(java.lang.CharSequence)
	 * reset(CharSequence)} method. Resetting a matcher discards its explicit state
	 * information and sets the append position to zero.
	 *
	 * <p>
	 * Instances of this class are not safe for use by multiple concurrent threads.
	 * </p>
	 *
	 * @author Mike McCloskey
	 * @author Mark Reinhold
	 * @author JSR-51 Expert Group
	 * @since 1.4
	 */
	
	public class Matcher implements MatchResult {
		
		/**
		 * The Pattern object that created this Matcher.
		 */
		Pattern parentPattern;
		
		/**
		 * Enables the creation of a so-called Capture Tree during matching.
		 *
		 * @see Matcher#setMode(int)
		 * @see Matcher#captureTree()
		 */
		public static final int CAPTURE_TREE = 1;
		int mode = 0;
		public boolean captureTreeMode;
		public Map<Class<? extends CustomNode>, Object> data;
		
		/**
		 * The range within the sequence that is to be matched. Anchors will match at
		 * these "hard" boundaries. Changing the region changes these values.
		 */
		public int from;
		public int to;
		
		/**
		 * Lookbehind uses this value to ensure that the subexpression match ends at the
		 * point where the lookbehind was encountered.
		 */
		public int lookbehindTo;
		
		/**
		 * The original string being matched.
		 */
		CharSequence text;
		
		/**
		 * Matcher state used by the last node. NOANCHOR is used when a match does not
		 * have to consume all of the input. ENDANCHOR is the mode used for matching all
		 * the input.
		 */
		public static final int ENDANCHOR = 1;
		public static final int NOANCHOR = 0;
		public int acceptMode = NOANCHOR;
		
		/**
		 * The range of string that last matched the pattern. If the last match failed
		 * then first is -1; last initially holds 0 then it holds the index of the end
		 * of the last match (which is where the next search starts).
		 */
		public int first = -1;
		public int last = 0;
		
		/**
		 * The end index of what matched in the last match operation.
		 */
		public int oldLast = -1;
		
		/**
		 * The index of the last position appended in a substitution.
		 */
		int lastAppendPosition = 0;
		
		/**
		 * Storage used by GroupHead and GroupTail nodes used to track where groups
		 * begin. The nodes themselves are stateless, so they rely on this field to hold
		 * state during a match.
		 */
		public Vector<Stack<Integer>> localVector;
		public int[] recursions;
		public int activity;
		public CaptureTreeNode captureTreeNode;
		public int[] groups;
		protected CaptureTree captureTree;
		public Node[] nextNodes;
		
		public int[] locals; // TODO Not using
		
		/**
		 * Boolean indicating whether or not more input could change the results of the
		 * last match.
		 * <p>
		 * If hitEnd is true, and a match was found, then more input might cause a
		 * different match to be found. If hitEnd is true and a match was not found,
		 * then more input could cause a match to be found. If hitEnd is false and a
		 * match was found, then more input will not change the match. If hitEnd is
		 * false and a match was not found, then more input will not cause a match to be
		 * found.
		 */
		public boolean hitEnd;
		
		/**
		 * Boolean indicating whether or not more input could change a positive match
		 * into a negative one.
		 * <p>
		 * If requireEnd is true, and a match was found, then more input could cause the
		 * match to be lost. If requireEnd is false and a match was found, then more
		 * input might change the match but the match won't be lost. If a match was not
		 * found, then requireEnd has no meaning.
		 */
		public boolean requireEnd;
		
		/**
		 * If transparentBounds is true then the boundaries of this matcher's region are
		 * transparent to lookahead, lookbehind, and boundary matching constructs that
		 * try to see beyond them.
		 */
		public boolean transparentBounds = false;
		
		/**
		 * If anchoringBounds is true then the boundaries of this matcher's region match
		 * anchors such as ^ and $.
		 */
		public boolean anchoringBounds = true;
		
		/**
		 * No default constructor.
		 */
		Matcher () {
		}
		
		/**
		 * All matchers have the state used by Pattern during a match.
		 */
		Matcher (Pattern parent, CharSequence text) {
			this.parentPattern = parent;
			this.text = text;
			
			init ();
			// Put fields into initial states
			reset ();
		}
		
		protected void init () {
			// Allocate state storage
			groups = new int[parentPattern.capturingGroupCount * 2];
			recursions = new int[parentPattern.capturingGroupCount * 3];
			localVector = new Vector<> (parentPattern.localCount);
			localVector.setSize (parentPattern.localCount);
			nextNodes = new Node[parentPattern.localCount];
		}
		
		protected void genData () {
			data = new HashMap<Class<? extends CustomNode>, Object> ();
		}
		
		/**
		 * Returns the pattern that is interpreted by this matcher.
		 *
		 * @return The pattern for which this matcher was created
		 */
		public Pattern pattern () {
			return parentPattern;
		}
		
		/**
		 * Returns the match state of this matcher as a {@link MatchResult}. The result
		 * is unaffected by subsequent operations performed upon this matcher.
		 *
		 * @return a <code>MatchResult</code> with the state of this matcher
		 * @since 1.5
		 */
		public MatchResult toMatchResult () {
			Matcher result = new Matcher (this.parentPattern, text.toString ());
			result.first = this.first;
			result.last = this.last;
			result.groups = new int[this.groups.length];
			for (int i = 0; i < this.groups.length; ++i) {
				result.groups[i] = this.groups[i];
			}
			result.captureTreeNode = this.captureTreeNode;
			return result;
			
		}
		
		/**
		 * Changes the <code>Pattern</code> that this <code>Matcher</code> uses to find matches
		 * with.
		 *
		 * <p>
		 * This method causes this matcher to lose information about the groups of the
		 * last match that occurred. The matcher's position in the input is maintained
		 * and its last append position is unaffected.
		 * </p>
		 *
		 * @param newPattern The new pattern used by this matcher
		 * @return This matcher
		 * @throws IllegalArgumentException If newPattern is <code>null</code>
		 * @since 1.5
		 */
		public Matcher usePattern (Pattern newPattern) {
			if (newPattern == null)
				throw new IllegalArgumentException ("Pattern cannot be null");
			parentPattern = newPattern;
			
			init ();
			// Put fields into initial states
			reset ();
			
			return this;
		}
		
		/**
		 * Resets this matcher.
		 *
		 * <p>
		 * Resetting a matcher discards all of its explicit state information and sets
		 * its append position to zero. The matcher's region is set to the default
		 * region, which is its entire character sequence. The anchoring and
		 * transparency of this matcher's region boundaries are unaffected.
		 *
		 * @return This matcher
		 */
		public Matcher reset () {
			first = -1;
			last = 0;
			oldLast = -1;
			captureTree = null;
			captureTreeNode = captureTreeMode ? new CaptureTreeNode () : null;
			for (int i = 0; i < localVector.size (); i++) {
				localVector.set (i, new Stack<Integer> ());
				nextNodes[i] = null;
			}
			Arrays.fill (groups, -1);
			resetRecursions ();
			activity = 0;
			genData ();
			lastAppendPosition = 0;
			from = 0;
			to = getTextLength ();
			return this;
		}
		
		protected void resetRecursions () {
			for (int i = 0; i < parentPattern.capturingGroupCount; ++i) {
				recursions[i * 3] = -1;
				//recursions[i*3+1] doesn't matter
				recursions[i * 3 + 2] = 0;
			}
		}
		
		/**
		 * Resets this matcher with a new input sequence.
		 *
		 * <p>
		 * Resetting a matcher discards all of its explicit state information and sets
		 * its append position to zero. The matcher's region is set to the default
		 * region, which is its entire character sequence. The anchoring and
		 * transparency of this matcher's region boundaries are unaffected.
		 *
		 * @param input The new input character sequence
		 * @return This matcher
		 */
		public Matcher reset (CharSequence input) {
			text = input;
			return reset ();
		}
		
		/**
		 * Returns the start index of the previous match.
		 *
		 * @return The index of the first character matched
		 * @throws IllegalStateException If no match has yet been attempted, or if the previous match
		 *                               operation failed
		 */
		public int start () {
			if (first < 0)
				throw new IllegalStateException ("No match available");
			return first;
		}
		
		/**
		 * Returns the start index of the subsequence captured by the given group during
		 * the previous match operation.
		 *
		 * <p>
		 * <a href="Pattern.html#cg">Capturing groups</a> are indexed from left to
		 * right, starting at one. Group zero denotes the entire pattern, so the
		 * expression <i>m.</i><code>start(0)</code> is equivalent to <i>m.</i>
		 * <code>start()</code>.
		 * </p>
		 *
		 * @param group The index of a capturing group in this matcher's pattern
		 * @return The index of the first character captured by the group, or
		 * <code>-1</code> if the match was successful but the group itself did not
		 * match anything
		 * @throws IllegalStateException     If no match has yet been attempted, or if the previous match
		 *                                   operation failed
		 * @throws IndexOutOfBoundsException If there is no capturing group in the pattern with the given
		 *                                   index
		 */
		public int start (int group) {
			if (first < 0)
				throw new IllegalStateException ("No match available");
			if (group < 0 || group > groupCount ())
				throw new IndexOutOfBoundsException ("No group " + group);
			return groups[group * 2];
		}
		
		/**
		 * Returns the start index of the subsequence captured by the given
		 * <a href="Pattern.html#groupname">named-capturing group</a> during the
		 * previous match operation.
		 *
		 * @param name The name of a named-capturing group in this matcher's pattern
		 * @return The index of the first character captured by the group, or {@code -1}
		 * if the match was successful but the group itself did not match
		 * anything
		 * @throws IllegalStateException    If no match has yet been attempted, or if the previous match
		 *                                  operation failed
		 * @throws IllegalArgumentException If there is no capturing group in the pattern with the given name
		 * @since 1.8
		 */
		public int start (String name) {
			return groups[getMatchedGroupIndex (name) * 2];
		}
		
		/**
		 * Returns the offset after the last character matched.
		 *
		 * @return The offset after the last character matched
		 * @throws IllegalStateException If no match has yet been attempted, or if the previous match
		 *                               operation failed
		 */
		public int end () {
			if (first < 0)
				throw new IllegalStateException ("No match available");
			return last;
		}
		
		/**
		 * Returns the offset after the last character of the subsequence captured by
		 * the given group during the previous match operation.
		 *
		 * <p>
		 * <a href="Pattern.html#cg">Capturing groups</a> are indexed from left to
		 * right, starting at one. Group zero denotes the entire pattern, so the
		 * expression <i>m.</i><code>end(0)</code> is equivalent to <i>m.</i>
		 * <code>end()</code>.
		 * </p>
		 *
		 * @param group The index of a capturing group in this matcher's pattern
		 * @return The offset after the last character captured by the group, or
		 * <code>-1</code> if the match was successful but the group itself did not
		 * match anything
		 * @throws IllegalStateException     If no match has yet been attempted, or if the previous match
		 *                                   operation failed
		 * @throws IndexOutOfBoundsException If there is no capturing group in the pattern with the given
		 *                                   index
		 */
		public int end (int group) {
			if (first < 0)
				throw new IllegalStateException ("No match available");
			if (group < 0 || group > groupCount ())
				throw new IndexOutOfBoundsException ("No group " + group);
			return groups[group * 2 + 1];
		}
		
		/**
		 * Returns the offset after the last character of the subsequence captured by
		 * the given <a href="Pattern.html#groupname">named-capturing group</a> during
		 * the previous match operation.
		 *
		 * @param name The name of a named-capturing group in this matcher's pattern
		 * @return The offset after the last character captured by the group, or
		 * {@code -1} if the match was successful but the group itself did not
		 * match anything
		 * @throws IllegalStateException    If no match has yet been attempted, or if the previous match
		 *                                  operation failed
		 * @throws IllegalArgumentException If there is no capturing group in the pattern with the given name
		 * @since 1.8
		 */
		public int end (String name) {
			return groups[getMatchedGroupIndex (name) * 2 + 1];
		}
		
		/**
		 * Returns the input subsequence matched by the previous match.
		 *
		 * <p>
		 * For a matcher <i>m</i> with input sequence <i>s</i>, the expressions
		 * <i>m.</i><code>group()</code> and <i>s.</i><code>substring(</code><i>m.</i>
		 * <code>start(),</code>&nbsp;<i>m.</i><code>end())</code> are equivalent.
		 * </p>
		 *
		 * <p>
		 * Note that some patterns, for example <code>a*</code>, match the empty string.
		 * This method will return the empty string when the pattern successfully
		 * matches the empty string in the input.
		 * </p>
		 *
		 * @return The (possibly empty) subsequence matched by the previous match, in
		 * string form
		 * @throws IllegalStateException If no match has yet been attempted, or if the previous match
		 *                               operation failed
		 */
		public String group () {
			return group (0);
		}
		
		/**
		 * Returns the input subsequence captured by the given group during the previous
		 * match operation.
		 *
		 * <p>
		 * For a matcher <i>m</i>, input sequence <i>s</i>, and group index <i>g</i> ,
		 * the expressions <i>m.</i><code>group(</code><i>g</i><code>)</code> and
		 * <i>s.</i><code>substring(</code><i>m.</i><code>start(</code><i>g</i><code>),</code>
		 * &nbsp;<i>m.</i><code>end(</code><i>g</i><code>))</code> are equivalent.
		 * </p>
		 *
		 * <p>
		 * <a href="Pattern.html#cg">Capturing groups</a> are indexed from left to
		 * right, starting at one. Group zero denotes the entire pattern, so the
		 * expression <code>m.group(0)</code> is equivalent to <code>m.group()</code>.
		 * </p>
		 *
		 * <p>
		 * If the match was successful but the group specified failed to match any part
		 * of the input sequence, then <code>null</code> is returned. Note that some groups,
		 * for example <code>(a*)</code>, match the empty string. This method will return
		 * the empty string when such a group successfully matches the empty string in
		 * the input.
		 * </p>
		 *
		 * @param group The index of a capturing group in this matcher's pattern
		 * @return The (possibly empty) subsequence captured by the group during the
		 * previous match, or <code>null</code> if the group failed to match part of
		 * the input
		 * @throws IllegalStateException     If no match has yet been attempted, or if the previous match
		 *                                   operation failed
		 * @throws IndexOutOfBoundsException If there is no capturing group in the pattern with the given
		 *                                   index
		 */
		public String group (int group) {
			if (first < 0)
				throw new IllegalStateException ("No match found");
			if (group < 0 || group > groupCount ())
				throw new IndexOutOfBoundsException ("No group " + group);
			if ((groups[group * 2] == -1) || (groups[group * 2 + 1] == -1))
				return null;
			return getSubSequence (groups[group * 2], groups[group * 2 + 1]).toString ();
		}
		
		/**
		 * Returns the input subsequence captured by the given
		 * <a href="Pattern.html#groupname">named-capturing group</a> during the
		 * previous match operation.
		 *
		 * <p>
		 * If the match was successful but the group specified failed to match any part
		 * of the input sequence, then <code>null</code> is returned. Note that some groups,
		 * for example <code>(a*)</code>, match the empty string. This method will return
		 * the empty string when such a group successfully matches the empty string in
		 * the input.
		 * </p>
		 *
		 * @param name The name of a named-capturing group in this matcher's pattern
		 * @return The (possibly empty) subsequence captured by the named group during
		 * the previous match, or <code>null</code> if the group failed to match
		 * part of the input
		 * @throws IllegalStateException    If no match has yet been attempted, or if the previous match
		 *                                  operation failed
		 * @throws IllegalArgumentException If there is no capturing group in the pattern with the given name
		 * @since 1.7
		 */
		public String group (String name) {
			int group = getMatchedGroupIndex (name);
			if ((groups[group * 2] == -1) || (groups[group * 2 + 1] == -1))
				return null;
			return getSubSequence (groups[group * 2], groups[group * 2 + 1]).toString ();
		}
		
		/**
		 * Sets this matcher's matching mode
		 *
		 * @param mode The matching mode, a bit mask that may include currently only
		 *             {@link Matcher#CAPTURE_TREE}
		 * @see Matcher#captureTree()
		 */
		public void setMode (int mode) {
			this.mode = mode;
			captureTreeMode = (mode & CAPTURE_TREE) != 0;
		}
		
		/**
		 * Returns this matcher's matching mode.
		 *
		 * @return The matching mode specified with {@link Matcher#setMode(int) }
		 */
		public int getMode () {
			return mode;
		}
		
		/**
		 * Returns the {@link CaptureTree} of the previous match operation.
		 *
		 * <p>
		 * The {@link CaptureTree} contains all captures made during the previous match
		 * operation of all <a href="Pattern.html#cg">capturing groups</a> in a
		 * hierarchical data structure. E.g.
		 * </p>
		 *
		 * <pre>
		 * Matcher matcher = Pattern.compile("(?x)" + "(?(DEFINE)" + "(?&lt;sum&gt; (?'summand')(?:\\+(?'summand'))+ )"
		 * 		+ "(?&lt;summand&gt; (?'product') |  (?'number') )" + "(?&lt;product&gt; (?'factor')(?:\\*(?'factor'))+ )"
		 * 		+ "(?&lt;factor&gt;(?'number') ) " + "(?&lt;number&gt;\\d++)" + ")" + "(?'sum')").matcher("5+6*8");
		 * matcher.setMode(Matcher.CAPTURE_TREE);
		 * matcher.matches();
		 * System.out.println(matcher.captureTree());
		 * </pre>
		 * <p>
		 * prints out
		 *
		 * <pre>
		 * 0
		 * 	sum
		 * 		summand
		 * 			number
		 * 		summand
		 * 			product
		 * 				factor
		 * 					number
		 * 				factor
		 * 					number
		 * </pre>
		 *
		 * @return The {@link CaptureTree} of the previous match operation
		 * @throws IllegalStateException If no match has yet been attempted, or if the previous match
		 *                               operation failed or if the CAPTURE_TREE matching mode hasn't been
		 *                               set wjth {@link Matcher#setMode(int)}
		 * @see CaptureTree
		 * @see Matcher#setMode(int)
		 * @see Matcher#CAPTURE_TREE
		 */
		public CaptureTree captureTree () {
			if (first < 0)
				throw new IllegalStateException ("No match available");
			if (!captureTreeMode)
				throw new IllegalStateException ("Mode CAPTURE_TREE is not set");
			if (captureTree == null) {
				captureTreeNode.setGroupName (parentPattern.groupNames ());
				captureTree = new CaptureTree (captureTreeNode);
			}
			return captureTree;
		}
		
		/**
		 * Returns the number of capturing groups in this matcher's pattern.
		 *
		 * <p>
		 * Group zero denotes the entire pattern by convention. It is not included in
		 * this count.
		 *
		 * <p>
		 * Any non-negative integer smaller than or equal to the value returned by this
		 * method is guaranteed to be a valid group index for this matcher.
		 * </p>
		 *
		 * @return The number of capturing groups in this matcher's pattern
		 */
		public int groupCount () {
			return parentPattern.capturingGroupCount - 1;
		}
		
		/**
		 * Attempts to match the entire region against the pattern.
		 *
		 * <p>
		 * If the match succeeds then more information can be obtained via the
		 * <code>start</code>, <code>end</code>, and <code>group</code> methods.
		 * </p>
		 *
		 * @return <code>true</code> if, and only if, the entire region sequence matches
		 * this matcher's pattern
		 */
		public boolean matches () {
			return match (from, ENDANCHOR);
		}
		
		/**
		 * Attempts to find the next subsequence of the input sequence that matches the
		 * pattern.
		 *
		 * <p>
		 * This method starts at the beginning of this matcher's region, or, if a
		 * previous invocation of the method was successful and the matcher has not
		 * since been reset, at the first character not matched by the previous match.
		 *
		 * <p>
		 * If the match succeeds then more information can be obtained via the
		 * <code>start</code>, <code>end</code>, and <code>group</code> methods.
		 * </p>
		 *
		 * @return <code>true</code> if, and only if, a subsequence of the input sequence
		 * matches this matcher's pattern
		 */
		public boolean find () {
			int nextSearchIndex = last;
			if (nextSearchIndex == first)
				nextSearchIndex++;
			
			// If next search starts before region, start it at region
			if (nextSearchIndex < from)
				nextSearchIndex = from;
			
			// If next search starts beyond region then it fails
			if (nextSearchIndex > to) {
				return false;
			}
			return search (nextSearchIndex);
		}
		
		/**
		 * Resets this matcher and then attempts to find the next subsequence of the
		 * input sequence that matches the pattern, starting at the specified index.
		 *
		 * <p>
		 * If the match succeeds then more information can be obtained via the
		 * <code>start</code>, <code>end</code>, and <code>group</code> methods, and subsequent
		 * invocations of the {@link #find()} method will start at the first character
		 * not matched by this match.
		 * </p>
		 *
		 * @param start the index to start searching for a match
		 * @return <code>true</code> if, and only if, a subsequence of the input sequence
		 * starting at the given index matches this matcher's pattern
		 * @throws IndexOutOfBoundsException If start is less than zero or if start is greater than the length
		 *                                   of the input sequence.
		 */
		public boolean find (int start) {
			int limit = getTextLength ();
			if ((start < 0) || (start > limit))
				throw new IndexOutOfBoundsException ("Illegal start index");
			reset ();
			return search (start);
		}
		
		/**
		 * Attempts to match the input sequence, starting at the beginning of the
		 * region, against the pattern.
		 *
		 * <p>
		 * Like the {@link #matches matches} method, this method always starts at the
		 * beginning of the region; unlike that method, it does not require that the
		 * entire region be matched.
		 *
		 * <p>
		 * If the match succeeds then more information can be obtained via the
		 * <code>start</code>, <code>end</code>, and <code>group</code> methods.
		 * </p>
		 *
		 * @return <code>true</code> if, and only if, a prefix of the input sequence matches
		 * this matcher's pattern
		 */
		public boolean lookingAt () {
			return match (from, NOANCHOR);
		}
		
		/**
		 * Returns a literal replacement <code>String</code> for the specified
		 * <code>String</code>.
		 * <p>
		 * This method produces a <code>String</code> that will work as a literal
		 * replacement <code>s</code> in the <code>appendReplacement</code> method of
		 * the {@link Matcher} class. The <code>String</code> produced will match the
		 * sequence of characters in <code>s</code> treated as a literal sequence.
		 * Slashes ('\') and dollar signs ('$') will be given no special meaning.
		 *
		 * @param s The string to be literalized
		 * @return A literal string replacement
		 * @since 1.5
		 */
		public static String quoteReplacement (String s) {
			if ((s.indexOf ('\\') == -1) && (s.indexOf ('$') == -1))
				return s;
			StringBuilder sb = new StringBuilder ();
			for (int i = 0; i < s.length (); i++) {
				char c = s.charAt (i);
				if (c == '\\' || c == '$') {
					sb.append ('\\');
				}
				sb.append (c);
			}
			return sb.toString ();
		}
		
		/**
		 * Implements a non-terminal append-and-replace step.
		 *
		 * <p>
		 * This method performs the following actions:
		 * </p>
		 *
		 * <ol>
		 *
		 * <li>
		 * <p>
		 * It reads characters from the input sequence, starting at the append position,
		 * and appends them to the given string buffer. It stops after reading the last
		 * character preceding the previous match, that is, the character at index
		 * {@link #start()}&nbsp;<code>-</code>&nbsp;<code>1</code>.
		 * </p>
		 * </li>
		 *
		 * <li>
		 * <p>
		 * It appends the given replacement string to the string buffer.
		 * </p>
		 * </li>
		 *
		 * <li>
		 * <p>
		 * It sets the append position of this matcher to the index of the last
		 * character matched, plus one, that is, to {@link #end()}.
		 * </p>
		 * </li>
		 *
		 * </ol>
		 *
		 * <p>
		 * The replacement string may contain references to subsequences captured during
		 * the previous match: Each occurrence of <code>${</code><i>name</i> <code>}</code> or
		 * <code>$</code><i>g</i> will be replaced by the result of evaluating the
		 * corresponding {@link #group(String) group(name)} or {@link #group(int)
		 * group(g)} respectively. For <code>$</code><i>g</i>, the first number after the
		 * <code>$</code> is always treated as part of the group reference. Subsequent
		 * numbers are incorporated into g if they would form a legal group reference.
		 * Only the numerals '0' through '9' are considered as potential components of
		 * the group reference. If the second group matched the string <code>"foo"</code>,
		 * for example, then passing the replacement string <code>"$2bar"</code> would cause
		 * <code>"foobar"</code> to be appended to the string buffer. A dollar sign
		 * (<code>$</code>) may be included as a literal in the replacement string by
		 * preceding it with a backslash ( <code>\$</code>).
		 *
		 * <p>
		 * Note that backslashes (<code>\</code>) and dollar signs (<code>$</code>) in the
		 * replacement string may cause the results to be different than if it were
		 * being treated as a literal replacement string. Dollar signs may be treated as
		 * references to captured subsequences as described above, and backslashes are
		 * used to escape literal characters in the replacement string.
		 *
		 * <p>
		 * This method is intended to be used in a loop together with the
		 * {@link #appendTail appendTail} and {@link #find find} methods. The following
		 * code, for example, writes <code>one dog two dogs in the
		 * yard</code> to the standard-output stream:
		 * </p>
		 *
		 * <blockquote>
		 *
		 * <pre>
		 * Pattern p = Pattern.compile("cat");
		 * Matcher m = p.matcher("one cat two cats in the yard");
		 * StringBuffer sb = new StringBuffer();
		 * while (m.find()) {
		 * 	m.appendReplacement(sb, "dog");
		 * }
		 * m.appendTail(sb);
		 * System.out.println(sb.toString());
		 * </pre>
		 *
		 * </blockquote>
		 *
		 * @param sb          The target string buffer
		 * @param replacement The replacement string
		 * @return This matcher
		 * @throws IllegalStateException     If no match has yet been attempted, or if the previous match
		 *                                   operation failed
		 * @throws IllegalArgumentException  If the replacement string refers to a named-capturing group that
		 *                                   does not exist in the pattern
		 * @throws IndexOutOfBoundsException If the replacement string refers to a capturing group that does
		 *                                   not exist in the pattern
		 */
		public Matcher appendReplacement (StringBuffer sb, String replacement) {
			// If no match, return error
			if (first < 0)
				throw new IllegalStateException ("No match available");
			
			// Process substitution string to replace group references with groups
			int cursor = 0;
			StringBuilder result = new StringBuilder ();
			
			while (cursor < replacement.length ()) {
				char nextChar = replacement.charAt (cursor);
				if (nextChar == '\\') {
					cursor++;
					if (cursor == replacement.length ())
						throw new IllegalArgumentException ("character to be escaped is missing");
					nextChar = replacement.charAt (cursor);
					result.append (nextChar);
					cursor++;
				} else if (nextChar == '$') {
					// Skip past $
					cursor++;
					// Throw IAE if this "$" is the last character in replacement
					if (cursor == replacement.length ())
						throw new IllegalArgumentException ("Illegal group reference: group index is missing");
					nextChar = replacement.charAt (cursor);
					int refNum = -1;
					if (nextChar == '{') {
						cursor++;
						StringBuilder gsb = new StringBuilder ();
						while (cursor < replacement.length ()) {
							nextChar = replacement.charAt (cursor);
							if (Pattern.isCharOfGroupname (nextChar)) {
								gsb.append (nextChar);
								cursor++;
							} else {
								break;
							}
						}
						if (gsb.length () == 0)
							throw new IllegalArgumentException ("named capturing group has 0 length name");
						if (nextChar != '}')
							throw new IllegalArgumentException ("named capturing group is missing trailing '}'");
						String gname = gsb.toString ();
						if (ASCII.isDigit (gname.charAt (0)))
							throw new IllegalArgumentException (
								"capturing group name {" + gname + "} starts with digit character");
						if (!parentPattern.groupIndices ().containsKey (gname))
							throw new IllegalArgumentException ("No group with name {" + gname + "}");
						refNum = parentPattern.groupIndices ().get (gname);
						cursor++;
					} else {
						// The first number is always a group
						refNum = (int) nextChar - '0';
						if ((refNum < 0) || (refNum > 9))
							throw new IllegalArgumentException ("Illegal group reference");
						cursor++;
						// Capture the largest legal group string
						boolean done = false;
						while (!done) {
							if (cursor >= replacement.length ()) {
								break;
							}
							int nextDigit = replacement.charAt (cursor) - '0';
							if ((nextDigit < 0) || (nextDigit > 9)) { // not a
								// number
								break;
							}
							int newRefNum = (refNum * 10) + nextDigit;
							if (groupCount () < newRefNum) {
								done = true;
							} else {
								refNum = newRefNum;
								cursor++;
							}
						}
					}
					// Append group
					if (start (refNum) != -1 && end (refNum) != -1)
						result.append (text, start (refNum), end (refNum));
				} else {
					result.append (nextChar);
					cursor++;
				}
			}
			// Append the intervening text
			sb.append (text, lastAppendPosition, first);
			// Append the match substitution
			sb.append (result);
			
			lastAppendPosition = last;
			return this;
		}
		
		public Matcher appendReplacement (StringBuffer sb, Function<Matcher, String> evaluator) {
			// If no match, return error
			if (first < 0)
				throw new IllegalStateException ("No match available");
			
			sb.append (text, lastAppendPosition, first);
			sb.append (evaluator.apply (this));
			
			lastAppendPosition = last;
			return this;
		}
		
		/**
		 * Implements a non-terminal append-and-replace step.
		 *
		 * @throws IllegalStateException If no match has yet been attempted, or if the previous match
		 *                               operation failed or if the CAPTURE_TREE matching mode hasn't been
		 *                               set with {@link Matcher#setMode(int)}
		 * @see Matcher#replaceAll(CaptureReplacer)
		 */
		public Matcher appendReplacement (StringBuffer sb, CaptureReplacer replacer) {
			// If no match, return error
			if (first < 0)
				throw new IllegalStateException ("No match available");
			if (!captureTreeMode)
				throw new IllegalStateException ("Mode CAPTURE_TREE is not set");
			
			sb.append (text, lastAppendPosition, first);
			replacer.setInput (text);
			sb.append (replacer.replace (captureTree ().getRoot ()));
			
			lastAppendPosition = last;
			return this;
		}
		
		/**
		 * Implements a terminal append-and-replace step.
		 *
		 * <p>
		 * This method reads characters from the input sequence, starting at the append
		 * position, and appends them to the given string buffer. It is intended to be
		 * invoked after one or more invocations of the {@link #appendReplacement
		 * appendReplacement} method in order to copy the remainder of the input
		 * sequence.
		 * </p>
		 *
		 * @param sb The target string buffer
		 * @return The target string buffer
		 */
		public StringBuffer appendTail (StringBuffer sb) {
			sb.append (text, lastAppendPosition, getTextLength ());
			return sb;
		}
		
		/**
		 * Replaces every subsequence of the input sequence that matches the pattern
		 * with the given replacement string.
		 *
		 * <p>
		 * This method first resets this matcher. It then scans the input sequence
		 * looking for matches of the pattern. Characters that are not part of any match
		 * are appended directly to the result string; each match is replaced in the
		 * result by the replacement string. The replacement string may contain
		 * references to captured subsequences as in the {@link #appendReplacement
		 * appendReplacement} method.
		 *
		 * <p>
		 * Note that backslashes (<code>\</code>) and dollar signs (<code>$</code>) in the
		 * replacement string may cause the results to be different than if it were
		 * being treated as a literal replacement string. Dollar signs may be treated as
		 * references to captured subsequences as described above, and backslashes are
		 * used to escape literal characters in the replacement string.
		 *
		 * <p>
		 * Given the regular expression <code>a*b</code>, the input
		 * <code>"aabfooaabfooabfoob"</code>, and the replacement string <code>"-"</code>, an
		 * invocation of this method on a matcher for that expression would yield the
		 * string <code>"-foo-foo-foo-"</code>.
		 *
		 * <p>
		 * Invoking this method changes this matcher's state. If the matcher is to be
		 * used in further matching operations then it should first be reset.
		 * </p>
		 *
		 * @param replacement The replacement string
		 * @return The string constructed by replacing each matching subsequence by the
		 * replacement string, substituting captured subsequences as needed
		 */
		public String replaceAll (String replacement) {
			reset ();
			boolean result = find ();
			if (result) {
				StringBuffer sb = new StringBuffer ();
				do {
					appendReplacement (sb, replacement);
					result = find ();
				} while (result);
				appendTail (sb);
				return sb.toString ();
			}
			return text.toString ();
		}
		
		/**
		 * Replaces every subsequence of the input sequence that matches the pattern
		 * with the replacement string computed with the given Match Evaluator.
		 *
		 * @param evaluator The Match Evaluator to be used to compute the replacement string
		 * @see Matcher#replaceAll(String)
		 */
		public String replaceAll (Function<Matcher, String> evaluator) {
			reset ();
			boolean result = find ();
			if (result) {
				StringBuffer sb = new StringBuffer ();
				do {
					appendReplacement (sb, evaluator);
					result = find ();
				} while (result);
				appendTail (sb);
				return sb.toString ();
			}
			return text.toString ();
		}
		
		/**
		 * Replaces every subsequence of the input sequence that matches the pattern
		 * with the replacement string computed with the given {@link CaptureReplacer}.
		 *
		 * <p>
		 * E.g.
		 * </p>
		 *
		 * <pre>
		 * Pattern pattern = Pattern.compile("(?x)" + "(?(DEFINE)" + "(?&lt;sum&gt; (?'summand')(?:\\+(?'summand'))+ )"
		 * 		+ "(?&lt;summand&gt;  (?'product') | (?'number') )" + "(?&lt;product&gt; (?'factor')(?:\\*(?'factor'))+ )"
		 * 		+ "(?&lt;factor&gt;(?'number') )" + "(?&lt;number&gt;\\d++)" + ")" + "(?'sum')");
		 * Matcher matcher = pattern.matcher("First: 6+7*8 Second: 6*8+7");
		 * String replacement = matcher.replaceAll(new DefaultCaptureReplacer() {
		 * 	&#64;Override
		 * 	public String replace(CaptureTreeNode node) {
		 * 		if ("sum".equals(node.getGroupName())) {
		 * 			return "sum(" + node.getChildren().stream().filter(n -&gt; "summand".equals(n.getGroupName()))
		 * 					.map(n -&gt; replace(n)).collect(Collectors.joining(",")) + ")";
		 *    } else if ("product".equals(node.getGroupName())) {
		 * 			return "product(" + node.getChildren().stream().filter(n -&gt; "factor".equals(n.getGroupName()))
		 * 					.map(n -&gt; replace(n)).collect(Collectors.joining(",")) + ")";
		 *    } else
		 * 			return super.replace(node);
		 *  }
		 * });
		 * System.out.println(replacement);
		 * </pre>
		 * <p>
		 * prints out
		 *
		 * <pre>
		 * First: sum(6,product(7,8)) Second: sum(product(6,8),7)
		 * </pre>
		 *
		 * @param replacer The {@link CaptureReplacer} to be used to compute the replacement
		 *                 string
		 * @see Matcher#replaceAll(String)
		 * @see DefaultCaptureReplacer
		 */
		public String replaceAll (CaptureReplacer replacer) {
			int saveMode = getMode ();
			try {
				setMode (saveMode | CAPTURE_TREE);
				reset ();
				boolean result = find ();
				if (result) {
					StringBuffer sb = new StringBuffer ();
					do {
						appendReplacement (sb, replacer);
						result = find ();
					} while (result);
					appendTail (sb);
					return sb.toString ();
				}
				return text.toString ();
			} finally {
				setMode (saveMode);
			}
		}
		
		/**
		 * Replaces the first subsequence of the input sequence that matches the pattern
		 * with the given replacement string.
		 *
		 * <p>
		 * This method first resets this matcher. It then scans the input sequence
		 * looking for a match of the pattern. Characters that are not part of the match
		 * are appended directly to the result string; the match is replaced in the
		 * result by the replacement string. The replacement string may contain
		 * references to captured subsequences as in the {@link #appendReplacement
		 * appendReplacement} method.
		 *
		 * <p>
		 * Note that backslashes (<code>\</code>) and dollar signs (<code>$</code>) in the
		 * replacement string may cause the results to be different than if it were
		 * being treated as a literal replacement string. Dollar signs may be treated as
		 * references to captured subsequences as described above, and backslashes are
		 * used to escape literal characters in the replacement string.
		 *
		 * <p>
		 * Given the regular expression <code>dog</code>, the input
		 * <code>"zzzdogzzzdogzzz"</code>, and the replacement string <code>"cat"</code>, an
		 * invocation of this method on a matcher for that expression would yield the
		 * string <code>"zzzcatzzzdogzzz"</code>.
		 * </p>
		 *
		 * <p>
		 * Invoking this method changes this matcher's state. If the matcher is to be
		 * used in further matching operations then it should first be reset.
		 * </p>
		 *
		 * @param replacement The replacement string
		 * @return The string constructed by replacing the first matching subsequence by
		 * the replacement string, substituting captured subsequences as needed
		 */
		public String replaceFirst (String replacement) {
			if (replacement == null)
				throw new NullPointerException ("replacement");
			reset ();
			if (!find ())
				return text.toString ();
			StringBuffer sb = new StringBuffer ();
			appendReplacement (sb, replacement);
			appendTail (sb);
			return sb.toString ();
		}
		
		public String replaceFirst (Function<Matcher, String> evaluator) {
			if (evaluator == null)
				throw new NullPointerException ("evaluator");
			reset ();
			if (!find ())
				return text.toString ();
			StringBuffer sb = new StringBuffer ();
			appendReplacement (sb, evaluator);
			appendTail (sb);
			return sb.toString ();
		}
		
		/**
		 * Replaces the first subsequence of the input sequence that matches the pattern
		 * with the replacement string computed with the given {@link CaptureReplacer}.
		 *
		 * @param replacer The {@link CaptureReplacer} to be used to compute the replacement
		 *                 string
		 * @see Matcher#replaceAll(CaptureReplacer)
		 */
		public String replaceFirst (CaptureReplacer replacer) {
			if (replacer == null)
				throw new NullPointerException ("replacer");
			int saveMode = getMode ();
			try {
				setMode (saveMode | CAPTURE_TREE);
				reset ();
				if (!find ())
					return text.toString ();
				StringBuffer sb = new StringBuffer ();
				appendReplacement (sb, replacer);
				appendTail (sb);
				return sb.toString ();
			} finally {
				setMode (saveMode);
			}
		}
		
		/**
		 * Sets the limits of this matcher's region. The region is the part of the input
		 * sequence that will be searched to find a match. Invoking this method resets
		 * the matcher, and then sets the region to start at the index specified by the
		 * <code>start</code> parameter and end at the index specified by the
		 * <code>end</code> parameter.
		 *
		 * <p>
		 * Depending on the transparency and anchoring being used (see
		 * {@link #useTransparentBounds useTransparentBounds} and
		 * {@link #useAnchoringBounds useAnchoringBounds}), certain constructs such as
		 * anchors may behave differently at or around the boundaries of the region.
		 *
		 * @param start The index to start searching at (inclusive)
		 * @param end   The index to end searching at (exclusive)
		 * @return this matcher
		 * @throws IndexOutOfBoundsException If start or end is less than zero, if start is greater than the
		 *                                   length of the input sequence, if end is greater than the length
		 *                                   of the input sequence, or if start is greater than end.
		 * @since 1.5
		 */
		public Matcher region (int start, int end) {
			if ((start < 0) || (start > getTextLength ()))
				throw new IndexOutOfBoundsException ("start");
			if ((end < 0) || (end > getTextLength ()))
				throw new IndexOutOfBoundsException ("end");
			if (start > end)
				throw new IndexOutOfBoundsException ("start > end");
			reset ();
			from = start;
			to = end;
			return this;
		}
		
		/**
		 * Reports the start index of this matcher's region. The searches this matcher
		 * conducts are limited to finding matches within {@link #regionStart
		 * regionStart} (inclusive) and {@link #regionEnd regionEnd} (exclusive).
		 *
		 * @return The starting point of this matcher's region
		 * @since 1.5
		 */
		public int regionStart () {
			return from;
		}
		
		/**
		 * Reports the end index (exclusive) of this matcher's region. The searches this
		 * matcher conducts are limited to finding matches within {@link #regionStart
		 * regionStart} (inclusive) and {@link #regionEnd regionEnd} (exclusive).
		 *
		 * @return the ending point of this matcher's region
		 * @since 1.5
		 */
		public int regionEnd () {
			return to;
		}
		
		/**
		 * Queries the transparency of region bounds for this matcher.
		 *
		 * <p>
		 * This method returns <code>true</code> if this matcher uses <i>transparent</i>
		 * bounds, <code>false</code> if it uses <i>opaque</i> bounds.
		 *
		 * <p>
		 * See {@link #useTransparentBounds useTransparentBounds} for a description of
		 * transparent and opaque bounds.
		 *
		 * <p>
		 * By default, a matcher uses opaque region boundaries.
		 *
		 * @return <code>true</code> iff this matcher is using transparent bounds,
		 * <code>false</code> otherwise.
		 * @see java.util.regex.Matcher#useTransparentBounds(boolean)
		 * @since 1.5
		 */
		public boolean hasTransparentBounds () {
			return transparentBounds;
		}
		
		/**
		 * Sets the transparency of region bounds for this matcher.
		 *
		 * <p>
		 * Invoking this method with an argument of <code>true</code> will set this matcher
		 * to use <i>transparent</i> bounds. If the boolean argument is <code>false</code>,
		 * then <i>opaque</i> bounds will be used.
		 *
		 * <p>
		 * Using transparent bounds, the boundaries of this matcher's region are
		 * transparent to lookahead, lookbehind, and boundary matching constructs. Those
		 * constructs can see beyond the boundaries of the region to see if a match is
		 * appropriate.
		 *
		 * <p>
		 * Using opaque bounds, the boundaries of this matcher's region are opaque to
		 * lookahead, lookbehind, and boundary matching constructs that may try to see
		 * beyond them. Those constructs cannot look past the boundaries so they will
		 * fail to match anything outside of the region.
		 *
		 * <p>
		 * By default, a matcher uses opaque bounds.
		 *
		 * @param b a boolean indicating whether to use opaque or transparent regions
		 * @return this matcher
		 * @see java.util.regex.Matcher#hasTransparentBounds
		 * @since 1.5
		 */
		public Matcher useTransparentBounds (boolean b) {
			transparentBounds = b;
			return this;
		}
		
		/**
		 * Queries the anchoring of region bounds for this matcher.
		 *
		 * <p>
		 * This method returns <code>true</code> if this matcher uses <i>anchoring</i>
		 * bounds, <code>false</code> otherwise.
		 *
		 * <p>
		 * See {@link #useAnchoringBounds useAnchoringBounds} for a description of
		 * anchoring bounds.
		 *
		 * <p>
		 * By default, a matcher uses anchoring region boundaries.
		 *
		 * @return <code>true</code> iff this matcher is using anchoring bounds,
		 * <code>false</code> otherwise.
		 * @see java.util.regex.Matcher#useAnchoringBounds(boolean)
		 * @since 1.5
		 */
		public boolean hasAnchoringBounds () {
			return anchoringBounds;
		}
		
		/**
		 * Sets the anchoring of region bounds for this matcher.
		 *
		 * <p>
		 * Invoking this method with an argument of <code>true</code> will set this matcher
		 * to use <i>anchoring</i> bounds. If the boolean argument is <code>false</code>,
		 * then <i>non-anchoring</i> bounds will be used.
		 *
		 * <p>
		 * Using anchoring bounds, the boundaries of this matcher's region match anchors
		 * such as ^ and $.
		 *
		 * <p>
		 * Without anchoring bounds, the boundaries of this matcher's region will not
		 * match anchors such as ^ and $.
		 *
		 * <p>
		 * By default, a matcher uses anchoring region boundaries.
		 *
		 * @param b a boolean indicating whether or not to use anchoring bounds.
		 * @return this matcher
		 * @see java.util.regex.Matcher#hasAnchoringBounds
		 * @since 1.5
		 */
		public Matcher useAnchoringBounds (boolean b) {
			anchoringBounds = b;
			return this;
		}
		
		/**
		 * <p>
		 * Returns the string representation of this matcher. The string representation
		 * of a <code>Matcher</code> contains information that may be useful for
		 * debugging. The exact format is unspecified.
		 *
		 * @return The string representation of this matcher
		 * @since 1.5
		 */
		@Override
		public String toString () {
			StringBuilder sb = new StringBuilder ();
			sb.append ("Matcher");
			sb.append ("[pattern=" + pattern ());
			sb.append (" region=");
			sb.append (regionStart () + "," + regionEnd ());
			sb.append (" lastmatch=");
			if ((first >= 0) && (group () != null)) {
				sb.append (group ());
			}
			sb.append ("]");
			return sb.toString ();
		}
		
		/**
		 * <p>
		 * Returns true if the end of input was hit by the search engine in the last
		 * match operation performed by this matcher.
		 *
		 * <p>
		 * When this method returns true, then it is possible that more input would have
		 * changed the result of the last search.
		 *
		 * @return true iff the end of input was hit in the last match; false otherwise
		 * @since 1.5
		 */
		public boolean hitEnd () {
			return hitEnd;
		}
		
		/**
		 * <p>
		 * Returns true if more input could change a positive match into a negative one.
		 *
		 * <p>
		 * If this method returns true, and a match was found, then more input could
		 * cause the match to be lost. If this method returns false and a match was
		 * found, then more input might change the match but the match won't be lost. If
		 * a match was not found, then requireEnd has no meaning.
		 *
		 * @return true iff more input could change a positive match into a negative
		 * one.
		 * @since 1.5
		 */
		public boolean requireEnd () {
			return requireEnd;
		}
		
		/**
		 * Initiates a search to find a Pattern within the given bounds. The groups are
		 * filled with default values and the match of the root of the state machine is
		 * called. The state machine will hold the state of the match as it proceeds in
		 * this matcher.
		 * <p>
		 * Matcher.from is not set here, because it is the "hard" boundary of the start
		 * of the search which anchors will set to. The from param is the "soft"
		 * boundary of the start of the search, meaning that the regex tries to match at
		 * that index but ^ won't match there. Subsequent calls to the search methods
		 * start at a new "soft" boundary which is the end of the previous match.
		 */
		boolean search (int from) {
			this.hitEnd = false;
			this.requireEnd = false;
			from = Math.max (0, from);
			this.first = from;
			this.oldLast = oldLast < 0 ? from : oldLast;
			genData ();
			captureTree = null;
			captureTreeNode = captureTreeMode ? new CaptureTreeNode () : null;
			Arrays.fill (groups, -1);
			resetRecursions ();
			activity = 0;
			acceptMode = NOANCHOR;
			boolean result = parentPattern.root.match (this, from, text);
			if (!result)
				this.first = -1;
			this.oldLast = this.last;
			return result;
		}
		
		/**
		 * Initiates a search for an anchored match to a Pattern within the given
		 * bounds. The groups are filled with default values and the match of the root
		 * of the state machine is called. The state machine will hold the state of the
		 * match as it proceeds in this matcher.
		 */
		boolean match (int from, int anchor) {
			this.hitEnd = false;
			this.requireEnd = false;
			from = Math.max (0, from);
			this.first = from;
			this.oldLast = oldLast < 0 ? from : oldLast;
			genData ();
			captureTree = null;
			Arrays.fill (groups, -1);
			resetRecursions ();
			activity = 0;
			captureTreeNode = captureTreeMode ? new CaptureTreeNode () : null;
			acceptMode = anchor;
			boolean result = parentPattern.matchRoot.match (this, from, text);
			if (!result)
				this.first = -1;
			this.oldLast = this.last;
			return result;
		}
		
		/**
		 * Returns the end index of the text.
		 *
		 * @return the index after the last character in the text
		 */
		public int getTextLength () {
			return text.length ();
		}
		
		/**
		 * Generates a String from this Matcher's input in the specified range.
		 *
		 * @param beginIndex the beginning index, inclusive
		 * @param endIndex   the ending index, exclusive
		 * @return A String generated from this Matcher's input
		 */
		CharSequence getSubSequence (int beginIndex, int endIndex) {
			return text.subSequence (beginIndex, endIndex);
		}
		
		/**
		 * Returns this Matcher's input character at index i.
		 *
		 * @return A char from the specified index
		 */
		char charAt (int i) {
			return text.charAt (i);
		}
		
		/**
		 * Returns the group index of the matched capturing group.
		 *
		 * @return the index of the named-capturing group
		 */
		int getMatchedGroupIndex (String name) {
			
			Objects.requireNonNull (name, "Group name");
			
			if (first < 0)
				throw new IllegalStateException ("No match found");
			
			if (!parentPattern.groupIndices ().containsKey (name))
				throw new IllegalArgumentException ("No group with name <" + name + ">");
			
			return parentPattern.groupIndices ().get (name);
			
		}
		
		public void setGroup0 (CharSequence seq, int start, int end) {
			
			if (captureTreeMode)
				captureTreeNode.capture = new Capture (seq, start, end);
			
			groups[0] = start;
			groups[1] = end;
			
		}
		
	}