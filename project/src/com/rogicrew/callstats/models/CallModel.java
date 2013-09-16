package com.rogicrew.callstats.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.rogicrew.callstats.utils.Utils;

public class CallModel{
	public static class Filter implements Cloneable, Serializable
	{
		private static final long serialVersionUID = -2404468916762266502L;
		public SimpleDate fromDate; 
		public SimpleDate toDate; 
		public SortByEnum sortBy;
		public String phone;
		public String contactName;
		public Object tag; //additonal info
		public Filter(){
			fromDate = toDate = null;
			contactName = phone = null;
			sortBy = SortByEnum.NameDurationDesc;
			tag = null;
		}
		//copy constructor
		public Filter(final Filter filter){
			copyFrom(filter);
		}		
		public Filter clone() throws CloneNotSupportedException {
             Filter newFilter = new Filter(this);
             return newFilter;
		}
		public void copyFrom(final Filter filter){
			fromDate = new SimpleDate(filter.fromDate);
            toDate = new SimpleDate(filter.toDate);
            sortBy = filter.sortBy;
            phone = filter.phone;
            contactName = filter.contactName;
            tag = filter.tag; //string are immutable but tag must be handled by programmer!
		}
	}
	
	public static class CallElement {
		public String name;
		public String phone;
		public long duration; //InSeconds;
		public long dateOfCall; //in miliseconds
		@Override
		public boolean equals(Object o){
			if (o != null && o instanceof CallElement){
				//if phones are equal than its same!
				CallElement ceo = (CallElement)o;
				return ceo.phone.equals(phone) && ceo.name.equals(name); 
			}
			return false;
		}
		@Override
		public int hashCode() {
			int hash = 1 * 31 + (phone != null ? phone.hashCode() : 0);
		    hash = hash * 31 + (phone != null ? name.hashCode() : 0);
			return hash;
		}
	}
	
	public class CallElementDurationComparator implements Comparator<CallElement> {
	    @Override
	    public int compare(CallElement o1, CallElement o2) {
	        if (o1.duration < o2.duration){
	        	return -1;
	        }
	        return o1.duration > o2.duration ? 1 : 0;
	    }
	}
	
	public class CallElementDurationReverseComparator implements Comparator<CallElement> {
	    @Override
	    public int compare(CallElement o1, CallElement o2) {
	        if (o1.duration < o2.duration){
	        	return 1;
	        }
	        return o1.duration > o2.duration ? -1 : 0;
	    }
	}
	
	public class CallElementDateReverseComparator implements Comparator<CallElement> {
	    @Override
	    public int compare(CallElement o1, CallElement o2) {
	        if (o1.dateOfCall < o2.dateOfCall){
	        	return 1;
	        }
	        return o1.dateOfCall > o2.dateOfCall ? -1 : 0;
	    }
	}
	
	private List<CallElement> mList = null;
	private int mDurationRoundingOption;
	private int mFirstDayOfMonth;
	private long mDurationSum;
	private SimpleDateFormat mSimpleDateFormatMonth = null;
	private SimpleDateFormat mSimpleDateFormatDay = null;
	
	private SimpleDateFormat getSimpleDateFormatMonth()
	{
		if (mSimpleDateFormatMonth == null){
			mSimpleDateFormatMonth = new SimpleDateFormat("MMMM");
		}
		return mSimpleDateFormatMonth;		
	}
	
	private SimpleDateFormat getSimpleDateFormatDay()
	{
		if (mSimpleDateFormatDay == null){
			mSimpleDateFormatDay = new SimpleDateFormat("EEEE");
		}
		return mSimpleDateFormatDay;		
	}
	
	public List<CallElement> getElements(){
		return mList;
	}
	
	public long getDurationSum() {		
		return mDurationSum;
	}
	
	public CallModel()
	{		
	}
	
