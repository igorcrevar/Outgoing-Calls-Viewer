package com.rogicrew.callstats;

import android.app.Activity;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.rogicrew.callstats.graphview.MyLineGraphView;
import com.rogicrew.callstats.models.CallModel;
import com.rogicrew.callstats.models.CallsFilter;
import com.rogicrew.callstats.models.SimpleDate;
import com.rogicrew.callstats.models.SortByEnum;
import com.rogicrew.callstats.utils.Utils;

public class ChartDayActivity extends Activity {
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
    	
    	Bundle params = this.getIntent().getExtras();
    	CallsFilter filterPassed = (CallsFilter)params.get("filter");
    	CallsFilter filter = new CallsFilter(filterPassed);
    	SimpleDate currentDate = new SimpleDate();    	
    	if (currentDate.isLessThen(filter.toDate)){
    		filter.toDate = currentDate;
    	}
    	
    	StringBuilder sb = new StringBuilder();
    	if (!Utils.isNullOrEmpty(filter.contactName)){
    		sb.append(filter.contactName);
    		sb.append(": ");
    	}
    	else if (!Utils.isNullOrEmpty(filter.phone)){
    		sb.append(filter.phone);
    		sb.append(": ");
    	}
    	
    	sb.append(Utils.getLongFormatedDate(filter.fromDate));
    	sb.append(" - ");
    	sb.append(Utils.getLongFormatedDate(filter.toDate));
    	
    	GraphView graphView = new MyLineGraphView(this, sb.toString());
    	graphView.setScrollable(true);
    	graphView.setScalable(true);
    	CallModel callModel = new CallModel();
    	filter.sortBy = SortByEnum.ByDays;
    	GraphViewData[] arrData = callModel.loadForChart(this, filter);
		graphView.addSeries(new GraphViewSeries(arrData));		
		setContentView(graphView);
	 }
}
