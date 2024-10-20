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
	
	package uplx.dsp;
	
	import java.io.ByteArrayOutputStream;
	import java.io.FileNotFoundException;
	import java.io.IOException;
	import java.io.InputStream;
	import upl.core.Hash;
	import upl.core.exceptions.OutOfMemoryException;
	import upl.io.BufferedInputStream;
	import upl.io.DataOutputStream;
	import upl.json.JSONArray;
	import upl.json.JSONObject;
	import upl.type.Strings;
	
	public class File extends upl.core.File {
		
		public String hubsDir, hubName, root;
		public long size = 0;
		
		public File (Object... file) throws FileNotFoundException {
			
			super (new upl.core.File (file));
			
			root = getParent ();
			
		}
		
		public File (InputStream in) {
			super (new BufferedInputStream (in));
		}
		
		protected void processFiles (java.io.File root) throws IOException {
			
			File node = new File (root);
			
			JSONObject file = new JSONObject ();
			
			if (node.isDirectory ()) {
				
				java.io.File[] subNote = node.listFiles ();
				
				if (subNote != null) {
					
					for (java.io.File filename : subNote) {
						
						if (new File (filename).isDirectory ()) {
							
							size = 0; // Collect the size of current folder
							
							processFiles (filename);
							
							file.put ("folder", filename.toString ());
							file.put ("size", size);
							
							String path = Strings.trimStart (filename.toString (), this.root);
							
							File dir = new File (hubsDir, hubName, path.substring (0, DSPServer.FOLDER_LENGTH), path);
							
							dir.makeDir ();
							
						} else processFiles (filename);
						
					}
					
				}
				
			} else {
				
				processFile (root);
				
				String path = Strings.trimStart (root.getParent (), this.root);
				
				file.put ("path", this.root);
				file.put ("file", path);
				file.put ("hash", hash);
				file.put ("parts", parts);
				file.put ("partSize", DSPServer.PART_LENGTH);
				file.put ("size", root.length ());
				
				String name = root.getName ();
				
				File dir = new File (hubsDir, hubName, path.substring (0, DSPServer.FOLDER_LENGTH), path, name);
				
				dir.makeDir ();
				
				new File (hubsDir, hubName, path.substring (0, DSPServer.FOLDER_LENGTH), path, name, name + ".json").write (file.toString (true));
				
			}
			
		}
		
		JSONArray parts = new JSONArray ();
		
		public void process () throws IOException {
			
			JSONObject data = new JSONObject ();
			
			processFiles (this);
			
			data.put ("path", root);
			data.put ("partSize", DSPServer.PART_LENGTH);
			data.put ("hash", rootHash);
			
			String name = getName ();
			
			File dir = new File (hubsDir, hubName, name);
			
			dir.makeDir ();
			
			new File (dir, name + ".info.json").write (data.toString (true));
			
		}
		
		Hash hash, rootHash = new Hash ();
		
		protected void processFile (java.io.File file) throws IOException {
			
			BufferedInputStream in = new BufferedInputStream (file);
			
			in.setBufferLength (DSPServer.PART_LENGTH);
			
			parts = new JSONArray ();
			hash = new Hash ();
			
			size += file.length ();
			
			while (in.isRead ()) {
				
				hash.process (in.buffer, 0, in.readLength);
				rootHash.process (in.buffer, 0, in.readLength);
				
				parts.put (new Hash ().process (in.buffer, 0, in.readLength));
				
			}
			
		}
		
		public void process (String file, JSONObject data, DataOutputStream out) throws IOException, OutOfMemoryException {
			
			File node = new File (hubsDir, hubName, file, file + ".json");
			
			if (node.exists ()) {
				
				if (node.isDirectory ()) {
					
					java.io.File[] subNote = node.listFiles ();
					
					if (subNote != null) {
						
						for (java.io.File filename : subNote)
							process (new File (filename), data, out);
						
					}
					
				} else {
					
					File file = new File (data.getString ());
					processWhole (new JSONObject (file.read ()), out);
					
				}
				
			} else {
			
			}
			
		}
		
		protected void processWhole (JSONObject data, DataOutputStream out) throws IOException {
			
			out.write ("\n"); // \n means next packet will be info packet
			out.write (data.toString ());
			
			BufferedInputStream in = new BufferedInputStream (new File (data.getString ("file")));
			
			in.setBufferLength (DSPServer.PART_LENGTH);
			
			while (in.isRead ()) {
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream ();
				
				baos.write (in.buffer, 0, in.readLength);
				
				out.write (baos.toByteArray ());
				
			}
			
		}
		
		protected void processParts (JSONObject data, DataOutputStream out) throws IOException {
			
			out.write ("\n"); // \n means next packet will be info packet
			out.write (data.toString ());
			
			BufferedInputStream in = new BufferedInputStream (new File (data.getString ("file")));
			
			in.setBufferLength (DSPServer.PART_LENGTH);
			
			while (in.isRead ()) {
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream ();
				
				baos.write (in.buffer, 0, in.readLength);
				
				out.write (baos.toByteArray ());
				
			}
			
		}
		
	}