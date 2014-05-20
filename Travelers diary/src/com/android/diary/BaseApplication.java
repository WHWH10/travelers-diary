package com.android.diary;

import Helpers.SharedPreferenceHelper;
import android.app.Application;

public class BaseApplication extends Application {
	private String username;

	public String getUsername() {		
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
		SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(this);
		sharedPreferenceHelper.setAuthenticationUsername(username);
	}
	
	public boolean isUserLoggedIn(){
		return this.username != null && this.username != "";
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		if(username == null || username == ""){
			username = new SharedPreferenceHelper(this).getAuthenticationUsername();
		}
	}
}
