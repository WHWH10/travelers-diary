package com.android.diary;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import android.database.Cursor;
import android.util.Log;

public class Route {

	private static final String LOG_TAG = "ROUTE OBJ";
	
	private int routeId;
	private String title;
	private String description;
	private Date dateCreated;
	private Date dateModified;
	
	public Route(int routeId, String title, String description,
			Date dateCreated, Date dateModified) 
	{
		super();
		this.routeId = routeId;
		this.title = title;
		this.description = description;
		this.dateCreated = dateCreated;
		this.dateModified = dateModified;
	}
	
	public Route(int routeId, String title, String description,
			String dateCreated, String dateModified) 
	{
		super();
		this.routeId = routeId;
		this.title = title;
		this.description = description;
		this.setDateCreated(dateCreated);
		this.setDateModified(dateModified);
	}

	public int getRouteId() {
		return routeId;
	}

	public void setRouteId(int routeId) {
		this.routeId = routeId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public Date getDateModified() {
		return dateModified;
	}

	public void setDateModified(Date dateModified) {
		this.dateModified = dateModified;
	}
	
	public void setDateModified(String dateModified){
		try {
			this.dateModified = DateFormat.getDateTimeInstance().parse(dateModified);
		} catch (ParseException e) {
			Log.e(LOG_TAG, e.toString());
			this.dateModified = new Date();
		}
	}
	
	public static Route parse(Cursor cursor)
	{
		if(cursor == null || cursor.isClosed())
			return null;
		return new Route(cursor.getInt(cursor.getColumnIndex(DatabaseHandler.KEY_ID)),
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_TITLE)),
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_DESCRIPTION)),
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_DATE_CREATED)), 
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_DATE_MODIFIED)));
	}
}
