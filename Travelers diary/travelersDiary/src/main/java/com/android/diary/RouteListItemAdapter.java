package com.android.diary;

import java.util.List;
import java.util.Map;

import android.content.Context;
import BaseClasses.BaseArrayAdapterImage;

public class RouteListItemAdapter extends BaseArrayAdapterImage {

	public RouteListItemAdapter(Context context, List<Map<String, String>> values) {
		super(context, values);
	}
	
	@Override
	protected String getImagePathFromDatabase(int position){
		DatabaseHandler db = new DatabaseHandler(this.context);
		String imagePath = db.getDefaultRouteItemImage(Integer.parseInt(values.get(position).get("id")));
		db.close();
		return imagePath;
	}
}
