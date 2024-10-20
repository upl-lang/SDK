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
	
	package uplx.server;
	
	/**
	 * The {@code Header} class encapsulates a single HTTP header.
	 */
	public class Header {
		
		protected final String name;
		protected final String value;
		
		/**
     * Constructs a header with the given name and value.
     * Leading and trailing whitespace are trimmed.
     *
     * @param name	the header name
     * @param value the header value
     * @throws NullPointerException     if name or value is null
     * @throws IllegalArgumentException if name is empty
     */
		public Header (String name, Object value) {
			
			this.name = name.trim ();
			this.value = value.toString ().trim ();
			
			// RFC2616#14.23 - header can have an empty value (e.g. Host)
			if (this.name.length () == 0) // but name cannot be empty
				throw new IllegalArgumentException ("name cannot be empty");
			
		}
		
		/**
     * Returns this header's name.
     *
     * @return this header's name
     */
		public String getName () {
			return name;
		}
		
		/**
     * Returns this header's value.
     *
     * @return this header's value
     */
		public String getValue () {
			return value;
		}
		
		@Override
		public String toString () {
			return getName () + ": " + getValue ();
		}
		
	}