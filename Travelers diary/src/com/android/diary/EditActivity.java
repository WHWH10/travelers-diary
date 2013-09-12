package com.android.diary;

import com.android.diary.R;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class EditActivity extends BaseActivity{
	
//	private static final String LOG_TAG = "EDIT ACTIVITY";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_item_edit);
		
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		
		int routeItemId = 0;
		
		if(getIntent().getExtras() != null)
		{
			routeItemId = getIntent().getExtras().getInt(RouteItemEditFragment.ROUTE_ITEM_ID, 0);
		}
		
		RouteItemEditFragment fragment = RouteItemEditFragment.newInstance(routeItemId);
		ft.add(R.id.routeItemEditFragment, fragment);
		ft.commit();
	}	
}
