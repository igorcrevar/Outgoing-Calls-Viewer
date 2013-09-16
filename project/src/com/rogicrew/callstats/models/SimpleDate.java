package com.rogicrew.callstats.models;

import java.io.Serializable;
import java.util.Calendar;

public class SimpleDate implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2317683244147680196L;

	public SimpleDate(SimpleDate sd){
		set(sd);
	}
	public SimpleDate(Calendar calendar){
		set(calendar);
	}
	public SimpleDate(java.util.Date date){
		set(date.getYear() + 1900, date.getMonth(), date.getDate());
	}
	public SimpleDate(int y, int m, int d){
		set(y, m, d);
	}
	public SimpleDate(){
		this(Calendar.getInstance());
	}

	public int year;
	public int month;
	public int day;

	public Calendar getCalendar(){
		Calendar rv = (Calendar)Calendar.getInstance().clone();
		rv.set(Calendar.DAY_OF_MONTH, day);
		rv.set(Calendar.MONTH, month);
		rv.set(Calendar.YEAR, year);
		return rv;
	}
	
	public void set(int year, int month, int day){
		this.year = year;
		this.month = month;
		this.day = day;
	}
	
	public void set(SimpleDate sd){
		set(sd.year, sd.month, sd.day);
	}
	
	public void set(Calendar calendar){
		set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
	}
	
	public boolean isLessThen(SimpleDate sd){
		return this.year < sd.year
			   ||  this.year == sd.year && this.month < sd.month
			   ||  this.year == sd.year && this.month == sd.month && this.day < sd.day;
	}
	
	public static SimpleDate getNextMonth(SimpleDate date){
		Calendar tmp = date.getCalendar();
		tmp.add(Calendar.MONTH, 1);
		tmp.add(Calendar.DAY_OF_MONTH, -1);
		return new SimpleDate(tmp);
	}
	
	public static SimpleDate getPrevMonthDay(SimpleDate date, int day){
		Calendar tmp = date.getCalendar();
		tmp.set(Calendar.DAY_OF_MONTH, day);
		tmp.add(Calendar.MONTH, -1);
		return new SimpleDate(tmp);
	}
	
	public SimpleDate getStartDateOfPeriod(int firstDayOfPeriod){
		if (this.day > firstDayOfPeriod){
			return new SimpleDate(this.year, this.month, firstDayOfPeriod);
		}
		else if (this.day == firstDayOfPeriod){
			return new SimpleDate(this.year, this.month, this.day);
		}
		
		Calendar cl = this.getCalendar();		
		cl.add(Calendar.MONTH, -1);
		cl.add(Calendar.DAY_OF_MONTH, -this.day + firstDayOfPeriod);
		return new SimpleDate(cl);
	 }
	 
	 public SimpleDate getEndDateOfPeriod(int firstDayOfPeriod){
		if (this.day < firstDayOfPeriod - 1){
			return new SimpleDate(this.year, this.month, firstDayOfPeriod - 1);
		}
		else if (this.day == firstDayOfPeriod - 1){
			return new SimpleDate(this.year, this.month, this.day);
		}
		
		Calendar cl = this.getCalendar();
		cl.add(Calendar.MONTH, 1);
		cl.add(Calendar.DAY_OF_MONTH, -this.day + firstDayOfPeriod - 1);
		return new SimpleDate(cl);
	 }
	
	@Override
	public String toString(){
		return String.format("%d-%d-%d", year, month+1, day);
	}
	
	public long toMiliseconds(boolean isEndOfDay)
	{
		java.util.Date dt = isEndOfDay ? new java.util.Date(year-1900, month, day, 23, 59, 59)
									   : new java.util.Date(year-1900, month, day, 0, 0, 0);
		return dt.getTime();
	}
	
	@Override
	public boolean equals(Object other){
		if (other != null  &&  other instanceof SimpleDate){
			SimpleDate o = (SimpleDate)other;
			return year == o.year  &&  month == o.month  && day == o.day;
		}
		return false;
	}
}