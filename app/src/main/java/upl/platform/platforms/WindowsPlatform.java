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
	
	package upl.platform.platforms;
	
	import upl.app.Application;
	import upl.platform.Environment;
	import upl.core.File;
	import upl.platform.Platform;
	
	public class WindowsPlatform extends Platform {
		
		public WindowsPlatform (Application app) {
			super (app);
		}
		
		@Override
		public String getName () {
			return "Windows";
		}
		
		@Override
		public boolean consume () {
			return getOSName ().toLowerCase ().startsWith ("windows");
		}
		
		@Override
		protected Environment setEnvironment () {
			return new WindowsEnvironment (null);
		}
		
		public static class WindowsEnvironment extends Environment {
			
			private FolderResolver resolver;
			
			public WindowsEnvironment (Application app) {
				super (app);
			}
			
			public interface FolderResolver {
				String resolveFolder (FolderId folderId);
			}
			
			public enum FolderId {
				APPDATA, LOCAL_APPDATA, COMMON_APPDATA;
			}
			
			protected String getAppData () {
				return resolver.resolveFolder (FolderId.APPDATA);
			}
			
			protected String getLocalAppData () {
				return resolver.resolveFolder (FolderId.LOCAL_APPDATA);
			}
			
			protected String getCommonAppData () {
				return resolver.resolveFolder (FolderId.COMMON_APPDATA);
			}
			
			@Override
			public File getAsset (String... name) {
				return null;
			}
			
			@Override
			public String getUserDataDir (String appName, String appVersion, String appAuthor, boolean roaming) {
				
				String dir = roaming ? getAppData () : getLocalAppData ();
				return buildPath (dir, appAuthor, appName, appVersion);
				
			}
			
			@Override
			public String getUserConfigDir (String appName, String appVersion, String appAuthor, boolean roaming) {
				return getUserDataDir (appName, appVersion, appAuthor, roaming);
			}
			
			@Override
			public String getUserCacheDir (String appName, String appVersion, String appAuthor) {
				return buildPath (getLocalAppData (), appAuthor, appName, "\\Cache", appVersion);
			}
			
			@Override
			public String getUserLogDir (String appName, String appVersion, String appAuthor) {
				return buildPath (getLocalAppData (), appAuthor, appName, "\\Logs", appVersion);
			}
			
			@Override
			public String getSharedDir (String appName, String appVersion, String appAuthor) {
				return buildPath (getCommonAppData (), appAuthor, appName, appVersion);
			}
			
		}
		
	}