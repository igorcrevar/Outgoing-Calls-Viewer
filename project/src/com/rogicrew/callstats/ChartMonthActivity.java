package com.rogicrew.callstats;

import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.rogicrew.callstats.graphview.MyLineGraphView;
import com.rogicrew.callstats.models.CallModel;
import com.rogicrew.callstats.models.SimpleDate;
import com.rogicrew.callstats.utils.Utils;

public class ChartMonthActivity extends Activity{
	
	  @Override
	 public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
    	
    	Bundle params = this.getIntent().getExtras();
    	CallModel.Filter filterPassed = (CallModel.Filter)params.get("filter");
    	CallModel.Filter filter = new CallModel.Filter(filterPassed);
    	//create nice string representation of start and end date
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
    	int firstDayOfMonth = Integer.parseInt(preferences.getString("start_of_month", "1"));
    	
    	//SimpleDate from = filter.fromDate.getStartDateOfPeriod(firstDayOfMonth);
    	//SimpleDate to = filter.toDate.getEndDateOfPeriod(firstDayOfMonth);
    	filter.toDate = new SimpleDate();
    	int month = filter.toDate.day < firstDayOfMonth ? filter.toDate.month - 4 : filter.toDate.month - 3;
    	int year = filter.toDate.year;
    	if (month < 0){
    		month += 11;
    		--year;
    	}
    	filter.fromDate = new SimpleDate(year, month, firstDayOfMonth);
    	
    	StringBuilder sb = new StringBuilder();
    	if (!Utils.isNullOrEmpty(filter.contactName)){
    		sb.append(filter.contactName);
    		sb.append(": ");
    	}
    	else if (!Utils.isNullOrEmpty(filter.phone)){
    		sb.append(filter.phone);
    		sb.append(": ");
    	}
    	
    	String fromTitle = Utils.getLongFormatedDate(filter.fromDate);
    	String toTitle = Utils.getLongFormatedDate(filter.toDate);
    	//end of creation
    	
    	sb.append(fromTitle);
    	sb.append(" - ");
    	sb.append(toTitle);
    
    	GraphView graphView = new MyLineGraphView(this, sb.toString());
    	graphView.setHorizontalLabels(new String[]{ fromTitle, "", toTitle});
    	graphView.setScrollable(true);
    	graphView.setScalable(true);
    	CallModel callModel = new CallModel();
    	List<GraphViewData> data = callModel.loadForMonthChart(this, filter);
		GraphViewData[] arrData = data.toArray(new GraphViewData[0]);
		graphView.addSeries(new GraphViewSeries(arrData));		
		setContentView(graphView);
	 }
}
