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
	 * Handles the branching of alternations. Note this is also used for the ?
	 * quantifier to branch between the case where it matches once and where it does
	 * not occur.
	 */
	public class Branch extends Node {
		
		public Node[] atoms = new Node[2];
		public int size = 0;
		public Node conn;
		
		public Branch (Node first, Node second, Node branchConn) {
			conn = branchConn;
			add (first);
			add (second);
		}
		
		public void add (Node node) {
			if (size >= atoms.length) {
				Node[] tmp = new Node[atoms.length * 2];
				System.arraycopy (atoms, 0, tmp, 0, atoms.length);
				atoms = tmp;
			}
			int i = size++;
			new Node () {
				@Override
				public void setNext (Node a) {
					atoms[i] = a;
					if (a != null)
						a.previous = this;
				}
			}.setNext (node);
		}
		
		@Override
		public boolean match (Matcher matcher, int i, CharSequence seq) {
			for (int n = 0; n < size; n++) {
				if (atoms[n] == null) {
					if (conn.getNext ().match (matcher, i, seq))
						return true;
				} else if (atoms[n].match (matcher, i, seq)) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public boolean study (TreeInfo info) {
			
			int minL = info.minLength;
			int maxL = info.maxLength;
			boolean maxV = info.maxValid;
			
			int minL2 = Integer.MAX_VALUE; // arbitrary large enough num
			int maxL2 = -1;
			for (int n = 0; n < size; n++) {
				info.reset ();
				if (atoms[n] != null)
					atoms[n].study (info);
				minL2 = Math.min (minL2, info.minLength);
				maxL2 = Math.max (maxL2, info.maxLength);
				maxV = (maxV & info.maxValid);
			}
			
			minL += minL2;
			maxL += maxL2;
			
			info.reset ();
			conn.getNext ().study (info);
			
			info.minLength += minL;
			info.maxLength += maxL;
			info.maxValid &= maxV;
			info.deterministic = false;
			return false;
		}
		
		@Override
		public Node getNext () {
			return conn;
		}
		
	}