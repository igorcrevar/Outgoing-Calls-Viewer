package com.rogicrew.callstats.models.command;

import java.util.List;
import android.app.Activity;
import com.rogicrew.callstats.models.CallElement;

public class DefaultCallHandler implements ICallHandler {

	@Override
	public void init(Activity activity) {
	}

	@Override
	public void execute(String name, String phone, long duration, long dateOfCall, List<CallElement> list) {
		CallElement element = new CallElement(name, phone, duration, dateOfCall);
		list.add(element);
	}
}
