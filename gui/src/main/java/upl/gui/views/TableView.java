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
	
	package upl.gui.views;
	
	import upl.gui.TableAdapter;
	import upl.type.Strings;
	
	public class TableView extends TableAdapter<TableView.TableHandler, Object[]> {
		
		public TableView () {
			super (new TableHandler ());
		}
		
		/*public TableView (Application app) {
			super (app);
		}*/
		
		protected static class TableHandler extends TableAdapter.Handler {
		
		}
		
		@Override
		protected void onHandler (int position) {
			
			if (position > 0) output.append (Strings.LS);
			
			Object[] columns = getItem (position);
			
			for (int i2 = 0; i2 < columns.length; i2++) {
				
				String column = String.valueOf (columns[i2]);
				
				if (position == 0 || position == getLength () - 1 || (title && position == 2)) {
					
					if (position == 0) {
						
						if (i2 == 0)
							output.append (style.getLeftUpperCorner ());
						else
							output.append (style.getCenterUpperCross ());
						
					} else if (position == getLength () - 1) {
						
						if (i2 == 0)
							output.append (style.getLeftLowerCorner ());
						else
							output.append (style.getCenterLowerCross ());
						
					} else if (title) {
						
						if (i2 == 0)
							output.append (style.getTitleLeftLowerCross ());
						else
							output.append (style.getTitleCenterCross ());
						
					}
					
					output.append (new Strings (style.getBorder ()).repeat (length[i2] + 2));
					
				} else {
					
					int spacesLeft, spacesRight, width = (length[i2] - column.length ());
					
					if (width % 2 != 0) {
						
						spacesLeft = ((width - 1) / 2);
						spacesRight = ((width + 1) / 2);
						
					} else spacesLeft = spacesRight = (width / 2);
					
					output.append (style.getColumnSeparator ());
					output.append (new Strings (style.getLeftSpace ()).repeat (spacesLeft + 1));
					output.append (column);
					output.append (new Strings (style.getRightSpace ()).repeat (spacesRight + 1));
					
				}
				
			}
			
			if (position == 0)
				output.append (style.getRightUpperCorner ());
			else if (position == getLength () - 1)
				output.append (style.getRightLowerCorner ());
			else if (title && position == 2)
				output.append (style.getTitleRightLowerCross ());
			else
				output.append (style.getColumnSeparator ());
			
		}
		
		protected int[] length;
		
		protected boolean title = false;
		
		protected Style style = new Style ();
		
		public TableView setTitle (Object... titles) {
			
			length = new int[titles.length];
			
			setRow (new Object[titles.length]);
			setRow (titles);
			setRow (new Object[titles.length]);
			
			title = true;
			
			return this;
			
		}
		
		public TableView setRow (Object... columns) {
			
			if (length == null) {
				
				length = new int[columns.length];
				setRow (new Object[columns.length]);
				
			}
			
			for (int i = 0; i < columns.length; i++) {
				
				String column = String.valueOf (columns[i]);
				
				if (column.length () > length[i])
					length[i] = column.length ();
				
			}
			
			setItem (columns);
			
			return this;
			
		}
		
		public String build () {
			
			setRow (new Object[length.length]);
			
			for (int j = 0; j < getLength (); j++)
				onHandler (j);
			
			return output.toString ();
			
		}
		
		public static class Style {
			
			public String getLeftUpperCorner () {
				return "\u250c";
			}
			
			public String getCenterUpperCross () {
				return "\u252c";
			}
			
			public String getRightUpperCorner () {
				return "\u2510";
			}
			
			public String getRightLowerCorner () {
				return "\u2518";
			}
			
			public String getCenterLowerCross () {
				return "\u2534";
			}
			
			public String getLeftLowerCorner () {
				return "\u2514";
			}
			
			public String getTitleLeftLowerCross () {
				return "\u251c";
			}
			
			public String getTitleCenterCross () {
				return "\u253c";
			}
			
			public String getTitleRightLowerCross () {
				return "\u2524";
			}
			
			public String getColumnSeparator () {
				return "\u2502";
			}
			
			public String getRightSpace () {
				return " ";
			}
			
			public String getLeftSpace () {
				return " ";
			}
			
			public String getBorder () {
				return "\u2500";
			}
			
		}
		
	}