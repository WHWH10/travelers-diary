package com.android.diary;

import java.util.HashMap;
import java.util.List;

import BaseClasses.BaseActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Window;

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

//	private static final String LOG_TAG = "MAP ACTIVITY";
	public static final String ROUTE_ID = "route_id";
	
	private List<RouteItem> routeItems;
	private GoogleMap mapView;
	private HashMap<String, Integer> markerId;
	private LatLng locMarked;
	private int routeId;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);                
        setContentView(R.layout.activity_map);        
        
        isNetworkAvailableWithToast();
        
        mapView = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();
                
        mapView.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			
			public void onInfoWindowClick(Marker marker) {
				if(markerId != null && markerId.get(marker.getId()) != null && markerId.get(marker.getId()) != 0)
				{
					Intent intent = new Intent(getApplicationContext(), RouteItemDetailsActivity.class);
					intent.putExtra(RouteItemDetailFragment.ROUTE_ITEM_ID, markerId.get(marker.getId()));
					startActivity(intent);
				}
			}
		});
        
        mapView.setOnMapClickListener(new OnMapClickListener() {
			
			public void onMapClick(LatLng loc) {
				locMarked = loc;
				createConfirmDialog();
			}
		});
    }
    
    @Override
	protected void onStart() {
		manageDrawingMarkers();
		super.onStart();
	}
    
    private void handleStartingPosition()
    {
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
        		mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location2.getLatitude(), location2.getLongitude()), Config.DEFAULT_ZOOM));
        	}
        }
        else
        {
        	if(location2 == null)
        	{
        		mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), Config.DEFAULT_ZOOM));
        	}
        	else
        	{
        		if(location.getTime() > location2.getTime())
        			mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), Config.DEFAULT_ZOOM));
                else
                	mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location2.getLatitude(), location2.getLongitude()), Config.DEFAULT_ZOOM));                
        	}
        }
    }

	private void manageDrawingMarkers()
    {
		mapView.clear();
		if(routeItems != null)
			routeItems.clear();
		if(markerId != null)
			markerId.clear();
		
    	if(getIntent().getExtras() != null)
		{
			routeId = getIntent().getExtras().getInt(MapActivity.ROUTE_ID, 0);			
		}
    	
    	if(routeId > 0)
		{
			DatabaseHandler db = new DatabaseHandler(this);
			routeItems = db.getRouteItems(routeId);
			db.close();
			markerId = new HashMap<String, Integer>(routeItems.size());
			drawLocations();
		}
    	else {
			handleStartingPosition();
		}
    }
    
    private void createConfirmDialog()
    {
    	new AlertDialog.Builder(this).setTitle(getString(R.string.dialog_confirmation)).setMessage(getString(R.string.dialog_mapAddPoint))
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		
			public void onClick(DialogInterface dialog, int which) {
				DatabaseHandler db = new DatabaseHandler(getApplicationContext());
				db.addLocationInfo(routeId, 0, locMarked.latitude, locMarked.longitude);				
				db.close();
				
				manageDrawingMarkers();
			}
		}).setNegativeButton(android.R.string.no, null).show();
    }
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
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
		this.mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(item.getLatitude(), item.getLongitude()), Config.DEFAULT_ZOOM));
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
		if(markerId == null)
			markerId = new HashMap<String, Integer>(routeItems.size());
		this.markerId.put(this.mapView.addMarker(markerOptions).getId(), item.getRouteItemId());		
	}
	
//	private void drawMarker(LatLng loc)
//	{
//		this.mapView.animateCamera(CameraUpdateFactory.newLatLng(loc));
//		MarkerOptions markerOptions = new MarkerOptions();
//		markerOptions.position(loc);
//		
//		markerOptions.title(getString(R.string.no_title));
//		
//		this.mapView.addMarker(markerOptions);
//	}
}
