package com.android.diary;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import android.database.Cursor;
import android.location.Address;
import android.util.Log;

public class RouteItem {

	private static final String LOG_TAG = "ROUTE ITEM OBJ";
	
	private int routeItemId;
	private int routeId;
	private String title;
	private String description;
	private Date dateCreated;
	private Date dateModified;
	private Address address;
	private boolean isImported;
	
	public RouteItem(int routeItemId, int routeId, String title,
			String description, Date dateCreated, Date dateModified,
			Address address, boolean isImported) 
	{
		super();
		this.routeItemId = routeItemId;
		this.routeId = routeId;
		this.title = title;
		this.description = description;
		this.dateCreated = dateCreated;
		this.dateModified = dateModified;
		this.address = address;
		this.isImported = isImported;
	}
	
	public RouteItem(int routeItemId, int routeId, String title,
			String description, String dateCreated, String dateModified,
			Address address, boolean isImported) 
	{
		super();
		this.routeItemId = routeItemId;
		this.routeId = routeId;
		this.title = title;
		this.description = description;
		this.setDateCreated(dateCreated);
		this.setDateModified(dateModified);
		this.address = address;
		this.isImported = isImported;
	}

	public int getRouteItemId() {
		return routeItemId;
	}

	public void setRouteItemId(int routeItemId) {
		this.routeItemId = routeItemId;
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
	
	public boolean getIsImported()
	{
		return this.isImported;
	}
	
	public void setIsImported(boolean isImported)
	{
		this.isImported = isImported;
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
	
	public static RouteItem parse(Cursor cursor)
	{
		if(cursor == null || cursor.isClosed())
			return null;
		return new RouteItem(cursor.getInt(cursor.getColumnIndex(DatabaseHandler.KEY_ID)),
				cursor.getInt(cursor.getColumnIndex(DatabaseHandler.KEY_ROUTE_ID)),
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_TITLE)),
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_DESCRIPTION)),
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_DATE_CREATED)), 
				cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_DATE_MODIFIED)),
				parseAddress(cursor),
				Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_DATE_MODIFIED))));
	}
	
	public static Address parseAddress(Cursor cursor)
	{
		String locale = cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_LOCALE));
		if(locale == null || locale.length() == 0){
			locale = "en";
		}

		Address address = new Address(new Locale(locale));
		address.setAddressLine(0, cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_ADDRESS_LINE)));		
		address.setAdminArea(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_ADMIN_AREA)));
		address.setCountryName(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_COUNTRY)));
		address.setFeatureName(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_FEATURE)));
		address.setLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_LATITUDE)));
		address.setLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_LONGITUDE)));
		address.setPostalCode(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_POSTAL_CODE)));
		address.setThoroughfare(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_THOROUGHFARE)));
		address.setSubThoroughfare(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_SUB_THOROUGHFARE)));
		address.setLocality(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_LOCALITY)));
		address.setCountryCode(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_COUNTRY_CODE)));
		
		return address;
	}
}
