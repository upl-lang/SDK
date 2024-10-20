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
	import com.florianingerl.util.regex.Pattern;
	import com.florianingerl.util.regex.TreeInfo;
	
	/**
	 * Base class for plugins into the regex engine.
	 * <p>
	 * A Regular Expression is compiled into a chained sequence of nodes, each node
	 * matching the input character sequence against its pattern and then asking its
	 * next node to do the same. By extending this class and overriding the
	 * {@link Node#match(Matcher, int, CharSequence) match} method, you thus
	 * provide a plugin into the regex engine that you can install via
	 * {@link Pattern#installPlugin(String, Class)}.
	 */
	public abstract class CustomNode extends Node {
		
		/**
		 * Matches the input character sequence against this node's pattern.
		 * <p>
		 * The character sequence seq is matched against this node's pattern, where the
		 * characters 0 to i-1 in seq have already been matched by previous nodes. If
		 * this node matches say n characters, you have to call
		 * {@link #matchNext(Matcher, int, CharSequence) matchNext}(matcher, i+n, seq).
		 * In case {@link #matchNext(Matcher, int, CharSequence) matchNext} returns
		 * true, this method usually should also return true. In case
		 * {@link #matchNext(Matcher, int, CharSequence) matchNext} returns false, this
		 * node might try to match something different with possibly either more or less
		 * characters and then call {@link #matchNext(Matcher, int, CharSequence)
		 * matchNext} again. Then this node would be a backtracking crossroad. Or this
		 * node might restore the state of matcher and return false.
		 *
		 * @see CustomNode#matchNext(Matcher, int, CharSequence)
		 */
		@Override
		public abstract boolean match (Matcher matcher, int i, CharSequence seq);
		
		protected boolean matchNext (Matcher matcher, int i, CharSequence seq) {
			return getNext ().match (matcher, i, seq);
		}
		
		/**
		 * Retrieves the data stored in matcher.
		 *
		 * @see CustomNode#storeData(Matcher, Object)
		 */
		protected Object retrieveData (Matcher matcher) {
			return matcher.data.get (this.getClass ());
		}
		
		/**
		 * Stores some search data in matcher.
		 * <p>
		 * Instances of the {@link Pattern} class and all its nodes are completely
		 * stateless allowing concurrent use. All state of the regex engine is therefore
		 * stored in matcher, which is what this method allows subclasses to do.
		 *
		 * @param matcher The {@link Matcher} in which to store the data.
		 * @param data    The data to store in matcher.
		 * @see CustomNode#retrieveData(Matcher)
		 */
		protected void storeData (Matcher matcher, Object data) {
			matcher.data.put (this.getClass (), data);
		}
		
		/**
		 * Returns the minimum number of characters that this node matches.
		 *
		 * @return The minimum number of characters that this node matches.
		 */
		protected abstract int minLength ();
		
		/**
		 * Returns the maximum number of characters that this node matches.
		 *
		 * @return The maximum number of characters that this node matches.
		 */
		protected abstract int maxLength ();
		
		/**
		 * Says whether this node can match arbitrary many characters
		 * <p>
		 * This is used by Lookbehinds. Only Lookbehinds with a valid maximum length are
		 * allowed.
		 *
		 * @return false, if this node can match arbitrary many characters.
		 */
		protected abstract boolean isMaxValid ();
		
		protected abstract boolean isDeterministic ();
		
		@Override
		public boolean study (TreeInfo info) {
			info.minLength += minLength ();
			info.maxLength += maxLength ();
			if (!isMaxValid ())
				info.maxValid = false;
			info.deterministic = info.deterministic & isDeterministic ();
			return getNext ().study (info);
		}
		
	}
