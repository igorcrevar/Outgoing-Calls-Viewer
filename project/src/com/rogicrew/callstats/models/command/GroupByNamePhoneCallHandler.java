package com.rogicrew.callstats.models.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import com.rogicrew.callstats.models.CallElement;

public class GroupByNamePhoneCallHandler implements ICallHandler {
	private boolean mIsContactNameKey;
	private Map<String, CallElement> mMap; 
	
	public GroupByNamePhoneCallHandler(boolean isContactNameKey) {
		this.mIsContactNameKey = isContactNameKey;
	}
	
	@Override
	public void init(Activity activity) {
		mMap = new HashMap<String, CallElement>();
	}

	@Override
	public void execute(String name, String phone, long duration, long dateOfCall, List<CallElement> list) {
		String key = mIsContactNameKey ? name : phone;
		CallElement callElement = mMap.get(key);
		if (callElement == null) {
			callElement = new CallElement(name, phone, duration, dateOfCall);
			list.add(callElement);
			mMap.put(key, callElement);
		}
		else {
			callElement.incDuration(duration);
			callElement.setDateOfCall(dateOfCall);
		}
	}

}