	private void loadNameDurationCase(Activity activity, final Filter filter) {
		ReadPref(activity);
		Cursor callCursor = executeQuery(activity, getDefaultFields(), getSortByString(filter.sortBy), filter);
		mDurationSum = 0;
		mList = new ArrayList<CallElement>();
		Map<String, Integer> map = new HashMap<String, Integer>(50); 
		//depending on filter key in hashmap will be contactname or phone number
		boolean isContactNameKey = !Utils.isNullOrEmpty(filter.contactName);
		
		if(callCursor.moveToFirst()){
			do{
				//skip calls never made(just dial and imediatelly hang on)
				long duration = callCursor.getLong(2); 
				if (duration == 0){
					continue;
				}
				String phone = callCursor.getString(0);
				String name = callCursor.getString(1);
				String key = isContactNameKey ? name : phone;
				long calcDuration = getDuration(duration);
				
				Integer pos = map.get(key);
				if (pos == null){
					map.put(key, mList.size()); //add to map position of this element
					CallElement el = new CallElement();
					mList.add(el);
					el.name = name;
					el.phone = phone;
					el.dateOfCall = callCursor.getLong(3);
					el.duration = calcDuration;
					mDurationSum += calcDuration;			
				}
				else{
					CallElement el = mList.get(pos);
					el.duration += calcDuration;
					mDurationSum += calcDuration;
				}
				
			} while (callCursor.moveToNext());			
		}		
		
		if (filter.sortBy == SortByEnum.NameDurationDesc){
			Collections.sort(mList, new CallElementDurationReverseComparator());
		}
		else
		{
			Collections.sort(mList, new CallElementDurationComparator());
		}
		
		callCursor.close();
	}
	
	private void loadDefaultCase(Activity activity, final Filter filter) {
		ReadPref(activity);
		Cursor callCursor = executeQuery(activity, getDefaultFields(), getSortByString(filter.sortBy), filter); 
		mDurationSum = 0;
		mList = new ArrayList<CallElement>();
		if(callCursor.moveToFirst()){
			do{
				//skip calls never made(just dial and imediatelly hang on)
				long duration = callCursor.getLong(2); 
				if (duration > 0){
					CallElement el = new CallElement();
					mList.add(el);
					el.name = callCursor.getString(1);
					el.phone = callCursor.getString(0);
					
					el.duration = getDuration(duration);
					el.dateOfCall = callCursor.getLong(3);
				
					mDurationSum += el.duration;
				}
			} while (callCursor.moveToNext());			
		}
		
		callCursor.close();
	}
	
	public void load(Activity activity, final Filter filter){
		if (filter.sortBy == SortByEnum.NameDurationAsc || filter.sortBy == SortByEnum.NameDurationDesc){
			loadNameDurationCase(activity, filter);		
		}
		else if (filter.sortBy == SortByEnum.ByDays){
			loadByDay(activity, filter);
		}
		else if (filter.sortBy == SortByEnum.ByMonths){
			loadByMonth(activity, filter);
		}
		else{		
			loadDefaultCase(activity, filter);
		}		
	}

	private long getDayDateKey(int year, int month, int day){		//year - 1900
		return day + month * 100 + year * 100 * 100;
	}
	
	public List<GraphViewData> loadForDayChart(Activity activity, final Filter filter){		
		Map<Long, CallElement> map = getDayMap(activity, filter);
		List<GraphViewData> data = new ArrayList<GraphViewData>();
		Calendar  fromDateCal = filter.fromDate.getCalendar();
		java.util.Date toDateTime = filter.toDate.getCalendar().getTime(); 
		
		while (fromDateCal.getTime().compareTo(toDateTime) < 1) {
			java.util.Date date = new java.util.Date(fromDateCal.getTimeInMillis());
			long valueX = getDayDateKey(date.getYear(), date.getMonth(), date.getDate());
			
			CallElement el = map.get(valueX);
			Long valueY = 0l;
			if (el != null){
				valueY = getDurationInMinutes(el.duration);
			}
			
			GraphViewData object = new GraphViewData(fromDateCal.getTimeInMillis(), valueY != null ? valueY : 0);
			data.add(object);
			fromDateCal.add(Calendar.DAY_OF_YEAR, 1);	        
	    }

		return data;
	}
	
