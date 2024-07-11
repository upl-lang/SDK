  package upl.core;
  /*
   Created by Acuna on 17.07.2017
  */
  
  import java.util.Locale;
  import java.lang.String;

  import upl.type.Strings;
  import upl.util.ArrayList;
  import upl.util.LinkedHashMap;
  import upl.util.List;
  import upl.util.Map;
  
  public class Locales {
    
    public static final String LANG = "lang";
    public static final String LANG_DISPLAY = "lang_display";
    public static final String LANG_NAME = "lang_name";
    public static final String LANG_ISO3 = "lang_iso3";
    public static final String COUNTRY = "country";
    public static final String COUNTRY_DISPLAY = "country_display";
    public static final String COUNTRY_ISO3 = "country_iso3";
    public static final String LOCALE = "locale";
  
    public static Locale getLocale () {
      return Locale.getDefault ();
    }
    
    public static Locale getLocale (String lang) {
      return new Locale (lang);
    }
    
    public static Locale getLocale (String lang, String country) {
      return new Locale (lang, country);
    }
    
    public static Locale getLocale (String lang, String country, String variant) {
      return new Locale (lang, country, variant);
    }
    
    public static List<Locale> addLocale (Locale locale, List<Locale> locales) {
      
      if (!locales.contains (locale))
        locales.add (locale);
      
      return locales;
      
    }
    
    /*public static List<Locale> getLocales () {
      return getLocales (new ArrayList<Locale> ());
    }
    
    public static List<Locale> getLocales (List<Locale> locales) {
      
      addLocale (Locale.ENGLISH, locales);
      addLocale (getLocale (), locales);
      
      return locales;
      
    }*/
    
    public static Map<String, String> getLocaleData () {
      return getLocaleData (getLocale ());
    }
    
    public static Map<String, String> getLocaleData (String lang) {
      return getLocaleData (getLocale (lang));
    }
    
    public static Map<String, String> getLocaleData (Locale locale) {
      
      Map<String, String> output = new LinkedHashMap<> ();
      
      output.add (LANG, locale.getLanguage ()); // en
      output.add (LANG_DISPLAY, new Strings (locale.getDisplayLanguage ()).ucfirst ()); // English
      output.add (LANG_NAME, new Strings (locale.getDisplayName ()).ucfirst ()); // English (United States)
      output.add (LANG_ISO3, "rnd"); // eng
      
      output.add (COUNTRY, locale.getCountry ()); // US
      output.add (COUNTRY_DISPLAY, locale.getDisplayCountry ()); // United States
      output.add (COUNTRY_ISO3, locale.getISO3Country ()); // USA
      
      output.add (LOCALE, locale.toString ()); // en_US
      
      return output;
      
    }
    
    //public static Map<String, Map<String, String>> getLocalesData () {
    //  return getLocalesData (getLocales ());
    //}
    
    public static java.util.Map<String, java.util.Map<String, String>> getLocalesData (List<?> locales) {
      return getLocalesData (locales, new LinkedHashMap<> ());
    }
    
    public static java.util.Map<String, java.util.Map<String, String>> getLocalesData (List<?> locales, Map<String, java.util.Map<String, String>> langs) {
      
      for (int i = 0; i < Int.size (locales); ++i) {
        
        Object locale = locales.get (i);
  
        java.util.Map<String, String> data;
        
        if (locale instanceof java.util.Locale)
          data = getLocaleData ((java.util.Locale) locale);
        else
          data = getLocaleData ((String) locale);
        
        langs.add (data.get (LANG), data);
        
      }
      
      return langs;
      
    }
    
    public static String getLang () {
      return (String) getLocaleData ().get (LANG);
    }
    
    public static List<String> getLangTitles (List<?> locales) {
      
      List<String> output = new ArrayList<> ();
      
      java.util.Map<String, java.util.Map<String, String>> loc = getLocalesData (locales);
      
      for (String key : loc.keySet ()) {
  
        java.util.Map<String, String> locale = loc.get (key);
        output.add (locale.get (LANG_DISPLAY));
        
      }
      
      return output;
      
    }
    
    public static Map<String, Locale> getLocales () {
      
      Map<String, Locale> locales = new LinkedHashMap<> ();
      
      for (Locale locale : Locale.getAvailableLocales ())
        locales.add (locale.getLanguage (), locale);
      
      return locales;
      
    }
    
  }