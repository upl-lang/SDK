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
  
  package upl.loggers;
  
  import java.lang.Object;
  import upl.core.Log;
  import upl.core.Logger;
  import upl.util.HashMap;
  import upl.util.List;
  import upl.util.Map;
  
  public class Console extends Logger {
    
    protected java.util.logging.Logger logger;
    
    protected Map<Level, java.util.logging.Level> levels = new HashMap<> ();
    
    public Console () {
      this ("UPL");
    }
    
    public Console (String name) {
      
      super (name);
      
      logger = java.util.logging.Logger.getLogger (name);
      
      /*ConsoleHandler handler = new ConsoleHandler ();
      
      handler.setFormatter (new SimpleFormatter () {
        
        @Override
        public synchronized String format (LogRecord record) {
          
          return String.format (
            "[%1$tF %1$tT] [%2$-7s] %3$s %n",
            new Date (record.getMillis ()),
            record.getLevel ().getLocalizedName (),
            record.getMessage ()
          );
          
        }
        
      });
      
      logger.addHandler (handler);*/
      
      levels.add (Level.DEBUG, java.util.logging.Level.CONFIG);
      levels.add (Level.WARNING, java.util.logging.Level.WARNING);
      levels.add (Level.INFO, java.util.logging.Level.INFO);
      levels.add (Level.VERBOSE, java.util.logging.Level.FINEST);
      levels.add (Level.ERROR, java.util.logging.Level.SEVERE);
      
    }
    
    public Console (Class<?> clazz) {
      this (clazz.getSimpleName ());
    }
    
    @Override
    public void log (Level level, Object... obj) {
      logger.log (levels.get (level), Log.msg (obj));
    }
    
    @Override
    public void log (Level level, Object msg, Exception e) {
      logger.log (levels.get (level), Log.msg (msg), e);
    }
    
    protected void log (Level level, List<?> msg) {
      log (level, msg.implode (", "));
    }
    
    protected void log (Level level, Map<?, ?> msg) {
      log (level, msg.implode ());
    }
    
    /*protected void log (Level level, Throwable err, String msg, Object... args) {
      logger.log (level, format (msg, args), err);
    }
    
    public void test () {
      
      log (Level.ALL, "all");
      log (Level.SEVERE, "severe");
      log (Level.WARNING, "warn");
      log (Level.INFO, "info");
      log (Level.OFF, "off");
      log (Level.CONFIG, "config");
      log (Level.FINE, "fine");
      log (Level.FINER, "finer");
      log (Level.FINEST, "finest");
      
    }*/
    
    public enum Color {
      
      // Color end string, color reset
      RESET ("\033[0m"),
      
      // Regular Colors. Normal color, no bold, background color etc.
      BLACK ("\033[0;30m"),    // BLACK
      RED ("\033[0;31m"),      // RED
      GREEN ("\033[0;32m"),    // GREEN
      YELLOW ("\033[0;33m"),   // YELLOW
      BLUE ("\033[0;34m"),     // BLUE
      MAGENTA ("\033[0;35m"),  // MAGENTA
      CYAN ("\033[0;36m"),     // CYAN
      WHITE ("\033[0;37m"),    // WHITE
      
      // Bold
      BLACK_BOLD ("\033[1;30m"),   // BLACK
      RED_BOLD ("\033[1;31m"),     // RED
      GREEN_BOLD ("\033[1;32m"),   // GREEN
      YELLOW_BOLD ("\033[1;33m"),  // YELLOW
      BLUE_BOLD ("\033[1;34m"),    // BLUE
      MAGENTA_BOLD ("\033[1;35m"), // MAGENTA
      CYAN_BOLD ("\033[1;36m"),    // CYAN
      WHITE_BOLD ("\033[1;37m"),   // WHITE
      
      // Underline
      BLACK_UNDERLINED ("\033[4;30m"),     // BLACK
      RED_UNDERLINED ("\033[4;31m"),       // RED
      GREEN_UNDERLINED ("\033[4;32m"),     // GREEN
      YELLOW_UNDERLINED ("\033[4;33m"),    // YELLOW
      BLUE_UNDERLINED ("\033[4;34m"),      // BLUE
      MAGENTA_UNDERLINED ("\033[4;35m"),   // MAGENTA
      CYAN_UNDERLINED ("\033[4;36m"),      // CYAN
      WHITE_UNDERLINED ("\033[4;37m"),     // WHITE
      
      // Background
      BLACK_BACKGROUND ("\033[40m"),   // BLACK
      RED_BACKGROUND ("\033[41m"),     // RED
      GREEN_BACKGROUND ("\033[42m"),   // GREEN
      YELLOW_BACKGROUND ("\033[43m"),  // YELLOW
      BLUE_BACKGROUND ("\033[44m"),    // BLUE
      MAGENTA_BACKGROUND ("\033[45m"), // MAGENTA
      CYAN_BACKGROUND ("\033[46m"),    // CYAN
      WHITE_BACKGROUND ("\033[47m"),   // WHITE
      
      // High Intensity
      BLACK_BRIGHT ("\033[0;90m"),     // BLACK
      RED_BRIGHT ("\033[0;91m"),       // RED
      GREEN_BRIGHT ("\033[0;92m"),     // GREEN
      YELLOW_BRIGHT ("\033[0;93m"),    // YELLOW
      BLUE_BRIGHT ("\033[0;94m"),      // BLUE
      MAGENTA_BRIGHT ("\033[0;95m"),   // MAGENTA
      CYAN_BRIGHT ("\033[0;96m"),      // CYAN
      WHITE_BRIGHT ("\033[0;97m"),     // WHITE
      
      // Bold High Intensity
      BLACK_BOLD_BRIGHT ("\033[1;90m"),    // BLACK
      RED_BOLD_BRIGHT ("\033[1;91m"),      // RED
      GREEN_BOLD_BRIGHT ("\033[1;92m"),    // GREEN
      YELLOW_BOLD_BRIGHT ("\033[1;93m"),   // YELLOW
      BLUE_BOLD_BRIGHT ("\033[1;94m"),     // BLUE
      MAGENTA_BOLD_BRIGHT ("\033[1;95m"),  // MAGENTA
      CYAN_BOLD_BRIGHT ("\033[1;96m"),     // CYAN
      WHITE_BOLD_BRIGHT ("\033[1;97m"),    // WHITE
      
      // High Intensity backgrounds
      BLACK_BACKGROUND_BRIGHT ("\033[0;100m"),     // BLACK
      RED_BACKGROUND_BRIGHT ("\033[0;101m"),       // RED
      GREEN_BACKGROUND_BRIGHT ("\033[0;102m"),     // GREEN
      YELLOW_BACKGROUND_BRIGHT ("\033[0;103m"),    // YELLOW
      BLUE_BACKGROUND_BRIGHT ("\033[0;104m"),      // BLUE
      MAGENTA_BACKGROUND_BRIGHT ("\033[0;105m"),   // MAGENTA
      CYAN_BACKGROUND_BRIGHT ("\033[0;106m"),      // CYAN
      WHITE_BACKGROUND_BRIGHT ("\033[0;107m");     // WHITE
      
      private final String code;
      
      Color (String code) {
        this.code = code;
      }
      
      @Override
      public String toString () {
        return code;
      }
      
    }
    
  }