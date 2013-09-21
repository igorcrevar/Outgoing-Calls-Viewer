package com.rogicrew.callstats.models;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.rogicrew.callstats.models.command.DefaultCallHandler;
import com.rogicrew.callstats.models.command.GroupByDayCallHandler;
import com.rogicrew.callstats.models.command.GroupByMonthCallHandler;
import com.rogicrew.callstats.models.command.GroupByNamePhoneCallHandler;
import com.rogicrew.callstats.models.command.ICallHandler;
import com.rogicrew.callstats.utils.Utils;

public class CallsFilter implements Cloneable, Serializable
{
	private static final long serialVersionUID = -2404468916762266502L;
	public SimpleDate fromDate; 
	public SimpleDate toDate; 
	public SortByEnum sortBy;
	public String phone;
	public String contactName;
	public Object tag; //additonal info
	
	public CallsFilter(){
		fromDate = toDate = null;
		contactName = phone = null;
		sortBy = SortByEnum.NameDurationDesc;
		tag = null;
	}
	
	//copy constructor
	public CallsFilter(final CallsFilter filter){
		copyFrom(filter);
	}
	
	public CallsFilter clone() throws CloneNotSupportedException {
         CallsFilter newFilter = new CallsFilter(this);
         return newFilter;
	}
	
	public void copyFrom(final CallsFilter filter){
		fromDate = new SimpleDate(filter.fromDate);
        toDate = new SimpleDate(filter.toDate);
        sortBy = filter.sortBy;
        phone = filter.phone;
        contactName = filter.contactName;
        tag = filter.tag; //string are immutable but tag must be handled by programmer!
	}
	
	public String getSortByString() {
		switch (sortBy) {
		case DateAsc: case ByDays: case ByMonths:
			return android.provider.CallLog.Calls.DATE + " ASC";
		case DateDesc:
			return android.provider.CallLog.Calls.DATE + " DESC";
		case NameAsc:
			return android.provider.CallLog.Calls.CACHED_NAME + " ASC";
		case NameDesc:
			return android.provider.CallLog.Calls.CACHED_NAME + " DESC";
		case DurationAsc:
			return android.provider.CallLog.Calls.DURATION + " ASC";
		case DurationDesc:
			return android.provider.CallLog.Calls.DURATION + " DESC";
		default:
			return "";
		}
	}
	
	public void appendFilter(StringBuilder where, Collection<String> values) {
		if (fromDate != null){
			where.append(" AND ").append(android.provider.CallLog.Calls.DATE).append(">=?");
			values.add(String.valueOf(fromDate.toMiliseconds(false)));
		}
		
		if (toDate != null){
			where.append(" AND ").append(android.provider.CallLog.Calls.DATE).append("<=?");
			values.add(String.valueOf(toDate.toMiliseconds(true)));
		}
				
		//can not filter by both contactName and phone
		if (!Utils.isNullOrEmpty(contactName)){
			where.append(" AND ").append(android.provider.CallLog.Calls.CACHED_NAME).append("=?");
			values.add(contactName);
		}
		else if (!Utils.isNullOrEmpty(phone)){
			where.append(" AND ").append(android.provider.CallLog.Calls.NUMBER).append("=?");
			values.add(phone);
		}
	}
	
	public ICallHandler getHandler() {
		switch (sortBy) {
		case NameDurationAsc: case NameDurationDesc:
			return new GroupByNamePhoneCallHandler(!Utils.isNullOrEmpty(contactName));
		case ByDays: case ByDaysDurationAsc: case ByDaysDurationDesc:
			return new GroupByDayCallHandler();
		case ByMonths: case ByMonthsDurationAsc: case ByMonthsDurationDesc:
			return new GroupByMonthCallHandler();
		default:
			return new DefaultCallHandler();
		}
	}
	
	public void sort(List<CallElement> elements) {
		switch (sortBy) {
		case NameDurationAsc: case ByDaysDurationAsc: case ByMonthsDurationAsc:
			Collections.sort(elements, new CallElementComparator.Duration());
			break;
		case NameDurationDesc: case ByMonthsDurationDesc : case ByDaysDurationDesc:
			Collections.sort(elements, new CallElementComparator.DurationReverse());
			break;
		default:
			break;
		}
	}
	
	public boolean isByDayOrMonthFilter() {
		switch (sortBy)
		{
		case ByMonths: case ByMonthsDurationAsc: case ByMonthsDurationDesc:
		case ByDays: case ByDaysDurationAsc: case ByDaysDurationDesc:
			return true;
		default:
			return false;
		}
	}
}
