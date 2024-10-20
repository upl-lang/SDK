	/*
	 * Copyright (c) 2020 - 2023 UPL Foundation
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
	
	package upl.core;
	
	import java.security.SecureRandom;
	
	public class Random extends SecureRandom {
		
		public static String SUMB_DIGITS = "0123456789";
		public static String SUMB_SPECIAL = "!?@#~$%^&*№+=;:«»[]—";
		public static String SUMB_SPECIAL_2 = ",\"'/()";
		public static String SUMB_LETTERS_LOW = "abcdefghijklmnopqrstuvwxyz";
		public static String SUMB_LETTERS_UP = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		
		protected int saltLen;
		protected String[] salt2;
		
		public StringBuilder salt = new StringBuilder ();
		
		public Random (String... symbols) {
			
			for (String symbol : symbols)
				salt.append (symbol);
			
			saltLen = (Int.size (salt) - 1);
			
			salt2 = Arrays.strSplit (salt.toString ());
			
		}
		
		public int generate (int min, int max) {
			return nextInt ((max - min) + 1) + min;
		}
		
		public long generate (long min, long max) {
			return min + ((long) (nextDouble () * (max - min) + 1));
		}
		
		public int[] generate (int min, int max, int num) {
			
			int[] output = new int[num];
			
			for (int i = 0; i < num; ++i)
				output[i] = generate (min, max);
			
			return output;
			
		}
		
		public String generate (int num) {
			return generate (num, new StringBuilder ());
		}
		
		public String generate (int num, StringBuilder rand) {
			
			for (int i = 0; i < num; ++i)
				rand.append (salt2[generate (0, saltLen)]);
			
			return rand.toString ();
			
		}
		
		public String generate (String... array) {
			return array[generate (0, Arrays.endKey (array))];
		}
		
		public int[] generateRange (int min, int max) {
			
			int diff = (max - min) + 1;
			int[] range = new int[diff];
			
			for (int i = 0; i < diff; i++)
				range[i] = min + i;
			
			for (int i = range.length - 1; i > 0; i--) {
				
				int index = nextInt (i + 1);
				
				int index2 = range[index];
				
				range[index] = range[i];
				range[i] = index2;
				
			}
			
			return range;
			
		}
		
	}