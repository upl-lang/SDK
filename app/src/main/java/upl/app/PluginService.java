  /*
  * Copyright (C) 2020 - 2023 O! Interactive
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
  
  package upl.app;
  
  import java.lang.module.Configuration;
  import java.lang.module.ModuleDescriptor;
  import java.lang.module.ModuleFinder;
  import java.lang.module.ModuleReference;
  import java.lang.reflect.InvocationTargetException;
  import java.nio.file.Path;
  import java.util.ArrayList;
  import java.util.Collection;
  
  import java.util.List;
  import java.util.ServiceLoader;
  import upl.util.HashMap;
  import upl.util.Map;
  
  public class PluginService<C extends Plugin> extends upl.type.Object {
    
    protected Map<Object, C> pluginsMap;
    
    protected int flags;
    
    protected Application app;
    
    public static class Options {
      
      public static int ADD_AS_NAME = 0;
      public static int ADD_AS_CLASS = 1;
      
    }
    
    public PluginService (Application app) {
      this (app, Options.ADD_AS_CLASS);
    }
    
    public PluginService (Application app, int flags) {
      
      this.flags = flags;
      this.app = app;
      
    }
    
    public PluginService (int flags) {
      this.flags = flags;
    }
    
    /*public Collection<C> getPlugins () {
      return getPlugins ("plugins");
    }*/
    
    @SuppressWarnings ("unused")
    public Collection<C> getPlugins (String dir) {
      return getPlugins (/*Path.of (dir)*/null);
    }
    
    @SuppressWarnings ({"unchecked", "unused"})
    public Collection<C> getPlugins () {
      
      if (pluginsMap == null) {
        
        ModuleFinder pluginsFinder = ModuleFinder.of ((Path) null);
        
        List<String> plugins = new ArrayList<> ();
        
        for (ModuleReference module : pluginsFinder.findAll ()) {
          
          ModuleDescriptor descriptor = module.descriptor ();
          
          if (descriptor != null && descriptor.name () != null)
            plugins.add (descriptor.name ());
          
        }
        
        Configuration pluginsConfig = ModuleLayer.boot ().configuration ().resolve (pluginsFinder, ModuleFinder.of (), plugins);
        
        for (Plugin obj : ServiceLoader.load (ModuleLayer.boot ().defineModulesWithOneLoader (pluginsConfig, ClassLoader.getSystemClassLoader ()), Plugin.class)) {
          
          if (flags == Options.ADD_AS_CLASS)
            addClass ((C) obj);
          else
            add ((C) obj);
          
        }
        
      }
      
      return pluginsMap.values ();
      
    }
    
    @SuppressWarnings ("return")
    public PluginService<C> add (C obj) {
      
      if (pluginsMap == null)
        pluginsMap = new HashMap<> ();
      
      pluginsMap.add (obj.getName (), obj);
      
      return this;
      
    }
    
    public PluginService<C> addClass (C obj) {
      
      if (pluginsMap == null)
        pluginsMap = new HashMap<> ();
      
      pluginsMap.add (obj.getClass (), obj);
      
      return this;
      
    }
    
    public C get (Object name) {
      return pluginsMap.get (name);
    }
    
    @SuppressWarnings ("unchecked")
    protected C getObject (String name) throws ClassNotFoundException {
      
      try {
        
        Class<C> clazz = (Class<C>) Class.forName (getClass ().getPackage ().getName () + "." + name + "." + getClass ().getSimpleName ());
        
        return clazz.getDeclaredConstructor (Application.class).newInstance (app);
        
      } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
        throw new ClassNotFoundException ("Class " + getClass ().getSimpleName () + " not found");
      }
      
    }
    
    public C get () {
      
      try {
        return getObject (app.getPlatform () + "." + app.getPlatform ().getName ());
      } catch (ClassNotFoundException e) {
        
        try {
          return getObject (app.getPlatform ().getName ());
        } catch (ClassNotFoundException e2) {
          return null;
        }
        
      }
      
    }
    
    public C getOrThrow () throws ClassNotFoundException {
      
      try {
        return getObject (app.getPlatform () + "." + app.getPlatform ().getName ());
      } catch (ClassNotFoundException e) {
        return getObject (app.getPlatform ().getName ());
      }
      
    }
    
  }