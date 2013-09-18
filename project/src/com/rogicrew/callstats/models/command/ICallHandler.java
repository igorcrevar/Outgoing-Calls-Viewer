package com.rogicrew.callstats.models.command;

import java.util.List;
import android.app.Activity;
import com.rogicrew.callstats.models.CallElement;

public interface ICallHandler {
	public void init(Activity activity);
	public void execute(String name, String phone, long duration, long dateOfCall, List<CallElement> list);
}
