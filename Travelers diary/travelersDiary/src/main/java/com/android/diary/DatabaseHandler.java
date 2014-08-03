package com.android.diary;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Helpers.MessageHelper;
import Helpers.SharedPreferenceHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

public class DatabaseHandler extends SQLiteOpenHelper {
	
	private Context context;
	
	private static final String LOG_TAG = "DATABASE HANDLER";
	
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "Routes.db";
	private static final String TABLE_ROUTE = "Route";
	private static final String TABLE_ROUTE_ITEM = "RouteItem";
	private static final String TABLE_LOG = "Log";
	private static final String TABLE_IMAGE = "Image";
	private static final String TABLE_TRACK_POINT = "TrackPoint";
	
	public static final String KEY_ID = "id";
	public static final String KEY_ROUTE_ID = "routeId";
	public static final String KEY_ROUTE_ITEM_ID = "routeItemId";
	
	public static final String KEY_TITLE = "title";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_DATE_CREATED = "dateCreated";
	public static final String KEY_DATE_MODIFIED = "dateModified";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_LOG_MESSAGE = "logMessage";
	public static final String KEY_USER = "user";
	public static final String KEY_OS = "OS";
	public static final String KEY_DEVICE = "device";
	public static final String KEY_MODEL = "model";
	public static final String KEY_PRODUCT = "product";
	public static final String KEY_TAG = "tag";
	public static final String KEY_IS_IMPORTED = "isImported";
	public static final String KEY_IMAGE_NAME = "imageName";
	public static final String KEY_IS_ADDRESS_UPDATED = "isAddressUpdated";
	public static final String KEY_IS_DEFAULT_ROUTE_IMAGE = "isDefaultRouteImage";
	public static final String KEY_IS_DEFAULT_ROUTE_ITEM_IMAGE = "isDefaultRouteItemImage";
	public static final String KEY_ROUTE_ITEM_OBJ = "routeItemObj";

	private static final String CREATE_ROUTE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ROUTE + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL  UNIQUE, " +
			KEY_TITLE + " TEXT, " + KEY_DESCRIPTION + " TEXT, " + KEY_DATE_CREATED + " DATETIME NOT NULL, " + 
			KEY_DATE_MODIFIED + " DATETIME NOT NULL, " + KEY_IS_IMPORTED + " INTEGER" + ")";
	
	private static final String CREATE_ROUTE_ITEM_TABLE = "CREATE  TABLE IF NOT EXISTS " + TABLE_ROUTE_ITEM + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL  UNIQUE, " +
			KEY_ROUTE_ID + " INTEGER NOT NULL, " + KEY_ROUTE_ITEM_OBJ + " TEXT, " + KEY_DATE_CREATED + " DATETIME NOT NULL, " + KEY_DATE_MODIFIED + " DATETIME NOT NULL, " + KEY_IS_IMPORTED + " INTEGER, " +
			KEY_IS_ADDRESS_UPDATED + " INTEGER, " + "CONSTRAINT fk_routeItem FOREIGN KEY(" + KEY_ROUTE_ID + ") REFERENCES " + TABLE_ROUTE + "(" + KEY_ID + ") " + "ON DELETE CASCADE ON UPDATE CASCADE" + ")";
	
	private static final String CREATE_LOG_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_LOG + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " + KEY_DATE_CREATED + 
			" DATETIME NOT NULL, " + KEY_LOG_MESSAGE + " TEXT, " + KEY_USER + " TEXT, " + KEY_OS + " TEXT, " + KEY_DEVICE + " TEXT, " + KEY_MODEL + " TEXT, " + KEY_PRODUCT + " TEXT, " +  KEY_TAG + " TEXT)";
	
	private static final String CREATE_IMAGE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_IMAGE + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " + KEY_DATE_CREATED + " DATETIME NOT NULL, "
			+ KEY_IMAGE_NAME + " TEXT, " + KEY_IS_IMPORTED + " INTEGER, " + KEY_ROUTE_ID + " INTEGER, " + KEY_ROUTE_ITEM_ID + " INTEGER, " + KEY_IS_DEFAULT_ROUTE_IMAGE + " INTEGER, " 
			+ KEY_IS_DEFAULT_ROUTE_ITEM_IMAGE + " INTEGER" + ")";
	