	public void loadByDay(Activity activity, final Filter filter){
		Map<Long, CallElement> map = getDayMap(activity, filter);
		mList = new ArrayList<CallElement>(map.values());
		Collections.sort(mList, new CallElementDateReverseComparator());
	}
	
	private Map<Long, CallElement> getDayMap(Activity activity, final Filter filter){
		ReadPref(activity);		
		Cursor callCursor = executeQuery(activity, new String[] { 
				android.provider.CallLog.Calls.DURATION, android.provider.CallLog.Calls.DATE }, 
				getSortByString(SortByEnum.ByDays), filter);
		
		mDurationSum = 0;		
		Map<Long, CallElement> map = new HashMap<Long, CallElement>();
		if(callCursor.moveToFirst()){
			do{
				//skip calls never made(just dial and imediatelly hang on)
				long duration = getDuration(callCursor.getLong(0)); 
				if (duration == 0){
					continue;
				}
				
				long milis = callCursor.getLong(1);
				java.util.Date date = new java.util.Date(milis);
				long key = getDayDateKey(date.getYear(), date.getMonth(), date.getDate());

				mDurationSum += duration;
				CallElement element = map.get(key);
				if (element != null){
					element.duration += duration;
				}
				else{
					element = new CallElement();
					element.name = getSimpleDateFormatDay().format(date);
					element.dateOfCall = new SimpleDate(date).toMiliseconds(false);
					element.duration = duration;
					element.phone = Utils.emptyString;
					map.put(key, element);
				}
				
			} while (callCursor.moveToNext());			
		}		
		callCursor.close();
		
		return map;
	}
		
	private long getMonthDateKey(Date dt){
		//if (dt.getDate() >= mFirstDayOfMonth){
		if (dt.getDate() < mFirstDayOfMonth){
			Calendar cl = Calendar.getInstance();
			cl.setTime(dt);
			cl.add(Calendar.MONTH, -1); //if lower than min day then belongs to prev month
			dt = cl.getTime();
		}
		return dt.getMonth() + dt.getYear() * 20;
	}
	
	public void loadByMonth(Activity activity, final Filter filter){
		Map<Long, CallElement> map = getMonthMap(activity, filter);
		mList = new ArrayList<CallElement>(map.values());
		Collections.sort(mList, new CallElementDateReverseComparator());
	}
	
	public List<GraphViewData> loadForMonthChart(Activity activity, final Filter filter){
		Map<Long, CallElement> map = getMonthMap(activity, filter);
		List<GraphViewData> data = new ArrayList<GraphViewData>();
		Calendar  fromDateCal = filter.fromDate.getCalendar();
		java.util.Date toDateTime = filter.toDate.getCalendar().getTime(); 
		
		while (fromDateCal.getTime().compareTo(toDateTime) < 1) {
			long valueX = getMonthDateKey(new java.util.Date(fromDateCal.getTimeInMillis()));
	        
			CallElement el = map.get(valueX);
			Long valueY = 0l;
			if (el != null){
				valueY = getDurationInMinutes(el.duration);				
			}
			
			GraphViewData object = new GraphViewData(fromDateCal.getTimeInMillis(), 
					valueY != null ? valueY : 0);
			data.add(object);
			fromDateCal.add(Calendar.MONTH, 1);	        
	    }

		return data;
	}
		
