package com.rogicrew.callstats;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class MyPreferencesActivity extends PreferenceActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        
        ListPreference prefStartOfMonth = (ListPreference)this.findPreference("start_of_month");
        String[] dates = new String[28];
        for (int i = 0; i < 28; ++i){
        	dates[i] = Integer.toString(i+1);
        }
        prefStartOfMonth.setEntries(dates);
        prefStartOfMonth.setDefaultValue(1);
        prefStartOfMonth.setEntryValues(dates);
        
        ListPreference prefMinuteRounding = (ListPreference)this.findPreference("minute_rounding");
        String[] keys = new String[prefMinuteRounding.getEntries().length];
        for (int i = 0; i < keys.length; ++i){
        	keys[i] = Integer.toString(i);
        }
       // prefMinuteRounding.setEntries(R.array.rounded_by);
        prefMinuteRounding.setEntryValues(keys);
        prefMinuteRounding.setDefaultValue(0);
        
        //only numbers are alowed        
        /*EditText myEditText = ((EditTextPreference)this.findPreference("pie_chart_entries")).getEditText(); 
        myEditText.setKeyListener(DigitsKeyListener.getInstance(false, false));
        myEditText.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(1) });*/
    }
}
