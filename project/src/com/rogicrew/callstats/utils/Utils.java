package com.rogicrew.callstats.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import com.rogicrew.callstats.models.SimpleDate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

public class Utils {

	public static SharedPreferences preferences;
	
	public static boolean isNullOrEmpty(String s)
	{
		return s == null || s.equals("");
	}
	
	public static boolean writeFile(Context ctx, String fileName, String content){
        try { 
        	//mode private only this app can open/write this file
            FileOutputStream fOut = ctx.openFileOutput(fileName, Context.MODE_WORLD_READABLE);//  Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fOut); 
            osw.write(content);
            osw.flush();
            osw.close();
            return true;
        }
        catch(IOException e){
        	//todo:log?
        	return false;
        }
	}
	
	public static String readFile(Context ctx, String fileName){
		try{
	        FileInputStream fIn = ctx.openFileInput(fileName);
	        InputStreamReader isr = new InputStreamReader(fIn);
	        StringBuilder sb = new StringBuilder(250);
	        char[] tmpBuffer = new char[100];
	        int cnt;
	        
	        while ( (cnt = isr.read(tmpBuffer)) != -1){
	        	sb.append(tmpBuffer, 0, cnt);
	        }
	        return sb.toString();
		} catch (IOException ioe) {
		    //todo: log?
			return null;
		}		
	}
	
	public static void simpleDialog(final Activity activity, final String text, final boolean close)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(text).setCancelable(false);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
        	   if (close){
        		   activity.setResult(0);
            	   activity.finish();
        	   }
           }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public static int getInt(String value, int defaultValue){
		try{
			return Integer.parseInt(value);
		}
		catch(Exception e){
			return defaultValue;
		}
	}
	
	public static boolean getBool(String value, boolean defaultValue){
		try{
			return Boolean.parseBoolean(value);
		}
		catch(Exception e){
			return defaultValue;
		}
	}
	
	public static String getFormatedDate(Context ctx, SimpleDate sd){
		return getFormatedDate(ctx, sd.year, sd.month, sd.day);
	}
	
	public static String getFormatedDate(Context ctx, int year, int month, int day)
	{
		Date javaDateToFormat = new Date(year-1900, month, day);
    	Context context = ctx.getApplicationContext();
    	DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
    	return dateFormat.format(javaDateToFormat);
	}
	
	public static String getLongFormatedDate(SimpleDate sd){
		return getLongFormatedDate(sd.year, sd.month, sd.day);
	}
	
	public static String getLongFormatedDate(int year, int month, int day)
	{
		Date javaDateToFormat = new Date(year-1900, month, day);
    	DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
    	return dateFormat.format(javaDateToFormat);
	}
	
	public static String getFormatedDateTime(Context ctx, long milis)
	{
		Date javaDateToFormat = new Date(milis);
    	Context context = ctx.getApplicationContext();
    	DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
    	DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
    	return dateFormat.format(javaDateToFormat)+" "+timeFormat.format(javaDateToFormat);
	}
	
	public static String getFormatedDate(Context ctx, long milis)
	{
		Date javaDateToFormat = new Date(milis);
    	Context context = ctx.getApplicationContext();
    	DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
    	return dateFormat.format(javaDateToFormat);
	}

	public static String getFormatedTime(int hour, int minute, int second)
	{
		boolean doNotShowSeconds = Integer.parseInt(preferences.getString("minute_rounding", "0")) > 1;
		
		if (doNotShowSeconds){			
			return String.format("%s:%s", digit2(hour), digit2(minute));
		}
		
		return String.format("%s:%s:%s", digit2(hour), digit2(minute), digit2(second));
	}
	
	private static String digit2(int comp)
	{
		return comp < 10 ? "0" + Integer.toString(comp) : Integer.toString(comp);
	}
	
	public static long getDuration(long duration, int roundingOption){
		switch (roundingOption){
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
