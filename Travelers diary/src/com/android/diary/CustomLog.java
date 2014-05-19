package com.android.diary;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import android.database.Cursor;
import android.util.Log;

public class CustomLog {
	private static final String LOG_TAG = "CUSTOM LOG OBJ";
	
	private int logId;
	private String os;
	private String device;
	private String model;
	private String product;
	private String message;
	private String tag;
	private String username;
	private Date dateCreated;
	
	public CustomLog(int logId, String os, String device, String model,
			String product, String message, String tag, String username,
			Date dateCreated) {
		super();
		this.logId = logId;
		this.os = os;
		this.device = device;
		this.model = model;
		this.product = product;
		this.message = message;
		this.tag = tag;
		this.username = username;
		this.dateCreated = dateCreated;
	}
	
	public CustomLog(int logId, String os, String device, String model,
			String product, String message, String tag, String username,
			String dateCreated) {
		super();
		this.logId = logId;
		this.os = os;
		this.device = device;
		this.model = model;
		this.product = product;
		this.message = message;
		this.tag = tag;
		this.username = username;
		this.setDateCreated(dateCreated);
	}

	public int getLogId() {
		return logId;
	}

	public void setLogId(int logId) {
		this.logId = logId;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public void setDateCreated(String dateCreated){
		try {
			this.dateCreated = DateFormat.getDateTimeInstance().parse(dateCreated);
		} catch (ParseException e) {
			Log.e(LOG_TAG, e.toString());
			this.dateCreated = new Date();
		}
	}

	public static String getLogTag() {
		return LOG_TAG;
	}
	
	public static CustomLog parse(Cursor cursor){
		if(cursor == null || cursor.isClosed())
			return null;

		return new CustomLog(cursor.getInt(cursor.getColumnIndex(DatabaseHandler.KEY_ID)), 
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_OS)), 
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_DEVICE)), 
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_MODEL)), 
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_PRODUCT)), 
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_LOG_MESSAGE)), 
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_TAG)), 
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_USER)), 
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_DATE_CREATED)));
	}
}
