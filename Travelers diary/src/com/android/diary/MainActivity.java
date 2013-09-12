package com.android.diary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends BaseActivity {
	
	private static final String LOG_TAG = "MAIN ACTIVITY";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
	}
	
	public void btnExistingRouteClicked(View view)
	{
		Intent intent = new Intent(this, RoutesActivity.class);
		startActivity(intent);
	}
}
