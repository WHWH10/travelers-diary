package com.android.diary;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class RouteDetailActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_route_detail);
		
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		
		int routeID = 0;
		if(getIntent().getExtras() != null)
		{
			routeID =  getIntent().getExtras().getInt(RouteDetailFragment.ROUTE_ID, 0);
		}
		
		RouteDetailFragment fragment = RouteDetailFragment.newInstance(routeID);
		ft.add(R.id.routeDetailFragment, fragment);
		ft.commit();
	}

}
