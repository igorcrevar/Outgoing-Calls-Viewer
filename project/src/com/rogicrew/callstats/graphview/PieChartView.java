package com.rogicrew.callstats.graphview;

import android.content.Context;
import android.widget.LinearLayout;

public class PieChartView extends LinearLayout{

	public PieChartView(Context context) {
		super(context);
		
		setWillNotDraw(false);
	}

	@Override
	public void onDraw(android.graphics.Canvas canvas){

	}
	
}
