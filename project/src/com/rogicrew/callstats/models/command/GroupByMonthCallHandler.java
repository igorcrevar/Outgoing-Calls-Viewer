package com.rogicrew.callstats.models.command;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.rogicrew.callstats.models.CallElement;

public class GroupByMonthCallHandler implements ICallHandler {
	private Map<Long, CallElement> mMap;
	private SimpleDateFormat mSimpleDateFormat;
	private int mFirstDayOfMonth;
	private Calendar mCalendar;
	
	public GroupByMonthCallHandler() {
	}
	
	@Override
	public void init(Activity activity) {
		mMap = new HashMap<Long, CallElement>();
		mSimpleDateFormat = new SimpleDateFormat("MMMM");
		mCalendar = Calendar.getInstance();
		// TODO: hard coupling
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext());
		mFirstDayOfMonth = Integer.parseInt(preferences.getString("start_of_month", "1"));
	}

	@Override
	public void execute(String name, String phone, long duration, long dateOfCall, List<CallElement> list) {
		// we must find first day of "month" for dateOfCall
		java.util.Date date = new java.util.Date(dateOfCall);
		if (date.getDate() < mFirstDayOfMonth){
			mCalendar.setTime(date);
			mCalendar.add(Calendar.MONTH, -1); //if lower than min day then belongs to prev month
			date = mCalendar.getTime();
		}
		
		date.setDate(mFirstDayOfMonth); // it should be always first day of "month"
		long key = date.getMonth() + date.getYear() * 20;
		CallElement element = mMap.get(key);		
		if (element != null) {
			element.incDuration(duration);
		}
		else {
			element = new CallElement(
						String.format("%s, %s", mSimpleDateFormat.format(date.getTime()), date.getYear() + 1900), 
						"", 
						duration, 
						date.getTime()); // date.getTime() instead of dateOfCall because we need time for first day in month
			mMap.put(key, element);
			list.add(element);
		}
	}
}
