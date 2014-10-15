package com.rogicrew.callstats.components;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rogicrew.callstats.R;
import com.rogicrew.callstats.models.SimpleDate;
import com.rogicrew.callstats.utils.Utils;

public class DatePickerComponent extends LinearLayout implements android.view.View.OnClickListener, DatePickerDialog.OnDateSetListener{
	public static interface IDatePickerOnChangeCallback
	{
		public void onDatePickerComponentChanged();
	}
	
	private TextView mTextView;
	// private Button mButton;
	private SimpleDate mDate;
	private IDatePickerOnChangeCallback mCallbackOnChange;
	
	public DatePickerComponent(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		LayoutInflater.from(context).inflate(R.layout.component_datepicker, this, true);
		this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.setOrientation(HORIZONTAL);
		
		mTextView = (TextView)findViewById(R.id.dp_text);
		mTextView.setOnClickListener(this);
	//	mButton = (Button)findViewById(R.id.dp_button);
	//	mButton.setOnClickListener(this);
		
		mDate = new SimpleDate();
	}

	public void set(int year, int monthOfYear, int dayOfMonth)
	{
		mDate.set(year, monthOfYear, dayOfMonth);
		updateDisplay();
	}

	public void set(SimpleDate sd)
	{
		mDate.set(sd);
		updateDisplay();
	}
	
	public void set(Calendar cal)
	{
		mDate.set(cal);
		updateDisplay();
	}

	public SimpleDate get()
	{
		return mDate;
	}
	
	public void setCallback(IDatePickerOnChangeCallback callbackOnChange){
		this.mCallbackOnChange = callbackOnChange;
	}
	
	@Override
	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		mTextView.setEnabled(enabled);
		// mButton.setEnabled(enabled);
	}
	
	@Override
	public void onClick(View v) {
		DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), this, 
				mDate.year, mDate.month, mDate.day);
		datePickerDialog.show();
	}
	
	// updates the date in the TextView
	private void updateDisplay() {		
    	String fd = Utils.getFormatedDate(this.getContext(), mDate.year, mDate.month, mDate.day);
    	mTextView.setText(fd);
    }

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		set(year, monthOfYear, dayOfMonth);
		//notify listener
		if (mCallbackOnChange != null){
			mCallbackOnChange.onDatePickerComponentChanged();
		}
	}
		
}
