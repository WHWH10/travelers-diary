package com.android.diary;

public class LoginInfo {
	private String email;
	private String displayName;
	private LoginType loginType;
	
	public LoginInfo(String email, String displayName, LoginType loginType) {
		super();
		this.email = email;
		this.displayName = displayName;
		this.loginType = loginType;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public LoginType getLoginType() {
		return loginType;
	}
	
	public void setLoginType(LoginType loginType) {
		this.loginType = loginType;
	}

	@Override
	public String toString() {
		return this.email + ";" + this.displayName + ";" + this.loginType.toString();
	}
}
