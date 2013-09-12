package com.android.diary;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

public abstract class BaseActivity extends Activity {
	
	public  void LogMessage(String logTag, String message)
	{
		LogToDatabase(message, "", logTag);
		Log.i(logTag, message);
	}
	
	public void LogErrorMessage(String logTag, String message)
	{
		LogToDatabase(message, "", logTag);		
		Log.e(logTag, message);
	}
	
	public void LogWarningMessage(String logTag, String message)
	{
		LogToDatabase(message, "", logTag);		
		Log.w(logTag, message);
	}
	
	public void ToastMessage(String message)
	{
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
	public void ToastMessageLong(String message)
	{
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	public boolean isNetworkAvailable()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
	}
	
	public boolean isNetworkAvailableWithToast(){
		ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager.getActiveNetworkInfo() == null || !connectivityManager.getActiveNetworkInfo().isConnected())
			ToastMessage(getString(R.string.warn_dataConnectionUnavailable));
		return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
	}
	
	private void LogToDatabase(String message, String user, String tag)
	{
		DatabaseHandler db = new DatabaseHandler(this);
		db.insertLog(message, user, tag);
		db.close();
	}
	
	public boolean IsOrientationPortrait()
	{
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}
}
