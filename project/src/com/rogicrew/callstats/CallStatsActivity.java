package com.rogicrew.callstats;

import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rogicrew.callstats.components.HeaderComponent;
import com.rogicrew.callstats.components.HeaderComponent.IHeaderComponentChanged;
import com.rogicrew.callstats.components.PerformanceOutgoingListAdapter;
import com.rogicrew.callstats.models.CallModel;
import com.rogicrew.callstats.models.CallModel.CallElement;
import com.rogicrew.callstats.models.SimpleDate;
import com.rogicrew.callstats.models.SortByEnum;
import com.rogicrew.callstats.utils.Utils;

public class CallStatsActivity extends Activity implements IHeaderComponentChanged, OnItemClickListener{
	private static final String breadcrumbsPrefixFirst = "<< ";
	private static final String breadcrumbsPrefixNext  = "<< ... ";
	
	private HeaderComponent mHeaderComponent;
	private CallModel mCallModel;
	private TextView mTextViewDuration;
	private TextView mBreadcrumb;
	private ListView mListView;
	private ProgressBar mProgressBar;
	private volatile boolean isRefreshing = false;
	private String mMinutesFormater;
	
	private CallModel.Filter mCurrentFilter;
	private Stack<CallModel.Filter> mStackFilters;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.outgoing);
        //set prerefences for utils
        Utils.preferences = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
        mCallModel = new CallModel();
        mCurrentFilter = new CallModel.Filter();
        mStackFilters = new Stack<CallModel.Filter>();
        
        mListView = (ListView)findViewById(R.id.listviewOutgoingCalls);   
        mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        mListView.setOnItemClickListener(this);
        mTextViewDuration = (TextView)findViewById(R.id.textviewDurationSum); 
        mProgressBar = (ProgressBar)findViewById(R.id.progressBarList);
        mBreadcrumb = (TextView)findViewById(R.id.breadcrumbs);
        
        //init header component last - it will call this activity callback after init to pick initial filter params
        mHeaderComponent = (HeaderComponent)findViewById(R.id.outgoingHeaderComponent);
        mHeaderComponent.initOnStart();
        mHeaderComponent.mHeaderComponentChangedCallback = this;
    }
    
    public void onClick(View view){
    	
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	mMinutesFormater = getString(R.string.minutes_formater);
    }
    
    //called after user navigates back to actvity
    @Override
    public void onRestart(){
    	super.onRestart();    		
    	mHeaderComponent.initOnRestart();
    	populateList();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	Bundle bundle;
    	Intent newIntent;
        switch (item.getItemId()) 
        {
            case R.id.setingsMenuItem:
            	Intent preferencesActivityIntent = new Intent(this, MyPreferencesActivity.class);
            	this.startActivity(preferencesActivityIntent);
            	return true;
            case R.id.chartDayMenuItem:
            	bundle = new Bundle();
            	bundle.putSerializable("filter", mCurrentFilter);
            	
            	newIntent = new Intent(this.getApplicationContext(), ChartDayActivity.class);
            	newIntent.putExtras(bundle);
            	startActivity(newIntent);
            	return true;
            case R.id.chartMonthMenuItem:
            	bundle = new Bundle();
            	bundle.putSerializable("filter", mCurrentFilter);
            	
            	newIntent = new Intent(this.getApplicationContext(), ChartMonthActivity.class);
            	newIntent.putExtras(bundle);
            	startActivity(newIntent);
            	return true;	
            case R.id.aboutMenuItem:
            	Utils.simpleDialog(this, getString(R.string.about_text), false);
            	return true;
            	
            case R.id.exitMenuItem:
            	finish();
            	return true;
        }
        return false;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
       if (keyCode == KeyEvent.KEYCODE_BACK){
    	    if (mStackFilters.size() > 0){
    	    	//current filter is previous one now
    	    	mCurrentFilter = mStackFilters.pop();
    	    	//update ui
    	    	mHeaderComponent.setSortBy(mCurrentFilter.sortBy);
            	mHeaderComponent.setDateInterval(mCurrentFilter.fromDate, mCurrentFilter.toDate);
            	populateList();
    	    }        	
        	else{
        		finish();
        	}
        	
        	return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }

	@Override
	public void onHeaderComponentChanged(SimpleDate from, SimpleDate to,
			SortByEnum sortBy) {
		mCurrentFilter.sortBy = sortBy;
		mCurrentFilter.fromDate = from;
		mCurrentFilter.toDate = to;
		populateList();
	}
	
	private void populateBreadcrumbs(String value){
		String prefix = mStackFilters.size() > 1 ? breadcrumbsPrefixNext : breadcrumbsPrefixFirst;
		mBreadcrumb.setText(prefix + value);
	}
	
	private void populateList(){
		if (!isRefreshing){
		
			isRefreshing = true;
			mProgressBar.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
			
			new AsyncTask<Object, Integer, Long>() {
				private CallStatsActivity thisActivity = null;
				
				@Override
				protected void onPreExecute(){
					if (!Utils.isNullOrEmpty(mCurrentFilter.contactName)){
						populateBreadcrumbs(mCurrentFilter.contactName);
					}
					else if (!Utils.isNullOrEmpty(mCurrentFilter.phone)){
						populateBreadcrumbs(mCurrentFilter.phone);
					}
					else if (mCurrentFilter.tag != null){
						populateBreadcrumbs((String)mCurrentFilter.tag);						
					}
					else{
						mBreadcrumb.setText(Utils.emptyString);
					}
				}
				
				@Override
				protected Long doInBackground(Object... params) {
					thisActivity = (CallStatsActivity)params[0];
					mCallModel.load(thisActivity, mCurrentFilter);
					return null;
				}
				
				@Override
				protected void onPostExecute(Long result) {
					PerformanceOutgoingListAdapter la = new PerformanceOutgoingListAdapter(thisActivity, thisActivity.getSortBy(), 
							R.id.listviewOutgoingCalls,
							mCallModel.getElements());
					mListView.setAdapter(la);
					
					long dur = mCallModel.getDurationSum();
					int hours = (int)(dur / 3600);
					int minutes = (int)(dur / 60 % 60);
					String d = Utils.getFormatedTime(hours, minutes, (int)(dur % 60));
					d += String.format(mMinutesFormater, Integer.toString(hours * 60 + minutes));
					mTextViewDuration.setText(d);
					
					isRefreshing = false;
					mProgressBar.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
			    }
			}.execute(this);
		}
		
	}
	
	public SortByEnum getSortBy(){
		return mHeaderComponent.getSortBy();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {		
		List<CallElement> elems = mCallModel.getElements();
		CallElement el = elems.get(position);
		
		//push current filter to stack
		mStackFilters.push(new CallModel.Filter(mCurrentFilter));
		
		if (!Utils.isNullOrEmpty(el.phone)){ //phone record is non empty if users are show
			//if user has name then filter by name otherwise by phone
			if (Utils.isNullOrEmpty(el.name)){
				mCurrentFilter.phone = el.phone;
				mCurrentFilter.contactName = null;
			}
			else{
				mCurrentFilter.phone = null;
				mCurrentFilter.contactName = el.name;
			}
			mCurrentFilter.tag = null; //no tag
			populateList();
		}
		else{								
			mCurrentFilter.phone = mCurrentFilter.contactName = null;		
			mCurrentFilter.tag = el.name; //save tag because it will be in breadcrumbs
			
			//calculate new filter from date - to date. new sort by depending on current sort by. Update UI
			mCurrentFilter.fromDate = mCurrentFilter.toDate = new SimpleDate(new java.util.Date(el.dateOfCall));
			if (getSortBy() ==  SortByEnum.ByMonths){
				mCurrentFilter.toDate = SimpleDate.getNextMonth(mCurrentFilter.fromDate);
				mHeaderComponent.setSortBy(mCurrentFilter.sortBy = SortByEnum.DateDesc);				
			}
			else{
				mHeaderComponent.setSortBy(mCurrentFilter.sortBy = SortByEnum.DurationDesc);
			}
				
			//update ui
			mHeaderComponent.setDateInterval(mCurrentFilter.fromDate, mCurrentFilter.toDate);
			populateList();
		}
	}
    
}