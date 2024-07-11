  /*
   * Copyright (c) 2020 - 2023 O! Interactive
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
  
  import upl.type.Object;
  import upl.util.HashMap;
  import upl.util.Map;
  
  public class Benchmark {
    
    protected static final Map<String, Long> cs = new HashMap<> ();
    
    public static void startTime () {
      startTime ("Benchmark");
    }
    
    public static void startTime (String name) {
      cs.add (name, java.lang.System.currentTimeMillis ());
    }
    
    public static void finishTime () {
      finishTime ("Benchmark");
    }
    
    public static void finishTime (String name) {
      finishTime (name, 2);
    }
    
    public static void finishTime (String name, int round) {
      Object.println (name + ": " + Math.round ((float) (java.lang.System.currentTimeMillis () - cs.get (name)) / 1000, round));
    }
    
  }