	private Map<Long, CallElement> getMonthMap(Activity activity, final Filter filter){
		ReadPref(activity);
		Filter newFilter = new Filter(filter);
		newFilter.fromDate = filter.fromDate.getStartDateOfPeriod(mFirstDayOfMonth);	
		newFilter.toDate = filter.toDate.getEndDateOfPeriod(mFirstDayOfMonth);
		Cursor callCursor = executeQuery(activity, new String[] { 
				android.provider.CallLog.Calls.DURATION, android.provider.CallLog.Calls.DATE }, newFilter); 
		
		Map<Long, CallElement> map = new HashMap<Long, CallElement>();
		mDurationSum = 0;
		if(callCursor.moveToFirst()){
			do{
				//skip calls never made(just dial and imediatelly hang on)
				long duration = getDuration(callCursor.getLong(0)); 
				if (duration == 0){
					continue;
				}
				
				//retrieve key
				java.util.Date date = new java.util.Date(callCursor.getLong(1));
				long key = getMonthDateKey(date);
				
				mDurationSum += duration;
				CallElement element = map.get(key);
				if (element != null){
					element.duration += duration;
				}
				else{
					SimpleDate dt = new SimpleDate(date).
							getStartDateOfPeriod(mFirstDayOfMonth);
					element = new CallElement();
					element.name = getSimpleDateFormatMonth().format(dt.getCalendar().getTime());
					element.dateOfCall = dt.toMiliseconds(false);
												
					element.duration = duration;
					element.phone = Utils.emptyString;
					map.put(key, element);
				}
				
			} while (callCursor.moveToNext());			
		}		
		callCursor.close();
		
		return map;
	}
	
//	public List<GraphViewData> loadForPie(Activity activity, SimpleDate fromDate, SimpleDate toDate){
//		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext());
//		mDurationRoundingOption = Integer.parseInt(preferences.getString("minute_rounding", "0"));
//		
//		String selection = null;
//		String[] selectionArgs = null;
//		Cursor callCursor = null;
//				
//		selection = String.format("%s=? AND %s>=? AND %s<=?",
//					android.provider.CallLog.Calls.TYPE, android.provider.CallLog.Calls.DATE, android.provider.CallLog.Calls.DATE);
//		selectionArgs = new String[] { String.valueOf(android.provider.CallLog.Calls.OUTGOING_TYPE), 
//					 					   String.valueOf(fromDate.toMiliseconds(false)), 
//					 					   String.valueOf(toDate.toMiliseconds(true)) };
//		
//		mDurationSum = 0;
//		callCursor = activity.getContentResolver().query(
//		        android.provider.CallLog.Calls.CONTENT_URI,
//		        new String[] { android.provider.CallLog.Calls.DURATION, android.provider.CallLog.Calls.NUMBER, 
//		        			   android.provider.CallLog.Calls.CACHED_NAME },
//		        selection,
//		        selectionArgs,
//		        null
//		        );
//		
//		Map<Long, Long> list = new HashMap<Long, Long>();
//		if(callCursor.moveToFirst()){
//			do{
//				//skip calls never made(just dial and imediatelly hang on)
//				long duration = getDuration(callCursor.getLong(0)); 
//				//converts secunds to minutes
//				duration = (duration / 3600 * 60 +  duration / 60 % 60);
//				if (duration == 0){
//					continue;
//				}
//				long date = callCursor.getLong(1);
//				java.util.Date dt = new java.util.Date(date);
//				long key = dt.getDate() + dt.getMonth() * 100 + dt.getYear() * 100 * 20;
//				Long oldVal = list.get(key);
//				if (oldVal != null){
//					list.put(key, oldVal.longValue() + duration);
//				}
//				else{
//					list.put(key, duration);
//				}
//				
//			} while (callCursor.moveToNext());			
//		}		
//		callCursor.close();
//		
//		List<GraphViewData> data = new ArrayList<GraphViewData>();
//		Calendar  fromDateCal = fromDate.getCalendar();
//		java.util.Date toDateTime = toDate.getCalendar().getTime(); 
//		
//		while (fromDateCal.getTime().compareTo(toDateTime) < 1) {
//			java.util.Date dt = new java.util.Date(fromDateCal.getTimeInMillis());
//	        long valueX = dt.getDate() + dt.getMonth() * 100 + dt.getYear() * 100 * 20;
//			Long valueY = list.get(valueX); 
//			GraphViewData object = new GraphViewData(fromDateCal.getTimeInMillis(), valueY != null ? valueY : 0);
//			data.add(object);
//			fromDateCal.add(Calendar.DAY_OF_YEAR, 1);	        
//	    }
//
//		return data;
//	}
	
		
	private void ReadPref(Activity activity){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext());
		mDurationRoundingOption = Integer.parseInt(preferences.getString("minute_rounding", "0"));
		mFirstDayOfMonth = Integer.parseInt(preferences.getString("start_of_month", "1"));
	}
	
	private String[] getDefaultFields(){
		return new String[] {
		        android.provider.CallLog.Calls.NUMBER, 
		        android.provider.CallLog.Calls.CACHED_NAME,
		        android.provider.CallLog.Calls.DURATION,
		        android.provider.CallLog.Calls.DATE,
		        android.provider.CallLog.Calls.NEW //Whether or not the call has been acknowledged
		    };
	}
	
	private Cursor executeQuery(Activity activity, String[] strFields, final Filter filter){
		return executeQuery(activity, strFields, getSortByString(filter.sortBy), filter);
	}
	
	private Cursor executeQuery(Activity activity, String[] strFields, String strOrder, final Filter filter){
		StringBuilder selection = new StringBuilder();
		List<String> selectedArgs = new ArrayList<String>();
		
		selection.append(android.provider.CallLog.Calls.TYPE).append("=?");
		selectedArgs.add(String.valueOf(android.provider.CallLog.Calls.OUTGOING_TYPE));
		
		if (filter.fromDate != null){
			selection.append(" AND ").append(android.provider.CallLog.Calls.DATE).append(">=?");
			selectedArgs.add(String.valueOf(filter.fromDate.toMiliseconds(false)));
		}
		
		if (filter.toDate != null){
			selection.append(" AND ").append(android.provider.CallLog.Calls.DATE).append("<=?");
			selectedArgs.add(String.valueOf(filter.toDate.toMiliseconds(true)));
		}
				
		//can not filter by both contactName and phone
		if (!Utils.isNullOrEmpty(filter.contactName)){
			selection.append(" AND ").append(android.provider.CallLog.Calls.CACHED_NAME).append("=?");
			selectedArgs.add(filter.contactName);
		}
		else if (!Utils.isNullOrEmpty(filter.phone)){
			selection.append(" AND ").append(android.provider.CallLog.Calls.NUMBER).append("=?");
			selectedArgs.add(filter.phone);
		}
			
		return activity.getContentResolver().query(
					        android.provider.CallLog.Calls.CONTENT_URI,
					        strFields,
					        selection.toString(),
					        selectedArgs.toArray(new String[]{}),
					        strOrder
		        );
	}
	
	private String getSortByString(SortByEnum sortBy) {
		String strOrder = null;
		switch(sortBy){
		case DateAsc:
			strOrder = android.provider.CallLog.Calls.DATE + " ASC"; 
			break;
		case NameAsc:
			strOrder = android.provider.CallLog.Calls.CACHED_NAME + " ASC";
			break;
		case NameDesc:
			strOrder = android.provider.CallLog.Calls.CACHED_NAME + " DESC";
			break;	
		case DurationAsc:
			strOrder = android.provider.CallLog.Calls.DURATION + " ASC";
			break;
		case DurationDesc:
			strOrder = android.provider.CallLog.Calls.DURATION + " DESC";
			break;		
		default:
			strOrder = android.provider.CallLog.Calls.DATE + " DESC";	
		}
		return strOrder;
	}
	
	private long getDurationInMinutes(long duration){
		return duration / 3600 * 60 +  (duration / 60) % 60;
	}
	
	private long getDuration(long duration){
		switch (mDurationRoundingOption){
		default: //no rounding
			return duration; 
		case 1: //only first minute
			if (duration < 60){
				return 60;
			}
			return duration;
		case 2: //every minute	
		{
			long minutes = duration / 60;
			long secondsRoundedToMin = duration % 60 > 0 ? 1 : 0;
			return (minutes + secondsRoundedToMin) * 60;
		}
		case 3: //first minute, other by 30 seconds
			if (duration < 60){
				return 60;
			}
			long minutes = duration / 60;
			long secondsRoundedToMin = duration % 60 >= 30 ? 1 : 0;
			return (minutes + secondsRoundedToMin) * 60;	
		}
	}
}