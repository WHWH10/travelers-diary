package com.android.diary;

import Helpers.MessageHelper;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;

public  class Globals {
	
	public static String getUsername(Context context){
		return ((BaseApplication)context.getApplicationContext()).getUsername();
		
	}
	
	public static boolean isUserLoggedIn(Context context){
		return ((BaseApplication)context.getApplicationContext()).isUserLoggedIn();		
	}
	
	public static boolean isOrientationPortrait(Context context){
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}
	
	public static boolean isNetworkAvailable(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
	}
	
	public static boolean isNetworkAvailableWithToast(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager.getActiveNetworkInfo() == null || !connectivityManager.getActiveNetworkInfo().isConnected())
			MessageHelper.ToastMessage(context.getApplicationContext(), context.getApplicationContext().getString(R.string.warn_dataConnectionUnavailable));
		
		return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
	}
}
