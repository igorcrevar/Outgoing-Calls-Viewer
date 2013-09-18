package com.rogicrew.callstats.models;

import com.rogicrew.callstats.utils.Utils;

public class CallElement {
	private String name;
	private String phone;
	private long duration; //InSeconds;
	private long dateOfCall; //in miliseconds
	
	public CallElement(String name, String phone, long duration, long dateOfCall) {
		setName(name);
		setPhone(phone);
		setDuration(duration);
		setDateOfCall(dateOfCall);
	}
	
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
	    hash = hash * 31 + (name != null ? name.hashCode() : 0);
		return hash;
	}
	
	public String getName() {
		return Utils.isNullOrEmpty(name) ? "-" : name;
	}
	
	public void incDuration(long duration) {
		setDuration(getDuration() + duration);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getDateOfCall() {
		return dateOfCall;
	}

	public void setDateOfCall(long dateOfCall) {
		this.dateOfCall = dateOfCall;
	}
}
