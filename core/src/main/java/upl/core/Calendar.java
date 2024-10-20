	package upl.core;
	
	import java.text.SimpleDateFormat;
	import java.util.Date;
	import java.util.GregorianCalendar;
	import java.util.Locale;
	
	public class Calendar extends GregorianCalendar {
		
		public long time;
		
		public Calendar () {
			this (new upl.core.Date ().getTimeInMillis ());
		}
		
		public Calendar (long time) {
			setTime (new Date (time));
		}
		
		public Calendar (Calendar calendar) {
			setTimeInMillis (calendar.getTimeInMillis ());
		}
		
		public Calendar (Date date) {
			setTime (date);
		}
		
		public Calendar setTime (long time) {
			
			this.time = time;
			return this;
			
		}
		
		public Calendar addNewDay () {
			return addNewDay (1);
		}
		
		public Calendar addNewDay (int day) {
			
			add (Calendar.DAY_OF_MONTH, day);
			return this;
			
		}
		
		public boolean equals (Calendar dstCal) {
			
			return equals (
				
				get (Calendar.YEAR),
				get (Calendar.MONTH),
				get (Calendar.DAY_OF_MONTH),
				
				dstCal.get (Calendar.YEAR),
				dstCal.get (Calendar.MONTH),
				dstCal.get (Calendar.DAY_OF_MONTH)
				
			);
			
		}
		
		private boolean equals (int lastYear, int lastMonth, int lastDay, int year, int month, int day) {
			
			//Log.w (lastYear, year, lastMonth, month, lastDay, day);
			return (lastYear == year && lastMonth == month && lastDay == day);
			
		}
		
		@Override
		public String toString () {
			return toString (1);
		}
		
		public String toString (String format) {
			return toString (format, Locale.getDefault ());
		}
		
		public String toString (int format) {
			return toString (upl.core.Date.formats[format]);
		}
		
		public String toString (String format, Locale locale) {
			
			SimpleDateFormat format1 = new SimpleDateFormat (format, locale);
			return format1.format (getTime ());
			
		}
		
		public int getDays () {
			return (int) (time / 1000 / 86400);
		}
		
		public int getHours () {
			return (int) (((time / 1000) % 86400) / 3600);
		}
		
		public int getMinutes () {
			return (int) ((((time / 1000) % 86400) % 3600) / 60);
		}
		
		public int getSeconds () {
			return (int) ((((time / 1000) % 86400) % 3600) % 60);
		}
		
		public int diff (Calendar dstCal) {
			return (int) (getTimeInMillis () - dstCal.getTimeInMillis ());
		}
		
		public int diffDays (Calendar dstCal) {
			
			time = diff (dstCal);
			return getDays ();
			
		}
		
	}