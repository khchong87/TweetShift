package com.bbs.tweetshift;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

public abstract class CustomizedArrayAdapter extends ArrayAdapter<Object> {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CustomizedArrayAdapter(Context context, int resource,
			int textViewResourceId, List objects) {
		super(context, resource, textViewResourceId, objects);
	}
	
	public abstract void onScrollBottom();
	
	public abstract void onScrollTop();
}
