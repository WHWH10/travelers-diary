package BaseClasses;

import Helpers.MessageHelper;
import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;

import com.android.diary.R;

public class BaseFragment extends Fragment {
	
	public void LogMessage(String logTag, String message)
	{
		MessageHelper.LogMessage(getActivity(), logTag, message);
	}	
	
	public void LogErrorMessage(String logTag, String message)
	{
		MessageHelper.LogErrorMessage(getActivity(), logTag, message);
	}
	
	public void LogWarningMessage(String logTag, String message)
	{
		MessageHelper.LogWarningMessage(getActivity(), logTag, message);
	}
	
	public void ToastMessage(String message)
	{
		MessageHelper.ToastMessage(getActivity(), message);
	}
	
	public void ToastMessageLong(String message)
	{
		MessageHelper.ToastMessageLong(getActivity(), message);
	}
	
	public boolean isNetworkAvailable()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
	}
	
	public boolean isNetworkAvailableWithToast(){
		ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager.getActiveNetworkInfo() == null || !connectivityManager.getActiveNetworkInfo().isConnected())
			ToastMessage(getString(R.string.warn_dataConnectionUnavailable));
		return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
	}
}
