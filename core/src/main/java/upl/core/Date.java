  package upl.core;
  
  import java.text.ParseException;
  import java.text.SimpleDateFormat;
  import java.util.Calendar;
  import java.util.Locale;
  import java.util.TimeZone;
  
  public class Date extends java.util.Date {
    
    public static String[] formats = new String[] {
      
      "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", // ISO 8601
      "dd.MM.yyyy HH:mm:ss",
      "dd.MM.yyyy",
      "d MMMM yyyy HH:mm",
      "d MMMM yyyy HH:mm:ss",
      "d MMMM yyyy",
      "yyyy/MM/dd - HH:mm:ss",
      "yyyyMMddHHmmss",
      "yyyy.MM.dd",
      "dd.MM.yy HH:mm",
      "dd-MM-yyyy",
      
    };
    
    @Override
    public String toString () {
      return toString (0);
    }
    
    public String toString (boolean isOffset) {
      return toString (0, getTimeInMillis (isOffset));
    }
    
    public String toString (int type) {
      return toString (type, getTimeInMillis ());
    }
    
    public String toString (int type, boolean isOffset) {
      return toString (type, getTimeInMillis (isOffset));
    }
    
    public String toString (int type, java.util.Date date) {
      return toString (type, toTime (date));
    }
    
    public String toString (int type, long time) {
      return toString (type, time, true);
    }
    
    public String toString (int type, long time, boolean offset) {
      return toString (formats[type], time, offset);
    }
    
    public String toString (String format) {
      return toString (format, getTimeInMillis ());
    }
    
    public String toString (String format, long time) {
      return toString (format, time, true);
    }
    
    public String toString (String format, long time, boolean isOffset) {
      return toString (format, getTimeInMillis (time, isOffset), Locale.getDefault ());
    }
    
    public String toString (String format, long date, Locale locale) {
      
      SimpleDateFormat sdf = new SimpleDateFormat (format, locale);
      sdf.setTimeZone (TimeZone.getTimeZone ("UTC"));
      
      return sdf.format (date);
      
    }
    
    public long getTimeInMillis () {
      return getTimeInMillis (false);
    }
    
    public long getTimeInMillis (boolean isOffset) {
      return getTimeInMillis (java.lang.System.currentTimeMillis (), isOffset);
    }
    
    public long getTimeInMillis (long time, boolean isOffset) {
      
      long offset = 0;
      
      if (isOffset) {
        
        java.util.Calendar cal = Calendar.getInstance ();
        offset = cal.getTimeZone ().getOffset (time);
        
      }
      
      return getTimeInMillis (time, offset);
      
    }
    
    public Date () {
      super ();
    }
    
    public Date (long time) {
      setTime (time);
    }
    
    public long getTimeInMillis (long time, long offset) {
      return (time + offset);
    }
    
    public String toTimeStamp (String date, int type) throws ParseException {
      return toTimeStamp (date, formats[type]);
    }
    
    public String toTimeStamp (String date, String pattern) throws ParseException {
      return toTimeStamp (date, pattern, Locale.getDefault ());
    }
    
    public String toTimeStamp (String date, String pattern, Locale locale) throws ParseException {
      
      long time = toTime (date, pattern, locale);
      return toString (0, time);
      
    }
    
    public long toTime (String date, int type) throws ParseException {
      return toTime (date, formats[type]);
    }
    
    public long toTime (String date, String pattern) throws ParseException {
      return toTime (date, pattern, Locale.getDefault ());
    }
    
    public long toTime (String date, String pattern, Locale locale) throws ParseException {
      return toDate (date, pattern, locale).getTime ();
    }
    
    public java.util.Date toDate (String date, String pattern, Locale locale) throws ParseException {
      return new SimpleDateFormat (pattern, locale).parse (date);
    }
    
    public long toTime (java.util.Date date) {
      return getTimeInMillis (date.getTime (), false);
    }
    
    public long getDifference (Date date) {
      return getDifference (date.getTimeInMillis ());
    }
    
    public long getDifference (long time) {
      return (getTimeInMillis () - time);
    }
    
  }