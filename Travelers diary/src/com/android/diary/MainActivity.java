package com.android.diary;

import BaseClasses.BaseActivity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends BaseActivity {
	
//	private static final String LOG_TAG = "MAIN ACTIVITY";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		startMainService();
	}
	
	private void startMainService()
	{
		if(!isMainServiceRunning())
		{
			Intent myService = new Intent(this, MainService.class);
			startService(myService);
		}
	}
	
	public void btnExistingRouteClicked(View view)
	{
		Intent intent = new Intent(this, RoutesActivity.class);
		startActivity(intent);
	}
	
	private boolean isMainServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	
	        if ("com.android.diary.MainService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public void btnGalleryClick(View view)
	{
		Intent intent = new Intent(this, MultiPhotoSelectActivity.class);
		intent.putExtra(MultiPhotoSelectActivity.SHOW_GALLERY, true);
		startActivity(intent);
	}
}
