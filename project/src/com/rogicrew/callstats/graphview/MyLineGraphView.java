package com.rogicrew.callstats.graphview;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;

import com.jjoe64.graphview.BarGraphView;

public class MyLineGraphView extends BarGraphView {

	public MyLineGraphView(Context context, String title) {
		super(context, title);
	}
	
	protected String formatLabel(double value, boolean isValueX) {
		if (isValueX){
			long longValue = (long)value;
		
			Date javaDateToFormat = new Date(longValue);
	    	DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this.getContext());
	    	String txt = dateFormat.format(javaDateToFormat);
	    	//remove year(its alwyas last i suppose)
	    	//find last non (0-9) character and return string from 0 to that character
	    	for (int i = txt.length() - 1; i >= 0; --i){
	    		if (txt.charAt(i) < '0'  ||  txt.charAt(i) > '9'){
	    			return txt.substring(0, i);
	    		}
	    	}
	    	
	    	return txt;
		}
		
		return Long.toString((long)value);
	}
}
