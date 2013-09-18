package com.rogicrew.callstats.models;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.rogicrew.callstats.models.command.GetCallsCommand;
import com.rogicrew.callstats.models.command.ICallHandler;

public class CallModel {
	private List<CallElement> mList = null;
	private long mDurationSum;
	
	public CallModel() {		
	}
	
	public List<CallElement> getElements() {
		return mList;
	}
	
	public long getDurationSum() {		
		return mDurationSum;
	}
	
	private void initSum() {
		mDurationSum = 0;
		for (CallElement el : mList) {
			mDurationSum += el.getDuration();
		}
	}
	
	public void load(final Activity activity, final CallsFilter filter) {
		ICallHandler handler = filter.getHandler();
		handler.init(activity);
		mList = new GetCallsCommand(activity, filter).execute(handler);
		initSum();
		filter.sort(mList);
	}
	
	public GraphViewData[] loadForChart(final Activity activity, final CallsFilter filter) {
		List<GraphViewData> result = new ArrayList<GraphViewData>();
		load(activity, filter);
		for (CallElement el : getElements()) {
			// we need duration in minutes
			long duration = el.getDuration();
			duration = duration  / 3600 * 60 +  (duration / 60) % 60;
			GraphViewData object = new GraphViewData(el.getDateOfCall(), duration); 
			result.add(object);
		}
		
		return result.toArray(new GraphViewData[result.size()]);
	}
}