	/*
	 * Copyright (c) 2020 - 2024 UPL Foundation
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 * 	  http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */
	
	package com.florianingerl.util.regex.nodes;
	
	import com.florianingerl.util.regex.Matcher;
	import com.florianingerl.util.regex.Node;
	import com.florianingerl.util.regex.TreeInfo;
	
	/**
	 * Attempts to match a slice in the input using the Boyer-Moore string matching
	 * algorithm. The algorithm is based on the idea that the pattern can be shifted
	 * farther ahead in the search text if it is matched right to left.
	 * <p>
	 * The pattern is compared to the input one character at a time, from the
	 * rightmost character in the pattern to the left. If the characters all match
	 * the pattern has been found. If a character does not match, the pattern is
	 * shifted right a distance that is the maximum of two functions, the bad
	 * character shift and the good suffix shift. This shift moves the attempted
	 * match position through the input more quickly than a naive one position at a
	 * time check.
	 * <p>
	 * The bad character shift is based on the character from the text that did not
	 * match. If the character does not appear in the pattern, the pattern can be
	 * shifted completely beyond the bad character. If the character does occur in
	 * the pattern, the pattern can be shifted to line the pattern up with the next
	 * occurrence of that character.
	 * <p>
	 * The good suffix shift is based on the idea that some subset on the right side
	 * of the pattern has matched. When a bad character is found, the pattern can be
	 * shifted right by the pattern length if the subset does not occur again in
	 * pattern, or by the amount of distance to the next occurrence of the subset in
	 * the pattern.
	 * <p>
	 * Boyer-Moore search methods adapted from code by Amy Yu.
	 */
	public class BnM extends Node {
		
		public int[] buffer;
		public int[] lastOcc;
		public int[] optoSft;
		
		/**
		 * Pre calculates arrays needed to generate the bad character shift and the good
		 * suffix shift. Only the last seven bits are used to see if chars match; This
		 * keeps the tables small and covers the heavily used ASCII range, but
		 * occasionally results in an aliased match for the bad character shift.
		 */
		public static Node optimize (Node node) {
			
			if (!(node instanceof Slice)) {
				return node;
			}
			
			int[] src = ((Slice) node).buffer;
			int patternLength = src.length;
			// The BM algorithm requires a bit of overhead;
			// If the pattern is short don't use it, since
			// a shift larger than the pattern length cannot
			// be used anyway.
			if (patternLength < 4) {
				return node;
			}
			int i, j, k;
			int[] lastOcc = new int[128];
			int[] optoSft = new int[patternLength];
			// Precalculate part of the bad character shift
			// It is a table for where in the pattern each
			// lower 7-bit value occurs
			for (i = 0; i < patternLength; i++) {
				lastOcc[src[i] & 0x7F] = i + 1;
			}
			// Precalculate the good suffix shift
			// i is the shift amount being considered
				NEXT:
			for (i = patternLength; i > 0; i--) {
				// j is the beginning index of suffix being considered
				for (j = patternLength - 1; j >= i; j--) {
					// Testing for good suffix
					if (src[j] == src[j - i]) {
						// src[j..len] is a good suffix
						optoSft[j - 1] = i;
					} else {
						// No match. The array has already been
						// filled up with correct values before.
						continue NEXT;
					}
				}
				// This fills up the remaining of optoSft
				// any suffix can not have larger shift amount
				// then its sub-suffix. Why???
				while (j > 0) {
					optoSft[--j] = i;
				}
			}
			// Set the guard value because of unicode compression
			optoSft[patternLength - 1] = 1;
			if (node instanceof SliceS)
				return new BnMS (src, lastOcc, optoSft, node.getNext ());
			return new BnM (src, lastOcc, optoSft, node.getNext ());
		}
		
		public BnM (int[] src, int[] lastOcc, int[] optoSft, Node next) {
			this.buffer = src;
			this.lastOcc = lastOcc;
			this.optoSft = optoSft;
			this.setNext (next);
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			int[] src = buffer;
			int patternLength = src.length;
			int last = matcher.to - patternLength;
			
			// Loop over all possible match positions in text
				NEXT:
			while (i <= last) {
				// Loop over pattern from right to left
				for (int j = patternLength - 1; j >= 0; j--) {
					int ch = seq.charAt (i + j);
					if (ch != src[j]) {
						// Shift search to the right by the maximum of the
						// bad character shift and the good suffix shift
						i += Math.max (j + 1 - lastOcc[ch & 0x7F], optoSft[j]);
						continue NEXT;
					}
				}
				// Entire pattern matched starting at i
				matcher.first = i;
				boolean ret = getNext ().match (matcher, i + patternLength, seq);
				if (ret) {
					matcher.first = i;
					matcher.setGroup0 (seq, matcher.first, matcher.last);
					return true;
				}
				i++;
			}
			// BnM is only used as the leading node in the unanchored case,
			// and it replaced its Start() which always searches to the end
			// if it doesn't find what it's looking for, so hitEnd is true.
			matcher.hitEnd = true;
			return false;
		}
		
		@Override
		public boolean study (TreeInfo info) {
			
			info.minLength += buffer.length;
			info.maxValid = false;
			
			return getNext ().study (info);
			
		}
		
	}