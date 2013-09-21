package com.rogicrew.callstats.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
	
	private interface IChartHelper
	{
		public SortByEnum getSortBy();
		public long generateKey(java.util.Date date);
		public int getCalendarIncType();
	}
	
	private GraphViewData[] loadForChart(final Activity activity, final CallsFilter filter, IChartHelper helper) {
		List<GraphViewData> result = new ArrayList<GraphViewData>();
		filter.sortBy = helper.getSortBy();
		load(activity, filter);
		List<CallElement> elements = getElements();
		int position = 0;
		
		Calendar fromDateCal = filter.fromDate.getCalendar();
		Date toDateTime = filter.toDate.getCalendar().getTime();
		// we must iterate over all days in range
		// if there is data for this particular date we pick calls duration for that day
		// otherwise just put 0 as calls duration
		while (fromDateCal.getTime().compareTo(toDateTime) < 1) {
			long valueY = 0;
			long key = helper.generateKey(new Date(fromDateCal.getTimeInMillis()));
			if (position < elements.size()) {
				CallElement el = elements.get(position);
				long keyForEl = helper.generateKey(new Date(el.getDateOfCall()));
				if (keyForEl == key) {
					long duration = el.getDuration();
					valueY = duration  / 3600 * 60 +  (duration / 60) % 60;
					position++;
				}
			}
			
			GraphViewData object = new GraphViewData(fromDateCal.getTimeInMillis(), valueY); 
			result.add(object);			
			fromDateCal.add(helper.getCalendarIncType(), 1);
		}

		return result.toArray(new GraphViewData[result.size()]);
	}
	
	public GraphViewData[] loadForDayChart(final Activity activity, final CallsFilter filter) {
		return loadForChart(activity, filter, new IChartHelper() {
			@Override
			public SortByEnum getSortBy() {
				return SortByEnum.ByDays;
			}
			@Override
			public long generateKey(Date date) {
				return date.getDate() + date.getMonth() * 100 + date.getYear() * 100 * 100;
			}
			@Override
			public int getCalendarIncType() {
				return Calendar.DAY_OF_YEAR;
			}			
		});
	}
	
	public GraphViewData[] loadForMonthChart(final Activity activity, final CallsFilter filter) {
		return loadForChart(activity, filter, new IChartHelper() {
			@Override
			public SortByEnum getSortBy() {
				return SortByEnum.ByMonths;
			}
			@Override
			public long generateKey(Date date) {
				return date.getMonth() + date.getYear() * 100;
			}
			@Override
			public int getCalendarIncType() {
				return Calendar.MONTH;
			}			
		});
	}
}