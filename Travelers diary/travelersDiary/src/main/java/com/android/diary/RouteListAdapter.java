package com.android.diary;

import java.util.List;
import java.util.Map;

import BaseClasses.BaseArrayAdapterImage;
import android.content.Context;

public class RouteListAdapter extends BaseArrayAdapterImage {	

	public RouteListAdapter(Context context, List<Map<String, String>> values) {
		super(context, values);
	}
	
	@Override
	protected String getImagePathFromDatabase(int position){
		DatabaseHandler db = new DatabaseHandler(this.context);
		String imagePath = db.getDefaultRouteImage(Integer.parseInt(values.get(position).get("id")));
		db.close();
		return imagePath;
	}
}
