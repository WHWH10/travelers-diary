package com.android.diary;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "locInfoManager";
	private static final String TABLE_LOC_INFO = "loc_info";
	
	private static final String KEY_ID = "id";
	private static final String KEY_TIME = "time";
	private static final String KEY_TITLE = "title";
	private static final String KEY_DESCRIPTION = "description";
	private static final String KEY_ADDRESS = "address";
	private static final String KEY_ROUTE = "route";
	private static final String KEY_ROUTE_ID = "route_id";
	
	public DatabaseHandler(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		String CREATE_LOC_INFO_TABLE = "CREATE TABLE " + TABLE_LOC_INFO +
				"(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT," +
				KEY_DESCRIPTION + " TEXT," + KEY_ROUTE_ID + " REAL," + KEY_ROUTE + " TEXT," + KEY_ADDRESS + 
				" TEXT," + KEY_TIME + " TEXT" + ")";
		db.execSQL(CREATE_LOC_INFO_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOC_INFO);
		
		onCreate(db);
	}
	
	public void addLocationInfo(LocationInfo locationInfo)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_ADDRESS, locationInfo.getAddressInString());
		values.put(KEY_DESCRIPTION, locationInfo.getDescription());
		values.put(KEY_TITLE, locationInfo.getTitle());
		values.put(KEY_ROUTE, locationInfo.getRoute());
		values.put(KEY_TIME, locationInfo.getTime());
		values.put(KEY_ROUTE_ID, locationInfo.getRoute_id());
		
		db.insertOrThrow(TABLE_LOC_INFO, null, values);
		db.close();
	}
	
	public LocationInfo getLocationInfo(int id)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.query(TABLE_LOC_INFO, new String[] {KEY_ID, KEY_ADDRESS, 
				KEY_TITLE, KEY_DESCRIPTION, KEY_TIME, KEY_ROUTE, KEY_ROUTE_ID}, KEY_ID + "=?", 
				new String[] {String.valueOf(id)},
				null, null, null, null);
		if(cursor != null)
		{
			cursor.moveToFirst();
		}
		
		LocationInfo loc = new LocationInfo(Integer.parseInt(cursor.getString(0)), 
				cursor.getString(1), cursor.getInt(6), cursor.getString(5), cursor.getString(2), 
				cursor.getString(3), Long.parseLong(cursor.getString(4)));
		cursor.close();
		return loc;
	}
	
	public List<LocationInfo> getAllLocationsInfos()
	{
		List<LocationInfo> locList = new ArrayList<LocationInfo>();
		
		String selectQuery = "SELECT * FROM " + TABLE_LOC_INFO;
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if(cursor.moveToFirst())
		{
			do
			{
				LocationInfo loc = new LocationInfo();
				loc.set_id(Integer.parseInt(cursor.getString(0)));
				loc.setTitle(cursor.getString(1));
				loc.setDescription(cursor.getString(2));
				loc.setRoute(cursor.getString(3));
				loc.setAddress(cursor.getString(4));
				loc.setTime(Long.parseLong(cursor.getString(5)));
				
				locList.add(loc);				
			}
			while(cursor.moveToNext());
		}
		cursor.close();
		return locList;
	}
	
	public List<LocationInfo> getRoute(int route)
	{
		List<LocationInfo> locList = new ArrayList<LocationInfo>();
		
		String selectQuery = "SELECT * FROM " + TABLE_LOC_INFO + " WHERE " + route + " = " 
				+ TABLE_LOC_INFO + "." + KEY_ROUTE_ID;
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if(cursor.moveToFirst())
		{
			do
			{
				LocationInfo loc = new LocationInfo();
				loc.set_id(Integer.parseInt(cursor.getString(0)));
				loc.setTitle(cursor.getString(1));
				loc.setDescription(cursor.getString(2));
				loc.setRoute_id(cursor.getInt(3));
				loc.setRoute(cursor.getString(4));
				loc.setAddress(cursor.getString(5));
				loc.setTime(Long.parseLong(cursor.getString(6)));
				
				locList.add(loc);
			}
			while(cursor.moveToNext());
		}
		cursor.close();
		return locList;
	}
	
	public List<LocationInfo> getRouteTitles()
	{
		List<LocationInfo> locList = new ArrayList<LocationInfo>();
		
//		String selectQuery = "SELECT " + KEY_ROUTE + " ," + KEY_TIME + " FROM " + TABLE_LOC_INFO;
		String selectQuery = "SELECT * FROM " + TABLE_LOC_INFO;
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if(cursor.moveToFirst())
		{
			do
			{
				boolean exists = false;
				for(int i = 0; i < locList.size(); i++)
					if(locList.get(i).getRoute_id() == cursor.getInt(3))
						exists = true;
				if(!exists)
				{
					LocationInfo loc = new LocationInfo();
					loc.set_id(Integer.parseInt(cursor.getString(0)));
					loc.setTitle(cursor.getString(1));
					loc.setDescription(cursor.getString(2));
					loc.setRoute_id(cursor.getInt(3));
					loc.setRoute(cursor.getString(4));
					loc.setAddress(cursor.getString(5));
					loc.setTime(Long.parseLong(cursor.getString(6)));
					
					locList.add(loc);
				}				
			}
			while(cursor.moveToNext());
		}
		cursor.close();
		return locList;
	}
		
	public int getLocationInfosCount()
	{
		String countQuery = "SELECT * FROM " + TABLE_LOC_INFO;
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();
		
		return cursor.getCount();
	}
	
	public int updateLocationInfo(LocationInfo locationInfo)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_ADDRESS, locationInfo.getAddressInString());
		values.put(KEY_DESCRIPTION, locationInfo.getDescription());
		values.put(KEY_TITLE, locationInfo.getTitle());
		values.put(KEY_ROUTE, locationInfo.getRoute());
		values.put(KEY_TIME, locationInfo.getTime());
		values.put(KEY_ROUTE_ID, locationInfo.getRoute_id());
		
		int i = db.update(TABLE_LOC_INFO, values, KEY_ID + " =?", 
				new String[] {String.valueOf(locationInfo.get_id())});
		
		db.close();
		return i;
	}
	
	public void deleteLocationInfo(LocationInfo locationInfo)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_LOC_INFO, KEY_ID + " =?", 
				new String[] {String.valueOf(locationInfo.get_id())});
		db.close();
	}
}
