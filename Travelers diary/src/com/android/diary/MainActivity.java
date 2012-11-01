package com.android.diary;

import java.util.Date;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.android.diary.R;
import com.google.android.maps.*;

public class MainActivity extends MapActivity {

	public static final String COORDINATES_FILE = "Coordinates.txt"; // File of coordinates
	public static final String APP_PREFS = "app_prefs";
	public static final String MARKER_ID = "marker_id";
	public static final String MARKER_TITLE = "marker_title";
	public static final String MARKER_DESCRIPTION = "marker_description";
	public static final String MARKER_LAT = "marker_lat";
	public static final String MARKER_LON = "marker_lon";
	public static final String MARKER_DATE = "marker_date";
	public static final String MARKER_ADDRESS = "marker_address";
	public static final String ROUTE = "route";
	public static final String ROUTE_ID = "route_id";
	public static final String LAST_ROUTE = "last_route";
	public static final int ROUTES_CTRL = -1;
	public static final int ROUTES_REQ_CODE = 1;

	private MapView mapView;
	private Intent myService;
	private List<LocationInfo> locationList;
	private List<Overlay> mapOverlays;
	private MapOverlay overlay;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(16);
        mapView.setSatellite(false);

        mapOverlays = mapView.getOverlays();
        overlay = new MapOverlay(getResources().getDrawable(R.drawable.mark_empty), this);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location location2 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        GeoPoint point;
        
        if(location == null)
        {
        	if(location2 == null)
        	{
        		mapOverlays.add(overlay);
        	}
        	else
        	{
        		point = getGeoPoint(location2.getLatitude(), location2.getLongitude());
        		mapOverlays.add(overlay);
        	    mapView.getController().animateTo(point);
        	}
        }
        else
        {
        	if(location2 == null)
        	{
        		point = getGeoPoint(location.getLatitude(), location.getLongitude());
        		mapOverlays.add(overlay);
                mapView.getController().animateTo(point);
        	}
        	else
        	{
        		if(location.getTime() > location2.getTime())
                	point = getGeoPoint(location.getLatitude(), location.getLongitude());
                else
                	point = getGeoPoint(location2.getLatitude(), location2.getLongitude());
                mapOverlays.add(overlay);
                mapView.getController().animateTo(point);
        	}
        }        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(isMyServiceRunning())
        {
        	menu.getItem(0).setVisible(false);
        	menu.getItem(1).setVisible(true);
        	menu.getItem(2).setEnabled(false);
        }
        else
        {
        	menu.getItem(0).setVisible(true);
        	menu.getItem(1).setVisible(false);        	
        	menu.getItem(2).setEnabled(true);
        }
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_startTracking:			
			myService = new Intent(this, LocationProviderService.class);		
			startService(myService);
			break;
			
		case R.id.menu_stopTraking:			
			if(isMyServiceRunning() && myService != null)
			{
				stopService(myService);
			}
			else
			{
				myService = new Intent(this, LocationProviderService.class);
				startService(myService);
				stopService(myService);			
			}			
			break;
			
		case R.id.menu_drawLocations:
//			getAllLocations();
			this.getLatestRoute();
			this.drawLocations();			
			break;
			
		case R.id.menu_routes:
			this.routesPressed();
			break;
			
		case R.id.menu_settings:
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
//	private void getAllLocations()
//	{
//		DatabaseHandler db = new DatabaseHandler(this);
//		if(locationList != null)
//			locationList.clear();
//		locationList = db.getAllLocationsInfos();
//		db.close();
//	}
	
	private void routesPressed()
	{
		Intent intent = new Intent(this, RoutesActivity.class);
		startActivityForResult(intent, ROUTES_REQ_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ROUTES_REQ_CODE:
			showRouteOnMap(resultCode);
			break;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onRestart() {
		SharedPreferences settings = getSharedPreferences(MainActivity.APP_PREFS, 0);
		int route = settings.getInt(LAST_ROUTE, -1);
		if(route == -1)
		{
			super.onRestart();
			return;
		}
		loadLastPoints(route);
		super.onRestart();
	}

	private void loadLastPoints(int route_id)
	{
		mapOverlays.add(overlay);
		showRouteOnMap(route_id);		
	}
	
	private void saveLastRoute(int route_id)
	{
		SharedPreferences settings = getSharedPreferences(MainActivity.APP_PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();		
		editor.putInt(LAST_ROUTE, route_id);
		editor.commit();
	}
	
	@Override
	protected void onStop() {
		 if(!mapOverlays.isEmpty())
	     {
	         mapOverlays.clear();
			 overlay.clear();
	         mapView.getOverlays().clear();
	         mapView.invalidate();
	         if(locationList == null || locationList.isEmpty())
	         {
	        	 super.onStop();
	        	 return;
	         }
	         saveLastRoute(locationList.get(0).getRoute_id());
	     }
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		saveLastRoute(ROUTES_CTRL);
		super.onDestroy();
	}

	private void showRouteOnMap(int routeID)
	{
		DatabaseHandler db = new DatabaseHandler(this);
		locationList = db.getRoute(routeID);
		db.close();
		mapOverlays.add(overlay);
		drawLocations();
	}
	
	private void getLatestRoute()
	{
		SharedPreferences settings = getSharedPreferences(MainActivity.APP_PREFS, 0);
		int route = settings.getInt(ROUTE_ID, 0);
		DatabaseHandler db = new DatabaseHandler(this);
		if(locationList != null)
			locationList.clear();
		locationList = db.getRoute(route);
		db.close();
	}
	
	private void drawLocations()
	{
		overlay.clear();
		for(int i = 0; i < locationList.size(); i++)
		{
			drawMarker(locationList.get(i));
		}		
	}
	
	private void drawMarker(LocationInfo loc)
	{
		GeoPoint point = getGeoPoint(loc.getAddress().getLatitude(), loc.getAddress().getLongitude());
		OverlayItem overlayItem = new OverlayItem(point, "", "");
        overlay.addOverlayItem(overlayItem);
        mapView.getController().animateTo(point);
	}
	
	private GeoPoint getGeoPoint(double latitude, double longitude)
	{
		Double lat = latitude*1E6;
		Double lon = longitude*1E6;
		return new GeoPoint(lat.intValue(), lon.intValue());
	}
	
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	
	        if ("com.android.diary.LocationProviderService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}   
	
	public void onEditPressed(int index)
	{
		Intent intent = new Intent(this, EditActivity.class);
		intent.putExtra(MARKER_ID, locationList.get(index).get_id());
		startActivity(intent);
	}
	
	public void onDetailsPressed(int index)
	{
		Intent intent = new Intent(this, DetailsActivity.class);
		
		System.out.println(index + " " + locationList.size());
		
		intent.putExtra(MARKER_TITLE, locationList.get(index).getTitle());
		intent.putExtra(MARKER_DESCRIPTION, locationList.get(index).getDescription());
		intent.putExtra(MARKER_ADDRESS, locationList.get(index).getAddressInString());
		intent.putExtra(MARKER_LAT,  locationList.get(index).getAddress().getLatitude());
		intent.putExtra(MARKER_LON, locationList.get(index).getAddress().getLongitude());
		Date dat = new Date();
		dat.setTime(locationList.get(index).getTime());
		intent.putExtra(MARKER_DATE, dat.toString());
		intent.putExtra(ROUTE, locationList.get(index).getRoute());
		intent.putExtra(ROUTE_ID, locationList.get(index).getRoute_id());
		
		startActivity(intent);
	}
}
