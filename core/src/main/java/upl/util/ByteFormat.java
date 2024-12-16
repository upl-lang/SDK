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
	
	package upl.util;
	
	import java.text.NumberFormat;
	
	public class ByteFormat {
		
		public enum StorageUnit {
			
			BYTE     ("b"),
			KILOBYTE ("Kb"),
			MEGABYTE ("Mb"),
			GIGABYTE ("Gb"),
			TERABYTE ("Tb"),
			PETABYTE ("Pb"),
			EXABYTE  ("Eb");
			
			private final String symbol;
			
			StorageUnit (String name) {
				this.symbol = name;
			}
			
		}
		
		protected NumberFormat nf;
		
		protected long number;
		
		public ByteFormat (long number) {
			
			this.number = number;
			
			nf = NumberFormat.getInstance ();
			
			nf.setGroupingUsed (false);
			nf.setMinimumFractionDigits (0);
			nf.setMaximumFractionDigits (1);
			
		}
		
		protected String divider;
		protected String symbol;
		
		protected int i = 0;
		
		public StorageUnit of (long number) {
			
			long n = number > 0 ? -number : number;
			
			for (StorageUnit unit : StorageUnit.values ()) {
				
				i += 10;
				
				if (n > -(1L << i))
					return unit;
				
			}
			
			return StorageUnit.EXABYTE;
			
		}
		
		public ByteFormat format () {
			
			StorageUnit unit = of (number);
			
			divider = nf.format ((double) number / (1L << (i - 10)));
			symbol = unit.symbol;
			
			return this;
			
		}
		
	}