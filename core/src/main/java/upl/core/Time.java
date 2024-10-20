	package upl.core;
	/*
	 Created by Acuna on 08.11.2018
	*/
	
	public class Time {
		
		private Calendar calendar;
		protected int seconds;
		
		public Time () {
			this (new Date ().getTimeInMillis (false));
		}
		
		public Time (long time) {
			
			calendar = new Calendar ();
			
			calendar.setTime (new java.util.Date (time));
			
		}
		
		public Time (int seconds) {
			this.seconds = seconds;
		}
		
		/*public Time addMS (int time) {
		
		}
		
		public Time addSecond (int num) {
			addMS (1000 * num);
		}
		
		public Time addMinute (int num) {
			addSecond (60 * num);
		}
		
		public Time addHour (int num) {
			addMinute (60 * num);
		}*/
		
		public long getTime () {
			return calendar.getTimeInMillis ();
		}
	
		public Time addDay () {
			return addDay (1);
		}
		
		public Time addDay (int num) {
			
			calendar.add (Calendar.DATE, num);
			return this;
			
		}
		
		public Time addWeek (int num) {
			return addDay (7 * num);
		}
		
		public Time addMonth (int num) {
			return addWeek (4 * num);
		}
		
		public Time addYear (int num) {
			return addMonth (12 * num);
		}
		
		public long startDayTime () {
			
			calendar.set (Calendar.HOUR_OF_DAY, 0);
			calendar.set (Calendar.MINUTE, 0);
			calendar.set (Calendar.SECOND, 1);
			
			return getTime ();
			
		}
		
		public String toDuration () {
			return toDuration (false);
		}
		
		public String toDuration (boolean hour) {
			
			int h = (seconds / 3600);
			int m = (seconds % 3600) / 60;
			int s = (seconds % 60);
			
			String duration = "";
			
			if (h > 0 || hour) {
				
				if (h < 10) duration += "0";
				duration += h + ":";
				
			}
			
			if (m < 10) duration += "0";
			duration += m + ":";
			
			if (s < 10) duration += "0";
			duration += s;
			
			return duration;
			
		}
		
	}