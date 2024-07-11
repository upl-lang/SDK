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
  
  import upl.util.List;
  import upl.util.Map;
  
  public abstract class Logger {
    
    protected Level level;
    
    public enum Level {
      
      DEBUG,
      WARNING,
      INFO,
      VERBOSE,
      ERROR,
      
    }
    
    protected String name;
    
    public Logger (Class<?> clazz) {
      this (clazz.getSimpleName ());
    }
    
    public Logger (String name) {
      this.name = name;
    }
    
    public abstract void log (Level level, Object... obj);
    public abstract void log (Level level, Object msg, Exception e);
    
    public Logger setLevel (Level level) {
      
      this.level = level;
      
      return this;
      
    }
    
    public void d (Object... msg) {
      i (msg);
    }
    
    public void d (Object msg, Exception e) {
      i (msg, e);
    }
    
    public void i (Object... msg) {
      log (Level.INFO, msg);
    }
    
    public void i (Object msg, Exception e) {
      log (Level.INFO, msg, e);
    }
    
    public void i (Throwable err, String msg, Object... args) {
      log (Level.INFO, err, msg, args);
    }
    
    public void w (List<?> msg) {
      log (Level.WARNING, msg);
    }
    
    public void w (Map<?, ?> msg) {
      log (Level.WARNING, msg);
    }
    
    public void w (Object... msg) {
      log (Level.WARNING, msg);
    }
    
    public void w (byte[] msg) {
      log (Level.WARNING, Arrays.toString (msg));
    }
    
    public void w (StackTraceElement[] msg) {
      log (Level.WARNING, Arrays.implode ("\n", msg));
    }
    
    public void w (Object msg, Exception e) {
      log (Level.WARNING, msg, e);
    }
    
    public void w (Throwable err, String msg, Object... args) {
      log (Level.WARNING, err, msg, args);
    }
    
    public void e (Object... msg) {
      log (Level.ERROR, msg);
    }
    
    public void e (Object msg, Exception e) {
      log (Level.ERROR, msg, e);
    }
    
    public void e (Throwable err, String msg, Object... args) {
      log (Level.ERROR, err, msg, args);
    }
    
    public void v (Object... msg) {
      log (Level.VERBOSE, msg);
    }
    
    public void v (Object msg, Exception e) {
      log (Level.VERBOSE, msg, e);
    }
    
  }