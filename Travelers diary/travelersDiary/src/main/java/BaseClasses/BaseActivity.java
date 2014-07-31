package BaseClasses;

import Helpers.MessageHelper;
import android.app.Activity;

public abstract class BaseActivity extends Activity {
	
	public void LogMessage(String logTag, String message){
		MessageHelper.LogMessage(this, logTag, message);
	}	
	
	public void LogErrorMessage(String logTag, String message){
		MessageHelper.LogErrorMessage(this, logTag, message);
	}
	
	public void LogWarningMessage(String logTag, String message){
		MessageHelper.LogWarningMessage(this, logTag, message);
	}
	
	public void ToastMessage(String message){
		MessageHelper.ToastMessage(getApplicationContext(), message);
	}
	
	public void ToastMessage(CharSequence message){
		MessageHelper.ToastMessage(getApplicationContext(), message);
	}
	
	public void ToastMessageLong(String message){
		MessageHelper.ToastMessageLong(getApplicationContext(), message);
	}
	
	public void ToastMessageLong(CharSequence message){
		MessageHelper.ToastMessageLong(getApplicationContext(), message);
	}	
}
