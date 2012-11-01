package com.android.diary;

import java.util.Locale;

import android.location.Address;
import android.location.Location;

public class LocationInfo {

	private static final int COUNTRY = -1;
	private static final int ADMIN_AREA = -2;
	private static final int FEATURE = -3;
	private static final int LATITUDE = -4;
	private static final int LONGITUDE = -5;
	private static final int POSTAL_CODE = -6;
	private static final int ADDRESS_LINE = -7;
	
	private int _id;
	private int route_id;
	private long time;
	private String title, description;
	private Address address;
	private String route;
	
	public LocationInfo()
	{
		this.time = 0;
		this.title = "";
		this.description = "";
		this.route = "";
		this.route_id = 0;
		this.address = null;
	}
	
	public LocationInfo(int id, Address address, int route_id, String route, String title, String description, long time) 
	{
		super();
		this._id = id;
		this.time = time;
		this.title = title;
		this.route_id = route_id;
		this.route = route;
		this.description = description;
		this.address = address;
	}
	
	public LocationInfo(int id, String address, int route_id, String route, String title, String description, long time) 
	{
		super();
		this._id = id;
		this.time = time;
		this.title = title;
		this.route_id = route_id;
		this.route = route;
		this.description = description;
		this.address = getAddressFromString(address);
	}	

	public LocationInfo(Location location, int route_id)
	{
		this.address = new Address(Locale.getDefault());
		this.address.setLatitude(location.getLatitude());
		this.address.setLongitude(location.getLongitude());
		this.time = location.getTime();
		this.route_id = route_id;
		this.route = "";
		this.title = "";
		this.description = "";
	}
	
	private Address getAddressFromString(String address)
	{
		System.out.println(address);
		Address ad = new Address(Locale.getDefault());
		String str[] = address.split(" ");
		String s = "";
		for(int i = 0; i < str.length; i++)
		{
			try {
				switch (Integer.parseInt(str[i])) {
				case COUNTRY:
					if(!s.contains("null"))
						ad.setCountryName(s);
					s = "";
					break;
					
				case ADMIN_AREA:
					if(!s.contains("null"))
						ad.setAdminArea(s);
					s = "";
					break;
					
				case FEATURE:
					if(!s.contains("null"))
						ad.setFeatureName(s);
					s = "";
					break;
					
				case LATITUDE:
					ad.setLatitude(Double.parseDouble(s));
					s = "";
					break;
					
				case LONGITUDE:
					ad.setLongitude(Double.parseDouble(s));
					s = "";
					break;
					
				case POSTAL_CODE:
					if(!s.contains("null"))
						ad.setPostalCode(s);
					s = "";
					break;
					
				case ADDRESS_LINE:
					if(!s.contains("null"))
						ad.setAddressLine(0, s);
					s = "";
					break;

				default:
					int j = Integer.parseInt(str[i]);
					if(j > 0)
					{
						if(s.equals(""))
							s = String.valueOf(j);
						else
							s = s + " " + j;
					}
					break;
				}
			} catch (NumberFormatException e) {
				if(s.equals(""))
					s = str[i];
				else
					s = s + " " + str[i];
			}
			
		}
		return ad;
	}
	
	public int getRoute_id() {
		return route_id;
	}

	public void setRoute_id(int route_id) {
		this.route_id = route_id;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
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

	public Address getAddress() {
		return address;
	}
	
	public String getAddressInString() {
		String str = address.getCountryName() + " " + COUNTRY + " " + address.getAdminArea() + 
				" " + ADMIN_AREA + " " + address.getFeatureName() + " " + FEATURE + " " + address.getLatitude() + 
				" " + LATITUDE + " " + address.getLongitude() + " " + LONGITUDE + " " + address.getPostalCode() + 
				" " + POSTAL_CODE + " " + address.getAddressLine(0) + " " + ADDRESS_LINE;
		return str;
	}

	public void setAddress(Address address) {
		this.address = address;
	}
	
	public void setAddress(String address)
	{
		this.address = getAddressFromString(address);
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	public boolean hasTitle()
	{
		if(this.title.equals(""))
			return true;
		return false;
	}
	
	public boolean hasDescription()
	{
		if(this.description.equals(""))
			return true;
		return false;
	}
}
