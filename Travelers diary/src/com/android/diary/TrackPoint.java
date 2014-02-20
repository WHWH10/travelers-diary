package com.android.diary;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import android.database.Cursor;
import android.location.Address;
import android.util.Log;

public class TrackPoint {

	private static final String LOG_TAG = "TRACK POINT OBJ";
	
	private int trackPointId;
	private int routeId;
	private Date dateCreated;
	private Address address;
	private boolean isImported;
	
	public TrackPoint(int trackPointId, int routeId, Date dateCreated, Address address, boolean isImported) 
	{
		super();
		this.trackPointId = trackPointId;
		this.routeId = routeId;
		this.dateCreated = dateCreated;
		this.address = address;
		this.isImported = isImported;
	}
	
	public TrackPoint(int trackPointId, int routeId, String dateCreated, Address address, boolean isImported) 
	{
		super();
		this.trackPointId = trackPointId;
		this.routeId = routeId;
		this.setDateCreated(dateCreated);
		this.address = address;
		this.isImported = isImported;
	}

	public int getTrackPointId() {
		return trackPointId;
	}

	public void setTrackPointId(int trackPointId) {
		this.trackPointId = trackPointId;
	}

	public int getRouteId() {
		return routeId;
	}

	public void setRouteId(int routeId) {
		this.routeId = routeId;
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
	
	public boolean getIsImported()
	{
		return this.isImported;
	}
	
	public void setIsImported(boolean isImported)
	{
		this.isImported = isImported;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}
	
	public double getLatitude()
	{
		if(this.address.hasLatitude())
			return this.address.getLatitude();
		return 0.0;
	}
	
	public double getLongitude()
	{
		if(this.address.hasLongitude())
			return this.address.getLongitude();
		return 0.0;
	}
	
	public static TrackPoint parse(Cursor cursor)
	{
		if(cursor == null || cursor.isClosed())
			return null;
		return new TrackPoint(cursor.getInt(cursor.getColumnIndex(DatabaseHandler.KEY_ID)),
				cursor.getInt(cursor.getColumnIndex(DatabaseHandler.KEY_ROUTE_ID)),
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_DATE_CREATED)), 
				parseAddress(cursor),
				Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_IS_IMPORTED))));
	}
	
	public static Address parseAddress(Cursor cursor)
	{
		String locale = "en";

		Address address = new Address(new Locale(locale));
		address.setLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_LATITUDE)));
		address.setLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_LONGITUDE)));
		
		return address;
	}
}
