	package upl.core;
	/*
	 Created by Acuna on 17.07.2017
	*/
	
	import upl.json.JSONArray;
	import upl.json.JSONException;
	import upl.json.JSONObject;
	
	import java.io.File;
	import java.io.IOException;
	import java.util.concurrent.TimeUnit;
	import java.util.regex.Matcher;
	import java.util.regex.Pattern;
	import upl.exceptions.ConsoleException;
	import upl.util.ArrayList;
	import upl.util.List;
	
	public class System {
		
		public static final String version = "1.3";
		
		public static void sleep (int time) {
			
			try {
				TimeUnit.SECONDS.sleep (time);
			} catch (InterruptedException e) {
				// empty
			}
			
		}
		
		public static void sleepMLS (int time) {
			
			try {
				TimeUnit.MILLISECONDS.sleep (time);
			} catch (InterruptedException e) {
				// empty
			}
			
		}
		
		public static List<String> exec (String shell, String cmd) throws ConsoleException {
			
			Console exec = new Console ();
			exec.shell (shell);
			
			return exec.query (cmd);
			
		}
		
		public static List<String> getPids (String id) throws ConsoleException {
			return getProcessData (1, id);
		}
		
		public static List<String> getPids (String[] ids) throws ConsoleException {
			return getProcessData (1, ids);
		}
		
		public static List<String> getProcessData (int type) throws ConsoleException {
			return getProcessData (type, new String[0]);
		}
		
		public static List<String> getProcessData (int type, String id) throws ConsoleException {
			return getProcessData (type, new String[] {id});
		}
		
		public static List<String> getProcessData (final int type, final String[] ids) throws ConsoleException {
			
			final List<String> output = new ArrayList<> ();
			
			Console exec = new Console (new Console.ProcListener () {
				
				@Override
				public void onProcess (String line, int i) {
					
					if (i > 0) {
						
						String[] data = new upl.type.Strings (line).pregExplode ("\\s+");
						
						if (Int.size (ids) > 0) {
							
							for (String id : ids)
								if (id.equals (data[8]))
									output.add (data[type]);
							
						} else output.add (data[type]);
						
					}
					
				}
				
			});
			
			exec.query ("ps");
			
			return output;
			
		}
		
		public static List<String> killProcess (String id) throws ConsoleException {
			return killProcess (new String[] {id}, new ArrayList<String> ());
		}
		
		public static List<String> killProcess (String id, List<String> cmds) throws ConsoleException {
			return killProcess (new String[] {id}, cmds);
		}
		
		public static List<String> killProcess (String[] ids, List<String> cmds) throws ConsoleException {
			
			List<String> output = getPids (ids);
			
			if (Int.size (output) > 0)
				cmds.add ("kill -9 " + output.implode (" "));
			
			return cmds;
			
		}
		
		public static List<String[]> getMountPoint (final String file) throws ConsoleException {
			
			final List<String[]> output = new ArrayList<> ();
			
			Console exec = new Console (new Console.ProcListener () {
				
				@Override
				public void onProcess (String line, int i) {
					
					List<String> data = new upl.type.Strings (line).explode (" ");
					
					if (data.get (1).equals (file))
						output.add (data.toArray (new String[0]));
					
				}
				
			});
			
			exec.query ("cat /proc/mounts");
			
			return output;
			
		}
		
		public static String debug (String msg) {
			return msg;
		}
		
		public static String debug (List<?> msg) {
			return msg.implode ("\n");
		}
		
		public static String debug (JSONArray... msg) {
			
			StringBuilder output = new StringBuilder ();
			
			for (JSONArray obj : msg) {
				
				try {
					output.append (obj.toString (2));
				} catch (JSONException e) {
					output.append (obj);
				}
				
				output.append ("\n");
				
			}
			
			return output.toString ();
			
		}
		
		public static String debug (JSONObject... msg) {
			
			StringBuilder output = new StringBuilder ();
			
			for (JSONObject obj : msg) {
				
				try {
					output.append (obj.toString (2));
				} catch (JSONException e) {
					output.append (obj);
				}
				
				output.append ("\n");
				
			}
			
			return output.toString ();
			
		}
		
		public static boolean checkMountPoint (String file) throws ConsoleException {
			return (Int.size (getMountPoint (file)) > 0);
		}
		
		public static List<File> findBinary (String binaryName) {
			
			List<File> files = new ArrayList<> ();
			
			String[] paths = {
				
				"/sbin/",
				"/system/bin/",
				"/system/xbin/",
				"/data/local/xbin/",
				"/data/local/bin/",
				"/system/sd/xbin/",
				"/system/bin/failsafe/",
				"/data/local/",
				"/su/bin/",
				
			};
			
			for (String path : paths) {
				
				File file = new File (path + binaryName);
				if (file.exists ()) files.add (file);
				
			}
			
			return files;
			
		}
		
		public static DirData getDirData () {
			return new DirData ();
		}
		
		public static List<String> setDirChmod (File folder, int chmod) throws ConsoleException {
			
			final List<String> errors = new ArrayList<> ();
			
			try {
				
				DirData data = getDirData ().get (folder);
				
				if (data.error.equals ("")) {
					
					if (data.uid != null && data.gid != null) {
						
						List<String> cmds = new ArrayList<> ();
						cmds = setDirChmod (folder, chmod, cmds, data);
						
						Console exec = new Console (new Console.Listener () {
							
							@Override
							public void onExecute (String line, int i) {
							}
							
							@Override
							public void onSuccess (String line, int i) {
							}
							
							@Override
							public void onError (String line, int i) {
								errors.add (line);
							}
							
						});
						
						exec.shell (Console.su);
						exec.query (cmds);
						
					} else throw new IOException ("Can't get folder ids: " + folder);
					
				} else throw new IOException (data.error);
				
			} catch (IOException e) {
				throw new ConsoleException (e);
			}
			
			return errors;
			
		}
		
		public static List<String> setDirChmod (File folder, int chmod, List<String> cmds, DirData data) {
			
			//if (Device.box ().equals ("toybox")) {
			
			cmds.add ("chown " + data.uid + ":" + data.gid + " " + folder);
			cmds.add ("chmod " + chmod + " " + folder);
				
				/*} else cmds.add ("sh -c 'for dir in " + folder + "/*; do" +
										     "	if " + Device.box () + " test `" + Device.box () + " basename $dir` != \"lib\";" +
										     "		then" +
										     "			" + Device.box () + " chown -R " + data.uid + ":" + data.gid + " $dir;" +
										     "			" + Device.box () + " chmod -R " + chmod + " $dir;" +
										     "	fi;" +
										     "done'");*/
			
			return cmds;
			
		}
		
		public static List<String> setFilesChmod (String folder, int chmod, List<String> cmds) {
			
			cmds.add ("find " + folder + " -type f | xargs chmod " + chmod);
			
			return cmds;
			
		}
		
		public static class DirData {
			
			public String uid, gid, chmod, error = "", path, symlink, shell = "";
			
			private DirData get (File folder) throws ConsoleException {
				
				Console exec = new Console (new Console.Listener () {
					
					@Override
					public void onExecute (String line, int i) {
					}
					
					@Override
					public void onSuccess (String line, int i) {
						matcher (line);
					}
					
					@Override
					public void onError (String line, int i) {
						error += line;
					}
					
				});
				
				exec.shell (shell);
				exec.query (shell (folder, new ArrayList<String> ()));
				
				return this;
				
			}
			
			public List<String> shell (File folder, List<String> cmds) {
				
				cmds.add ("stat \"" + folder + "\"");
				return cmds;
				
			}
			
			public DirData matcher (String line) {
				
				Matcher mFile = Pattern.compile ("File:\\s*(.+)").matcher (line);
				Matcher mUid = Pattern.compile ("Uid:\\s*\\(\\s*(\\d+)").matcher (line);
				Matcher mGid = Pattern.compile ("Gid:\\s*\\(\\s*(\\d+)").matcher (line);
				Matcher mChmod = Pattern.compile ("Access:\\s*\\(\\s*(\\d+)").matcher (line);
				
				if (mFile.find ()) path = mFile.group (1).replace ("'", "");
				if (mUid.find ()) uid = mUid.group (1);
				if (mGid.find ()) gid = mGid.group (1);
				if (mChmod.find ()) chmod = mChmod.group (1);
				
				List<String> file = new upl.type.Strings (path).explode ("->");
				if (Int.size (file) > 1) symlink = file.get (1).replace (" ", "").replace ("'", "");
				
				return this;
				
			}
			
		}
		
		public interface ExceptionListener {
			void uncaughtException (java.lang.Thread t, Throwable e);
			
		}
		
		public static class ExceptionHandler implements Thread.UncaughtExceptionHandler {
			
			private ExceptionListener listener;
			private Thread.UncaughtExceptionHandler handler;
			
			public ExceptionHandler (ExceptionListener listener) {
				
				this.listener = listener;
				this.handler = Thread.getDefaultUncaughtExceptionHandler ();
				
			}
			
			@Override
			public void uncaughtException (java.lang.Thread thread, Throwable throwable) {
				
				listener.uncaughtException (thread, throwable);
				if (handler != null) handler.uncaughtException (thread, throwable);
				
			}
			
		}
		
	}