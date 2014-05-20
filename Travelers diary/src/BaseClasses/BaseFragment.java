package BaseClasses;

import Helpers.MessageHelper;
import android.app.Fragment;
import android.content.Intent;

import com.android.diary.Globals;
import com.android.diary.LogInActivity;
import com.android.diary.R;

public class BaseFragment extends Fragment {
	
	@Override
	public void onStart() {
		super.onStart();
		checkAuthentication();
	}
	
	private void checkAuthentication(){
		if(!Globals.isUserLoggedIn(getActivity().getApplicationContext())){
			ToastMessage(getString(R.string.warn_pleaseLogIn));
			Intent intent = new Intent(getActivity().getApplicationContext(), LogInActivity.class);
			startActivity(intent);
		}
	}

	public void LogMessage(String logTag, String message){
		MessageHelper.LogMessage(getActivity(), logTag, message);
	}	
	
	public void LogErrorMessage(String logTag, String message){
		MessageHelper.LogErrorMessage(getActivity(), logTag, message);
	}
	
	public void LogWarningMessage(String logTag, String message){
		MessageHelper.LogWarningMessage(getActivity(), logTag, message);
	}
	
	public void ToastMessage(String message){
		MessageHelper.ToastMessage(getActivity().getApplicationContext(), message);
	}
	
	public void ToastMessageLong(String message){
		MessageHelper.ToastMessageLong(getActivity().getApplicationContext(), message);
	}
}
