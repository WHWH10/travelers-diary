package com.android.diary;

import android.app.Application;

public class BaseApplication extends Application {
	private String userName;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public boolean isUserLoggedIn(){
		return this.userName != null && this.userName != "";
	}
}
