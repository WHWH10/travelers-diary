package com.android.diary;

import java.util.List;

import android.app.Activity;
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
import android.view.Window;

import com.android.diary.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity {

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

	private static final int DEFAULT_ZOOM = 14;
	
	private Intent myService;
	private List<RouteItem> routeItems;
	private GoogleMap mapView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
                
        setContentView(R.layout.activity_map);       
        
        mapView = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location location2 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        if(location == null)
        {
        	if(location2 == null)
        	{
        	}
        	else
        	{
        		mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location2.getLatitude(), location2.getLongitude()), DEFAULT_ZOOM));
        	}
        }
        else
        {
        	if(location2 == null)
        	{
        		mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
        	}
        	else
        	{
        		if(location.getTime() > location2.getTime())
        			mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
                else
                	mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location2.getLatitude(), location2.getLongitude()), DEFAULT_ZOOM));                
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
//		if(isMyServiceRunning())
//        {
//        	menu.getItem(0).setVisible(false);
//        	menu.getItem(1).setVisible(true);
//        	menu.getItem(2).setEnabled(false);
//        }
//        else
//        {
//        	menu.getItem(0).setVisible(true);
//        	menu.getItem(1).setVisible(false);        	
//        	menu.getItem(2).setEnabled(true);
//        }
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_startTracking:			
			myService = new Intent(this, LocationProviderService.class);		
			startService(myService);
			break;
			
		case R.id.menu_stopTracking:			
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
	protected void onRestart() {
//		SharedPreferences settings = getSharedPreferences(MapActivity.APP_PREFS, 0);
//		int route = settings.getInt(LAST_ROUTE, -1);
//		if(route == -1)
//		{
//			super.onRestart();
//			return;
//		}
//		loadLastPoints(route);
		super.onRestart();
	}	
	
	@Override
	protected void onStop() {
//		 if(!mapOverlays.isEmpty())
//	     {
//	         mapOverlays.clear();
//			 overlay.clear();
////	         mapView.getOverlays().clear();
////	         mapView.invalidate();
//	         if(locationList == null || locationList.isEmpty())
//	         {
//	        	 super.onStop();
//	        	 return;
//	         }
//	         saveLastRoute(locationList.get(0).getRoute_id());
//	     }
		super.onStop();
	}

	@Override
	protected void onDestroy() {
//		saveLastRoute(ROUTES_CTRL);
		super.onDestroy();
	}

	private void showRouteOnMap(int routeID)
	{
		DatabaseHandler db = new DatabaseHandler(this);
		routeItems = db.getRouteItems(routeID);
		db.close();
		drawLocations();
	}
	
	private void getLatestRoute()
	{
		SharedPreferences settings = getSharedPreferences(MapActivity.APP_PREFS, 0);
		int routeId = settings.getInt(ROUTE_ID, 0);
		DatabaseHandler db = new DatabaseHandler(this);
		if(routeItems != null)
			routeItems.clear();
		routeItems = db.getRouteItems(routeId);
		db.close();
	}
	
	private void drawLocations()
	{
		if(routeItems == null)
			return;
		for(int i = 0; i < routeItems.size(); i++)
		{
			drawMarker(routeItems.get(i));
		}		
	}
	
	private void drawMarker(RouteItem item)
	{
		this.mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(item.getLatitude(), item.getLongitude()), DEFAULT_ZOOM));
		this.mapView.addMarker(new MarkerOptions().position(new LatLng(item.getLatitude(), item.getLongitude())).title("labas").snippet("snipetas"));
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
		intent.putExtra(MARKER_ID, routeItems.get(index).getRouteItemId());
		startActivity(intent);
	}
	
	public void onDetailsPressed(int index)
	{
		Intent intent = new Intent(this, DetailsActivity.class);
		
//		intent.putExtra(MARKER_TITLE, locationList.get(index).getTitle());
//		intent.putExtra(MARKER_DESCRIPTION, locationList.get(index).getDescription());
//		intent.putExtra(MARKER_ADDRESS, locationList.get(index).getAddressInString());
//		intent.putExtra(MARKER_LAT,  locationList.get(index).getAddress().getLatitude());
//		intent.putExtra(MARKER_LON, locationList.get(index).getAddress().getLongitude());
//		Date dat = new Date();
//		dat.setTime(locationList.get(index).getTime());
//		intent.putExtra(MARKER_DATE, dat.toString());
//		intent.putExtra(ROUTE, locationList.get(index).getRoute());
//		intent.putExtra(ROUTE_ID, locationList.get(index).getRoute_id());
		
		startActivity(intent);
	}
}
