package com.rogicrew.callstats.models.command;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.rogicrew.callstats.models.CallElement;
import com.rogicrew.callstats.models.SimpleDate;

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
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext());
		mFirstDayOfMonth = Integer.parseInt(preferences.getString("start_of_month", "1"));
		mCalendar = Calendar.getInstance();		
	}

	@Override
	public void execute(String name, String phone, long duration, long dateOfCall, List<CallElement> list) {
		//retrieve key
		java.util.Date date = new java.util.Date(dateOfCall);
		long key = getKey(date);
		CallElement element = mMap.get(key);
		
		if (element != null){
			element.incDuration(duration);
			element.setDateOfCall(dateOfCall);
		}
		else {
			SimpleDate dt = new SimpleDate(date).getStartDateOfPeriod(mFirstDayOfMonth);
			element = new CallElement(mSimpleDateFormat.format(dt.getCalendar().getTime()),
									  "", duration, dt.toMiliseconds(false));
			mMap.put(key, element);
			list.add(element);
		}
	}
	
	private int getKey(Date dt) {
		if (dt.getDate() < mFirstDayOfMonth){
			mCalendar.setTime(dt);
			mCalendar.add(Calendar.MONTH, -1); //if lower than min day then belongs to prev month
			dt = mCalendar.getTime();
		}
		
		return dt.getMonth() + dt.getYear() * 20;
	}
}
