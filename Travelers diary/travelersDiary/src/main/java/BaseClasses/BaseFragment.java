package BaseClasses;

import Helpers.MessageHelper;
import android.app.Fragment;

public class BaseFragment extends Fragment {
	
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