	private static final String CREATE_TRACK_POINT_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_TRACK_POINT + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL  UNIQUE, " +
			KEY_LATITUDE + " DOUBLE, " + KEY_LONGITUDE + " DOUBLE, " + KEY_DATE_CREATED + " DATETIME NOT NULL, " + KEY_ROUTE_ID + " INTEGER, " + KEY_IS_IMPORTED
			+ " INTEGER, CONSTRAINT fk_trackPoint FOREIGN KEY(" + KEY_ROUTE_ID + ") REFERENCES " + TABLE_ROUTE + "(" + KEY_ID + ") ON DELETE CASCADE ON UPDATE CASCADE" + ")";
	
	public DatabaseHandler(Context context)	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if(!db.isReadOnly())
			db.execSQL("PRAGMA foreign_keys=ON;");
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		try {
			db.execSQL(CREATE_ROUTE_TABLE);
			db.execSQL(CREATE_ROUTE_ITEM_TABLE);	
			db.execSQL(CREATE_LOG_TABLE);
			db.execSQL(CREATE_IMAGE_TABLE);
			db.execSQL(CREATE_TRACK_POINT_TABLE);
		} catch (Exception e) {
			MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
		}		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		try {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTE);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTE_ITEM);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOG);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGE);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACK_POINT);
			
			onCreate(db);
		} catch (Exception e) {
			MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
		}		
	}

    //region Routes handling

    public List<Route> getRoutes() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ROUTE, new String[]{KEY_ID, KEY_TITLE, KEY_DESCRIPTION,
                KEY_DATE_CREATED, KEY_DATE_MODIFIED, KEY_IS_IMPORTED}, null, null, null, null, KEY_ID + " DESC", null);

        List<Route> list = new ArrayList<Route>();

        if (cursor.moveToFirst()) {
            do {
                Route route = Route.parse(cursor);
                if (route == null)
                    continue;
                list.add(route);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return list;
    }

    public Route getRoute(int routeId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ROUTE, new String[]{KEY_ID, KEY_TITLE, KEY_DESCRIPTION,
                KEY_DATE_CREATED, KEY_DATE_MODIFIED, KEY_IS_IMPORTED}, KEY_ID + "=?", new String[]{String.valueOf(routeId)}, null, null, null, null);

        Route route = null;

        if (cursor.moveToFirst()) {
            route = Route.parse(cursor);
        }

        cursor.close();
        db.close();

        return route;
    }

    public void insertRoute(ContentValues contentValues) {
        if (contentValues == null)
            return;

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            contentValues.put(KEY_DATE_CREATED, DateFormat.getDateTimeInstance().format(new Date()));
            contentValues.put(KEY_DATE_MODIFIED, DateFormat.getDateTimeInstance().format(new Date()));
            contentValues.put(KEY_IS_IMPORTED, 0);
            db.insertOrThrow(TABLE_ROUTE, null, contentValues);

        } catch (SQLException e) {
            MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
        } finally {
            db.close();
        }
    }

    public void updateRoute(ContentValues contentValues, int routeId) {
        if (contentValues == null)
            return;

        contentValues.put(KEY_DATE_MODIFIED, DateFormat.getDateTimeInstance().format(new Date()));
        contentValues.put(KEY_IS_IMPORTED, 0);

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.update(TABLE_ROUTE, contentValues, KEY_ID + "=?", new String[]{String.valueOf(routeId)});

        } catch (SQLException e) {
            MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
        } finally {
            db.close();
        }
    }

    public void setRouteImported(int routeId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_IS_IMPORTED, 1);

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.update(TABLE_ROUTE, contentValues, KEY_ID + "=?", new String[]{String.valueOf(routeId)});

        } catch (SQLException e) {
            MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
        } finally {
            db.close();
        }
    }

    public void deleteRoute(int routeId) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_ROUTE, KEY_ID + "=?", new String[]{String.valueOf(routeId)});
        db.delete(TABLE_IMAGE, KEY_ROUTE_ID + "=?", new String[]{String.valueOf(routeId)});

        db.close();
    }

    //endregion

    //region RouteItem handling

    public void insertRouteItem(RouteItem routeItem) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_ROUTE_ITEM_OBJ, RouteItem.toGson(routeItem));
            contentValues.put(KEY_ROUTE_ID, routeItem.getRouteId());
            contentValues.put(KEY_DATE_CREATED, DateFormat.getDateTimeInstance().format(new Date()));
            contentValues.put(KEY_DATE_MODIFIED, DateFormat.getDateTimeInstance().format(new Date()));
            contentValues.put(KEY_IS_IMPORTED, 0);
            contentValues.put(KEY_IS_ADDRESS_UPDATED, 0);
            db.insertOrThrow(TABLE_ROUTE_ITEM, null, contentValues);

            SharedPreferenceHelper helper = new SharedPreferenceHelper(context);
            helper.setAddressUpdateFlag(true);

        } catch (SQLException e) {
            MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
        } finally {
            db.close();
        }
    }

    public List<RouteItem> getRouteItems(int routeId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ROUTE_ITEM, new String[]{KEY_ID, KEY_ROUTE_ID, KEY_ROUTE_ITEM_OBJ, KEY_DATE_CREATED,
                KEY_DATE_MODIFIED, KEY_IS_IMPORTED, KEY_IS_ADDRESS_UPDATED}, KEY_ROUTE_ID + "=?", new String[]{String.valueOf(routeId)}, null, null, null, null);

        List<RouteItem> list = new ArrayList<RouteItem>();

        if (cursor.moveToFirst()) {
            do {
                RouteItem route = RouteItem.parse(cursor);
                if (route == null)
                    continue;
                list.add(route);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return list;
    }

    public List<RouteItem> getRouteItems_withoutAddresses() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ROUTE_ITEM, new String[]{KEY_ID, KEY_ROUTE_ID, KEY_ROUTE_ITEM_OBJ, KEY_DATE_CREATED, KEY_DATE_MODIFIED,
                KEY_IS_IMPORTED, KEY_IS_ADDRESS_UPDATED}, KEY_IS_ADDRESS_UPDATED + "=?", new String[]{String.valueOf(0)}, null, null, null, null);

        List<RouteItem> list = new ArrayList<RouteItem>();

        if (cursor.moveToFirst()) {
            do {
                RouteItem route = RouteItem.parse(cursor);
                if (route == null)
                    continue;
                list.add(route);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return list;
    }

    public RouteItem getRouteItem(int routeItemId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ROUTE_ITEM, new String[]{KEY_ID, KEY_ROUTE_ID, KEY_ROUTE_ITEM_OBJ, KEY_DATE_CREATED, KEY_DATE_MODIFIED,
                KEY_IS_IMPORTED, KEY_IS_ADDRESS_UPDATED}, KEY_ID + "=?", new String[]{String.valueOf(routeItemId)}, null, null, null, null);

        RouteItem routeItem = null;

        if (cursor.moveToFirst()) {
            routeItem = RouteItem.parse(cursor);
        }

        cursor.close();
        db.close();

        return routeItem;
    }

    public void updateRouteItem(RouteItem routeItem, boolean isAddressUpdated, boolean addAddressUpdateFlag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ROUTE_ITEM_OBJ, RouteItem.toGson(routeItem));
        contentValues.put(KEY_ROUTE_ID, routeItem.getRouteId());
        contentValues.put(KEY_DATE_CREATED, DateFormat.getDateTimeInstance().format(routeItem.getDateCreated()));
        contentValues.put(KEY_DATE_MODIFIED, DateFormat.getDateTimeInstance().format(new Date()));
        contentValues.put(KEY_IS_IMPORTED, 0);

        if(addAddressUpdateFlag)
            contentValues.put(KEY_IS_ADDRESS_UPDATED, isAddressUpdated ? 1 : 0);

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.update(TABLE_ROUTE_ITEM, contentValues, KEY_ID + "=?", new String[]{String.valueOf(routeItem.getRouteItemId())});

        } catch (SQLException e) {
            MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
        } finally {
            db.close();
        }
    }

    public void setRouteItemImported(int routeItemId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_IS_IMPORTED, 1);

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.update(TABLE_ROUTE_ITEM, contentValues, KEY_ID + "=?", new String[]{String.valueOf(routeItemId)});

        } catch (SQLException e) {
            MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
        } finally {
            db.close();
        }
    }

    public void deleteRouteItem(int routeItemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ROUTE_ITEM, KEY_ID + "=?", new String[]{String.valueOf(routeItemId)});
        db.close();
    }

    //endregion

    //region Log handling

    public void insertLog(String message, String user, String tag) {
        if (message == null || message.isEmpty() || user == null)
            return;

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_LOG_MESSAGE, message);
            contentValues.put(KEY_USER, user);
            contentValues.put(KEY_DATE_CREATED, DateFormat.getDateTimeInstance().format(new Date()));
            contentValues.put(KEY_OS, Build.VERSION.SDK_INT);
            contentValues.put(KEY_MODEL, Build.MODEL);
            contentValues.put(KEY_PRODUCT, Build.PRODUCT);
            contentValues.put(KEY_DEVICE, Build.DEVICE);
            contentValues.put(KEY_TAG, tag);

            db.insertOrThrow(TABLE_LOG, null, contentValues);

        } catch (SQLException e) {
            MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
        } finally {
            db.close();
        }
    }

    public void deleteLog(int logId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_LOG, KEY_ID + "=?", new String[]{String.valueOf(logId)});
        } catch (SQLException e) {
            MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
        } finally {
            db.close();
        }
    }

    public List<CustomLog> getLog() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_LOG, new String[]{KEY_ID, KEY_OS, KEY_MODEL, KEY_DEVICE,
                KEY_PRODUCT, KEY_LOG_MESSAGE, KEY_TAG, KEY_USER, KEY_DATE_CREATED}, null, null, null, null, null);

        List<CustomLog> list = new ArrayList<CustomLog>();

        if (cursor.moveToFirst()) {
            do {
                CustomLog customLog = CustomLog.parse(cursor);
                if (customLog == null)
                    continue;
                list.add(customLog);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return list;
    }

    //endregion

    //region Image handling

    public void insertImage(int routeId, int routeItemId, String imagePath) {
        if (checkIfImageExists(routeId, routeItemId, imagePath))
            return;

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues contentValues = new ContentValues();

            contentValues.put(KEY_DATE_CREATED, DateFormat.getDateTimeInstance().format(new Date()));
            contentValues.put(KEY_IS_IMPORTED, 0);
            contentValues.put(KEY_ROUTE_ID, routeId);
            contentValues.put(KEY_ROUTE_ITEM_ID, routeItemId);
            contentValues.put(KEY_IMAGE_NAME, imagePath);
            contentValues.put(KEY_IS_DEFAULT_ROUTE_IMAGE, 0);
            contentValues.put(KEY_IS_DEFAULT_ROUTE_ITEM_IMAGE, 0);

            db.insertOrThrow(TABLE_IMAGE, null, contentValues);

        } catch (SQLException e) {
            MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
        } finally {
            db.close();
        }
    }

    private boolean checkIfImageExists(int routeId, int routeItemId, String imagePath) {
        String query = "SELECT " + KEY_ID + " FROM " + TABLE_IMAGE + " WHERE " + KEY_ROUTE_ID + "=" + routeId + " AND " + KEY_ROUTE_ITEM_ID + "=" + routeItemId + " AND " + KEY_IMAGE_NAME + "='" + imagePath + "'";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();

            return true;
        }

        cursor.close();
        db.close();

        return false;
    }

    public void insertDefaultImage(int routeId, int routeItemId, String imagePath, boolean isDefaultRouteImage) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues contentValues = new ContentValues();

            contentValues.put(KEY_IS_IMPORTED, 0);

            if (isDefaultRouteImage) {
                contentValues.put(KEY_IS_DEFAULT_ROUTE_IMAGE, 0);
                db.update(TABLE_IMAGE, contentValues, KEY_ROUTE_ID + "=? AND " + KEY_IS_DEFAULT_ROUTE_IMAGE + " =?", new String[]{String.valueOf(routeId), "1"});
            } else {
                contentValues.put(KEY_IS_DEFAULT_ROUTE_ITEM_IMAGE, 0);
                db.update(TABLE_IMAGE, contentValues, KEY_ROUTE_ITEM_ID + "=? AND " + KEY_IS_DEFAULT_ROUTE_ITEM_IMAGE + " =?", new String[]{String.valueOf(routeItemId), "1"});
            }

            contentValues.clear();

            contentValues.put(KEY_DATE_CREATED, DateFormat.getDateTimeInstance().format(new Date()));
            contentValues.put(KEY_IS_IMPORTED, 0);
            contentValues.put(KEY_ROUTE_ID, routeId);
            contentValues.put(KEY_ROUTE_ITEM_ID, routeItemId);
            contentValues.put(KEY_IMAGE_NAME, imagePath);

            if (isDefaultRouteImage)
                contentValues.put(KEY_IS_DEFAULT_ROUTE_IMAGE, 1);
            else {
                contentValues.put(KEY_IS_DEFAULT_ROUTE_ITEM_IMAGE, 1);
            }

            db.insertOrThrow(TABLE_IMAGE, null, contentValues);

        } catch (SQLException e) {
            MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
        } finally {
            db.close();
        }
    }

    public String getDefaultRouteImage(int routeId) {
        String countQuery = "SELECT * FROM " + TABLE_IMAGE + " WHERE " + KEY_ROUTE_ID + "=" + routeId + " AND " + KEY_IS_DEFAULT_ROUTE_IMAGE + "=1";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);
        String image = "";
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            image = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_NAME));
        }

        cursor.close();
        db.close();

        return image;
    }

    public String getDefaultRouteItemImage(int routeItemId) {
        String countQuery = "SELECT * FROM " + TABLE_IMAGE + " WHERE " + KEY_ROUTE_ITEM_ID + "=" + routeItemId + " AND " + KEY_IS_DEFAULT_ROUTE_ITEM_IMAGE + "=1";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);
        String image = "";
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            image = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_NAME));
        }

        cursor.close();
        db.close();

        return image;
    }

    public List<String> getImagesPath(int routeId, int routeItemId) {
        List<String> imagesList = new ArrayList<String>();

        String query = "SELECT DISTINCT " + KEY_IMAGE_NAME + " FROM " + TABLE_IMAGE;
        if (routeId > 0 && routeItemId == 0)
            query += " WHERE " + KEY_ROUTE_ID + " = " + routeId;
        else if (routeId > 0 && routeItemId > 0)
            query += " WHERE " + KEY_ROUTE_ID + " = " + routeId + " AND " + KEY_ROUTE_ITEM_ID + " = " + routeItemId;
        else if (routeId == 0 && routeItemId > 0)
            query += " WHERE " + KEY_ROUTE_ITEM_ID + " = " + routeItemId;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                imagesList.add(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_IMAGE_NAME)));
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return imagesList;
    }

    public void deleteImage(String imageName, int routeId, int routeItemId) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (routeId > 0 && routeItemId > 0)
            db.delete(TABLE_IMAGE, KEY_IMAGE_NAME + "=? AND " + KEY_ROUTE_ID + "=? AND " + KEY_ROUTE_ITEM_ID + "=?", new String[]{imageName, String.valueOf(routeId), String.valueOf(routeItemId)});
        else if (routeId > 0 && routeItemId == 0)
            db.delete(TABLE_IMAGE, KEY_IMAGE_NAME + "=? AND " + KEY_ROUTE_ID + "=?", new String[]{imageName, String.valueOf(routeId)});
        else if (routeId == 0 && routeItemId > 0)
            db.delete(TABLE_IMAGE, KEY_IMAGE_NAME + "=? AND " + KEY_ROUTE_ITEM_ID + "=?", new String[]{imageName, String.valueOf(routeItemId)});
        else if (routeId == 0 && routeItemId == 0)
            db.delete(TABLE_IMAGE, KEY_IMAGE_NAME + "=?", new String[]{imageName});


        db.close();
    }

    //endregion

    //region TrackPoint handling

    public void addTrackPoint(int routeId, double latitude, double longitude)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_ROUTE_ID, routeId);
		values.put(KEY_LATITUDE, latitude);
		values.put(KEY_LONGITUDE, longitude);
		values.put(KEY_DATE_CREATED, DateFormat.getDateTimeInstance().format(new Date()));
		values.put(KEY_IS_IMPORTED, 0);
		
		SQLiteDatabase db = this.getWritableDatabase();		
		
		db.insertOrThrow(TABLE_TRACK_POINT, null, values);
		db.close();
	}

    public List<TrackPoint> getTrackPoints(int routeId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TRACK_POINT, new String[]{KEY_ID, KEY_ROUTE_ID, KEY_DATE_CREATED,
                KEY_LATITUDE, KEY_LONGITUDE, KEY_IS_IMPORTED}, KEY_ROUTE_ID + "=?", new String[]{String.valueOf(routeId)}, null, null, null, null);

        List<TrackPoint> list = new ArrayList<TrackPoint>();

        if (cursor == null)
            return null;

        if (cursor.moveToFirst()) {
            do {
                TrackPoint point = TrackPoint.parse(cursor);
                if (point == null)
                    continue;
                list.add(point);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    //endregion

}
