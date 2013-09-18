package com.rogicrew.callstats.models.command;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import com.rogicrew.callstats.models.CallElement;
import com.rogicrew.callstats.models.SimpleDate;

public class GroupByDayCallHandler implements ICallHandler {
	private Map<Long, CallElement> mMap;
	private SimpleDateFormat mSimpleDateFormat;
	
	public GroupByDayCallHandler() {
	}
	
	@Override
	public void init(Activity activity) {
		mMap = new HashMap<Long, CallElement>();
		mSimpleDateFormat = new SimpleDateFormat("EEEE");
	}

	@Override
	public void execute(String name, String phone, long duration, long dateOfCall, List<CallElement> list) {
		java.util.Date date = new java.util.Date(dateOfCall);
		long key = getKey(date);
		
		CallElement element = mMap.get(key);
		if (element != null) {
			element.incDuration(duration);
			element.setDateOfCall(dateOfCall);
		}
		else {
			element = new CallElement(mSimpleDateFormat.format(date), "", duration, new SimpleDate(date).toMiliseconds(false));
			list.add(element);
			mMap.put(key, element);
		}
	}
	
	private int getKey(java.util.Date date) {
		return date.getDate() + date.getMonth() * 100 + date.getYear() * 100 * 100;
	}
	
}
