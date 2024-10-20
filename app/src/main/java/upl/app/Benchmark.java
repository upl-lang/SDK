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
	
	package upl.app;
	
	import java.math.BigDecimal;
	import upl.core.Math;
	import upl.util.LinkedHashMap;
	import upl.util.Map;
	
	public class Benchmark {
		
		public Application app;
		
		protected final Map<String, Long> cs = new LinkedHashMap<> ();
		protected int i = 0;
		
		protected final String defName = "Benchmark";
		
		public void startTime () {
			startTime (defName);
			
		}
		
		public void startTime (String name) {
			cs.put (name, System.currentTimeMillis ());
		}
		
		public void finishTime () {
			finishTime (defName);
		}
		
		public void finishTime (String name) {
			finishTime (name, 2);
		}
		
		public void finishTime (int round) {
			finishTime (defName, round);
		}
		
		public void finishTime (String name, int round) {
			System.out.println (name + ": " + getTimeMs (name) + "ms / " + getTimeSec (name, round) + "sec");
		}
		
		public long getTimeMs () {
			return getTimeMs (defName);
		}
		
		public long getTimeMs (String name) {
			return System.currentTimeMillis () - cs.get (name);
		}
		
		public BigDecimal getTimeSec (String name) {
			return getTimeSec (name, 2);
		}
		
		public BigDecimal getTimeSec (String name, int round) {
			return Math.round (((float) getTimeMs (name) / 1000), round);
		}
		
	}