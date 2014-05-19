package com.android.diary;

import BaseClasses.BaseActivity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends BaseActivity{
//	private static final String LOG_TAG = "MAIN ACTIVITY";
	public static final String KEY_CLOSE_APP = "keyCloseApp";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			if(bundle.getBoolean(KEY_CLOSE_APP)){
				if(isLocationProviderServiceRunning(this)){
					Intent myService = new Intent(this, LocationProviderService.class);
					startService(myService);
					stopService(myService);
				}
				
				finish();
			}
		}
		
		startMainService();
	}

	private void startMainService()	{
		if(!isMainServiceRunning(this)){
			Intent myService = new Intent(this, MainService.class);
			startService(myService);
		}
	}
	
	public void btnExistingRouteClicked(View view){
		Intent intent = new Intent(this, RoutesActivity.class);
		startActivity(intent);
	}
	
	public void btnNewRouteClicked(View view){
		Intent intent = new Intent(this, NewRouteActivity.class);
		startActivity(intent);
	}
	
	public static boolean isMainServiceRunning(Context context) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	
	        if ("com.android.diary.MainService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public static boolean isLocationProviderServiceRunning(Context context) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	
	        if ("com.android.diary.LocationProviderService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public void btnGalleryClick(View view){
		Intent intent = new Intent(this, MultiPhotoSelectActivity.class);
		intent.putExtra(MultiPhotoSelectActivity.SHOW_GALLERY, true);
		startActivity(intent);
	}
	
	public void btnSettingsClick(View view){
		Intent intent = new Intent(this, LogInActivity.class);
		intent.putExtra(LogInActivity.LOG_OUT_TAG, true);
		startActivity(intent);
	}
}
