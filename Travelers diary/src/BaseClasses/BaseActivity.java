package BaseClasses;

import com.android.diary.Globals;
import com.android.diary.LogInActivity;
import com.android.diary.R;

import Helpers.MessageHelper;
import android.app.Activity;
import android.content.Intent;

public abstract class BaseActivity extends Activity {
	
	@Override
	protected void onStart() {		
		super.onStart();
		checkAuthentication();
	}
	
	private void checkAuthentication(){
		if(!Globals.isUserLoggedIn(getApplicationContext())){
			ToastMessage(getString(R.string.warn_pleaseLogIn));
			Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
			startActivity(intent);
		}
	}

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
