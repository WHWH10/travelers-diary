package Helpers;

import java.util.ArrayList;
import java.util.List;

import com.android.diary.Config;
import com.android.diary.LoginInfo;
import com.android.diary.LoginType;

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
	
	public void setAuthenticationData(List<LoginInfo> loginInfos){
		if(loginInfos != null){
			String info = "";
			
			for (int i = 0; i < loginInfos.size(); i++) {
				if(i == 0)
					info += loginInfos.get(i).toString();
				else {
					info += ";" + loginInfos.get(i).toString();
				}
			}
			
			editor.putString(Config.AUTHENTICATION_DATA, info);
			editor.commit();
		}
		else {
			editor.putString(Config.AUTHENTICATION_DATA, null);
			editor.commit();
		}
	}
	
	public List<LoginInfo> getAuthenticationData(){
		String info = sharedPreferences.getString(Config.AUTHENTICATION_DATA, null);
		
		if(info == null || info == "")
			return null;
		else {
			List<LoginInfo> loginInfos = new ArrayList<LoginInfo>();
			
			String[] values = info.split(";");
			
			for (int i = 0; i < values.length; i+=3) {
				loginInfos.add(new LoginInfo(values[i], values[i+1], LoginType.parse(values[i+2])));
			}
			
			return loginInfos;
		}
	}
}
