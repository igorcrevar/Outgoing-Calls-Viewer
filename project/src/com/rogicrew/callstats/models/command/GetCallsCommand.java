package com.rogicrew.callstats.models.command;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.rogicrew.callstats.models.CallElement;
import com.rogicrew.callstats.models.CallsFilter;
import com.rogicrew.callstats.utils.Utils;

public class GetCallsCommand {
	private Activity mActivity;
	private CallsFilter mFilter;
	
	public GetCallsCommand(Activity activity, CallsFilter filter) {
		this.mActivity = activity;
		this.mFilter = filter;		
	}
	
	public List<CallElement> execute(ICallHandler callHandler) {
		ArrayList<String> values = new ArrayList<String>();
		
		// TODO: hard dependancy 
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivity.getBaseContext());
		int durationRoundingOption = Integer.parseInt(preferences.getString("minute_rounding", "0"));
	
		// init call handler
		callHandler.init(mActivity);
		
		ArrayList<CallElement> result = new ArrayList<CallElement>(); 
		StringBuilder where = new StringBuilder();
		
		// we need only outgoing calls
		where.append(android.provider.CallLog.Calls.TYPE).append("=?");
		values.add(String.valueOf(android.provider.CallLog.Calls.OUTGOING_TYPE));
		// populate other filters
		mFilter.appendFilter(where, values);
			
		String[] fields = new String[] {
		        android.provider.CallLog.Calls.NUMBER, 
		        android.provider.CallLog.Calls.CACHED_NAME,
		        android.provider.CallLog.Calls.DURATION,
		        android.provider.CallLog.Calls.DATE,
		};
		
		Cursor cursor = mActivity.getContentResolver().query(
					        android.provider.CallLog.Calls.CONTENT_URI,
					        fields,
					        where.toString(),
					        values.toArray(new String[]{}),
					        mFilter.getSortByString());
		
		if(cursor.moveToFirst()) {
			do {
				long duration = cursor.getLong(2); 
				//skip calls never made(just dial and immediately hang on)
				if (duration == 0) {
					continue;
				}
				
				String phone = cursor.getString(0);
				String name = cursor.getString(1);
				duration = Utils.getDuration(duration, durationRoundingOption);
				long dateOfCall = cursor.getLong(3);
				
				// call handler - it will populate result list
				callHandler.execute(name, phone, duration, dateOfCall, result);				
			} 
			while (cursor.moveToNext());	
		}	
		
		cursor.close();
		return result;
	}
}
