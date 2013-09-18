package com.rogicrew.callstats.models;

import java.util.Comparator;

public class CallElementComparator {
	public static class Duration implements Comparator<CallElement> {
	    @Override
	    public int compare(CallElement o1, CallElement o2) {
	    	return (int)(o1.getDuration() - o2.getDuration());
	    }
	}
	
	public static class DurationReverse implements Comparator<CallElement> {
	    @Override
	    public int compare(CallElement o1, CallElement o2) {
	    	return (int)(o2.getDuration() - o1.getDuration());
	    }
	}
	
	public static class Date implements Comparator<CallElement> {
	    @Override
	    public int compare(CallElement o1, CallElement o2) {
	    	return (int)(o1.getDateOfCall() - o2.getDateOfCall());
	    }
	}
	
	public static class DateReverse implements Comparator<CallElement> {
	    @Override
	    public int compare(CallElement o1, CallElement o2) {
	    	return (int)(o2.getDateOfCall() - o1.getDateOfCall());
	    }
	}
	
	public static class Name implements Comparator<CallElement> {
	    @Override
	    public int compare(CallElement o1, CallElement o2) {
	    	return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
	    }
	}
	
	public static class NameReverse implements Comparator<CallElement> {
	    @Override
	    public int compare(CallElement o1, CallElement o2) {
	    	return o2.getName().toLowerCase().compareTo(o1.getName().toLowerCase());
	    }
	}
}
