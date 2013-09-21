package com.rogicrew.callstats.models.command;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import com.rogicrew.callstats.models.CallElement;
import com.rogicrew.callstats.utils.Utils;

public class GroupByDayCallHandler implements ICallHandler {
	private Map<Long, CallElement> mMap;
	private SimpleDateFormat mSimpleDateFormat;
	private Activity mActivity;
	
	public GroupByDayCallHandler() {
	}
	
	@Override
	public void init(Activity activity) {
		mMap = new HashMap<Long, CallElement>();
		mSimpleDateFormat = new SimpleDateFormat("EEEE");
		mActivity = activity;
	}

	@Override
	public void execute(String name, String phone, long duration, long dateOfCall, List<CallElement> list) {
		Date date = new Date(dateOfCall);
		long key = date.getDate() + date.getMonth() * 100 + date.getYear() * 100 * 100;
		
		CallElement element = mMap.get(key);
		if (element != null) {
			element.incDuration(duration);
		}
		else {
			element = new CallElement(mSimpleDateFormat.format(date), 
									  Utils.getFormatedDate(mActivity, dateOfCall), 
									  duration, dateOfCall);
			list.add(element);
			mMap.put(key, element);
		}
	}
}
