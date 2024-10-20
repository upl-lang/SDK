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
	
	import com.florianingerl.util.regex.nodes.Behind;
	import com.florianingerl.util.regex.nodes.BehindBase;
	import com.florianingerl.util.regex.nodes.BehindS;
	import com.florianingerl.util.regex.nodes.BnM;
	import com.florianingerl.util.regex.nodes.Bound;
	import com.florianingerl.util.regex.nodes.Branch;
	import com.florianingerl.util.regex.nodes.BranchConn;
	import com.florianingerl.util.regex.nodes.CIBackRef;
	import com.florianingerl.util.regex.nodes.Conditional;
	import com.florianingerl.util.regex.nodes.ConditionalGP;
	import com.florianingerl.util.regex.nodes.ConditionalLookahead;
	import com.florianingerl.util.regex.nodes.Curly;
	import com.florianingerl.util.regex.nodes.CurlyBase;
	import com.florianingerl.util.regex.nodes.DeterministicCurly;
	import com.florianingerl.util.regex.nodes.GroupHead;
	import com.florianingerl.util.regex.nodes.GroupTail;
	import com.florianingerl.util.regex.nodes.Neg;
	import com.florianingerl.util.regex.nodes.NotBehind;
	import com.florianingerl.util.regex.nodes.NotBehindS;
	import com.florianingerl.util.regex.nodes.Pos;
	import com.florianingerl.util.regex.nodes.RecursiveGroupCall;
	import com.florianingerl.util.regex.nodes.rules.All;
	import com.florianingerl.util.regex.nodes.AtomicGroup;
	import com.florianingerl.util.regex.nodes.BackRefBase;
	import com.florianingerl.util.regex.nodes.Begin;
	import com.florianingerl.util.regex.nodes.BitClass;
	import com.florianingerl.util.regex.nodes.Block;
	import com.florianingerl.util.regex.nodes.Caret;
	import com.florianingerl.util.regex.nodes.Category;
	import com.florianingerl.util.regex.nodes.CharProperty;
	import com.florianingerl.util.regex.nodes.Ctype;
	import com.florianingerl.util.regex.nodes.CustomNode;
	import com.florianingerl.util.regex.nodes.Dollar;
	import com.florianingerl.util.regex.nodes.End;
	import com.florianingerl.util.regex.nodes.HorizWS;
	import com.florianingerl.util.regex.nodes.LastMatch;
	import com.florianingerl.util.regex.nodes.LastNode;
	import com.florianingerl.util.regex.nodes.LineEnding;
	import com.florianingerl.util.regex.nodes.Navigator;
	import com.florianingerl.util.regex.nodes.Script;
	import com.florianingerl.util.regex.nodes.Single;
	import com.florianingerl.util.regex.nodes.SingleI;
	import com.florianingerl.util.regex.nodes.SingleS;
	import com.florianingerl.util.regex.nodes.SingleU;
	import com.florianingerl.util.regex.nodes.Slice;
	import com.florianingerl.util.regex.nodes.SliceI;
	import com.florianingerl.util.regex.nodes.SliceIS;
	import com.florianingerl.util.regex.nodes.SliceS;
	import com.florianingerl.util.regex.nodes.SliceU;
	import com.florianingerl.util.regex.nodes.SliceUS;
	import com.florianingerl.util.regex.nodes.Start;
	import com.florianingerl.util.regex.nodes.StartS;
	import com.florianingerl.util.regex.nodes.UnixCaret;
	import com.florianingerl.util.regex.nodes.UnixDollar;
	import com.florianingerl.util.regex.nodes.Utype;
	import com.florianingerl.util.regex.nodes.VertWS;
	import com.florianingerl.util.regex.nodes.rules.Dot;
	import com.florianingerl.util.regex.nodes.rules.UnixDot;
	import java.io.IOException;
	import java.lang.reflect.Constructor;
	import java.lang.reflect.InvocationTargetException;
	import java.text.Normalizer;
	import java.util.ArrayList;
	import java.util.Arrays;
	import java.util.HashMap;
	import java.util.HashSet;
	import java.util.Iterator;
	import java.util.LinkedList;
	import java.util.List;
	import java.util.Locale;
	import java.util.Map;
	import java.util.NoSuchElementException;
	import java.util.Set;
	import java.util.Spliterator;
	import java.util.Spliterators;
	import java.util.function.Predicate;
	import java.util.stream.Stream;
	import java.util.stream.StreamSupport;
	
	/**
	 * A compiled representation of a regular expression.
	 *
	 * <p>
	 * A regular expression, specified as a string, must first be compiled into an
	 * instance of this class. The resulting pattern can then be used to create a
	 * {@link Matcher} object that can match arbitrary
	 * {@linkplain java.lang.CharSequence character sequences} against the regular
	 * expression. All of the state involved in performing a match resides in the
	 * matcher, so many matchers can share the same pattern.
	 *
	 * <p>
	 * A typical invocation sequence is thus
	 *
	 * <blockquote>
	 *
	 * <pre>
	 * Pattern p = Pattern.{@link #compile compile}("a*b");
	 * Matcher m = p.{@link #matcher matcher}("aaaaab");
	 * boolean b = m.{@link Matcher#matches matches}();
	 * </pre>
	 *
	 * </blockquote>
	 *
	 * <p>
	 * A {@link #matches matches} method is defined by this class as a convenience
	 * for when a regular expression is used just once. This method compiles an
	 * expression and matches an input sequence against it in a single invocation.
	 * The statement
	 *
	 * <blockquote>
	 *
	 * <pre>
	 * boolean b = Pattern.{@link #matches matches}("a*b", "aaaaab");
	 * </pre>
	 *
	 * </blockquote>
	 * <p>
	 * is equivalent to the three statements above, though for repeated matches it
	 * is less efficient since it does not allow the compiled pattern to be reused.
	 *
	 * <p>
	 * Instances of this class are immutable and are safe for use by multiple
	 * concurrent threads. Instances of the {@link Matcher} class are not safe for
	 * such use.
	 *
	 *
	 * <h2><a>Summary of regular-expression constructs</a></h2>
	 *
	 * <table  style="padding: 1px" >
	 * <caption></caption>
	 *
	 * <tr style="text-align: left">
	 * <th style="text-align: left" id="construct">Construct</th>
	 * <th style="text-align: left" id="matches">Matches</th>
	 * </tr>
	 *
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2" id="characters">Characters</th>
	 * </tr>
	 *
	 * <tr>
	 * <td style="vertical-align: top" headers="construct characters"><i>x</i></td>
	 * <td headers="matches">The character <i>x</i></td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct characters"><code>\\</code></td>
	 * <td headers="matches">The backslash character</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct characters"><code>\0</code><i>n</i></td>
	 * <td headers="matches">The character with octal value <code>0</code><i>n</i>
	 * (0&nbsp;<code>&lt;=</code>&nbsp;<i>n</i>&nbsp;<code>&lt;=</code>&nbsp;7)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct characters"><code>\0</code><i>nn</i></td>
	 * <td headers="matches">The character with octal value <code>0</code><i>nn</i>
	 * (0&nbsp;<code>&lt;=</code>&nbsp;<i>n</i>&nbsp;<code>&lt;=</code>&nbsp;7)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct characters"><code>\0</code><i>mnn</i></td>
	 * <td headers="matches">The character with octal value <code>0</code><i>mnn</i>
	 * (0&nbsp;<code>&lt;=</code>&nbsp;<i>m</i>&nbsp;<code>&lt;=</code>&nbsp;3, 0&nbsp;
	 * <code>&lt;=</code>&nbsp;<i>n</i>&nbsp;<code>&lt;=</code>&nbsp;7)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct characters"><code>\x</code><i>hh</i></td>
	 * <td headers="matches">The character with hexadecimal&nbsp;value&nbsp;
	 * <code>0x</code><i>hh</i></td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct characters"><code>&#92;u</code><i>hhhh</i>
	 * </td>
	 * <td headers="matches">The character with hexadecimal&nbsp;value&nbsp;
	 * <code>0x</code><i>hhhh</i></td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct characters"><code>&#92;x</code><i>{h...h}</i>
	 * </td>
	 * <td headers="matches">The character with hexadecimal&nbsp;value&nbsp;
	 * <code>0x</code><i>h...h</i> ({@link java.lang.Character#MIN_CODE_POINT
	 * Character.MIN_CODE_POINT} &nbsp;&lt;=&nbsp;<code>0x</code><i>h...h</i>
	 * &nbsp;&lt;=&nbsp; {@link java.lang.Character#MAX_CODE_POINT
	 * Character.MAX_CODE_POINT})</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="matches"><code>\t</code></td>
	 * <td headers="matches">The tab character (<code>'&#92;u0009'</code>)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct characters"><code>\n</code></td>
	 * <td headers="matches">The newline (line feed) character (
	 * <code>'&#92;u000A'</code>)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct characters"><code>\r</code></td>
	 * <td headers="matches">The carriage-return character (<code>'&#92;u000D'</code>)
	 * </td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct characters"><code>\f</code></td>
	 * <td headers="matches">The form-feed character (<code>'&#92;u000C'</code>)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct characters"><code>\a</code></td>
	 * <td headers="matches">The alert (bell) character (<code>'&#92;u0007'</code>)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct characters"><code>\e</code></td>
	 * <td headers="matches">The escape character (<code>'&#92;u001B'</code>)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct characters"><code>\c</code><i>x</i></td>
	 * <td headers="matches">The control character corresponding to <i>x</i></td>
	 * </tr>
	 *
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2" id="classes">Character classes</th>
	 * </tr>
	 *
	 * <tr>
	 * <td style="vertical-align: top" headers="construct classes">{@code [abc]}</td>
	 * <td headers="matches">{@code a}, {@code b}, or {@code c} (simple class)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct classes">{@code [^abc]}</td>
	 * <td headers="matches">Any character except {@code a}, {@code b}, or {@code c}
	 * (negation)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct classes">{@code [a-zA-Z]}</td>
	 * <td headers="matches">{@code a} through {@code z} or {@code A} through
	 * {@code Z}, inclusive (range)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct classes">{@code [a-d[m-p]]}</td>
	 * <td headers="matches">{@code a} through {@code d}, or {@code m} through
	 * {@code p}: {@code [a-dm-p]} (union)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct classes">{@code [a-z&&[def]]}</td>
	 * <td headers="matches">{@code d}, {@code e}, or {@code f} (intersection)
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct classes">{@code [a-z&&[^bc]]}</td>
	 * <td headers="matches">{@code a} through {@code z}, except for {@code b} and
	 * {@code c}: {@code [ad-z]} (subtraction)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct classes">{@code [a-z&&[^m-p]]}</td>
	 * <td headers="matches">{@code a} through {@code z}, and not {@code m} through
	 * {@code p}: {@code [a-lq-z]}(subtraction)</td>
	 * </tr>
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 *
	 * <tr style="text-align: left">
	 * <th colspan="2" id="predef">Predefined character classes</th>
	 * </tr>
	 *
	 * <tr>
	 * <td style="vertical-align: top" headers="construct predef"><code>.</code></td>
	 * <td headers="matches">Any character (may or may not match <a href="#lt">line
	 * terminators</a>)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct predef"><code>\d</code></td>
	 * <td headers="matches">A digit: <code>[0-9]</code></td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct predef"><code>\D</code></td>
	 * <td headers="matches">A non-digit: <code>[^0-9]</code></td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct predef"><code>\h</code></td>
	 * <td headers="matches">A horizontal whitespace character:
	 * <code>[ \t\xA0&#92;u1680&#92;u180e&#92;u2000-&#92;u200a&#92;u202f&#92;u205f&#92;u3000]</code>
	 * </td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct predef"><code>\H</code></td>
	 * <td headers="matches">A non-horizontal whitespace character: <code>[^\h]</code>
	 * </td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct predef"><code>\s</code></td>
	 * <td headers="matches">A whitespace character: <code>[ \t\n\x0B\f\r]</code></td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct predef"><code>\S</code></td>
	 * <td headers="matches">A non-whitespace character: <code>[^\s]</code></td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct predef"><code>\v</code></td>
	 * <td headers="matches">A vertical whitespace character:
	 * <code>[\n\x0B\f\r\x85&#92;u2028&#92;u2029]</code></td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct predef"><code>\V</code></td>
	 * <td headers="matches">A non-vertical whitespace character: <code>[^\v]</code>
	 * </td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct predef"><code>\w</code></td>
	 * <td headers="matches">A word character: <code>[a-zA-Z_0-9]</code></td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct predef"><code>\W</code></td>
	 * <td headers="matches">A non-word character: <code>[^\w]</code></td>
	 * </tr>
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2" id="posix"><b>POSIX character classes (US-ASCII only)</b>
	 * </th>
	 * </tr>
	 *
	 * <tr>
	 * <td style="vertical-align: top" headers="construct posix">{@code \p{Lower}}</td>
	 * <td headers="matches">A lower-case alphabetic character: {@code [a-z]}</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct posix">{@code \p{Upper}}</td>
	 * <td headers="matches">An upper-case alphabetic character:{@code [A-Z]}</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct posix">{@code \p{ASCII}}</td>
	 * <td headers="matches">All ASCII:{@code [\x00-\x7F]}</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct posix">{@code \p{Alpha}}</td>
	 * <td headers="matches">An alphabetic character:{@code [\p{Lower}\p{Upper}]}
	 * </td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct posix">{@code \p{Digit}}</td>
	 * <td headers="matches">A decimal digit: {@code [0-9]}</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct posix">{@code \p{Alnum}}</td>
	 * <td headers="matches">An alphanumeric character:{@code [\p{Alpha}\p{Digit}]}
	 * </td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct posix">{@code \p{Punct}}</td>
	 * <td headers="matches">Punctuation: One of {@code !"#$%&'()*+,-./:;
	 * <=>?@[\]^_`{|}~}</td>
	 * </tr>
	 * <!-- {@code [\!"#\$%&'\(\)\*\+,\-\./:;\<=\>\?@\[\\\]\^_`\{\|\}~]}
	 * {@code [\X21-\X2F\X31-\X40\X5B-\X60\X7B-\X7E]} -->
	 * <tr>
	 * <td style="vertical-align: top" headers="construct posix">{@code \p{Graph}}</td>
	 * <td headers="matches">A visible character: {@code [\p{Alnum}\p{Punct}]}</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct posix">{@code \p{Print}}</td>
	 * <td headers="matches">A printable character: {@code [\p{Graph}\x20]}</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct posix">{@code \p{Blank}}</td>
	 * <td headers="matches">A space or a tab: {@code [ \t]}</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct posix">{@code \p{Cntrl}}</td>
	 * <td headers="matches">A control character: {@code [\x00-\x1F\x7F]}</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct posix">{@code \p{XDigit}}</td>
	 * <td headers="matches">A hexadecimal digit: {@code [0-9a-fA-F]}</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct posix">{@code \p{Space}}</td>
	 * <td headers="matches">A whitespace character: {@code [ \t\n\x0B\f\r]}</td>
	 * </tr>
	 *
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2">java.lang.Character classes (simple <a href="#jcc">java
	 * character type</a>)</th>
	 * </tr>
	 *
	 * <tr>
	 * <td style="vertical-align: top"><code>\p{javaLowerCase}</code></td>
	 * <td>Equivalent to java.lang.Character.isLowerCase()</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top"><code>\p{javaUpperCase}</code></td>
	 * <td>Equivalent to java.lang.Character.isUpperCase()</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top"><code>\p{javaWhitespace}</code></td>
	 * <td>Equivalent to java.lang.Character.isWhitespace()</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top"><code>\p{javaMirrored}</code></td>
	 * <td>Equivalent to java.lang.Character.isMirrored()</td>
	 * </tr>
	 *
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2" id="unicode">Classes for Unicode scripts, blocks, categories
	 * and binary properties</th>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct unicode">{@code \p{IsLatin}}</td>
	 * <td headers="matches">A Latin&nbsp;script character (
	 * <a href="#usc">script</a>)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct unicode">{@code \p{InGreek}}</td>
	 * <td headers="matches">A character in the Greek&nbsp;block (
	 * <a href="#ubc">block</a>)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct unicode">{@code \p{Lu}}</td>
	 * <td headers="matches">An uppercase letter (<a href="#ucc">category</a>)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct unicode">{@code \p{IsAlphabetic}}</td>
	 * <td headers="matches">An alphabetic character (<a href="#ubpc">binary
	 * property</a>)</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct unicode">{@code \p{Sc}}</td>
	 * <td headers="matches">A currency symbol</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct unicode">{@code \P{InGreek}}</td>
	 * <td headers="matches">Any character except one in the Greek block (negation)
	 * </td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct unicode">{@code [\p{L}&&[^\p{Lu}]]}</td>
	 * <td headers="matches">Any letter except an uppercase letter (subtraction)
	 * </td>
	 * </tr>
	 *
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2" id="bounds">Boundary matchers</th>
	 * </tr>
	 *
	 * <tr>
	 * <td style="vertical-align: top" headers="construct bounds"><code>^</code></td>
	 * <td headers="matches">The beginning of a line</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct bounds"><code>$</code></td>
	 * <td headers="matches">The end of a line</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct bounds"><code>\b</code></td>
	 * <td headers="matches">A word boundary</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct bounds"><code>\B</code></td>
	 * <td headers="matches">A non-word boundary</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct bounds"><code>\A</code></td>
	 * <td headers="matches">The beginning of the input</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct bounds"><code>\G</code></td>
	 * <td headers="matches">The end of the previous match</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct bounds"><code>\Z</code></td>
	 * <td headers="matches">The end of the input but for the final
	 * <a href="#lt">terminator</a>, if&nbsp;any</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct bounds"><code>\z</code></td>
	 * <td headers="matches">The end of the input</td>
	 * </tr>
	 *
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2" id="lineending">Linebreak matcher</th>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct lineending"><code>\R</code></td>
	 * <td headers="matches">Any Unicode linebreak sequence, is equivalent to
	 * <code>&#92;u000D&#92;u000A|[&#92;u000A&#92;u000B&#92;u000C&#92;u000D&#92;u0085&#92;u2028&#92;u2029]
	 *     </code></td>
	 * </tr>
	 *
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2" id="greedy">Greedy quantifiers</th>
	 * </tr>
	 *
	 * <tr>
	 * <td style="vertical-align: top" headers="construct greedy"><i>X</i><code>?</code></td>
	 * <td headers="matches"><i>X</i>, once or not at all</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct greedy"><i>X</i><code>*</code></td>
	 * <td headers="matches"><i>X</i>, zero or more times</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct greedy"><i>X</i><code>+</code></td>
	 * <td headers="matches"><i>X</i>, one or more times</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct greedy"><i>X</i><code>{</code><i>n</i>
	 * <code>}</code></td>
	 * <td headers="matches"><i>X</i>, exactly <i>n</i> times</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct greedy"><i>X</i><code>{</code><i>n</i>
	 * <code>,}</code></td>
	 * <td headers="matches"><i>X</i>, at least <i>n</i> times</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct greedy"><i>X</i><code>{</code><i>n</i>
	 * <code>,</code><i>m</i><code>}</code></td>
	 * <td headers="matches"><i>X</i>, at least <i>n</i> but not more than <i>m</i>
	 * times</td>
	 * </tr>
	 *
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2" id="reluc">Reluctant quantifiers</th>
	 * </tr>
	 *
	 * <tr>
	 * <td style="vertical-align: top" headers="construct reluc"><i>X</i><code>??</code></td>
	 * <td headers="matches"><i>X</i>, once or not at all</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct reluc"><i>X</i><code>*?</code></td>
	 * <td headers="matches"><i>X</i>, zero or more times</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct reluc"><i>X</i><code>+?</code></td>
	 * <td headers="matches"><i>X</i>, one or more times</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct reluc"><i>X</i><code>{</code><i>n</i>
	 * <code>}?</code></td>
	 * <td headers="matches"><i>X</i>, exactly <i>n</i> times</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct reluc"><i>X</i><code>{</code><i>n</i>
	 * <code>,}?</code></td>
	 * <td headers="matches"><i>X</i>, at least <i>n</i> times</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct reluc"><i>X</i><code>{</code><i>n</i>
	 * <code>,</code><i>m</i><code>}?</code></td>
	 * <td headers="matches"><i>X</i>, at least <i>n</i> but not more than <i>m</i>
	 * times</td>
	 * </tr>
	 *
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2" id="poss">Possessive quantifiers</th>
	 * </tr>
	 *
	 * <tr>
	 * <td style="vertical-align: top" headers="construct poss"><i>X</i><code>?+</code></td>
	 * <td headers="matches"><i>X</i>, once or not at all</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct poss"><i>X</i><code>*+</code></td>
	 * <td headers="matches"><i>X</i>, zero or more times</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct poss"><i>X</i><code>++</code></td>
	 * <td headers="matches"><i>X</i>, one or more times</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct poss"><i>X</i><code>{</code><i>n</i>
	 * <code>}+</code></td>
	 * <td headers="matches"><i>X</i>, exactly <i>n</i> times</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct poss"><i>X</i><code>{</code><i>n</i>
	 * <code>,}+</code></td>
	 * <td headers="matches"><i>X</i>, at least <i>n</i> times</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct poss"><i>X</i><code>{</code><i>n</i>
	 * <code>,</code><i>m</i><code>}+</code></td>
	 * <td headers="matches"><i>X</i>, at least <i>n</i> but not more than <i>m</i>
	 * times</td>
	 * </tr>
	 *
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2" id="logical">Logical operators</th>
	 * </tr>
	 *
	 * <tr>
	 * <td style="vertical-align: top" headers="construct logical"><i>XY</i></td>
	 * <td headers="matches"><i>X</i> followed by <i>Y</i></td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct logical"><i>X</i><code>|</code><i>Y</i></td>
	 * <td headers="matches">Either <i>X</i> or <i>Y</i></td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct logical"><code>(</code><i>X</i><code>)</code>
	 * </td>
	 * <td headers="matches">X, as a <a href="#cg">capturing group</a></td>
	 * </tr>
	 *
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2" id="backref">Back references</th>
	 * </tr>
	 *
	 * <tr>
	 * <td style="vertical-align: bottom" headers="construct backref"><code>\</code><i>n</i></td>
	 * <td style="vertical-align: bottom" headers="matches">Whatever the <i>n</i><sup>th</sup>
	 * <a href="#cg">capturing group</a> matched</td>
	 * </tr>
	 *
	 * <tr>
	 * <td style="vertical-align: bottom" headers="construct backref"><code>\</code><i>k</i>&lt;
	 * <i>name</i>&gt;</td>
	 * <td style="vertical-align: bottom" headers="matches">Whatever the
	 * <a href="#groupname">named-capturing group</a> "name" matched</td>
	 * </tr>
	 *
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2" id="quot">Quotation</th>
	 * </tr>
	 *
	 * <tr>
	 * <td style="vertical-align: top" headers="construct quot"><code>\</code></td>
	 * <td headers="matches">Nothing, but quotes the following character</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct quot"><code>\Q</code></td>
	 * <td headers="matches">Nothing, but quotes all characters until <code>\E</code>
	 * </td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct quot"><code>\E</code></td>
	 * <td headers="matches">Nothing, but ends quoting started by <code>\Q</code></td>
	 * </tr>
	 * <!-- Metachars: !$()*+.<>?[\]^{|} -->
	 *
	 *
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2" id="recursive"><a>Recursive
	 * expressions</a></th>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct recursive">
	 * <code>(?'<a href="#groupname">name</a>')</code></td>
	 * <td headers="matches">Matches the pattern of the
	 * <a href="#groupname">named-capturing group</a> "name" again.</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct recursive"><code>(?</code><i>n</i><code>)</code>
	 * </td>
	 * <td headers="matches">Matches the pattern given by the <i>n</i><sup>th</sup>
	 * group again. <i>n</i> must be greater than 0.</td>
	 * </tr>
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2" id="conditional">Conditional expressions</th>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct conditional">
	 * <code>(?(<a href="#groupname">name</a>)Y|N)</code></td>
	 * <td headers="matches">If the capture stack of the
	 * <a href="#groupname">named-capturing group</a> "name" isn't empty, Y is
	 * matched. Otherwise the optional pattern N is matched.</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct conditional"><code>(?(</code><i>n</i>
	 * <code>)Y|N)</code></td>
	 * <td headers="matches">If the capture stack of the <i>n</i><sup>th</sup> group
	 * isn't empty, Y is matched. Otherwise the optional pattern N is matched.</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct conditional"><code>(?(?=COND)Y|N)</code></td>
	 * <td headers="matches">If the pattern (?=COND) can be matched, Y is matched.
	 * Otherwise the optional pattern N is matched.</td>
	 * </tr>
	 *
	 * <tr>
	 * <th>&nbsp;</th>
	 * </tr>
	 * <tr style="text-align: left">
	 * <th colspan="2" id="special">Special constructs (named-capturing and
	 * non-capturing)</th>
	 * </tr>
	 *
	 * <tr>
	 * <td style="vertical-align: top" headers="construct special">
	 * <code>(?&lt;<a href="#groupname">name</a>&gt;</code><i>X</i><code>)</code></td>
	 * <td headers="matches"><i>X</i>, as a named-capturing group</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct
	 * special"><code>(?(DEFINE)</code><i>X</i><code>)</code></td>
	 * <td headers="matches">Defines the pattern <i>X</i> that is never executed and
	 * matches no characters. This is usually used to define one or more named
	 * groups which are referred to from elsewhere in the pattern.</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct special"><code>(?:</code><i>X</i><code>)</code>
	 * </td>
	 * <td headers="matches"><i>X</i>, as a non-capturing group</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct special">
	 * <code>(?idmsuxU-idmsuxU)&nbsp;</code></td>
	 * <td headers="matches">Nothing, but turns match flags
	 * <a href="#CASE_INSENSITIVE">i</a> <a href="#UNIX_LINES">d</a>
	 * <a href="#MULTILINE">m</a> <a href="#DOTALL">s</a>
	 * <a href="#UNICODE_CASE">u</a> <a href="#COMMENTS">x</a>
	 * <a href="#UNICODE_CHARACTER_CLASS">U</a> on - off</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct special"><code>(?idmsux-idmsux:</code>
	 * <i>X</i><code>)</code>&nbsp;&nbsp;</td>
	 * <td headers="matches"><i>X</i>, as a <a href="#cg">non-capturing group</a>
	 * with the given flags <a href="#CASE_INSENSITIVE">i</a>
	 * <a href="#UNIX_LINES">d</a> <a href="#MULTILINE">m</a>
	 * <a href="#DOTALL">s</a> <a href="#UNICODE_CASE">u</a >
	 * <a href="#COMMENTS">x</a> on - off</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct special"><code>(?=</code><i>X</i><code>)</code>
	 * </td>
	 * <td headers="matches"><i>X</i>, via zero-width positive lookahead</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct special"><code>(?!</code><i>X</i><code>)</code>
	 * </td>
	 * <td headers="matches"><i>X</i>, via zero-width negative lookahead</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct special"><code>(?&lt;=</code><i>X</i>
	 * <code>)</code></td>
	 * <td headers="matches"><i>X</i>, via zero-width positive lookbehind</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct special"><code>(?&lt;!</code><i>X</i>
	 * <code>)</code></td>
	 * <td headers="matches"><i>X</i>, via zero-width negative lookbehind</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align: top" headers="construct special"><code>(?&gt;</code><i>X</i>
	 * <code>)</code></td>
	 * <td headers="matches"><i>X</i>, as an atomic, non-capturing group</td>
	 * </tr>
	 * </table>
	 *
	 * <hr>
	 *
	 *
	 * <h2><a>Backslashes, escapes, and quoting</a></h2>
	 *
	 * <p>
	 * The backslash character (<code>'\'</code>) serves to introduce escaped
	 * constructs, as defined in the table above, as well as to quote characters
	 * that otherwise would be interpreted as unescaped constructs. Thus the
	 * expression <code>\\</code> matches a single backslash and <code>\{</code> matches a
	 * left brace.
	 *
	 * <p>
	 * It is an error to use a backslash prior to any alphabetic character that does
	 * not denote an escaped construct; these are reserved for future extensions to
	 * the regular-expression language. A backslash may be used prior to a
	 * non-alphabetic character regardless of whether that character is part of an
	 * unescaped construct.
	 *
	 * <p>
	 * Backslashes within string literals in Java source code are interpreted as
	 * required by <cite>The Java&trade; Language Specification</cite> as either
	 * Unicode escapes (section 3.3) or other character escapes (section 3.10.6) It
	 * is therefore necessary to double backslashes in string literals that
	 * represent regular expressions to protect them from interpretation by the Java
	 * bytecode compiler. The string literal <code>"&#92;b"</code>, for example, matches
	 * a single backspace character when interpreted as a regular expression, while
	 * <code>"&#92;&#92;b"</code> matches a word boundary. The string literal
	 * <code>"&#92;(hello&#92;)"</code> is illegal and leads to a compile-time error; in
	 * order to match the string <code>(hello)</code> the string literal
	 * <code>"&#92;&#92;(hello&#92;&#92;)"</code> must be used.
	 *
	 * <h2><a>Character Classes</a></h2>
	 *
	 * <p>
	 * Character classes may appear within other character classes, and may be
	 * composed by the union operator (implicit) and the intersection operator (
	 * <code>&amp;&amp;</code>). The union operator denotes a class that contains every
	 * character that is in at least one of its operand classes. The intersection
	 * operator denotes a class that contains every character that is in both of its
	 * operand classes.
	 *
	 * <p>
	 * The precedence of character-class operators is as follows, from highest to
	 * lowest:
	 *
	 * <blockquote>
	 * <table  style="padding: 1px" >
	 * <caption></caption>
	 * <tr>
	 * <th>1&nbsp;&nbsp;&nbsp;&nbsp;</th>
	 * <td>Literal escape&nbsp;&nbsp;&nbsp;&nbsp;</td>
	 * <td><code>\x</code></td>
	 * </tr>
	 * <tr>
	 * <th>2&nbsp;&nbsp;&nbsp;&nbsp;</th>
	 * <td>Grouping</td>
	 * <td><code>[...]</code></td>
	 * </tr>
	 * <tr>
	 * <th>3&nbsp;&nbsp;&nbsp;&nbsp;</th>
	 * <td>Range</td>
	 * <td><code>a-z</code></td>
	 * </tr>
	 * <tr>
	 * <th>4&nbsp;&nbsp;&nbsp;&nbsp;</th>
	 * <td>Union</td>
	 * <td><code>[a-e][i-u]</code></td>
	 * </tr>
	 * <tr>
	 * <th>5&nbsp;&nbsp;&nbsp;&nbsp;</th>
	 * <td>Intersection</td>
	 * <td>{@code [a-z&&[aeiou]]}</td>
	 * </tr>
	 * </table>
	 * </blockquote>
	 *
	 * <p>
	 * Note that a different set of metacharacters are in effect inside a character
	 * class than outside a character class. For instance, the regular expression
	 * <code>.</code> loses its special meaning inside a character class, while the
	 * expression <code>-</code> becomes a range forming metacharacter.
	 *
	 * <h2><a>Line terminators</a></h2>
	 *
	 * <p>
	 * A <i>line terminator</i> is a one- or two-character sequence that marks the
	 * end of a line of the input character sequence. The following are recognized
	 * as line terminators:
	 *
	 * <ul>
	 *
	 * <li>A newline (line feed) character&nbsp;(<code>'\n'</code>),
	 *
	 * <li>A carriage-return character followed immediately by a newline
	 * character&nbsp;(<code>"\r\n"</code>),
	 *
	 * <li>A standalone carriage-return character&nbsp;(<code>'\r'</code>),
	 *
	 * <li>A next-line character&nbsp;(<code>'&#92;u0085'</code>),
	 *
	 * <li>A line-separator character&nbsp;(<code>'&#92;u2028'</code>), or
	 *
	 * <li>A paragraph-separator character&nbsp;(<code>'&#92;u2029</code>).
	 *
	 * </ul>
	 * <p>
	 * If {@link #UNIX_LINES} mode is activated, then the only line terminators
	 * recognized are newline characters.
	 *
	 * <p>
	 * The regular expression <code>.</code> matches any character except a line
	 * terminator unless the {@link #DOTALL} flag is specified.
	 *
	 * <p>
	 * By default, the regular expressions <code>^</code> and <code>$</code> ignore line
	 * terminators and only match at the beginning and the end, respectively, of the
	 * entire input sequence. If {@link #MULTILINE} mode is activated then
	 * <code>^</code> matches at the beginning of input and after any line terminator
	 * except at the end of input. When in {@link #MULTILINE} mode <code>$</code>
	 * matches just before a line terminator or the end of the input sequence.
	 *
	 * <h2><a>Groups and capturing</a></h2>
	 *
	 * <h2><a>Group number</a></h2>
	 * <p>
	 * Capturing groups are numbered by counting their opening parentheses from left
	 * to right. In the expression <code>((A)(B(C)))</code>, for example, there are four
	 * such groups:
	 * </p>
	 *
	 * <blockquote>
	 * <table  style="padding: 1px" >
	 * <caption></caption>
	 * <tr>
	 * <th>1&nbsp;&nbsp;&nbsp;&nbsp;</th>
	 * <td><code>((A)(B(C)))</code></td>
	 * </tr>
	 * <tr>
	 * <th>2&nbsp;&nbsp;&nbsp;&nbsp;</th>
	 * <td><code>(A)</code></td>
	 * </tr>
	 * <tr>
	 * <th>3&nbsp;&nbsp;&nbsp;&nbsp;</th>
	 * <td><code>(B(C))</code></td>
	 * </tr>
	 * <tr>
	 * <th>4&nbsp;&nbsp;&nbsp;&nbsp;</th>
	 * <td><code>(C)</code></td>
	 * </tr>
	 * </table>
	 * </blockquote>
	 *
	 * <p>
	 * Group zero always stands for the entire expression.
	 *
	 * <p>
	 * Capturing groups are so named because, during a match, each subsequence of
	 * the input sequence that matches such a group is saved. The captured
	 * subsequence may be used later in the expression, via a back reference, and
	 * may also be retrieved from the matcher once the match operation is complete.
	 *
	 * <h2><a>Group name</a></h2>
	 * <p>
	 * A capturing group can also be assigned a "name", a
	 * <code>named-capturing group</code>, and then be back-referenced later by the
	 * "name". Group names are composed of the following characters. The first
	 * character must be a <code>letter</code>.
	 *
	 * <ul>
	 * <li>The uppercase letters <code>'A'</code> through <code>'Z'</code> (
	 * <code>'&#92;u0041'</code>&nbsp;through&nbsp;<code>'&#92;u005a'</code>),
	 * <li>The lowercase letters <code>'a'</code> through <code>'z'</code> (
	 * <code>'&#92;u0061'</code>&nbsp;through&nbsp;<code>'&#92;u007a'</code>),
	 * <li>The digits <code>'0'</code> through <code>'9'</code> (<code>'&#92;u0030'</code>
	 * &nbsp;through&nbsp;<code>'&#92;u0039'</code>),
	 * </ul>
	 *
	 * <p>
	 * A <code>named-capturing group</code> is still numbered as described in
	 * <a href="#gnumber">Group number</a>.
	 *
	 * <p>
	 * The captured input associated with a group is always the subsequence that the
	 * group most recently matched. If a group is evaluated a second time because of
	 * quantification then its previously-captured value, if any, will be retained
	 * if the second evaluation fails. Matching the string <code>"aba"</code> against
	 * the expression <code>(a(b)?)+</code>, for example, leaves group two set to
	 * <code>"b"</code>. All captured input is discarded at the beginning of each match.
	 *
	 * <p>
	 * Groups beginning with <code>(?</code> are either pure, <i>non-capturing</i>
	 * groups that do not capture text and do not count towards the group total, or
	 * <i>named-capturing</i> group.
	 *
	 * <h2>Unicode support</h2>
	 *
	 * <p>
	 * This class is in conformance with Level 1 of
	 * <a href="http://www.unicode.org/reports/tr18/"><i>Unicode Technical Standard
	 * #18: Unicode Regular Expression</i></a>, plus RL2.1 Canonical Equivalents.
	 * <p>
	 * <b>Unicode escape sequences</b> such as <code>&#92;u2014</code> in Java source
	 * code are processed as described in section 3.3 of <cite>The Java&trade;
	 * Language Specification</cite>. Such escape sequences are also implemented
	 * directly by the regular-expression parser so that Unicode escapes can be used
	 * in expressions that are read from files or from the keyboard. Thus the
	 * strings <code>"&#92;u2014"</code> and <code>"\\u2014"</code>, while not equal,
	 * compile into the same pattern, which matches the character with hexadecimal
	 * value <code>0x2014</code>.
	 * <p>
	 * A Unicode character can also be represented in a regular-expression by using
	 * its <b>Hex notation</b>(hexadecimal code point value) directly as described
	 * in construct <code>&#92;x{...}</code>, for example a supplementary character
	 * U+2011F can be specified as <code>&#92;x{2011F}</code>, instead of two
	 * consecutive Unicode escape sequences of the surrogate pair
	 * <code>&#92;uD840</code><code>&#92;uDD1F</code>.
	 * <p>
	 * Unicode scripts, blocks, categories and binary properties are written with
	 * the <code>\p</code> and <code>\P</code> constructs as in Perl. <code>\p{</code>
	 * <i>prop</i><code>}</code> matches if the input has the property <i>prop</i>,
	 * while <code>\P{</code><i>prop</i><code>}</code> does not match if the input has that
	 * property.
	 * <p>
	 * Scripts, blocks, categories and binary properties can be used both inside and
	 * outside of a character class.
	 *
	 * <p>
	 * <b><a>Scripts</a></b> are specified either with the prefix
	 * {@code Is}, as in {@code IsHiragana}, or by using the {@code script} keyword
	 * (or its short form {@code sc})as in {@code script=Hiragana} or
	 * {@code sc=Hiragana}.
	 * <p>
	 * The script names supported by <code>Pattern</code> are the valid script names
	 * accepted and defined by
	 * {@link java.lang.Character.UnicodeScript#forName(String)
	 * UnicodeScript.forName}.
	 *
	 * <p>
	 * <b><a>Blocks</a></b> are specified with the prefix {@code In}, as
	 * in {@code InMongolian}, or by using the keyword {@code block} (or its short
	 * form {@code blk}) as in {@code block=Mongolian} or {@code blk=Mongolian}.
	 * <p>
	 * The block names supported by <code>Pattern</code> are the valid block names
	 * accepted and defined by
	 * {@link java.lang.Character.UnicodeBlock#forName(String) UnicodeBlock.forName}
	 * .
	 * <p>
	 *
	 * <b><a>Categories</a></b> may be specified with the optional prefix
	 * {@code Is}: Both {@code \p{L}} and {@code \p{IsL}} denote the category of
	 * Unicode letters. Same as scripts and blocks, categories can also be specified
	 * by using the keyword {@code general_category} (or its short form {@code gc})
	 * as in {@code general_category=Lu} or {@code gc=Lu}.
	 * <p>
	 * The supported categories are those of
	 * <a href="http://www.unicode.org/unicode/standard/standard.html"> <i>The
	 * Unicode Standard</i></a> in the version specified by the
	 * {@link java.lang.Character Character} class. The category names are those
	 * defined in the Standard, both normative and informative.
	 * <p>
	 *
	 * <b><a>Binary properties</a></b> are specified with the prefix
	 * {@code Is}, as in {@code IsAlphabetic}. The supported binary properties by
	 * <code>Pattern</code> are
	 * <ul>
	 * <li>Alphabetic
	 * <li>Ideographic
	 * <li>Letter
	 * <li>Lowercase
	 * <li>Uppercase
	 * <li>Titlecase
	 * <li>Punctuation
	 * <Li>Control
	 * <li>White_Space
	 * <li>Digit
	 * <li>Hex_Digit
	 * <li>Join_Control
	 * <li>Noncharacter_Code_Point
	 * <li>Assigned
	 * </ul>
	 * <p>
	 * The following <b>Predefined Character classes</b> and <b>POSIX character
	 * classes</b> are in conformance with the recommendation of <i>Annex C:
	 * Compatibility Properties</i> of
	 * <a href="http://www.unicode.org/reports/tr18/"><i>Unicode Regular Expression
	 * </i></a>, when {@link #UNICODE_CHARACTER_CLASS} flag is specified.
	 *
	 * <table  style="padding: 1px" >
	 * <caption></caption>
	 * <tr style="text-align: left">
	 * <th style="text-align: left" id="predef_classes">Classes</th>
	 * <th style="text-align: left" id="predef_matches">Matches</th>
	 * </tr>
	 * <tr>
	 * <td><code>\p{Lower}</code></td>
	 * <td>A lowercase character:<code>\p{IsLowercase}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>\p{Upper}</code></td>
	 * <td>An uppercase character:<code>\p{IsUppercase}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>\p{ASCII}</code></td>
	 * <td>All ASCII:<code>[\x00-\x7F]</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>\p{Alpha}</code></td>
	 * <td>An alphabetic character:<code>\p{IsAlphabetic}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>\p{Digit}</code></td>
	 * <td>A decimal digit character:<code>p{IsDigit}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>\p{Alnum}</code></td>
	 * <td>An alphanumeric character:<code>[\p{IsAlphabetic}\p{IsDigit}]</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>\p{Punct}</code></td>
	 * <td>A punctuation character:<code>p{IsPunctuation}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>\p{Graph}</code></td>
	 * <td>A visible character:
	 * <code>[^\p{IsWhite_Space}\p{gc=Cc}\p{gc=Cs}\p{gc=Cn}]</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>\p{Print}</code></td>
	 * <td>A printable character: {@code [\p{Graph}\p{Blank}&&[^\p{Cntrl}]]}</td>
	 * </tr>
	 * <tr>
	 * <td><code>\p{Blank}</code></td>
	 * <td>A space or a tab:
	 * {@code [\p{IsWhite_Space}&&[^\p{gc=Zl}\p{gc=Zp}\x0a\x0b\x0c\x0d\x85]]}</td>
	 * </tr>
	 * <tr>
	 * <td><code>\p{Cntrl}</code></td>
	 * <td>A control character: <code>\p{gc=Cc}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>\p{XDigit}</code></td>
	 * <td>A hexadecimal digit: <code>[\p{gc=Nd}\p{IsHex_Digit}]</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>\p{Space}</code></td>
	 * <td>A whitespace character:<code>\p{IsWhite_Space}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>\d</code></td>
	 * <td>A digit: <code>\p{IsDigit}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>\D</code></td>
	 * <td>A non-digit: <code>[^\d]</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>\s</code></td>
	 * <td>A whitespace character: <code>\p{IsWhite_Space}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>\S</code></td>
	 * <td>A non-whitespace character: <code>[^\s]</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>\w</code></td>
	 * <td>A word character:
	 * <code>[\p{Alpha}\p{gc=Mn}\p{gc=Me}\p{gc=Mc}\p{Digit}\p{gc=Pc}\p{IsJoin_Control}]</code>
	 * </td>
	 * </tr>
	 * <tr>
	 * <td><code>\W</code></td>
	 * <td>A non-word character: <code>[^\w]</code></td>
	 * </tr>
	 * </table>
	 * <p>
	 * <a> Categories that behave like the java.lang.Character boolean is
	 * <i>methodname</i> methods (except for the deprecated ones) are available
	 * through the same <code>\p{</code><i>prop</i><code>}</code> syntax where the specified
	 * property has the name <code>java<i>methodname</i></code></a>.
	 *
	 * <h2>Comparison to Perl 5</h2>
	 *
	 * <p>
	 * The <code>Pattern</code> engine performs traditional NFA-based matching with
	 * ordered alternation as occurs in Perl 5.
	 *
	 * <p>
	 * Perl constructs not supported by this class:
	 * </p>
	 *
	 * <ul>
	 * <li>
	 * <p>
	 * Predefined character classes (Unicode character)
	 * <p>
	 * <code>\X&nbsp;&nbsp;&nbsp;&nbsp;</code>Match Unicode
	 * <a href="http://www.unicode.org/reports/tr18/#Default_Grapheme_Clusters"> <i>
	 * extended grapheme cluster</i></a>
	 * </p>
	 * </li>
	 *
	 * <li>
	 * <p>
	 * The backreference constructs, <code>\g{</code><i>n</i><code>}</code> for the <i>n</i>
	 * <sup>th</sup><a href="#cg">capturing group</a> and <code>\g{</code><i>name</i>
	 * <code>}</code> for <a href="#groupname">named-capturing group</a>.
	 * </p>
	 * </li>
	 *
	 * <li>
	 * <p>
	 * The named character construct, <code>\N{</code><i>name</i><code>}</code> for a
	 * Unicode character by its name.
	 * </p>
	 * </li>
	 *
	 * <li>
	 * <p>
	 * The conditional constructs <code>(?(</code><i>condition</i><code>)</code><i>X</i>
	 * <code>)</code> and <code>(?(</code><i>condition</i><code>)</code><i>X</i><code>|</code>
	 * <i>Y</i><code>)</code>,
	 * </p>
	 * </li>
	 *
	 * <li>
	 * <p>
	 * The embedded code constructs <code>(?{</code><i>code</i><code>})</code> and
	 * <code>(??{</code><i>code</i><code>})</code>,
	 * </p>
	 * </li>
	 *
	 * <li>
	 * <p>
	 * The embedded comment syntax <code>(?#comment)</code>, and
	 * </p>
	 * </li>
	 *
	 * <li>
	 * <p>
	 * The preprocessing operations <code>\l</code> <code>&#92;u</code>, <code>\L</code>, and
	 * <code>\U</code>.
	 * </p>
	 * </li>
	 *
	 * </ul>
	 *
	 * <p>
	 * Constructs supported by this class but not by Perl:
	 * </p>
	 *
	 * <ul>
	 *
	 * <li>
	 * <p>
	 * Character-class union and intersection as described <a href="#cc">above</a>.
	 * </p>
	 * </li>
	 *
	 * </ul>
	 *
	 * <p>
	 * Notable differences from Perl:
	 * </p>
	 *
	 * <ul>
	 *
	 * <li>
	 * <p>
	 * In Perl, <code>\1</code> through <code>\9</code> are always interpreted as back
	 * references; a backslash-escaped number greater than <code>9</code> is treated as
	 * a back reference if at least that many subexpressions exist, otherwise it is
	 * interpreted, if possible, as an octal escape. In this class octal escapes
	 * must always begin with a zero. In this class, <code>\1</code> through <code>\9</code>
	 * are always interpreted as back references, and a larger number is accepted as
	 * a back reference if at least that many subexpressions exist at that point in
	 * the regular expression, otherwise the parser will drop digits until the
	 * number is smaller or equal to the existing number of groups or it is one
	 * digit.
	 * </p>
	 * </li>
	 *
	 * <li>
	 * <p>
	 * Perl uses the <code>g</code> flag to request a match that resumes where the last
	 * match left off. This functionality is provided implicitly by the
	 * {@link Matcher} class: Repeated invocations of the {@link Matcher#find find}
	 * method will resume where the last match left off, unless the matcher is
	 * reset.
	 * </p>
	 * </li>
	 *
	 * <li>
	 * <p>
	 * In Perl, embedded flags at the top level of an expression affect the whole
	 * expression. In this class, embedded flags always take effect at the point at
	 * which they appear, whether they are at the top level or within a group; in
	 * the latter case, flags are restored at the end of the group just as in Perl.
	 * </p>
	 * </li>
	 *
	 * </ul>
	 *
	 *
	 * <p>
	 * For a more precise description of the behavior of regular expression
	 * constructs, please see <a href="http://www.oreilly.com/catalog/regex3/">
	 * <i>Mastering Regular Expressions, 3nd Edition</i>, Jeffrey E. F. Friedl,
	 * O'Reilly and Associates, 2006.</a>
	 * </p>
	 *
	 * @author Mike McCloskey
	 * @author Mark Reinhold
	 * @author JSR-51 Expert Group
	 * @see java.lang.String#split(String, int)
	 * @see java.lang.String#split(String)
	 * @since 1.4
	 */
	
	public class Pattern implements java.io.Serializable {
		
		/**
		 * Regular expression modifier values. Instead of being passed as arguments,
		 * they can also be passed as inline modifiers. For example, the following
		 * statements have the same effect.
		 *
		 * <pre>
		 * RegExp r1 = RegExp.compile("abc", Pattern.I | Pattern.M);
		 * RegExp r2 = RegExp.compile("(?im)abc", 0);
		 * </pre>
		 *
		 * The flags are duplicated so that the familiar Perl match flag names are
		 * available.
		 */
		
		/**
		 * Enables Unix lines mode.
		 *
		 * <p>
		 * In this mode, only the <code>'\n'</code> line terminator is recognized in the
		 * behavior of <code>.</code>, <code>^</code>, and <code>$</code>.
		 *
		 * <p>
		 * Unix lines mode can also be enabled via the embedded flag
		 * expression&nbsp;<code>(?d)</code>.
		 */
		public static final int UNIX_LINES = 0x01;
		
		/**
		 * Enables case-insensitive matching.
		 *
		 * <p>
		 * By default, case-insensitive matching assumes that only characters in the
		 * US-ASCII charset are being matched. Unicode-aware case-insensitive matching
		 * can be enabled by specifying the {@link #UNICODE_CASE} flag in conjunction
		 * with this flag.
		 *
		 * <p>
		 * Case-insensitive matching can also be enabled via the embedded flag
		 * expression&nbsp;<code>(?i)</code>.
		 *
		 * <p>
		 * Specifying this flag may impose a slight performance penalty.
		 * </p>
		 */
		public static final int CASE_INSENSITIVE = 0x02;
		
		/**
		 * Permits whitespace and comments in pattern.
		 *
		 * <p>
		 * In this mode, whitespace is ignored, and embedded comments starting with
		 * <code>#</code> are ignored until the end of a line.
		 *
		 * <p>
		 * Comments mode can also be enabled via the embedded flag expression&nbsp;
		 * <code>(?x)</code>.
		 */
		public static final int COMMENTS = 0x04;
		
		/**
		 * Enables multiline mode.
		 *
		 * <p>
		 * In multiline mode the expressions <code>^</code> and <code>$</code> match just after
		 * or just before, respectively, a line terminator or the end of the input
		 * sequence. By default these expressions only match at the beginning and the
		 * end of the entire input sequence.
		 *
		 * <p>
		 * Multiline mode can also be enabled via the embedded flag expression&nbsp;
		 * <code>(?m)</code>.
		 * </p>
		 */
		public static final int MULTILINE = 0x08;
		
		/**
		 * Enables literal parsing of the pattern.
		 *
		 * <p>
		 * When this flag is specified then the input string that specifies the pattern
		 * is treated as a sequence of literal characters. Metacharacters or escape
		 * sequences in the input sequence will be given no special meaning.
		 *
		 * <p>
		 * The flags CASE_INSENSITIVE and UNICODE_CASE retain their impact on matching
		 * when used in conjunction with this flag. The other flags become superfluous.
		 *
		 * <p>
		 * There is no embedded flag character for enabling literal parsing.
		 *
		 * @since 1.5
		 */
		public static final int LITERAL = 0x10;
		
		/**
		 * Enables dotall mode.
		 *
		 * <p>
		 * In dotall mode, the expression <code>.</code> matches any character, including a
		 * line terminator. By default this expression does not match line terminators.
		 *
		 * <p>
		 * Dotall mode can also be enabled via the embedded flag expression&nbsp;
		 * <code>(?s)</code>. (The <code>s</code> is a mnemonic for "single-line" mode, which is
		 * what this is called in Perl.)
		 * </p>
		 */
		public static final int DOTALL = 0x20;
		
		/**
		 * Enables Unicode-aware case folding.
		 *
		 * <p>
		 * When this flag is specified then case-insensitive matching, when enabled by
		 * the {@link #CASE_INSENSITIVE} flag, is done in a manner consistent with the
		 * Unicode Standard. By default, case-insensitive matching assumes that only
		 * characters in the US-ASCII charset are being matched.
		 *
		 * <p>
		 * Unicode-aware case folding can also be enabled via the embedded flag
		 * expression&nbsp;<code>(?u)</code>.
		 *
		 * <p>
		 * Specifying this flag may impose a performance penalty.
		 * </p>
		 */
		public static final int UNICODE_CASE = 0x40;
		
		
		/**
		 * Enables the Unicode version of <i>Predefined character classes</i> and
		 * <i>POSIX character classes</i>.
		 *
		 * <p>
		 * When this flag is specified then the (US-ASCII only) <i>Predefined character
		 * classes</i> and <i>POSIX character classes</i> are in conformance with
		 * <a href="http://www.unicode.org/reports/tr18/"><i>Unicode Technical Standard
		 * #18: Unicode Regular Expression</i></a> <i>Annex C: Compatibility
		 * Properties</i>.
		 * <p>
		 * The UNICODE_CHARACTER_CLASS mode can also be enabled via the embedded flag
		 * expression&nbsp;<code>(?U)</code>.
		 * <p>
		 * The flag implies UNICODE_CASE, that is, it enables Unicode-aware case
		 * folding.
		 * <p>
		 * Specifying this flag may impose a performance penalty.
		 * </p>
		 *
		 * @since 1.7
		 */
		public static final int UNICODE_CHARACTER_CLASS = 0x100;
		
		/*
		 * Pattern has only two serialized components: The pattern string and the flags,
		 * which are all that is needed to recompile the pattern when it is
		 * deserialized.
		 */
		
		/**
		 * use serialVersionUID from Merlin b59 for interoperability
		 */
		public static final long serialVersionUID = 5073258162644648461L;
		
		/**
		 * The original regular-expression pattern string.
		 *
		 * @serial
		 */
		public String pattern;
		
		/**
		 * The original pattern flags.
		 *
		 * @serial
		 */
		public int flags;
		public boolean inLookaround;
		
		/**
		 * Boolean indicating this Pattern is compiled; this is necessary in order to
		 * lazily compile deserialized Patterns.
		 */
		public transient volatile boolean compiled = false;
		
		/**
		 * The normalized pattern string.
		 */
		public transient String normalizedPattern;
		
		/**
		 * The starting point of state machine for the find operation. This allows a
		 * match to start anywhere in the input.
		 */
		transient Node root;
		
		/**
		 * The root of object tree for a match operation. The pattern is matched at the
		 * beginning. This may include a find that uses BnM or a First node.
		 */
		transient Node matchRoot;
		public transient Node matchRootSetter;
		
		public void setMatchRoot (Node node) {
			
			if (matchRootSetter == null) {
				
				matchRootSetter = new Node () {
					
					@Override
					public void setNext (Node a) {
						matchRoot = a;
						if (a != null) a.previous = this;
					}
					
				};
				
			}
			
			matchRootSetter.setNext (node);
			
		}
		
		/**
		 * Temporary storage used by parsing pattern slice.
		 */
		transient int[] buffer;
		
		static Map<String, Class<? extends CustomNode>> plugins = new HashMap<> ();
		/**
		 * Map the "name" of the "named capturing group" to its group id node.
		 */
		transient volatile Map<String, Integer> groupIndices;
		transient volatile Map<Integer, String> groupNames;
		transient volatile Set<Object> recursivelyCalledGroups;
		
		public transient ArrayList<GroupHeadAndTail> groupHeadAndTailNodes;
		public transient List<Runnable> groupExistsChecks;
		public transient List<Runnable> lookbehindHasMaxChecks;
		public transient List<Runnable> curlyDeterministicChecks;
		public transient List<Runnable> groupCalledRecursivelyChecks;
		
		/**
		 * Temporary null terminated code point array used by pattern compiling.
		 */
		public transient int[] temp;
		
		/**
		 * The number of capturing groups in this Pattern. Used by matchers to allocate
		 * storage needed to perform a match.
		 */
		transient int capturingGroupCount;
		
		/**
		 * The local variable count used by parsing tree. Used by matchers to allocate
		 * storage needed to perform a match.
		 */
		transient int localCount;
		
		/**
		 * Index into the pattern string that keeps track of how much has been parsed.
		 */
		public transient int cursor;
		
		/**
		 * Holds the length of the pattern string.
		 */
		public transient int patternLength;
		
		/**
		 * If the Start node might possibly match supplementary characters. It is set to
		 * true during compiling if (1) There is supplementary char in pattern, or (2)
		 * There is complement node of Category or Block
		 */
		public transient boolean hasSupplementary;
		
		/**
		 * Compiles the given regular expression into a pattern.
		 *
		 * @param regex The expression to be compiled
		 * @return the given regular expression compiled into a pattern
		 * @throws PatternSyntaxException If the expression's syntax is invalid
		 */
		public static Pattern compile (String regex) {
			return new Pattern (regex, 0);
		}
		
		/**
		 * Compiles the given regular expression into a pattern with the given flags.
		 *
		 * @param regex The expression to be compiled
		 * @param flags Match flags, a bit mask that may include
		 *              {@link #CASE_INSENSITIVE}, {@link #MULTILINE}, {@link #DOTALL} ,
		 *              {@link #UNICODE_CASE}, {@link #UNIX_LINES},
		 *              {@link #LITERAL}, {@link #UNICODE_CHARACTER_CLASS} and
		 *              {@link #COMMENTS}
		 * @return the given regular expression compiled into a pattern with the given
		 * flags
		 * @throws IllegalArgumentException If bit values other than those corresponding to the defined match
		 *                                  flags are set in <code>flags</code>
		 * @throws PatternSyntaxException   If the expression's syntax is invalid
		 */
		public static Pattern compile (String regex, int flags) {
			return new Pattern (regex, flags);
		}
		
		/**
		 * Installs a plugin into this regex engine.
		 * <p>
		 * Refer to this plugin with \c{name} in your regular expression pattern. In
		 * order for this to work, the class clazz needs to have a no-argument
		 * constructor. You can also pass parameters to your plugin by e.g. writing
		 * \c[name,param1,param2} in your regular expression pattern and providing a
		 * constructor with two String parameters in the class clazz.
		 *
		 * @param name  The name of the plugin.
		 * @param clazz The class of the plugin
		 * @see Pattern#uninstallPlugin(String)
		 * @see CustomNode
		 */
		public static void installPlugin (String name, Class<? extends CustomNode> clazz) {
			plugins.put (name, clazz);
		}
		
		public static void uninstallPlugin (String name) {
			plugins.remove (name);
		}
		
		/**
		 * Returns the regular expression from which this pattern was compiled.
		 *
		 * @return The source of this pattern
		 */
		public String pattern () {
			return pattern;
		}
		
		/**
		 * <p>
		 * Returns the string representation of this pattern. This is the regular
		 * expression from which this pattern was compiled.
		 * </p>
		 *
		 * @return The string representation of this pattern
		 * @since 1.5
		 */
		public String toString () {
			return pattern;
		}
		
		/**
		 * Creates a matcher that will match the given input against this pattern.
		 *
		 * @param input The character sequence to be matched
		 * @return A new matcher for this pattern
		 */
		public Matcher matcher (CharSequence input) {
			if (!compiled) {
				synchronized (this) {
					if (!compiled)
						compile ();
				}
			}
			Matcher m = new Matcher (this, input);
			return m;
		}
		
		/**
		 * Returns this pattern's match flags.
		 *
		 * @return The match flags specified when this pattern was compiled
		 */
		public int flags () {
			return flags;
		}
		
		/**
		 * Compiles the given regular expression and attempts to match the given input
		 * against it.
		 *
		 * <p>
		 * An invocation of this convenience method of the form
		 *
		 * <blockquote>
		 *
		 * <pre>
		 * Pattern.matches(regex, input);
		 * </pre>
		 *
		 * </blockquote>
		 * <p>
		 * behaves in exactly the same way as the expression
		 *
		 * <blockquote>
		 *
		 * <pre>
		 * Pattern.compile(regex).matcher(input).matches()
		 * </pre>
		 *
		 * </blockquote>
		 *
		 * <p>
		 * If a pattern is to be used multiple times, compiling it once and reusing it
		 * will be more efficient than invoking this method each time.
		 * </p>
		 *
		 * @param regex The expression to be compiled
		 * @param input The character sequence to be matched
		 * @return whether or not the regular expression matches on the input
		 * @throws PatternSyntaxException If the expression's syntax is invalid
		 */
		public static boolean matches (String regex, CharSequence input) {
			Pattern p = Pattern.compile (regex);
			Matcher m = p.matcher (input);
			return m.matches ();
		}
		
		/**
		 * Splits the given input sequence around matches of this pattern.
		 *
		 * <p>
		 * The array returned by this method contains each substring of the input
		 * sequence that is terminated by another subsequence that matches this pattern
		 * or is terminated by the end of the input sequence. The substrings in the
		 * array are in the order in which they occur in the input. If this pattern does
		 * not match any subsequence of the input then the resulting array has just one
		 * element, namely the input sequence in string form.
		 *
		 * <p>
		 * When there is a positive-width match at the beginning of the input sequence
		 * then an empty leading substring is included at the beginning of the resulting
		 * array. A zero-width match at the beginning however never produces such empty
		 * leading substring.
		 *
		 * <p>
		 * The <code>limit</code> parameter controls the number of times the pattern is
		 * applied and therefore affects the length of the resulting array. If the limit
		 * <i>n</i> is greater than zero then the pattern will be applied at most
		 * <i>n</i>&nbsp;-&nbsp;1 times, the array's length will be no greater than
		 * <i>n</i>, and the array's last entry will contain all input beyond the last
		 * matched delimiter. If <i>n</i> is non-positive then the pattern will be
		 * applied as many times as possible and the array can have any length. If
		 * <i>n</i> is zero then the pattern will be applied as many times as possible,
		 * the array can have any length, and trailing empty strings will be discarded.
		 *
		 * <p>
		 * The input <code>"boo:and:foo"</code>, for example, yields the following results
		 * with these parameters:
		 *
		 * <blockquote>
		 * <table  style="padding: 1px" >
		 * <caption></caption>
		 * <tr>
		 * <th style="text-align: left"><i>Regex&nbsp;&nbsp;&nbsp;&nbsp;</i></th>
		 * <th style="text-align: left"><i>Limit&nbsp;&nbsp;&nbsp;&nbsp;</i></th>
		 * <th style="text-align: left"><i>Result&nbsp;&nbsp;&nbsp;&nbsp;</i></th>
		 * </tr>
		 * <tr>
		 * <td style="text-align: center">:</td>
		 * <td style="text-align: center">2</td>
		 * <td><code>{ "boo", "and:foo" }</code></td>
		 * </tr>
		 * <tr>
		 * <td style="text-align: center">:</td>
		 * <td style="text-align: center">5</td>
		 * <td><code>{ "boo", "and", "foo" }</code></td>
		 * </tr>
		 * <tr>
		 * <td style="text-align: center">:</td>
		 * <td style="text-align: center">-2</td>
		 * <td><code>{ "boo", "and", "foo" }</code></td>
		 * </tr>
		 * <tr>
		 * <td style="text-align: center">o</td>
		 * <td style="text-align: center">5</td>
		 * <td><code>{ "b", "", ":and:f", "", "" }</code></td>
		 * </tr>
		 * <tr>
		 * <td style="text-align: center">o</td>
		 * <td style="text-align: center">-2</td>
		 * <td><code>{ "b", "", ":and:f", "", "" }</code></td>
		 * </tr>
		 * <tr>
		 * <td style="text-align: center">o</td>
		 * <td style="text-align: center">0</td>
		 * <td><code>{ "b", "", ":and:f" }</code></td>
		 * </tr>
		 * </table>
		 * </blockquote>
		 *
		 * @param input The character sequence to be split
		 * @param limit The result threshold, as described above
		 * @return The array of strings computed by splitting the input around matches
		 * of this pattern
		 */
		public String[] split (CharSequence input, int limit) {
			int index = 0;
			boolean matchLimited = limit > 0;
			ArrayList<String> matchList = new ArrayList<String> ();
			Matcher m = matcher (input);
			
			// Add segments before each match found
			while (m.find ()) {
				if (!matchLimited || matchList.size () < limit - 1) {
					if (index == 0 && index == m.start () && m.start () == m.end ()) {
						// no empty leading substring included for zero-width match
						// at the beginning of the input char sequence.
						continue;
					}
					String match = input.subSequence (index, m.start ()).toString ();
					matchList.add (match);
					index = m.end ();
				} else if (matchList.size () == limit - 1) { // last one
					String match = input.subSequence (index, input.length ()).toString ();
					matchList.add (match);
					index = m.end ();
				}
			}
			
			// If no match was found, return this
			if (index == 0)
				return new String[] {input.toString ()};
			
			// Add remaining segment
			if (!matchLimited || matchList.size () < limit)
				matchList.add (input.subSequence (index, input.length ()).toString ());
			
			// Construct result
			int resultSize = matchList.size ();
			if (limit == 0)
				while (resultSize > 0 && matchList.get (resultSize - 1).equals (""))
					resultSize--;
			String[] result = new String[resultSize];
			return matchList.subList (0, resultSize).toArray (result);
		}
		
		/**
		 * Splits the given input sequence around matches of this pattern.
		 *
		 * <p>
		 * This method works as if by invoking the two-argument
		 * {@link #split(java.lang.CharSequence, int) split} method with the given input
		 * sequence and a limit argument of zero. Trailing empty strings are therefore
		 * not included in the resulting array.
		 * </p>
		 *
		 * <p>
		 * The input <code>"boo:and:foo"</code>, for example, yields the following results
		 * with these expressions:
		 *
		 * <blockquote>
		 * <table  style="padding: 1px" >
		 * <caption></caption>
		 * <tr>
		 * <th style="text-align: left"><i>Regex&nbsp;&nbsp;&nbsp;&nbsp;</i></th>
		 * <th style="text-align: left"><i>Result</i></th>
		 * </tr>
		 * <tr>
		 * <td style="text-align: center">:</td>
		 * <td><code>{ "boo", "and", "foo" }</code></td>
		 * </tr>
		 * <tr>
		 * <td style="text-align: center">o</td>
		 * <td><code>{ "b", "", ":and:f" }</code></td>
		 * </tr>
		 * </table>
		 * </blockquote>
		 *
		 * @param input The character sequence to be split
		 * @return The array of strings computed by splitting the input around matches
		 * of this pattern
		 */
		public String[] split (CharSequence input) {
			return split (input, 0);
		}
		
		/**
		 * Returns a literal pattern <code>String</code> for the specified
		 * <code>String</code>.
		 *
		 * <p>
		 * This method produces a <code>String</code> that can be used to create a
		 * <code>Pattern</code> that would match the string <code>s</code> as if it were
		 * a literal pattern.
		 * </p>
		 * Metacharacters or escape sequences in the input sequence will be given no
		 * special meaning.
		 *
		 * @param s The string to be literalized
		 * @return A literal string replacement
		 * @since 1.5
		 */
		public static String quote (String s) {
			int slashEIndex = s.indexOf ("\\E");
			if (slashEIndex == -1)
				return "\\Q" + s + "\\E";
			
			StringBuilder sb = new StringBuilder (s.length () * 2);
			sb.append ("\\Q");
			slashEIndex = 0;
			int current = 0;
			while ((slashEIndex = s.indexOf ("\\E", current)) != -1) {
				sb.append (s.substring (current, slashEIndex));
				current = slashEIndex + 2;
				sb.append ("\\E\\\\E\\Q");
			}
			sb.append (s.substring (current, s.length ()));
			sb.append ("\\E");
			return sb.toString ();
		}
		
		/**
		 * Recompile the Pattern instance from a stream. The original pattern string is
		 * read in and the object tree is recompiled from it.
		 */
		public void readObject (java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
			
			// Read in all fields
			s.defaultReadObject ();
			
			// Initialize counts
			capturingGroupCount = 1;
			localCount = 0;
			
			// if length > 0, the Pattern is lazily compiled
			compiled = false;
			if (pattern.length () == 0) {
				root = new Start (lastAccept);
				setMatchRoot (lastAccept);
				compiled = true;
			}
		}
		
		/**
		 * This public constructor is used to create all Patterns. The pattern string
		 * and match flags are all that is needed to completely describe a Pattern. An
		 * empty pattern string results in an object tree with only a Start node and a
		 * LastNode node.
		 */
		public Pattern (String p, int f) {
			pattern = p;
			flags = f;
			
			// to use UNICODE_CASE if UNICODE_CHARACTER_CLASS present
			if ((flags & UNICODE_CHARACTER_CLASS) != 0)
				flags |= UNICODE_CASE;
			
			int saveFlags = flags;
			// Reset group index count
			capturingGroupCount = 1;
			localCount = 0;
			
			if (pattern.length () > 0) {
				compile ();
			} else {
				root = new Start (lastAccept);
				setMatchRoot (lastAccept);
			}
			flags = saveFlags;
		}
		
		
		/**
		 * Attempts to compose input by combining the first character with the first
		 * combining mark following it. Returns a String that is the composition of the
		 * leading character with its first combining mark followed by the remaining
		 * combining marks. Returns null if the first two characters cannot be further
		 * composed.
		 */
		public String composeOneStep (String input) {
			int len = countChars (input, 0, 2);
			String firstTwoCharacters = input.substring (0, len);
			String result = Normalizer.normalize (firstTwoCharacters, Normalizer.Form.NFC);
			
			if (result.equals (firstTwoCharacters))
				return null;
			else {
				String remainder = input.substring (len);
				return result + remainder;
			}
		}
		
		/**
		 * Preprocess any \Q...\E sequences in `temp', meta-quoting them. See the
		 * description of `quotemeta' in perlfunc(1).
		 */
		public void RemoveQEQuoting () {
			final int pLen = patternLength;
			int i = 0;
			while (i < pLen - 1) {
				if (temp[i] != '\\')
					i += 1;
				else if (temp[i + 1] != 'Q')
					i += 2;
				else
					break;
			}
			if (i >= pLen - 1) // No \Q sequence found
				return;
			int j = i;
			i += 2;
			int[] newtemp = new int[j + 3 * (pLen - i) + 2];
			System.arraycopy (temp, 0, newtemp, 0, j);
			
			boolean inQuote = true;
			boolean beginQuote = true;
			while (i < pLen) {
				int c = temp[i++];
				if (!ASCII.isAscii (c) || ASCII.isAlpha (c)) {
					newtemp[j++] = c;
				} else if (ASCII.isDigit (c)) {
					if (beginQuote) {
						/*
						 * A unicode escape \[0xu] could be before this quote, and we don't want this
						 * numeric char to processed as part of the escape.
						 */
						newtemp[j++] = '\\';
						newtemp[j++] = 'x';
						newtemp[j++] = '3';
					}
					newtemp[j++] = c;
				} else if (c != '\\') {
					if (inQuote)
						newtemp[j++] = '\\';
					newtemp[j++] = c;
				} else if (inQuote) {
					if (temp[i] == 'E') {
						i++;
						inQuote = false;
					} else {
						newtemp[j++] = '\\';
						newtemp[j++] = '\\';
					}
				} else {
					if (temp[i] == 'Q') {
						i++;
						inQuote = true;
						beginQuote = true;
						continue;
					} else {
						newtemp[j++] = c;
						if (i != pLen)
							newtemp[j++] = temp[i++];
					}
				}
				
				beginQuote = false;
			}
			
			patternLength = j;
			temp = Arrays.copyOf (newtemp, j + 2); // double zero termination
		}
		
		/**
		 * Copies regular expression to an int array and invokes the parsing of the
		 * expression which will create the object tree.
		 */
		public void compile () {
			// Handle canonical equivalences
			normalizedPattern = pattern;
			patternLength = normalizedPattern.length ();
			
			// Copy pattern to int array for convenience
			// Use double zero to terminate pattern
			temp = new int[patternLength + 2];
			
			hasSupplementary = false;
			int c, count = 0;
			// Convert all chars into code points
			for (int x = 0; x < patternLength; x += Character.charCount (c)) {
				c = normalizedPattern.codePointAt (x);
				if (isSupplementary (c)) {
					hasSupplementary = true;
				}
				temp[count++] = c;
			}
			
			patternLength = count; // patternLength now in code points
			
			if (!has (LITERAL))
				RemoveQEQuoting ();
			
			// Allocate all temporary objects here.
			buffer = new int[32];
			groupIndices = null;
			groupNames = null;
			
			if (has (LITERAL)) {
				// Literal pattern handling
				setMatchRoot (newSlice (temp, patternLength, hasSupplementary));
				matchRoot.setNext (lastAccept);
			} else {
				// Start recursive descent parsing
				setMatchRoot (expr (lastAccept));
				// Check extra pattern characters
				if (patternLength != cursor) {
					if (peek () == ')') {
						throw error ("Unmatched closing ')'");
					} else {
						throw error ("Unexpected internal error");
					}
				}
			}
			
			for (Runnable r : groupExistsChecks ()) {
				r.run ();
			}
			
			for (Runnable r : groupCalledRecursivelyChecks ()) {
				r.run ();
			}
			
			for (Runnable r : lookbehindHasMaxChecks ()) {
				r.run ();
			}
			
			for (Runnable r : curlyDeterministicChecks ()) {
				r.run ();
			}
			
			// Peephole optimization
			if (matchRoot instanceof Slice) {
				root = BnM.optimize (matchRoot);
				if (root == matchRoot) {
					root = hasSupplementary ? new StartS (matchRoot) : new Start (matchRoot);
				}
			} else if (matchRoot instanceof Begin) {
				root = matchRoot;
			} else {
				root = hasSupplementary ? new StartS (matchRoot) : new Start (matchRoot);
			}
			
			// Release temporary storage
			temp = null;
			buffer = null;
			patternLength = 0;
			compiled = true;
		}
		
		Map<String, Integer> groupIndices () {
			if (groupIndices == null)
				groupIndices = new HashMap<> (2);
			return groupIndices;
		}
		
		Map<Integer, String> groupNames () {
			if (groupNames == null)
				groupNames = new HashMap<> ();
			return groupNames;
		}
		
		List<Runnable> groupExistsChecks () {
			if (groupExistsChecks == null)
				groupExistsChecks = new LinkedList<> ();
			return groupExistsChecks;
		}
		
		List<Runnable> lookbehindHasMaxChecks () {
			if (lookbehindHasMaxChecks == null)
				lookbehindHasMaxChecks = new LinkedList<> ();
			return lookbehindHasMaxChecks;
		}
		
		List<Runnable> curlyDeterministicChecks () {
			if (curlyDeterministicChecks == null)
				curlyDeterministicChecks = new LinkedList<> ();
			return curlyDeterministicChecks;
		}
		
		List<Runnable> groupCalledRecursivelyChecks () {
			if (groupCalledRecursivelyChecks == null)
				groupCalledRecursivelyChecks = new LinkedList<> ();
			return groupCalledRecursivelyChecks;
		}
		
		Set<Object> recursivelyCalledGroups () {
			if (recursivelyCalledGroups == null)
				recursivelyCalledGroups = new HashSet<Object> ();
			return recursivelyCalledGroups;
		}
		
		/*
		 * The following public methods are mainly used to improve the readability of
		 * the code. In order to let the Java compiler easily inline them, we should not
		 * put many assertions or error checks in them.
		 */
		
		/**
		 * Indicates whether a particular flag is set or not.
		 */
		public boolean has (int f) {
			return (flags & f) != 0;
		}
		
		/**
		 * Match next character, signal error if failed.
		 */
		public void accept (int ch, String s) {
			int testChar = temp[cursor++];
			if (has (COMMENTS))
				testChar = parsePastWhitespace (testChar);
			if (ch != testChar) {
				throw error (s);
			}
		}
		
		/**
		 * Mark the end of pattern with a specific character.
		 */
		public void mark (int c) {
			temp[patternLength] = c;
		}
		
		/**
		 * Peek the next character, and do not advance the cursor.
		 */
		public int peek () {
			int ch = temp[cursor];
			if (has (COMMENTS))
				ch = peekPastWhitespace (ch);
			return ch;
		}
		
		/**
		 * Read the next character, and advance the cursor by one.
		 */
		public int read () {
			int ch = temp[cursor++];
			if (has (COMMENTS))
				ch = parsePastWhitespace (ch);
			return ch;
		}
		
		/**
		 * Advance the cursor by one, and peek the next character.
		 */
		public int next () {
			int ch = temp[++cursor];
			if (has (COMMENTS))
				ch = peekPastWhitespace (ch);
			return ch;
		}
		
		/**
		 * Advance the cursor by one, and peek the next character, ignoring the COMMENTS
		 * setting
		 */
		public int nextEscaped () {
			return temp[++cursor];
		}
		
		/**
		 * If in xmode peek past whitespace and comments.
		 */
		public int peekPastWhitespace (int ch) {
			while (ASCII.isSpace (ch) || ch == '#') {
				while (ASCII.isSpace (ch))
					ch = temp[++cursor];
				if (ch == '#') {
					ch = peekPastLine ();
				}
			}
			return ch;
		}
		
		/**
		 * If in xmode parse past whitespace and comments.
		 */
		public int parsePastWhitespace (int ch) {
			while (ASCII.isSpace (ch) || ch == '#') {
				while (ASCII.isSpace (ch))
					ch = temp[cursor++];
				if (ch == '#')
					ch = parsePastLine ();
			}
			return ch;
		}
		
		/**
		 * xmode parse past comment to end of line.
		 */
		public int parsePastLine () {
			int ch = temp[cursor++];
			while (ch != 0 && !isLineSeparator (ch))
				ch = temp[cursor++];
			return ch;
		}
		
		/**
		 * xmode peek past comment to end of line.
		 */
		public int peekPastLine () {
			int ch = temp[++cursor];
			while (ch != 0 && !isLineSeparator (ch))
				ch = temp[++cursor];
			return ch;
		}
		
		/**
		 * Determines if character is a line separator in the current mode
		 */
		public boolean isLineSeparator (int ch) {
			if (has (UNIX_LINES)) {
				return ch == '\n';
			} else {
				return (ch == '\n' || ch == '\r' || (ch | 1) == '\u2029' || ch == '\u0085');
			}
		}
		
		/**
		 * Read the character after the next one, and advance the cursor by two.
		 */
		public int skip () {
			int i = cursor;
			int ch = temp[i + 1];
			cursor = i + 2;
			return ch;
		}
		
		/**
		 * Unread one next character, and retreat cursor by one.
		 */
		public void unread () {
			cursor--;
		}
		
		/**
		 * Internal method used for handling all syntax errors. The pattern is displayed
		 * with a pointer to aid in locating the syntax error.
		 */
		public PatternSyntaxException error (String s) {
			return error (s, cursor - 1);
		}
		
		public PatternSyntaxException error (String s, int index) {
			return new PatternSyntaxException (s, normalizedPattern, index);
		}
		
		/**
		 * Determines if there is any supplementary character or unpaired surrogate in
		 * the specified range.
		 */
		public boolean findSupplementary (int start, int end) {
			for (int i = start; i < end; i++) {
				if (isSupplementary (temp[i]))
					return true;
			}
			return false;
		}
		
		/**
		 * Determines if the specified code point is a supplementary character or
		 * unpaired surrogate.
		 */
		public static boolean isSupplementary (int ch) {
			return ch >= Character.MIN_SUPPLEMENTARY_CODE_POINT || Character.isSurrogate ((char) ch);
		}
		
		/**
		 * The following methods handle the main parsing. They are sorted according to
		 * their precedence order, the lowest one first.
		 */
		
		/**
		 * The expression is parsed with branch nodes added for alternations. This may
		 * be called recursively to parse sub expressions that may contain alternations.
		 */
		public Node expr (Node end) {
			Node prev = null;
			Node firstTail = null;
			Branch branch = null;
			Node branchConn = null;
			
			for (;;) {
				Node node = sequence (end);
				Node nodeTail = root; // double return
				if (prev == null) {
					prev = node;
					firstTail = nodeTail;
				} else {
					// Branch
					if (branchConn == null) {
						branchConn = new BranchConn ();
						branchConn.setNext (end);
					}
					if (node == end) {
						// if the node returned from sequence() is "end"
						// we have an empty expr, set a null atom into
						// the branch to indicate to go "next" directly.
						node = null;
					} else {
						// the "tail.next" of each atom goes to branchConn
						nodeTail.setNext (branchConn);
					}
					if (prev == branch) {
						branch.add (node);
					} else {
						if (prev == end) {
							prev = null;
						} else {
							// replace the "end" with "branchConn" at its tail.next
							// when put the "prev" into the branch as the first
							// atom.
							firstTail.setNext (branchConn);
						}
						prev = branch = new Branch (prev, node, branchConn);
					}
				}
				if (peek () != '|') {
					return prev;
				}
				next ();
			}
		}
		
		public Node expr2 (Node end) {
			Node node = sequence (end);
			if (peek () != '|') {
				root = null;
				return node;
			}
			next ();
			root = sequence (end);
			return node;
		}
		
		/*
		 * Parsing of sequences between alternations.
		 */
		public Node sequence (Node end) {
			
			Node head = null;
			Node tail = null;
			Node node;
			
				LOOP:
			for (;;) {
				int ch = peek ();
				switch (ch) {
					case '(':
						// Because group handles its own closure,
						// we need to treat it differently
						node = group0 ();
						// Check for comment or flag group
						if (node == null)
							continue;
						if (head == null)
							head = node;
						else
							tail.setNext (node);
						// Double return: Tail was returned in root
						tail = root;
						continue;
					case '[':
						node = clazz (true);
						break;
					case '\\':
						ch = nextEscaped ();
						if (ch == 'p' || ch == 'P') {
							boolean oneLetter = true;
							boolean comp = (ch == 'P');
							ch = next (); // Consume { if present
							if (ch != '{') {
								unread ();
							} else {
								oneLetter = false;
							}
							node = family (oneLetter, comp);
						} else if (ch == 'c' || ch == 'C') {
							next ();
							accept ('{', "Expected {pluginName}"); // Consume {
							node = custom ();
						} else {
							unread ();
							node = atom ();
						}
						break;
					case '^':
						next ();
						if (has (MULTILINE)) {
							if (has (UNIX_LINES))
								node = new UnixCaret ();
							else
								node = new Caret ();
						} else {
							node = new Begin ();
						}
						break;
					case '$':
						next ();
						if (has (UNIX_LINES))
							node = new UnixDollar (has (MULTILINE));
						else
							node = new Dollar (has (MULTILINE));
						break;
					case '.':
						next ();
						if (has (DOTALL)) {
							node = new All ();
						} else {
							if (has (UNIX_LINES))
								node = new UnixDot ();
							else {
								node = new Dot ();
							}
						}
						break;
					case '|':
					case ')':
						break LOOP;
					case ']': // Now interpreting dangling ] and } as literals
					case '}':
						node = atom ();
						break;
					case '?':
					case '*':
					case '+':
						next ();
						throw error ("Dangling meta character '" + ((char) ch) + "'");
					case 0:
						if (cursor >= patternLength) {
							break LOOP;
						}
						// Fall through
					default:
						node = atom ();
						break;
				}
				
				node = closure (node, node);
				
				if (head == null) {
					head = tail = node;
				} else {
					tail.setNext (node);
					tail = node;
				}
			}
			if (head == null) {
				return end;
			}
			tail.setNext (end);
			root = tail; // double return
			return head;
		}
		
		/*
		 * Parse and add a new Single or Slice.
		 */
		public Node atom () {
			
			int first = 0;
			int prev = -1;
			
			boolean hasSupplementary = false;
			int ch = peek ();
			
			for (;;) {
				
				switch (ch) {
					case '*':
					case '+':
					case '?':
					case '{':
						if (first > 1) {
							cursor = prev; // Unwind one character
							first--;
						}
						break;
					case '$':
					case '.':
					case '^':
					case '(':
					case '[':
					case '|':
					case ')':
						break;
					case '\\':
						ch = nextEscaped ();
						if (ch == 'p' || ch == 'P') { // Property
							if (first > 0) { // Slice is waiting; handle it first
								unread ();
								break;
							} else { // No slice; just return the family node
								boolean comp = (ch == 'P');
								boolean oneLetter = true;
								ch = next (); // Consume { if present
								if (ch != '{')
									unread ();
								else
									oneLetter = false;
								return family (oneLetter, comp);
							}
						}
						unread ();
						prev = cursor;
						ch = escape (false, first == 0, false);
						if (ch >= 0) {
							append (ch, first);
							first++;
							if (isSupplementary (ch)) {
								hasSupplementary = true;
							}
							ch = peek ();
							continue;
						} else if (first == 0) {
							return root;
						}
						// Unwind meta escape sequence
						cursor = prev;
						break;
					case 0:
						if (cursor >= patternLength) {
							break;
						}
						// Fall through
					default:
						prev = cursor;
						append (ch, first);
						first++;
						if (isSupplementary (ch)) {
							hasSupplementary = true;
						}
						ch = next ();
						continue;
				}
				
				break;
				
			}
			
			if (first == 1)
				return newSingle (buffer[0]);
			else
				return newSlice (buffer, first, hasSupplementary);
			
		}
		
		public void append (int ch, int len) {
			
			if (len >= buffer.length) {
				
				int[] tmp = new int[len + len];
				System.arraycopy (buffer, 0, tmp, 0, len);
				buffer = tmp;
				
			}
			
			buffer[len] = ch;
			
		}
		
		/**
		 * Parses a backref greedily, taking as many numbers as it can. The first digit
		 * is always treated as a backref, but multi digit numbers are only treated as a
		 * backref if at least that many backrefs exist at this point in the regex.
		 */
		public Node ref (int refNum) {
			
			boolean done = false;
			
			while (!done) {
				
				int ch = peek ();
				
				switch (ch) {
					
					case '0':
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
					case '8':
					case '9':
						int newRefNum = (refNum * 10) + (ch - '0');
						// Add another number if it doesn't make a group
						// that doesn't exist
						if (capturingGroupCount - 1 < newRefNum) {
							done = true;
							break;
						}
						refNum = newRefNum;
						read ();
						break;
					default:
						done = true;
						break;
						
				}
				
			}
			
			if (!isGroupDefined (refNum)) {
				
				int index = cursor - 1;
				int groupNumber = refNum;
				
				groupExistsChecks ().add (() -> {
					
					if (!isGroupDefined (groupNumber))
						throw error ("Backreference to non-existent capturing group " + groupNumber, index);
					
				});
				
			}
			
			if (has (CASE_INSENSITIVE))
				return new CIBackRef (refNum, has (UNICODE_CASE));
			else
				return new BackRef (refNum);
			
		}
		
		/**
		 * Parses an escape sequence to determine the actual value that needs to be
		 * matched. If -1 is returned and create was true a new object was added to the
		 * tree to handle the escape sequence. If the returned value is greater than
		 * zero, it is the value that matches the escape sequence.
		 */
		public int escape (boolean inClass, boolean create, boolean isRange) {
			
			int ch = skip ();
			
			switch (ch) {
				
				case '0':
					return o ();
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					if (inClass)
						break;
					if (create) {
						root = ref (ch - '0');
					}
					return -1;
				case 'A':
					if (inClass)
						break;
					if (create)
						root = new Begin ();
					return -1;
				case 'B':
					if (inClass)
						break;
					if (create)
						root = new Bound (Bound.NONE, has (UNICODE_CHARACTER_CLASS));
					return -1;
				case 'C':
					break;
				case 'D':
					if (create)
						root = has (UNICODE_CHARACTER_CLASS) ? new Utype (UnicodeProp.DIGIT).complement ()
							       : new Ctype (ASCII.DIGIT).complement ();
					return -1;
				case 'E':
				case 'F':
					break;
				case 'G':
					if (inClass)
						break;
					if (create)
						root = new LastMatch ();
					return -1;
				case 'H':
					if (create)
						root = new HorizWS ().complement ();
					return -1;
				case 'I':
				case 'J':
				case 'K':
				case 'L':
				case 'M':
				case 'N':
				case 'O':
				case 'P':
				case 'Q':
					break;
				
				case 'R':
					if (inClass)
						break;
					if (create)
						root = new LineEnding ();
					return -1;
				
				case 'S':
					if (create)
						root = has (UNICODE_CHARACTER_CLASS) ? new Utype (UnicodeProp.WHITE_SPACE).complement ()
							       : new Ctype (ASCII.SPACE).complement ();
					return -1;
				
				case 'T':
				case 'U':
					break;
				
				case 'V':
					if (create)
						root = new VertWS ().complement ();
					return -1;
				
				case 'W':
					if (create)
						root = has (UNICODE_CHARACTER_CLASS) ? new Utype (UnicodeProp.WORD).complement ()
							       : new Ctype (ASCII.WORD).complement ();
					return -1;
				
				case 'X':
				case 'Y':
					break;
				
				case 'Z':
					if (inClass)
						break;
					if (create) {
						if (has (UNIX_LINES))
							root = new UnixDollar (false);
						else
							root = new Dollar (false);
					}
					return -1;
				
				case 'a':
					return '\007';
				case 'b':
					if (inClass)
						break;
					if (create)
						root = new Bound (Bound.BOTH, has (UNICODE_CHARACTER_CLASS));
					return -1;
				case 'c':
					return c ();
				case 'd':
					if (create)
						root = has (UNICODE_CHARACTER_CLASS) ? new Utype (UnicodeProp.DIGIT) : new Ctype (ASCII.DIGIT);
					return -1;
				case 'e':
					return '\033';
				case 'f':
					return '\f';
				case 'g':
					break;
				case 'h':
					if (create)
						root = new HorizWS ();
					return -1;
				case 'i':
				case 'j':
					break;
				case 'k':
					if (inClass)
						break;
					if (read () != '<')
						throw error ("\\k is not followed by '<' for named capturing group");
					final String name = groupname (read ());
					
					if (create) {
						if (isGroupDefined (name)) {
							if (has (CASE_INSENSITIVE))
								root = new CIBackRef (groupIndices ().get (name), has (UNICODE_CASE));
							else
								root = new BackRef (groupIndices ().get (name));
						} else {
							final BackRefBase brb;
							if (has (CASE_INSENSITIVE))
								root = brb = new CIBackRef (has (UNICODE_CASE));
							else
								root = brb = new BackRef ();
							
							int index = cursor - 1;
							groupExistsChecks ().add (() -> {
								if (!isGroupDefined (name))
									throw error ("Backreference to non-existent named capturing group '" + name + "'", index);
								brb.groupIndex = groupIndices ().get (name);
							});
						}
					}
					return -1;
				case 'l':
				case 'm':
					break;
				case 'n':
					return '\n';
				case 'o':
				case 'p':
				case 'q':
					break;
				case 'r':
					return '\r';
				case 's':
					if (create)
						root = has (UNICODE_CHARACTER_CLASS) ? new Utype (UnicodeProp.WHITE_SPACE) : new Ctype (ASCII.SPACE);
					return -1;
				case 't':
					return '\t';
				case 'u':
					return u ();
				case 'v':
					// '\v' was implemented as VT/0x0B in releases < 1.8 (though
					// undocumented). In JDK8 '\v' is specified as a predefined
					// character class for all vertical whitespace characters.
					// So [-1, root=VertWS node] pair is returned (instead of a
					// single 0x0B). This breaks the range if '\v' is used as
					// the start or end value, such as [\v-...] or [...-\v], in
					// which a single definite value (0x0B) is expected. For
					// compatibility concern '\013'/0x0B is returned if isRange.
					if (isRange)
						return '\013';
					if (create)
						root = new VertWS ();
					return -1;
				case 'w':
					if (create)
						root = has (UNICODE_CHARACTER_CLASS) ? new Utype (UnicodeProp.WORD) : new Ctype (ASCII.WORD);
					return -1;
				case 'x':
					return x ();
				case 'y':
					break;
					
				case 'z': {
					
					if (inClass)
						break;
					
					if (create)
						root = new End ();
					
					return -1;
					
				}
				
				default:
					return ch;
					
			}
			
			throw error ("Illegal/unsupported escape sequence");
			
		}
		
		/**
		 * Parse a character class, and return the node that matches it.
		 * <p>
		 * Consumes a ] on the way out if consume is true. Usually consume is true
		 * except for the case of [abc&&def] where def is a separate right hand node
		 * with "understood" brackets.
		 */
		public CharProperty clazz (boolean consume) {
			
			CharProperty prev = null;
			CharProperty node = null;
			
			BitClass bits = new BitClass ();
			
			boolean include = true;
			boolean firstInClass = true;
			
			int ch = next ();
			
			for (;;) {
				
				switch (ch) {
					
					case '^': {
						
						// Negates if first char in a class, otherwise literal
						if (firstInClass) {
							if (temp[cursor - 1] != '[')
								break;
							ch = next ();
							include = !include;
							continue;
						} else {
							// ^ not first in class, treat as literal
							break;
						}
						
					}
					
					case '[': {
						
						firstInClass = false;
						node = clazz (true);
						if (prev == null)
							prev = node;
						else
							prev = union (prev, node);
						ch = peek ();
						continue;
						
					}
					
					case '&': {
						firstInClass = false;
						ch = next ();
						if (ch == '&') {
							ch = next ();
							CharProperty rightNode = null;
							while (ch != ']' && ch != '&') {
								if (ch == '[') {
									if (rightNode == null)
										rightNode = clazz (true);
									else
										rightNode = union (rightNode, clazz (true));
								} else { // abc&&def
									unread ();
									rightNode = clazz (false);
								}
								ch = peek ();
							}
							if (rightNode != null)
								node = rightNode;
							if (prev == null) {
								if (rightNode == null)
									throw error ("Bad class syntax");
								else
									prev = rightNode;
							} else {
								prev = intersection (prev, node);
							}
						} else {
							// treat as a literal &
							unread ();
							break;
						}
						
						continue;
						
					}
					
					case 0: {
						
						firstInClass = false;
						
						if (cursor >= patternLength)
							throw error ("Unclosed character class");
						
						break;
						
					}
					
					case ']': {
						
						firstInClass = false;
						
						if (prev != null) {
							
							if (consume) next ();
							
							return prev;
							
						}
						
						break;
						
					}
					
					default:
						firstInClass = false;
						break;
						
				}
				
				node = range (bits);
				
				if (include) {
					
					if (prev == null) {
						prev = node;
					} else {
						
						if (prev != node)
							prev = union (prev, node);
					}
					
				} else {
					
					if (prev == null) {
						prev = node.complement ();
					} else {
						
						if (prev != node)
							prev = setDifference (prev, node);
					}
					
				}
				
				ch = peek ();
				
			}
			
		}
		
		public CharProperty bitsOrSingle (BitClass bits, int ch) {
			
			/*
			 * Bits can only handle codepoints in [u+0000-u+00ff] range. Use "single" node
			 * instead of bits when dealing with unicode case folding for codepoints listed
			 * below. (1)Uppercase out of range: u+00ff, u+00b5 toUpperCase(u+00ff) ->
			 * u+0178 toUpperCase(u+00b5) -> u+039c (2)LatinSmallLetterLongS u+17f
			 * toUpperCase(u+017f) -> u+0053 (3)LatinSmallLetterDotlessI u+131
			 * toUpperCase(u+0131) -> u+0049 (4)LatinCapitalLetterIWithDotAbove u+0130
			 * toLowerCase(u+0130) -> u+0069 (5)KelvinSign u+212a toLowerCase(u+212a) ==>
			 * u+006B (6)AngstromSign u+212b toLowerCase(u+212b) ==> u+00e5
			 */
			if (
				ch < 256 &&
					!(has (CASE_INSENSITIVE) && has (UNICODE_CASE) &&
						  (
							  ch == 0xff || ch == 0xb5 || ch == 0x49 || ch == 0x69 || // I and i
								  ch == 0x53 || ch == 0x73 || // S and s
								  ch == 0x4b || ch == 0x6b || // K and k
								  ch == 0xc5 || ch == 0xe5
						  )
					)
			) // A+ring
				return bits.add (ch, flags ());
			
			return newSingle (ch);
			
		}
		
		/**
		 * Parse a single character or a character range in a character class and return
		 * its representative node.
		 */
		public CharProperty range (BitClass bits) {
			
			int ch = peek ();
			
			if (ch == '\\') {
				
				ch = nextEscaped ();
				
				if (ch == 'p' || ch == 'P') { // A property
					
					boolean comp = (ch == 'P');
					boolean oneLetter = true;
					// Consume { if present
					ch = next ();
					if (ch != '{')
						unread ();
					else
						oneLetter = false;
					return family (oneLetter, comp);
					
				} else { // ordinary escape
					
					boolean isrange = temp[cursor + 1] == '-';
					unread ();
					
					ch = escape (true, true, isrange);
					
					if (ch == -1)
						return (CharProperty) root;
					
				}
				
			} else next ();
			
			if (ch >= 0) {
				
				if (peek () == '-') {
					
					int endRange = temp[cursor + 1];
					if (endRange == '[') {
						return bitsOrSingle (bits, ch);
					}
					if (endRange != ']') {
						next ();
						int m = peek ();
						if (m == '\\') {
							m = escape (true, false, true);
						} else {
							next ();
						}
						if (m < ch) {
							throw error ("Illegal character range");
						}
						if (has (CASE_INSENSITIVE))
							return caseInsensitiveRangeFor (ch, m);
						else
							return rangeFor (ch, m);
					}
					
				}
				
				return bitsOrSingle (bits, ch);
				
			}
			
			throw error ("Unexpected character '" + ((char) ch) + "'");
			
		}
		
		public CustomNode custom () {
			
			int i = cursor;
			
			mark ('}');
			
			while (read () != '}') {
			}
			mark ('\000');
			int j = cursor;
			if (j > patternLength)
				throw error ("Unclosed custom plugin");
			if (i + 1 >= j)
				throw error ("Empty character family");
			String[] name = new String (temp, i, j - i - 1).split (",");
			
			Class<? extends CustomNode> clazz = plugins.get (name[0]);
			if (clazz == null)
				throw error ("Plugin " + name[0] + " hasn't been installed!");
			try {
				Class<?>[] parameterTypes = new Class[name.length - 1];
				String[] parameters = new String[name.length - 1];
				for (int k = 0; k < parameterTypes.length; ++k) {
					parameterTypes[k] = String.class;
					parameters[k] = name[k + 1];
				}
				Constructor<? extends CustomNode> constructor = clazz.getConstructor (parameterTypes);
				return constructor.newInstance (parameters);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				         | NoSuchMethodException | SecurityException e) {
				throw error ("Plugin " + name + " can't be used!");
			}
			
		}
		
		/**
		 * Parses a Unicode character family and returns its representative node.
		 */
		public CharProperty family (boolean singleLetter, boolean maybeComplement) {
			
			next ();
			String name;
			CharProperty node = null;
			
			if (singleLetter) {
				int c = temp[cursor];
				if (!Character.isSupplementaryCodePoint (c)) {
					name = String.valueOf ((char) c);
				} else {
					name = new String (temp, cursor, 1);
				}
				read ();
			} else {
				int i = cursor;
				mark ('}');
				while (read () != '}') {
				}
				mark ('\000');
				int j = cursor;
				if (j > patternLength)
					throw error ("Unclosed character family");
				if (i + 1 >= j)
					throw error ("Empty character family");
				name = new String (temp, i, j - i - 1);
			}
			
			int i = name.indexOf ('=');
			if (i != -1) {
				// property construct \p{name=value}
				String value = name.substring (i + 1);
				name = name.substring (0, i).toLowerCase (Locale.ENGLISH);
				switch (name) {
					case "sc": case "script":
						node = unicodeScriptPropertyFor (value);
						break;
					case "blk": case "block":
						node = unicodeBlockPropertyFor (value);
						break;
					case "gc": case "general_category":
						node = charPropertyNodeFor (value);
						break;
					default:
						throw error ("Unknown Unicode property {name=<" + name + ">, " + "value=<" + value + ">}");
				}
				
			} else {
				
				if (name.startsWith ("In")) {
					// \p{inBlockName}
					node = unicodeBlockPropertyFor (name.substring (2));
				} else if (name.startsWith ("Is")) {
					// \p{isGeneralCategory} and \p{isScriptName}
					name = name.substring (2);
					UnicodeProp uprop = UnicodeProp.forName (name);
					if (uprop != null)
						node = new Utype (uprop);
					if (node == null)
						node = CharPropertyNames.charPropertyFor (name);
					if (node == null)
						node = unicodeScriptPropertyFor (name);
					
				} else {
					
					if (has (UNICODE_CHARACTER_CLASS)) {
						
						UnicodeProp uprop = UnicodeProp.forPOSIXName (name);
						if (uprop != null)
							node = new Utype (uprop);
						
					}
					
					if (node == null)
						node = charPropertyNodeFor (name);
					
				}
				
			}
			
			if (maybeComplement) {
				
				if (node instanceof Category || node instanceof Block)
					hasSupplementary = true;
				
				node = node.complement ();
				
			}
			
			return node;
			
		}
		
		/**
		 * Returns a CharProperty matching all characters belong to a UnicodeScript.
		 */
		public CharProperty unicodeScriptPropertyFor (String name) {
			final Character.UnicodeScript script;
			try {
				script = Character.UnicodeScript.forName (name);
			} catch (IllegalArgumentException iae) {
				throw error ("Unknown character script name {" + name + "}");
			}
			return new Script (script);
		}
		
		/**
		 * Returns a CharProperty matching all characters in a UnicodeBlock.
		 */
		public CharProperty unicodeBlockPropertyFor (String name) {
			final Character.UnicodeBlock block;
			try {
				block = Character.UnicodeBlock.forName (name);
			} catch (IllegalArgumentException iae) {
				throw error ("Unknown character block name {" + name + "}");
			}
			return new Block (block);
		}
		
		/**
		 * Returns a CharProperty matching all characters in a named property.
		 */
		public CharProperty charPropertyNodeFor (String name) {
			CharProperty p = CharPropertyNames.charPropertyFor (name);
			if (p == null)
				throw error ("Unknown character property name {" + name + "}");
			return p;
		}
		
		static boolean isCharOfGroupname (int ch) {
			return ASCII.isLower (ch) || ASCII.isUpper (ch) || ASCII.isDigit (ch) || ASCII.isUnderscore (ch);
		}
		
		/**
		 * Parses and returns the name of a "named capturing group", the trailing ">" is
		 * consumed after parsing.
		 */
		public String groupname (int ch) {
			StringBuilder sb = new StringBuilder ();
			sb.append (Character.toChars (ch));
			while (isCharOfGroupname (ch = read ())) {
				sb.append (Character.toChars (ch));
			}
			if (sb.length () == 0)
				throw error ("Named capturing group has zero-length name");
			if (ch != '>')
				throw error ("Named capturing group is missing trailing '>'");
			return sb.toString ();
		}
		
		static boolean isFirstCharOfGroupname (int ch) {
			return ASCII.isLower (ch) || ASCII.isUpper (ch);
		}
		
		/**
		 * Parses a group and returns the head node of a set of nodes that process the
		 * group. Sometimes a double return system is used where the tail is returned in
		 * root.
		 */
		protected Node group0 () {
			Node head;
			Node tail;
			int save = flags;
			boolean ila = inLookaround;
			root = null;
			int ch = next ();
			if (ch == '?') {
				ch = next ();
				read ();
				switch (ch) {
					case ':': // (?:xxx) pure group
						head = createGroup (true);
						tail = root;
						head.setNext (expr (tail));
						break;
					case '=': // (?=xxx) and (?!xxx) lookahead
					case '!':
						head = createGroup (true);
						tail = root;
						inLookaround = true;
						head.setNext (expr (tail));
						inLookaround = ila;
						if (ch == '=') {
							head = tail = new Pos (head);
						} else {
							head = tail = new Neg (head);
						}
						break;
					case '>': // (?>xxx) independent group
						head = tail = new AtomicGroup (expr (accept));
						break;
					case '<': { // (?<xxx) look behind
						ch = read ();
						if (isFirstCharOfGroupname (ch)) {
							// named captured group
							final String name = groupname (ch);
							if (groupIndices ().containsKey (name))
								throw error ("Named capturing group <" + name + "> is already defined");
							head = createGroup (false);
							tail = root;
							int group = capturingGroupCount - 1;
							groupIndices ().put (name, group);
							groupNames ().put (group, name);
							head.setNext (expr (tail));
							final RecursiveGroupCall rgc = new RecursiveGroupCall (this, group, false, inLookaround);
							groupCalledRecursivelyChecks ().add (() -> {
								if (recursivelyCalledGroups ().contains (group) || recursivelyCalledGroups ().contains (name) || rgc.getPrevious () == null)
									return;
								GroupHeadAndTail ghat = groupHeadAndTailNodes ().get (group);
								ghat.groupTail.setNext (rgc.getNext ());
								rgc.getPrevious ().setNext (ghat.groupHead);
							});
							head = tail = rgc;
							break;
						}
						int start = cursor;
						head = createGroup (true);
						tail = root;
						inLookaround = true;
						head.setNext (expr (tail));
						inLookaround = ila;
						tail.setNext (lookbehindEnd);
						
						boolean hasSupplementary = findSupplementary (start, patternLength);
						final BehindBase bb;
						final Node cond = head;
						if (ch == '=') {
							bb = (hasSupplementary ? new BehindS (cond) : new Behind (cond));
						} else if (ch == '!') {
							bb = (hasSupplementary ? new NotBehindS (cond) : new NotBehind (cond));
						} else {
							throw error ("Unknown look-behind group", cursor - 2);
						}
						
						lookbehindHasMaxChecks ().add (() -> {
							
							TreeInfo info = new TreeInfo ();
							cond.study (info);
							
							if (!info.maxValid)
								throw error ("Look-behind group does not have an obvious maximum length");
							
							bb.setMinMaxLength (info.maxLength, info.minLength);
							
						});
						
						head = tail = bb;
						
						break;
						
					}
					
					case '(': { // (?(groupNumber)yes|no) or (?(DEFINE)regex that's not executed)
						
						if (doesDEFINEFollow ()) {
							expr (accept);
							accept (')', "Unclosed DEFINE construct");
							return null;
						}
						
						Conditional conditional;
						
						int groupNumber;
						String groupName;
						
						if ((groupNumber = doesGroupNumberFollowBefore (')')) != -1) {
							conditional = new ConditionalGP (groupNumber);
							if (!isGroupDefined (groupNumber)) {
								int index = cursor - 1;
								groupExistsChecks ().add (() -> {
									if (!isGroupDefined (groupNumber))
										throw error ("capturing group < " + groupNumber + " > does not exist", index);
									
								});
							}
						} else if ((groupName = doesGroupNameFollowBefore (')')) != null) {
							if (isGroupDefined (groupName)) {
								conditional = new ConditionalGP (groupIndices.get (groupName));
							} else {
								ConditionalGP cgp = new ConditionalGP ();
								int index = cursor - 1;
								final String gn = groupName;
								groupExistsChecks ().add (() -> {
									if (!isGroupDefined (gn))
										throw error ("named capturing group < " + gn + " > does not exist", index);
									cgp.groupNumber = groupIndices.get (gn);
								});
								conditional = cgp;
							}
						} else {
							accept ('?', "Unkown condition");
							accept ('=', "Unkown condition");
							Pos pos = new Pos (expr (accept));
							accept (')', "Unclosed condition");
							conditional = new ConditionalLookahead (pos);
						}
						
						head = createGroup (true);// Conditionals are really uncaptured groups
						tail = root;
						conditional.setYes (expr2 (tail));
						conditional.setNot (root);
						head.setNext (conditional);
						head.getNext ().setNext (tail);
						
						break;
						
					}
					
					case '$':
					case '@':
						throw error ("Unknown group type");
					default: // (?xxx:) inlined match flags or (?digit) recursive group call
						unread ();
						int groupNumber;
						if (peek () == '\'') {
							read ();
							final String groupName;
							if ((groupName = doesGroupNameFollowBefore ('\'')) == null || peek () != ')')
								throw error ("Unknown recursion syntax");
							recursivelyCalledGroups ().add (groupName);
							if (isGroupDefined (groupName)) {
								head = tail = new RecursiveGroupCall (this, groupIndices.get (groupName), true, inLookaround);
							} else {
								RecursiveGroupCall rcg = new RecursiveGroupCall (this, true, inLookaround);
								int index = cursor - 1;
								groupExistsChecks ().add (0, () -> {
									if (!isGroupDefined (groupName))
										throw error ("Recursion to non-existent named capturing group '" + groupName + "'", index);
									rcg.setGroupNumber (groupIndices.get (groupName));
								});
								head = tail = rcg;
								
							}
							
						} else if ((groupNumber = doesGroupNumberFollowBefore (')')) != -1) {
							unread ();
							recursivelyCalledGroups ().add (groupNumber);
							if (isGroupDefined (groupNumber))
								head = tail = new RecursiveGroupCall (this, groupNumber, true, inLookaround);
							else {
								final RecursiveGroupCall rgc = new RecursiveGroupCall (this, true, inLookaround);
								head = tail = rgc;
								int index = cursor - 1;
								groupExistsChecks ().add (0, () -> {
									if (!isGroupDefined (groupNumber))
										throw error ("Recursion to non-existent capturing group " + groupNumber, index);
									rgc.setGroupNumber (groupNumber);
									
								});
							}
						} else {
							addFlag ();
							ch = read ();
							if (ch == ')') {
								return null; // Inline modifier only
							}
							if (ch != ':') {
								throw error ("Unknown inline modifier");
							}
							head = createGroup (true);
							tail = root;
							head.setNext (expr (tail));
							break;
						}
				}
			} else { // (xxx) a regular group
				
				head = createGroup (false);
				tail = root;
				
				int groupNumber = capturingGroupCount - 1;
				head.setNext (expr (tail));
				
				final RecursiveGroupCall rgc = new RecursiveGroupCall (this, groupNumber, false, inLookaround);
				
				groupCalledRecursivelyChecks ().add (() -> {
					
					if (recursivelyCalledGroups ().contains (groupNumber))
						return;
					
					GroupHeadAndTail ghat = groupHeadAndTailNodes ().get (groupNumber);
					ghat.groupTail.setNext (rgc.getNext ());
					rgc.getPrevious ().setNext (ghat.groupHead);
					
				});
				
				head = tail = rgc;
				
			}
			
			accept (')', "Unclosed group");
			flags = save;
			
			// Check for quantifiers
			Node node = closure (head, tail);
			if (node == head) { // No closure
				root = tail;
				return node; // Dual return
			}
			if (head == tail) { // Zero length assertion
				root = node;
				return node; // Dual return
			}
			
			root = node;
			
			return node;
			
		}
		
		public String doesGroupNameFollowBefore (int closing) {
			int ch = peek ();
			int save = cursor;
			if (isFirstCharOfGroupname (ch)) {
				StringBuilder sb = new StringBuilder ();
				while (isCharOfGroupname (ch = read ())) {
					sb.append (Character.toChars (ch));
				}
				if (ch != closing) {
					cursor = save;
					return null;
				}
				return sb.toString ();
			}
			return null;
		}
		
		public boolean isGroupDefined (String groupName) {
			return groupIndices ().containsKey (groupName);
		}
		
		public boolean doesDEFINEFollow () {
			int save = cursor;
			peek ();
			int[] define = new int[] {'D', 'E', 'F', 'I', 'N', 'E'};
			for (int j : define) {
				if (cursor >= temp.length || temp[cursor++] != j) {
					cursor = save;
					return false;
				}
			}
			accept (')', "Expected ) after DEFINE!");
			return true;
		}
		
		public int doesGroupNumberFollowBefore (int closing) {
			int number = 0;
			int ch;
			int save = cursor;
			while (ASCII.isDigit (ch = read ())) {
				number = number * 10 + ch - '0';
			}
			if (number <= 0 || ch != closing) {
				cursor = save;
				return -1;
			}
			return number;
		}
		
		public boolean isGroupDefined (int groupNumber) {
			return groupNumber < capturingGroupCount;
		}
		
		/**
		 * Create group head and tail nodes using double return. If the group is created
		 * with anonymous true then it is a pure group and should not affect group
		 * counting.
		 */
		public Node createGroup (boolean anonymous) {
			int localIndex = localCount++;
			int groupIndex = 0;
			if (!anonymous)
				groupIndex = capturingGroupCount++;
			GroupHead head = new GroupHead (localIndex, groupIndex, inLookaround);
			GroupTail tail = new GroupTail (localIndex, groupIndex);
			root = tail;
			if (!anonymous) {
				groupHeadAndTailNodes ().add (new GroupHeadAndTail (head, tail));
			}
			return head;
		}
		
		public ArrayList<GroupHeadAndTail> groupHeadAndTailNodes () {
			if (groupHeadAndTailNodes == null) {
				groupHeadAndTailNodes = new ArrayList<GroupHeadAndTail> (10);
				groupHeadAndTailNodes.add (null);
			}
			return groupHeadAndTailNodes;
		}
		
		/*
		 * Parses inlined match flags and set them appropriately.
		 */
		public void addFlag () {
			
			int ch = peek ();
			
			for (;;) {
				
				switch (ch) {
					
					case 'i':
						flags |= CASE_INSENSITIVE;
						break;
					case 'm':
						flags |= MULTILINE;
						break;
					case 's':
						flags |= DOTALL;
						break;
					case 'd':
						flags |= UNIX_LINES;
						break;
					case 'u':
						flags |= UNICODE_CASE;
						break;
					case 'x':
						flags |= COMMENTS;
						break;
					case 'U':
						flags |= (UNICODE_CHARACTER_CLASS | UNICODE_CASE);
						break;
					case '-': // subFlag then fall through
						ch = next ();
						subFlag ();
					default:
						return;
						
				}
				
				ch = next ();
				
			}
			
		}
		
		/*
		 * Parses the second part of inlined match flags and turns off flags
		 * appropriately.
		 */
		public void subFlag () {
			
			int ch = peek ();
			
			for (;;) {
				
				switch (ch) {
					
					case 'i':
						flags &= ~CASE_INSENSITIVE;
						break;
					case 'm':
						flags &= ~MULTILINE;
						break;
					case 's':
						flags &= ~DOTALL;
						break;
					case 'd':
						flags &= ~UNIX_LINES;
						break;
					case 'u':
						flags &= ~UNICODE_CASE;
						break;
					case 'x':
						flags &= ~COMMENTS;
						break;
					case 'U':
						flags &= ~(UNICODE_CHARACTER_CLASS | UNICODE_CASE);
					default:
						return;
						
				}
				
				ch = next ();
				
			}
			
		}
		
		public static final int MAX_REPS = 0x7FFFFFFF;
		
		public static final int GREEDY = 0;
		public static final int LAZY = 1;
		public static final int POSSESSIVE = 2;
		
		public Navigator createNavigator (Node endNode) {
			
			if (endNode instanceof Navigator)
				return (Navigator) endNode;
			
			Navigator nav = new Navigator (localCount++);
			
			nav.setNext (endNode.getNext ());
			
			endNode.setNext (nav);
			
			return nav;
			
		}
		
		public boolean isDeterministic (Node node) {
			TreeInfo info = new TreeInfo ();
			node.study (info);
			return info.deterministic;
		}
		
		public int getType (int modifier) {
			if (modifier == '?') {
				next ();
				return LAZY;
			} else if (modifier == '+') {
				next ();
				return POSSESSIVE;
			}
			return GREEDY;
		}
		
		/**
		 * Processes repetition. If the next character peeked is a quantifier then new
		 * nodes must be appended to handle the repetition. Prev could be a single or a
		 * group, so it could be a chain of nodes.
		 */
		public Node closure (Node beginNode, Node endNode) {
			
			int ch = peek ();
			int cmin, cmax, type;
			
			switch (ch) {
				case '?':
					cmin = 0;
					cmax = 1;
					ch = next ();
					type = getType (ch);
					break;
				case '*':
					cmin = 0;
					cmax = MAX_REPS;
					ch = next ();
					type = getType (ch);
					break;
				case '+':
					ch = next ();
					cmin = 1;
					cmax = MAX_REPS;
					type = getType (ch);
					break;
				case '{':
					ch = temp[cursor + 1];
					if (ASCII.isDigit (ch)) {
						skip ();
						cmin = 0;
						do {
							cmin = cmin * 10 + (ch - '0');
						} while (ASCII.isDigit (ch = read ()));
						cmax = cmin;
						if (ch == ',') {
							ch = read ();
							cmax = MAX_REPS;
							if (ch != '}') {
								cmax = 0;
								while (ASCII.isDigit (ch)) {
									cmax = cmax * 10 + (ch - '0');
									ch = read ();
								}
							}
						}
						if (ch != '}')
							throw error ("Unclosed counted closure");
						if (((cmin) | (cmax) | (cmax - cmin)) < 0)
							throw error ("Illegal repetition range");
						ch = peek ();
						type = getType (ch);
						break;
					} else {
						throw error ("Illegal repetition");
					}
				default:
					return beginNode;
			}
			
			final CurlyBase cb = new CurlyBase (this, beginNode, cmin, cmax, type);
			
			curlyDeterministicChecks ().add (() -> {
				CurlyBase curly;
				if (isDeterministic (cb.beginNode)) {
					curly = new DeterministicCurly (this, cb.beginNode, cb.cmin, cb.cmax, cb.type);
				} else {
					curly = new Curly (this, cb.beginNode, cb.cmin, cb.cmax, cb.type);
				}
				curly.setNext (cb.getNext ());
				cb.getPrevious ().setNext (curly);
				
			});
			
			return cb;
			
		}
		
		/**
		 * Utility method for parsing control escape sequences.
		 */
		public int c () {
			if (cursor < patternLength) {
				return read () ^ 64;
			}
			throw error ("Illegal control escape sequence");
		}
		
		/**
		 * Utility method for parsing octal escape sequences.
		 */
		public int o () {
			int n = read ();
			if (((n - '0') | ('7' - n)) >= 0) {
				int m = read ();
				if (((m - '0') | ('7' - m)) >= 0) {
					int o = read ();
					if ((((o - '0') | ('7' - o)) >= 0) && (((n - '0') | ('3' - n)) >= 0)) {
						return (n - '0') * 64 + (m - '0') * 8 + (o - '0');
					}
					unread ();
					return (n - '0') * 8 + (m - '0');
				}
				unread ();
				return (n - '0');
			}
			throw error ("Illegal octal escape sequence");
		}
		
		/**
		 * Utility method for parsing hexadecimal escape sequences.
		 */
		public int x () {
			int n = read ();
			if (ASCII.isHexDigit (n)) {
				int m = read ();
				if (ASCII.isHexDigit (m)) {
					return ASCII.toDigit (n) * 16 + ASCII.toDigit (m);
				}
			} else if (n == '{' && ASCII.isHexDigit (peek ())) {
				int ch = 0;
				while (ASCII.isHexDigit (n = read ())) {
					ch = (ch << 4) + ASCII.toDigit (n);
					if (ch > Character.MAX_CODE_POINT)
						throw error ("Hexadecimal codepoint is too big");
				}
				if (n != '}')
					throw error ("Unclosed hexadecimal escape sequence");
				return ch;
			}
			throw error ("Illegal hexadecimal escape sequence");
		}
		
		/**
		 * Utility method for parsing unicode escape sequences.
		 */
		public int cursor () {
			return cursor;
		}
		
		public void setcursor (int pos) {
			cursor = pos;
		}
		
		public int uxxxx () {
			
			int n = 0;
			for (int i = 0; i < 4; i++) {
				int ch = read ();
				if (!ASCII.isHexDigit (ch)) {
					throw error ("Illegal Unicode escape sequence");
				}
				n = n * 16 + ASCII.toDigit (ch);
			}
			return n;
		}
		
		public int u () {
			
			int n = uxxxx ();
			
			if (Character.isHighSurrogate ((char) n)) {
				int cur = cursor ();
				if (read () == '\\' && read () == 'u') {
					int n2 = uxxxx ();
					if (Character.isLowSurrogate ((char) n2))
						return Character.toCodePoint ((char) n, (char) n2);
				}
				setcursor (cur);
			}
			return n;
			
		}
		
		//
		// Utility methods for code point support
		//
		
		public static int countChars (CharSequence seq, int index, int lengthInCodePoints) {
			// optimization
			if (lengthInCodePoints == 1 && !Character.isHighSurrogate (seq.charAt (index))) {
				assert (index >= 0 && index < seq.length ());
				return 1;
			}
			int length = seq.length ();
			int x = index;
			if (lengthInCodePoints >= 0) {
				assert (index >= 0 && index < length);
				for (int i = 0; x < length && i < lengthInCodePoints; i++) {
					if (Character.isHighSurrogate (seq.charAt (x++))) {
						if (x < length && Character.isLowSurrogate (seq.charAt (x))) {
							x++;
						}
					}
				}
				return x - index;
			}
			
			assert (index >= 0 && index <= length);
			if (index == 0) {
				return 0;
			}
			int len = -lengthInCodePoints;
			for (int i = 0; x > 0 && i < len; i++) {
				if (Character.isLowSurrogate (seq.charAt (--x))) {
					if (x > 0 && Character.isHighSurrogate (seq.charAt (x - 1))) {
						x--;
					}
				}
			}
			return index - x;
		}
		
		public static int countCodePoints (CharSequence seq) {
			int length = seq.length ();
			int n = 0;
			for (int i = 0; i < length; ) {
				n++;
				if (Character.isHighSurrogate (seq.charAt (i++))) {
					if (i < length && Character.isLowSurrogate (seq.charAt (i))) {
						i++;
					}
				}
			}
			return n;
		}
		
		/**
		 * Returns a suitably optimized, single character matcher.
		 */
		public CharProperty newSingle (final int ch) {
			
			if (has (CASE_INSENSITIVE)) {
				
				int lower, upper;
				
				if (has (UNICODE_CASE)) {
					
					upper = Character.toUpperCase (ch);
					lower = Character.toLowerCase (upper);
					
					if (upper != lower)
						return new SingleU (lower);
					
				} else if (ASCII.isAscii (ch)) {
					
					lower = ASCII.toLower (ch);
					upper = ASCII.toUpper (ch);
					
					if (lower != upper)
						return new SingleI (lower, upper);
					
				}
				
			}
			
			if (isSupplementary (ch))
				return new SingleS (ch); // Match a given Unicode character
			
			return new Single (ch); // Match a given BMP character
			
		}
		
		/**
		 * Utility method for creating a string slice matcher.
		 */
		public Node newSlice (int[] buf, int count, boolean hasSupplementary) {
			int[] tmp = new int[count];
			if (has (CASE_INSENSITIVE)) {
				if (has (UNICODE_CASE)) {
					for (int i = 0; i < count; i++) {
						tmp[i] = Character.toLowerCase (Character.toUpperCase (buf[i]));
					}
					return hasSupplementary ? new SliceUS (tmp) : new SliceU (tmp);
				}
				for (int i = 0; i < count; i++) {
					tmp[i] = ASCII.toLower (buf[i]);
				}
				return hasSupplementary ? new SliceIS (tmp) : new SliceI (tmp);
			}
			for (int i = 0; i < count; i++) {
				tmp[i] = buf[i];
			}
			return hasSupplementary ? new SliceS (tmp) : new Slice (tmp);
		}
		
		public static boolean inRange (int lower, int ch, int upper) {
			return lower <= ch && ch <= upper;
		}
		
		/**
		 * Returns node for matching characters within an explicit value range.
		 */
		public static CharProperty rangeFor (final int lower, final int upper) {
			
			return new CharProperty () {
				
				@Override
				public boolean isSatisfiedBy (int ch) {
					return inRange (lower, ch, upper);
				}
				
			};
			
		}
		
		/**
		 * Returns node for matching characters within an explicit value range in a case
		 * insensitive manner.
		 */
		public CharProperty caseInsensitiveRangeFor (final int lower, final int upper) {
			
			if (has (UNICODE_CASE)) {
				
				return new CharProperty () {
					
					@Override
					public boolean isSatisfiedBy (int ch) {
						
						if (inRange (lower, ch, upper)) return true;
						int up = Character.toUpperCase (ch);
						
						return inRange (lower, up, upper) || inRange (lower, Character.toLowerCase (up), upper);
						
					}
					
				};
				
			}
			
			return new CharProperty () {
				public boolean isSatisfiedBy (int ch) {
					return inRange (lower, ch, upper) || ASCII.isAscii (ch)
						                                     && (inRange (lower, ASCII.toUpper (ch), upper) || inRange (lower, ASCII.toLower (ch), upper));
				}
			};
		}
		
		public static class GroupHeadAndTail {
			
			public GroupHead groupHead;
			public GroupTail groupTail;
			
			public GroupHeadAndTail (GroupHead groupHead, GroupTail groupTail) {
				
				this.groupHead = groupHead;
				this.groupTail = groupTail;
				
			}
			
		}
		
		/**
		 * For use with lookbehinds; matches the position where the lookbehind was
		 * encountered.
		 */
		public static Node lookbehindEnd = new Node () {
			
			@Override
			public boolean match (Matcher matcher, int i, CharSequence seq) {
				return i == matcher.lookbehindTo;
			}
			
		};
		
		/**
		 * Returns the set union of two CharProperty nodes.
		 */
		public static CharProperty union (final CharProperty lhs, final CharProperty rhs) {
			
			return new CharProperty () {
				
				@Override
				public boolean isSatisfiedBy (int ch) {
					return lhs.isSatisfiedBy (ch) || rhs.isSatisfiedBy (ch);
				}
				
			};
			
		}
		
		/**
		 * Returns the set intersection of two CharProperty nodes.
		 */
		public static CharProperty intersection (final CharProperty lhs, final CharProperty rhs) {
			
			return new CharProperty () {
				
				@Override
				public boolean isSatisfiedBy (int ch) {
					return lhs.isSatisfiedBy (ch) && rhs.isSatisfiedBy (ch);
				}
				
			};
			
		}
		
		/**
		 * Returns the set difference of two CharProperty nodes.
		 */
		public static CharProperty setDifference (final CharProperty lhs, final CharProperty rhs) {
			
			return new CharProperty () {
				
				@Override
				public boolean isSatisfiedBy (int ch) {
					return !rhs.isSatisfiedBy (ch) && lhs.isSatisfiedBy (ch);
				}
				
			};
			
		}
		
		/**
		 * Non spacing marks only count as word characters in bounds calculations if
		 * they have a base character.
		 */
		public static boolean hasBaseCharacter (Matcher matcher, int i, CharSequence seq) {
			int start = (!matcher.transparentBounds) ? matcher.from : 0;
			for (int x = i; x >= start; x--) {
				int ch = Character.codePointAt (seq, x);
				if (Character.isLetterOrDigit (ch))
					return true;
				if (Character.getType (ch) == Character.NON_SPACING_MARK)
					continue;
				return false;
			}
			return false;
		}
		
		///////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////
		
		/**
		 * This must be the very first initializer.
		 */
		public static Node accept = new Node ();
		
		static Node lastAccept = new LastNode ();
		
		public static class CharPropertyNames {
			
			public static CharProperty charPropertyFor (String name) {
				CharPropertyFactory m = map.get (name);
				return m == null ? null : m.make ();
			}
			
			public abstract static class CharPropertyFactory {
				abstract CharProperty make ();
				
			}
			
			public void defCategory (String name, final int typeMask) {
				
				map.put (name, new CharPropertyFactory () {
					
					@Override
					public CharProperty make () {
						return new Category (typeMask);
					}
					
				});
			}
			
			public void defRange (String name, final int lower, final int upper) {
				map.put (name, new CharPropertyFactory () {
					
					@Override
					public CharProperty make () {
						return rangeFor (lower, upper);
					}
					
				});
				
			}
			
			public void defCtype (String name, final int ctype) {
				
				map.put (name, new CharPropertyFactory () {
					
					@Override
					public CharProperty make () {
						return new Ctype (ctype);
					}
					
				});
				
			}
			
			public static abstract class CloneableProperty extends CharProperty implements Cloneable {
				
				@Override
				public CloneableProperty clone () {
					
					try {
						return (CloneableProperty) super.clone ();
					} catch (CloneNotSupportedException e) {
						throw new AssertionError (e);
					}
					
				}
				
			}
			
			public void defClone (String name, final CloneableProperty p) {
				
				map.put (name, new CharPropertyFactory () {
					
					@Override
					public CharProperty make () {
						return p.clone ();
					}
					
				});
				
			}
			
			public static HashMap<String, CharPropertyFactory> map = new HashMap<> ();
			
			public CharPropertyNames () {
				
				// Unicode character property aliases, defined in
				// http://www.unicode.org/Public/UNIDATA/PropertyValueAliases.txt
				defCategory ("Cn", 1 << Character.UNASSIGNED);
				defCategory ("Lu", 1 << Character.UPPERCASE_LETTER);
				defCategory ("Ll", 1 << Character.LOWERCASE_LETTER);
				defCategory ("Lt", 1 << Character.TITLECASE_LETTER);
				defCategory ("Lm", 1 << Character.MODIFIER_LETTER);
				defCategory ("Lo", 1 << Character.OTHER_LETTER);
				defCategory ("Mn", 1 << Character.NON_SPACING_MARK);
				defCategory ("Me", 1 << Character.ENCLOSING_MARK);
				defCategory ("Mc", 1 << Character.COMBINING_SPACING_MARK);
				defCategory ("Nd", 1 << Character.DECIMAL_DIGIT_NUMBER);
				defCategory ("Nl", 1 << Character.LETTER_NUMBER);
				defCategory ("No", 1 << Character.OTHER_NUMBER);
				defCategory ("Zs", 1 << Character.SPACE_SEPARATOR);
				defCategory ("Zl", 1 << Character.LINE_SEPARATOR);
				defCategory ("Zp", 1 << Character.PARAGRAPH_SEPARATOR);
				defCategory ("Cc", 1 << Character.CONTROL);
				defCategory ("Cf", 1 << Character.FORMAT);
				defCategory ("Co", 1 << Character.PRIVATE_USE);
				defCategory ("Cs", 1 << Character.SURROGATE);
				defCategory ("Pd", 1 << Character.DASH_PUNCTUATION);
				defCategory ("Ps", 1 << Character.START_PUNCTUATION);
				defCategory ("Pe", 1 << Character.END_PUNCTUATION);
				defCategory ("Pc", 1 << Character.CONNECTOR_PUNCTUATION);
				defCategory ("Po", 1 << Character.OTHER_PUNCTUATION);
				defCategory ("Sm", 1 << Character.MATH_SYMBOL);
				defCategory ("Sc", 1 << Character.CURRENCY_SYMBOL);
				defCategory ("Sk", 1 << Character.MODIFIER_SYMBOL);
				defCategory ("So", 1 << Character.OTHER_SYMBOL);
				defCategory ("Pi", 1 << Character.INITIAL_QUOTE_PUNCTUATION);
				defCategory ("Pf", 1 << Character.FINAL_QUOTE_PUNCTUATION);
				
				defCategory ("L", ((1 << Character.UPPERCASE_LETTER) | (1 << Character.LOWERCASE_LETTER)
					                   | (1 << Character.TITLECASE_LETTER) | (1 << Character.MODIFIER_LETTER)
					                   | (1 << Character.OTHER_LETTER)));
				defCategory ("M", ((1 << Character.NON_SPACING_MARK) | (1 << Character.ENCLOSING_MARK)
					                   | (1 << Character.COMBINING_SPACING_MARK)));
				defCategory ("N", ((1 << Character.DECIMAL_DIGIT_NUMBER) | (1 << Character.LETTER_NUMBER)
					                   | (1 << Character.OTHER_NUMBER)));
				defCategory ("Z", ((1 << Character.SPACE_SEPARATOR) | (1 << Character.LINE_SEPARATOR)
					                   | (1 << Character.PARAGRAPH_SEPARATOR)));
				defCategory ("C", ((1 << Character.CONTROL) | (1 << Character.FORMAT) | (1 << Character.PRIVATE_USE)
					                   | (1 << Character.SURROGATE))); // Other
				defCategory ("P",
					((1 << Character.DASH_PUNCTUATION) | (1 << Character.START_PUNCTUATION)
						 | (1 << Character.END_PUNCTUATION) | (1 << Character.CONNECTOR_PUNCTUATION)
						 | (1 << Character.OTHER_PUNCTUATION) | (1 << Character.INITIAL_QUOTE_PUNCTUATION)
						 | (1 << Character.FINAL_QUOTE_PUNCTUATION)));
				defCategory ("S", ((1 << Character.MATH_SYMBOL) | (1 << Character.CURRENCY_SYMBOL)
					                   | (1 << Character.MODIFIER_SYMBOL) | (1 << Character.OTHER_SYMBOL)));
				defCategory ("LC", ((1 << Character.UPPERCASE_LETTER) | (1 << Character.LOWERCASE_LETTER)
					                    | (1 << Character.TITLECASE_LETTER)));
				defCategory ("LD",
					((1 << Character.UPPERCASE_LETTER) | (1 << Character.LOWERCASE_LETTER)
						 | (1 << Character.TITLECASE_LETTER) | (1 << Character.MODIFIER_LETTER)
						 | (1 << Character.OTHER_LETTER) | (1 << Character.DECIMAL_DIGIT_NUMBER)));
				defRange ("L1", 0x00, 0xFF); // Latin-1
				map.put ("all", new CharPropertyFactory () {
					CharProperty make () {
						return new All ();
					}
				});
				
				// Posix regular expression character classes, defined in
				// http://www.unix.org/onlinepubs/009695399/basedefs/xbd_chap09.html
				defRange ("ASCII", 0x00, 0x7F); // ASCII
				defCtype ("Alnum", ASCII.ALNUM); // Alphanumeric characters
				defCtype ("Alpha", ASCII.ALPHA); // Alphabetic characters
				defCtype ("Blank", ASCII.BLANK); // Space and tab characters
				defCtype ("Cntrl", ASCII.CNTRL); // Control characters
				defRange ("Digit", '0', '9'); // Numeric characters
				defCtype ("Graph", ASCII.GRAPH); // printable and visible
				defRange ("Lower", 'a', 'z'); // Lower-case alphabetic
				defRange ("Print", 0x20, 0x7E); // Printable characters
				defCtype ("Punct", ASCII.PUNCT); // Punctuation characters
				defCtype ("Space", ASCII.SPACE); // Space characters
				defRange ("Upper", 'A', 'Z'); // Upper-case alphabetic
				defCtype ("XDigit", ASCII.XDIGIT); // hexadecimal digits
				
				// Java character properties, defined by methods in Character.java
				defClone ("javaLowerCase", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isLowerCase (ch);
					}
				});
				defClone ("javaUpperCase", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isUpperCase (ch);
					}
				});
				defClone ("javaAlphabetic", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isAlphabetic (ch);
					}
				});
				defClone ("javaIdeographic", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isIdeographic (ch);
					}
				});
				defClone ("javaTitleCase", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isTitleCase (ch);
					}
				});
				defClone ("javaDigit", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isDigit (ch);
					}
				});
				defClone ("javaDefined", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isDefined (ch);
					}
				});
				defClone ("javaLetter", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isLetter (ch);
					}
				});
				defClone ("javaLetterOrDigit", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isLetterOrDigit (ch);
					}
				});
				defClone ("javaJavaIdentifierStart", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isJavaIdentifierStart (ch);
					}
				});
				defClone ("javaJavaIdentifierPart", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isJavaIdentifierPart (ch);
					}
				});
				defClone ("javaUnicodeIdentifierStart", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isUnicodeIdentifierStart (ch);
					}
				});
				defClone ("javaUnicodeIdentifierPart", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isUnicodeIdentifierPart (ch);
					}
				});
				defClone ("javaIdentifierIgnorable", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isIdentifierIgnorable (ch);
					}
				});
				defClone ("javaSpaceChar", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isSpaceChar (ch);
					}
				});
				defClone ("javaWhitespace", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isWhitespace (ch);
					}
				});
				defClone ("javaISOControl", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isISOControl (ch);
					}
				});
				defClone ("javaMirrored", new CloneableProperty () {
					public boolean isSatisfiedBy (int ch) {
						return Character.isMirrored (ch);
					}
				});
			}
			
		}
		
		/**
		 * Creates a predicate which can be used to match a string.
		 *
		 * @return The predicate which can be used for matching on a string
		 * @since 1.8
		 */
		public Predicate<String> asPredicate () {
			return s -> matcher (s).find ();
		}
		
		/**
		 * Creates a stream from the given input sequence around matches of this
		 * pattern.
		 *
		 * <p>
		 * The stream returned by this method contains each substring of the input
		 * sequence that is terminated by another subsequence that matches this pattern
		 * or is terminated by the end of the input sequence. The substrings in the
		 * stream are in the order in which they occur in the input. Trailing empty
		 * strings will be discarded and not encountered in the stream.
		 *
		 * <p>
		 * If this pattern does not match any subsequence of the input then the
		 * resulting stream has just one element, namely the input sequence in string
		 * form.
		 *
		 * <p>
		 * When there is a positive-width match at the beginning of the input sequence
		 * then an empty leading substring is included at the beginning of the stream. A
		 * zero-width match at the beginning however never produces such empty leading
		 * substring.
		 *
		 * <p>
		 * If the input sequence is mutable, it must remain constant during the
		 * execution of the terminal stream operation. Otherwise, the result of the
		 * terminal stream operation is undefined.
		 *
		 * @param input The character sequence to be split
		 * @return The stream of strings computed by splitting the input around matches
		 * of this pattern
		 * @see #split(CharSequence)
		 * @since 1.8
		 */
		public Stream<String> splitAsStream (final CharSequence input) {
			
			class MatcherIterator implements Iterator<String> {
				
				public final Matcher matcher;
				// The start position of the next sub-sequence of input
				// when current == input.length there are no more elements
				public int current;
				// null if the next element, if any, needs to obtained
				public String nextElement;
				// > 0 if there are N next empty elements
				public int emptyElementCount;
				
				public MatcherIterator () {
					this.matcher = matcher (input);
				}
				
				@Override
				public String next () {
					
					if (!hasNext ())
						throw new NoSuchElementException ();
					
					if (emptyElementCount == 0) {
						String n = nextElement;
						nextElement = null;
						return n;
					} else {
						emptyElementCount--;
						return "";
					}
				}
				
				@Override
				public boolean hasNext () {
					
					if (nextElement != null || emptyElementCount > 0)
						return true;
					
					if (current == input.length ())
						return false;
					
					// Consume the next matching element
					// Count sequence of matching empty elements
					while (matcher.find ()) {
						nextElement = input.subSequence (current, matcher.start ()).toString ();
						current = matcher.end ();
						if (!nextElement.isEmpty ()) {
							return true;
						} else if (current > 0) { // no empty leading substring for zero-width match at the beginning of the input
							emptyElementCount++;
						}
					}
					
					// Consume last matching element
					nextElement = input.subSequence (current, input.length ()).toString ();
					current = input.length ();
					if (!nextElement.isEmpty ()) {
						return true;
					} else {
						// Ignore a terminal sequence of matching empty elements
						emptyElementCount = 0;
						nextElement = null;
						return false;
					}
				}
				
			}
			
			return StreamSupport.stream (Spliterators.spliteratorUnknownSize (new MatcherIterator (), Spliterator.ORDERED | Spliterator.NONNULL), false);
			
		}
		
	}