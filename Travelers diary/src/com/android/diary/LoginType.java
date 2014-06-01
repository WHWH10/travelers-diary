package com.android.diary;

public enum LoginType {
	GooglePlus ("google_plus"),
	Facebook ("facebook");
	
	private String value;
	
	private LoginType(String value){
		this.value = value;
	}

	@Override
	public String toString() {		
		return this.value;
	}
	
	public static LoginType parse(String type){
		for (int i = 0; i < LoginType.values().length; i++) {
			if(LoginType.values()[i].toString().equals(type) || LoginType.values()[i].name().equals(type))
				return LoginType.values()[i];
		}
		
		return null;
	}
}
