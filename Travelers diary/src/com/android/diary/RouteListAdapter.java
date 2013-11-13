package com.android.diary;

import java.util.List;
import java.util.Map;

import BaseClasses.BaseArrayAdapterImage;
import android.content.Context;
import android.util.DisplayMetrics;

public class RouteListAdapter extends BaseArrayAdapterImage {	

	public RouteListAdapter(Context context, List<Map<String, String>> values, DisplayMetrics displayMetrics, boolean isOrientationPortrait) {
		super(context, values, displayMetrics, isOrientationPortrait);
	}
	
	@Override
	protected String getImagePathFromDatabase(int position){
		DatabaseHandler db = new DatabaseHandler(this.context);
		String imagePath = db.getDefaultRouteImage(Integer.parseInt(values.get(position).get("id")));
		db.close();
		return imagePath;
	}
}
