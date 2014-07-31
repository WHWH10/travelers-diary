package com.android.diary;

public class Config {
	
	/*---- WebService configuration ----*/	
	public static final String WEBSERVICE_NAMESPACE = "http://travelersDiary.com/";
	public static final String WEBSERVICE_URL = "http://192.168.0.110/Services/AndroidService.asmx";
	public static final String WEBSERVICE_METHOD_LOG_IMPORT = "ImportLog";
	public static final String WEBSERVICE_METHOD_ROUTE_IMPORT = "ImportRoute";
	public static final String WEBSERVICE_METHOD_ROUTE_ITEM_IMPORT = "ImportRouteItem";
	/*----------------------------------*/
	
	/* Shared preferences configuration */	
	public static final String PREFS_FILE = "TravelersDiary";
	public static final String UPDATE_ADDRESSES = "updateAddresses";
	public static final String CAMERA_START_TIME = "cameraStartTime";
	public static final String AUTHENTICATION_DATA = "authtenticationData";
	public static final String UPLOAD_CONNECTION_TYPE = "uploadConnectionType";
	/*----------------------------------*/
	
	/*-------- Map configuration -------*/	
	public static final int DEFAULT_ZOOM = 14;
	public static final float MAP_MARKER_ZOOM = 13f;
	public static final int MAP_TRACK_COLOR_RED = 43;
	public static final int MAP_TRACK_COLOR_GREEN = 41;
	public static final int MAP_TRACK_COLOR_BLUE = 181;
	public static final int MAP_TRACK_COLOR_ALPHA = 128;
	public static final float MAP_TRACK_LINE_WIDTH = 12f;
	public static final int MAP_ZOOM_PADDING = 150;
	/*----------------------------------*/
	
	/*- Location handling configuration */
	public static final long LOCATION_UPDATE_TIME = 0;
	public static final long SERVICE_SLEEP_TIME = 1000;
	/*----------------------------------*/
	
	/*--- Image helper configuration ---*/
	public static final double IMAGE_SCALE_X = 0.3;
	public static final double IMAGE_SCALE_Y = 0.3;
	public static final double IMAGE_SCALE_Y_DIVIDER = 5;
	/*----------------------------------*/
}
