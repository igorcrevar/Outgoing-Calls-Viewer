package com.rogicrew.callstats.components;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.rogicrew.callstats.R;
import com.rogicrew.callstats.components.DatePickerComponent.IDatePickerOnChangeCallback;
import com.rogicrew.callstats.models.SimpleDate;
import com.rogicrew.callstats.models.SortByEnum;
import com.rogicrew.callstats.utils.Utils;

public class HeaderComponent extends LinearLayout implements IDatePickerOnChangeCallback, OnItemSelectedListener{
	public static interface IHeaderComponentChanged{
		public void onHeaderComponentChanged(SimpleDate from, SimpleDate to, SortByEnum sortBy);
	}
	
    private SimpleDate mValidFromDate;
    private SimpleDate mValidToDate;
    private DatePickerComponent mDPFromDate;
    private DatePickerComponent mDPToDate;
    private Spinner mSpinnerOrderBy;
    private SharedPreferences mPreferences;
    public IHeaderComponentChanged mHeaderComponentChangedCallback;

	public HeaderComponent(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.component_header, this, true);
		this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.setOrientation(VERTICAL);
		
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		mDPFromDate = (DatePickerComponent)findViewById(R.id.datePickerFrom);
        mDPFromDate.setCallback(this);
        mDPToDate = (DatePickerComponent)findViewById(R.id.datePickerTo);
        mDPToDate.setCallback(this);
        
        mSpinnerOrderBy = (Spinner) findViewById( R.id.spinnerOrderBy );
        mSpinnerOrderBy.setOnItemSelectedListener(this);
        initOrderBy(true);
    } 
	
	public void initOnStart()
	{
		int startOfMonthInt = Integer.parseInt(mPreferences.getString("start_of_month", "1"));	
		SimpleDate currentDate = new SimpleDate();
        
		if (currentDate.day < startOfMonthInt){
			mValidFromDate = SimpleDate.getPrevMonthDay(currentDate, startOfMonthInt);
        }
        else{
        	mValidFromDate = new SimpleDate(currentDate.year, currentDate.month, startOfMonthInt);
        }
		mDPFromDate.set(mValidFromDate);
     
		mValidToDate = SimpleDate.getNextMonth(mValidFromDate);
		mDPToDate.set(mValidToDate);
		//init on restart will call callback at the end
		initOnRestart();
	}
	
	public void initOnRestart(){
		boolean isFixedToDate = mPreferences.getBoolean("is_to_date_fixed", true);
		mDPToDate.setEnabled(!isFixedToDate);
		onDatePickerComponentChanged();
	}
	
	public SimpleDate getDateFrom(){
		return mValidFromDate;
	}
	
	public SimpleDate getDateTo(){
		return mValidToDate;
	}
	
	public int getOrderBy(){
		return (int)mSpinnerOrderBy.getSelectedItemId();
	}

	@Override
	public void onDatePickerComponentChanged() {
		boolean isFixedToDate = mPreferences.getBoolean("is_to_date_fixed", true);
		
		SimpleDate from = mDPFromDate.get();
		SimpleDate to = mDPToDate.get();
		if (isFixedToDate){
			//set new valid dates
			mValidFromDate.set(from);
			//update to date automaticly
			mValidToDate.set(SimpleDate.getNextMonth(mValidFromDate));
			mDPToDate.set(mValidToDate);
		}
		else if (to.isLessThen(from)){
			//set old valid dates
			mDPFromDate.set(mValidFromDate);
			mDPToDate.set(mValidToDate);
			//show dialog with error msg
			Utils.simpleDialog((Activity)this.getContext(), getContext().getString(R.string.invalid_date_interval_message), false);
		}
		else{
			//new dates are valid!
			mValidFromDate.set(from);
			mValidToDate.set(to);			
		}
		
		callHeaderComponentCallback();
	}
	
	public SortByEnum getSortBy()
	{
		int i = mSpinnerOrderBy.getSelectedItemPosition();
		SortByEnum sortBy = SortByEnum.values()[i];
		return sortBy;
	}
	
	private void callHeaderComponentCallback(){
		if (mHeaderComponentChangedCallback != null){
			mHeaderComponentChangedCallback.onHeaderComponentChanged(mDPFromDate.get(), mDPToDate.get(), getSortBy());
		}			
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {		
		mSpinnerOrderBy.setSelection(position);
		callHeaderComponentCallback();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		//must be something selected
		mSpinnerOrderBy.setSelection(0);
		callHeaderComponentCallback();
	}
	
	public void initOrderBy(boolean isAll)
	{
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
								this.getContext(), isAll ? R.array.sort_by : R.array.sort_by_names, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerOrderBy.setAdapter(adapter);
		mSpinnerOrderBy.setSelection(0);        	
	}
	
	public void setDateInterval(SimpleDate from, SimpleDate to)
	{
		mValidFromDate = from;
		mValidToDate = to;
		mDPFromDate.set(from);
		mDPToDate.set(to);
	}
	
	public void setSortBy(SortByEnum sortBy){
		mSpinnerOrderBy.setSelection(sortBy.ordinal());	
	}
}
