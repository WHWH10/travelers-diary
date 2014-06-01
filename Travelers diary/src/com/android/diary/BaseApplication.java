package com.android.diary;

import java.util.ArrayList;
import java.util.List;

import Helpers.SharedPreferenceHelper;
import android.app.Application;

public class BaseApplication extends Application {
	private List<LoginInfo> logIns;

	public String getUsername() {
		if(logIns != null && logIns.size() > 0){
			return logIns.get(0).getDisplayName();
		}
		
		return null;
	}
	
	public LoginInfo getUserInfo(LoginType loginType){
		if(logIns == null || logIns.size() == 0)
			return null;
		else {
			if(loginType == null){
				return logIns.get(0);
			}else {
				for (int i = 0; i < logIns.size(); i++) {
					if(logIns.get(i).getLoginType() == loginType)
						return logIns.get(i);
				}
				
				return logIns.get(0);
			}
		}		
	}
	
	public void removeUserInfo(LoginType loginType){
		if(logIns != null){
			for (int i = 0; i < logIns.size(); i++) {
				if(logIns.get(i).getLoginType() == loginType)
					logIns.remove(i);
			}
		}
	}
	
	public void setUserInfo(String email, String displayName, LoginType loginType){
		if(logIns == null){
			logIns = new ArrayList<LoginInfo>();
			
			logIns.add(new LoginInfo(email, displayName, loginType));
		}
		else {
			if(logIns.size() == 0)
				logIns.add(new LoginInfo(email, displayName, loginType));
			else{
				boolean added = false;
				
				for (int i = 0; i < logIns.size(); i++) {
					if(logIns.get(i).getLoginType() == loginType){
						return;
					}
				}
				
				if(!added){
					logIns.add(new LoginInfo(email, displayName, loginType));
				}
			}
		}
		
		SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(this);
		sharedPreferenceHelper.setAuthenticationData(logIns);
	}
	
	public boolean isUserLoggedIn(){
		return this.logIns != null && this.logIns.size() > 0;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		logIns = new ArrayList<LoginInfo>();
		
		if(logIns == null || logIns.size() == 0){
			logIns = new SharedPreferenceHelper(this).getAuthenticationData();
		}
	}
}
