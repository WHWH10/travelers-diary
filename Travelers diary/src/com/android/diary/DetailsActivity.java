package com.android.diary;

import com.android.diary.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailsActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		Bundle bundle = getIntent().getExtras();
		
		String text;
		
		TextView title = (TextView) findViewById(R.id.titleText_details);
		text = bundle.getString(MainActivity.MARKER_TITLE);
		if(!text.equals(""))
			title.setText(text);
		
		TextView description = (TextView) findViewById(R.id.descriptionText_details);
		text = bundle.getString(MainActivity.MARKER_DESCRIPTION);
		if(!text.equals(""))
			description.setText(text);
		
		TextView address = (TextView) findViewById(R.id.addressText_details);
		text = bundle.getString(MainActivity.MARKER_ADDRESS);
		if(!text.equals(""))
			address.setText(text);
		
		TextView lat = (TextView) findViewById(R.id.coordinatesLat_details);		
		lat.append(" " + String.valueOf(bundle.getDouble(MainActivity.MARKER_LAT, 0)));
		
		TextView lon = (TextView) findViewById(R.id.coordinatesLon_details);
		lon.append(" " + String.valueOf(bundle.getDouble(MainActivity.MARKER_LON, 0)));
		
		TextView date = (TextView) findViewById(R.id.dateText_details);
		date.setText(bundle.getString(MainActivity.MARKER_DATE));
		
		TextView route = (TextView) findViewById(R.id.routeText_details);
		String r = bundle.getString(MainActivity.ROUTE);
		if(r.equals(""))
			r = getResources().getString(R.string.route) + ": " + bundle.getInt(MainActivity.ROUTE_ID);
		route.setText(r);
	}	
}
