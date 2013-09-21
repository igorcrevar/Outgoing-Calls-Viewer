package com.rogicrew.callstats.components;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rogicrew.callstats.R;
import com.rogicrew.callstats.models.CallElement;
import com.rogicrew.callstats.models.SortByEnum;
import com.rogicrew.callstats.utils.Utils;

/* http://sogacity.com/how-to-make-a-custom-arrayadapter-for-listview/ */
public class PerformanceOutgoingListAdapter extends ArrayAdapter<CallElement> {
	private final List<CallElement> elements;
	private final SortByEnum sortBy;
	private final Activity activity;

	public PerformanceOutgoingListAdapter(Activity context, SortByEnum sortBy, int textViewId, List<CallElement> elements) {
		super(context, textViewId, elements);
		this.activity = context;
		this.sortBy = sortBy;
		this.elements = elements;
	}
	
	private class ViewHolder {
		public ViewHolder(View view) {
			nameView = (TextView) view.findViewById(R.id.listName);
			phoneView = (TextView) view.findViewById(R.id.listPhoneNumber);
			dateView = (TextView) view.findViewById(R.id.listDate);
			durationView = (TextView) view.findViewById(R.id.listDuration);
		}
		
		public void update(String name, String phone, String date, String duration) {
			nameView.setText(name);
			phoneView.setText(phone);
			dateView.setText(date);
			durationView.setText(duration);
		}
		
        public TextView nameView;
        public TextView durationView;
        public TextView dateView;
        public TextView phoneView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;		
		CallElement el = elements.get(position);
		View rowView = convertView;
		
		if (rowView == null) {
			rowView = activity.getLayoutInflater().inflate(R.layout.outgoing_list_layout, null);
			viewHolder = new ViewHolder(rowView);
			rowView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder)rowView.getTag();
		}
		
		// generate duration string
		long duration = el.getDuration();
		String durationStr = Utils.getFormatedTime((int)(duration / 3600), (int)(duration / 60 % 60), (int)(duration % 60));
		if (sortBy.ordinal() >= SortByEnum.ByDays.ordinal()) {
			int minSum = (int)(duration / 3600 * 60 +  duration / 60 % 60);
			durationStr = String.format("%s (%d)", durationStr, minSum);
		}
		
		// generate date string
		String dateStr;
		switch (sortBy)
		{
		case ByMonths: case ByMonthsDurationAsc: case ByMonthsDurationDesc:
		case ByDays: case ByDaysDurationAsc: case ByDaysDurationDesc:
			dateStr = "";
			break;
		default:
			dateStr = Utils.getFormatedDateTime(this.getContext(), el.getDateOfCall());
		}
		
		viewHolder.update(
				el.getName(),
				el.getPhone(),
				dateStr,
				durationStr);
		
		return rowView;
	}

}