package com.rogicrew.callstats.graphview;

public class PieChartElement {
	public float percent; //0-1
	public String value;
	public PieChartElement(float p, String v){
		percent = p;
		value = v;
	}
}
