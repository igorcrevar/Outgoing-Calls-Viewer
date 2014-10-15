package com.rogicrew.callstats;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.rogicrew.callstats.utils.Utils;

public class ChartMonthActivity extends ActionBarActivity {
	private AdView adView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle params = this.getIntent().getExtras();
		CallsFilter filterPassed = (CallsFilter) params.get("filter");
		CallsFilter filter = new CallsFilter(filterPassed);
		// create nice string representation of start and end date
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this.getBaseContext());
		int firstDayOfMonth = Integer.parseInt(preferences.getString(
				"start_of_month", "1"));

		// SimpleDate from =
		// filter.fromDate.getStartDateOfPeriod(firstDayOfMonth);
		// SimpleDate to = filter.toDate.getEndDateOfPeriod(firstDayOfMonth);
		filter.toDate = new SimpleDate();
		int month = filter.toDate.day < firstDayOfMonth ? filter.toDate.month - 4
				: filter.toDate.month - 3;
		int year = filter.toDate.year;
		if (month < 0) {
			month += 11;
			--year;
		}
		filter.fromDate = new SimpleDate(year, month, firstDayOfMonth);

		StringBuilder sb = new StringBuilder();
		if (!Utils.isNullOrEmpty(filter.contactName)) {
			sb.append(filter.contactName);
			sb.append(": ");
		} else if (!Utils.isNullOrEmpty(filter.phone)) {
			sb.append(filter.phone);
			sb.append(": ");
		}

		String fromTitle = Utils.getLongFormatedDate(filter.fromDate);
		String toTitle = Utils.getLongFormatedDate(filter.toDate);
		// end of creation

		sb.append(fromTitle);
		sb.append(" - ");
		sb.append(toTitle);

		setContentView(R.layout.chart);
		RelativeLayout chartLayout = (RelativeLayout)findViewById(R.id.chart);
		RelativeLayout adMobLayout = (RelativeLayout)findViewById(R.id.layoutForAdMob);
		
		GraphView graphView = new MyLineGraphView(this, sb.toString());
		graphView.setHorizontalLabels(new String[] { fromTitle, "", toTitle });
		graphView.setScrollable(false);
		graphView.setScalable(false);
		CallModel callModel = new CallModel();
		GraphViewData[] arrData = callModel.loadForMonthChart(this, filter);
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
