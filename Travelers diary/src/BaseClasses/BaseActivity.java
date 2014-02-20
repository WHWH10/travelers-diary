package BaseClasses;

import com.android.diary.R;

import Helpers.MessageHelper;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;

public abstract class BaseActivity extends Activity {
	
	public void LogMessage(String logTag, String message)
	{
		MessageHelper.LogMessage(this, logTag, message);
	}	
	
	public void LogErrorMessage(String logTag, String message)
	{
		MessageHelper.LogErrorMessage(this, logTag, message);
	}
	
	public void LogWarningMessage(String logTag, String message)
	{
		MessageHelper.LogWarningMessage(this, logTag, message);
	}
	
	public void ToastMessage(String message)
	{
		MessageHelper.ToastMessage(this, message);
	}
	
	public void ToastMessage(CharSequence message)
	{
		MessageHelper.ToastMessage(this, message);
	}
	
	public void ToastMessageLong(String message)
	{
		MessageHelper.ToastMessageLong(this, message);
	}
	
	public void ToastMessageLong(CharSequence message)
	{
		MessageHelper.ToastMessageLong(this, message);
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
	
	public boolean IsOrientationPortrait()
	{
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}
}
