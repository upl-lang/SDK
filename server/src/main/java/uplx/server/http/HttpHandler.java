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
	
	package uplx.server.http;
	
	import uplx.server.Handler;
	import uplx.server.http.handlers.FilesHandler;
	
	public abstract class HttpHandler extends Handler<Request, Response> {
		
		protected volatile String directoryIndex = "index.html";
		
		public String url;
		
		/**
		 * Sets the directory index file. For every request whose URI ends with
		 * a '/' (i.e. a directory), the index file is appended to the path,
		 * and the resulting resource is served if it exists. If it does not
		 * exist, an auto-generated index for the requested directory may be
		 * served, depending on whether {@link FilesHandler#setAllowGeneratedIndex
		 * a generated index is allowed}, otherwise an error is returned.
		 * The default directory index file is "index.html".
		 *
		 * @param directoryIndex the directory index file, or null if no
		 *                       index file should be used
		 */
		public void setDirectoryIndex (String directoryIndex) {
			this.directoryIndex = directoryIndex;
		}
		
		/**
		 * Gets this host's directory index file.
		 *
		 * @return the directory index file, or null
		 */
		public String getDirectoryIndex () {
			return directoryIndex;
		}
		
	}