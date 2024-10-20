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
	
	package upl.compiler;
	
	import javax.script.Invocable;
	import javax.script.ScriptEngineManager;
	import javax.script.ScriptException;
	
	public class ScriptEngine extends CompilerAdapter {
		
		protected Invocable invocable;
		
		public ScriptEngine (String engine, String content) throws CompilerException {
			
			super (engine, content);
			
			try {
				
				invocable = (Invocable) new ScriptEngineManager ().getEngineByName (engine);
				((javax.script.ScriptEngine) invocable).eval (content);
				
			} catch (ScriptException e) {
				throw new CompilerException (e);
			}
			
		}
		
		@Override
		public Object invoke (String fn, Object... params) throws CompilerException {
			
			try {
				return invocable.invokeFunction (fn, params);
			} catch (ScriptException | NoSuchMethodException e) {
				throw new CompilerException (e);
			}
			
		}
		
	}