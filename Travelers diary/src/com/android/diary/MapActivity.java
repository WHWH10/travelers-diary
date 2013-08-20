package com.android.diary;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.android.diary.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends BaseActivity {

	private static final String LOG_TAG = "MAP ACTIVITY";
	public static final String COORDINATES_FILE = "Coordinates.txt"; // File of coordinates
	public static final String APP_PREFS = "app_prefs";
	public static final String ROUTE_ID = "route_id";
	public static final String LAST_ROUTE = "last_route";
	public static final int ROUTES_REQ_CODE = 1;

	private static final int DEFAULT_ZOOM = 14;
	
	private List<RouteItem> routeItems;
	private GoogleMap mapView;
	private HashMap<String, Integer> markerId;
	
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
        
        mapView.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			
			public void onInfoWindowClick(Marker marker) {				
				if(markerId != null && markerId.get(marker.getId()) != null && markerId.get(marker.getId()) != 0)
				{
					Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
					intent.putExtra(RouteItemDetailFragment.ROUTE_ITEM_ID, markerId.get(marker.getId()));
					startActivity(intent);
				}
			}
		});
        
        mapView.setOnMapClickListener(new OnMapClickListener() {
			
			public void onMapClick(LatLng loc) {
				ToastMessage(loc.latitude + " " + loc.longitude);
				drawMarker(loc);
			}
		});
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

	private void showRouteOnMap(int routeID)
	{
		DatabaseHandler db = new DatabaseHandler(this);
		routeItems = db.getRouteItems(routeID);
		db.close();
		this.markerId = new HashMap<String, Integer>(routeItems.size());
		drawLocations();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
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
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(new LatLng(item.getLatitude(),item.getLongitude()));
		
		if(item.getTitle() == null || item.getTitle().isEmpty())
			markerOptions.title(getString(R.string.no_title));
		else {
			markerOptions.title(item.getTitle());
		}
		if(item.getDescription() != null && !item.getTitle().isEmpty())
		{
			markerOptions.snippet(item.getDescription());
		}
		this.markerId.put(this.mapView.addMarker(markerOptions).getId(), item.getRouteItemId());		
	}
	
	private void drawMarker(LatLng loc)
	{
		this.mapView.animateCamera(CameraUpdateFactory.newLatLng(loc));
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(loc);
		
		markerOptions.title(getString(R.string.no_title));
		
		this.mapView.addMarker(markerOptions);
	}
	
	public void onEditPressed(int index)
	{
		Intent intent = new Intent(this, EditActivity.class);
//		intent.putExtra(MARKER_ID, routeItems.get(index).getRouteItemId());
		startActivity(intent);
	}
	
	public void onDetailsPressed(int index)
	{		
		if(validateIndex(index))
		{
			Intent intent = new Intent(this, DetailsActivity.class);
			intent.putExtra(ROUTE_ID, this.routeItems.get(index).getRouteId());
			startActivity(intent);
		}
		else
		{
			Toast.makeText(this, getString(R.string.error_indexOutOfBounds), Toast.LENGTH_SHORT).show();
			Log.e(LOG_TAG, getString(R.string.error_indexOutOfBounds_Log));
		}
	}
	
	private boolean validateIndex(int index)
	{
		if(this.routeItems == null || this.routeItems.size() <= index)
			return false;
		return true;
	}
}
