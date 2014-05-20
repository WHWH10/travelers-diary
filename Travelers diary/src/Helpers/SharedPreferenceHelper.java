package Helpers;

import com.android.diary.Config;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceHelper {
	private Context context;
	private SharedPreferences.Editor editor;
	private SharedPreferences sharedPreferences;
	private final int PREFERENCE_MODE = 0;
	
	public SharedPreferenceHelper(Context context){
		this.context = context;
		this.sharedPreferences = context.getSharedPreferences(Config.PREFS_FILE, PREFERENCE_MODE);
		this.editor = this.sharedPreferences.edit();
	}
	
	public void setAddressUpdateFlag(boolean needsUpdating){
		editor.putBoolean(Config.UPDATE_ADDRESSES, needsUpdating);
		editor.commit();
	}
	
	public boolean getAddressUpdateFlag(){
		sharedPreferences = context.getSharedPreferences(Config.PREFS_FILE, PREFERENCE_MODE);
		return sharedPreferences.getBoolean(Config.UPDATE_ADDRESSES, true);
	}
	
	public void setCameraStartTime(long time){
		editor.putLong(Config.CAMERA_START_TIME, time);
		editor.commit();
	}
	
	public long getCameraStartTime(){
		sharedPreferences = context.getSharedPreferences(Config.PREFS_FILE, PREFERENCE_MODE);
		long time = sharedPreferences.getLong(Config.CAMERA_START_TIME, 0);				
		return time;
	}
	
	public void removeValue(String key){
		editor.remove(key);
		editor.commit();
	}
	
	public void setAuthenticationUsername(String username){
		editor.putString(Config.AUTHENTICATION_USERNAME, username);
		editor.commit();
	}
	
	public String getAuthenticationUsername(){
		return sharedPreferences.getString(Config.AUTHENTICATION_USERNAME, null);
	}
}
