package com.rogicrew.callstats;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.rogicrew.callstats.graphview.MyLineGraphView;
import com.rogicrew.callstats.models.CallModel;
import com.rogicrew.callstats.models.CallsFilter;
import com.rogicrew.callstats.models.SimpleDate;
import com.rogicrew.callstats.models.SortByEnum;
import com.rogicrew.callstats.utils.Utils;

public class ChartDayActivity extends ActionBarActivity {
	private AdView adView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle params = this.getIntent().getExtras();
		CallsFilter filterPassed = (CallsFilter) params.get("filter");
		CallsFilter filter = new CallsFilter(filterPassed);
		SimpleDate currentDate = new SimpleDate();
		if (currentDate.isLessThen(filter.toDate)) {
			filter.toDate = currentDate;
		}

		StringBuilder sb = new StringBuilder();
		if (!Utils.isNullOrEmpty(filter.contactName)) {
			sb.append(filter.contactName);
			sb.append(": ");
		} else if (!Utils.isNullOrEmpty(filter.phone)) {
			sb.append(filter.phone);
			sb.append(": ");
		}

		sb.append(Utils.getLongFormatedDate(filter.fromDate));
		sb.append(" - ");
		sb.append(Utils.getLongFormatedDate(filter.toDate));

		setContentView(R.layout.chart);
		RelativeLayout chartLayout = (RelativeLayout)findViewById(R.id.chart);
		RelativeLayout adMobLayout = (RelativeLayout)findViewById(R.id.layoutForAdMob);
		
		GraphView graphView = new MyLineGraphView(this, sb.toString());
		graphView.setGravity(1);
		graphView.setScrollable(false);
		graphView.setScalable(false);
		CallModel callModel = new CallModel();
		filter.sortBy = SortByEnum.ByDays;
		GraphViewData[] arrData = callModel.loadForDayChart(this, filter);
		graphView.addSeries(new GraphViewSeries(arrData));
		
		chartLayout.addView(graphView);
		adView = Utils.getAddView(this);
	    Utils.addToLayout(adView, true, adMobLayout);
	}

	@Override
	protected void onPause() {
		adView.pause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		adView.resume();
	}

	@Override
	protected void onDestroy() {
		adView.destroy();
		super.onDestroy();
	}
}
