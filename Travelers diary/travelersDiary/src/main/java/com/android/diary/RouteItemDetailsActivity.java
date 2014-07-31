package com.android.diary;

import com.android.diary.R;

import BaseClasses.BaseActivity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class RouteItemDetailsActivity extends BaseActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_route_item_details);
		
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		
		int routeItemId = 0;
		
		if(getIntent().getExtras() != null)
		{
			routeItemId = getIntent().getExtras().getInt(RouteItemDetailFragment.ROUTE_ITEM_ID, 0);
		}
		
		RouteItemDetailFragment fragment = RouteItemDetailFragment.newInstance(routeItemId);
		ft.add(R.id.routeItemDetailFragment, fragment);
		ft.commit();
	}	
}
