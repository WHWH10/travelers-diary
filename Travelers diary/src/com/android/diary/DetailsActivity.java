package com.android.diary;

import com.android.diary.R;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class DetailsActivity extends Activity{

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
