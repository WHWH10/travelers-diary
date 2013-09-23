package Helpers;

import com.android.diary.Config;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceHelper {
	private Context context;
	private final int PREFERENCE_MODE = 0;
	
	public SharedPreferenceHelper(Context context)
	{
		this.context = context;
	}
	
	public void setAddressUpdateFlag(boolean needsUpdating)
	{
		SharedPreferences.Editor editor = context.getSharedPreferences(Config.PREFS_FILE, PREFERENCE_MODE).edit();
		editor.putBoolean(Config.UPDATE_ADDRESSES, needsUpdating);
		editor.commit();
	}
	
	public boolean getAddressUpdateFlag()
	{
		SharedPreferences sharedPreferences = context.getSharedPreferences(Config.PREFS_FILE, PREFERENCE_MODE);
		return sharedPreferences.getBoolean(Config.UPDATE_ADDRESSES, true);
	}
}
