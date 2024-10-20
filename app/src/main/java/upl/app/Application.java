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
	
	package upl.app;
	
	import upl.app.design.Design;
	import upl.app.device.Device;
	import upl.loggers.Console;
	import upl.app.packages.StubPackage;
	import upl.core.Logger;
	import upl.platform.Platform;
	import upl.platform.platforms.OtherPlatform;
	import upl.util.ArrayList;
	import upl.util.HashMap;
	import upl.util.List;
	import upl.util.Map;
	
	public abstract class Application extends upl.type.Object {
		
		public Logger log;
		
		protected Application () {
			
			log = getLogger ();
			
		}
		
		protected Package getPackage () {
			return new StubPackage ();
		}
		
		protected Map<String, Localization> localizations = new HashMap<> ();
		
		protected Manifest setManifest () {
			return new Manifest ();
		}
		
		protected Manifest manifest;
		
		protected Manifest getManifest () {
			
			if (manifest == null)
				manifest = setManifest ();
			
			manifest.app = this;
			
			return manifest;
			
		}
		
		protected Design design;
		
		protected abstract Design setDesign ();
		
		protected final Design getDesign () {
			
			if (design == null)
				design = setDesign ();
			
			return design;
			
		}
		
		protected Device device;
		
		protected Device setDevice () {
			return new Device ();
		}
		
		protected final Device getDevice () {
			
			if (device == null)
				device = setDevice ();
			
			device.app = this;
			
			return device;
			
		}
		
		public Benchmark benchmark;
		
		protected Benchmark setBenchmark () {
			return new Benchmark ();
		}
		
		protected final Benchmark getBenchmark () {
			
			if (benchmark == null)
				benchmark = setBenchmark ();
			
			benchmark.app = this;
			
			return benchmark;
			
		}
		
		protected final Logger getLogger () {
			
			if (log == null)
				log = setLogger ();
			
			return log;
			
		}
		
		protected Logger setLogger () {
			return new Console (getClass ());
		}
		
		protected List<Platform> platforms = new ArrayList<> ();
		
		protected void setPlatform (Platform platform) {
			platforms.put (platform);
		}
		
		protected Platform platform;
		
		protected final Platform getPlatform () {
			
			if (platform == null) {
				
				for (Platform platform : platforms) {
					
					if (platform.consume ()) {
						
						for (Platform type : platform.types) {
							
							if (type.consume ()) {
								
								this.platform = type;
								return this.platform;
								
							}
							
						}
						
						this.platform = platform;
						
						return this.platform;
						
					}
					
				}
				
				platform = new OtherPlatform (this);
				
			}
			
			return platform;
			
		}
		
		protected Application addLocalization (Localization local) {
			
			localizations.add (local.getCode (), local);
			
			return this;
			
		}
		
		@SuppressWarnings ("unchecked")
		protected <C extends Localization> C getLocalization (String code) {
			
			C local = (C) localizations.get (code);
			
			if (local != null)
				return local;
			else
				throw new NullPointerException ("Localization file with code \"" + code + "\" not found. Use addLocalization method to add it.");
			
		}
		
	}