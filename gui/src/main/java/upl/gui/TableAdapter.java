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
	
	package upl.gui;
	
	import upl.util.ArrayList;
	import upl.util.List;
	
	public abstract class TableAdapter<T extends TableAdapter.Handler, V> extends View {
		
		protected T handler;
		
		protected List<V> items = new ArrayList<> ();
		
		public TableAdapter (T handler) {
			this.handler = handler;
		}
		
		public abstract static class Handler {
		
		}
		
		public TableAdapter<T, V> setItem (V item) {
			
			items.put (item);
			return this;
			
		}
		
		public V getItem (int id) {
			return items.get (id);
		}
		
		protected abstract void onHandler (int position);
		
		public int getLength () {
			return items.length ();
		}
		
	}