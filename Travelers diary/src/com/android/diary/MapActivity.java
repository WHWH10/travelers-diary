package com.android.diary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import BaseClasses.BaseActivity;
import Helpers.ILocationListener;
import Helpers.LocationHelper;
import Helpers.MessageHelper;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.RelativeLayout;

import com.android.diary.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends BaseActivity {

//	private static final String LOG_TAG = "MAP ACTIVITY";
	public static final String ROUTE_ID = "route_id";
	private static final int MAP_MARKER_CURRENT_LOCATION = R.drawable.map_marker_current_location;
	private static final int MAP_MARKER_ROUTE_EMPTY = R.drawable.map_marker_grey_small;
	private static final int MAP_MARKER_ROUTE_FILLED = R.drawable.map_marker_red_small;
	
	private List<RouteItem> routeItems;
	private List<LatLng> trackPoints;
	private GoogleMap mapView;
	private HashMap<String, Integer> markerId;
	private LatLng locMarked;
	private int routeId;
	private LocationHelper locationHelper;
	private boolean isAddNewLocationClicked;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);                
        setContentView(R.layout.activity_map);
        
        this.isAddNewLocationClicked = false;
        
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
				if(isAddNewLocationClicked){
					locMarked = loc;
					isAddNewLocationClicked = false;
					
					DatabaseHandler db = new DatabaseHandler(getApplicationContext());
					db.addLocationInfo(routeId, 0, locMarked.latitude, locMarked.longitude);				
					db.close();
					
					manageDrawingMarkers();
				}				
			}
		});
        
        RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.activity_map_layout);
        mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {			
			public void onGlobalLayout() {
				zoomMap();
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
		
    	if(getIntent().getExtras() != null)
		{
			routeId = getIntent().getExtras().getInt(MapActivity.ROUTE_ID, 0);			
		}
    	
    	if(routeId > 0)
		{
			loadRouteItems();
			loadTrackPoints();
			
			drawLocations();
			drawTrack();
		}
    	else {
			handleStartingPosition();
		}
    }
	
	private void loadRouteItems(){
		if(routeItems != null)
			routeItems.clear();
		if(markerId != null)
			markerId.clear();
		
		DatabaseHandler db = new DatabaseHandler(this);
		routeItems = db.getRouteItems(routeId);
		db.close();
		
		markerId = new HashMap<String, Integer>(routeItems.size());
	}
	
	private void loadTrackPoints(){
		if(trackPoints != null)
			trackPoints.clear();
		else {
			trackPoints = new ArrayList<LatLng>();
		}
		
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		List<TrackPoint> points = db.getTrackPoints(routeId);
		db.close();

		for (TrackPoint trackPoint : points) {
			trackPoints.add(new LatLng(trackPoint.getLatitude(), trackPoint.getLongitude()));
		}
	}
    	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	private void drawLocations()
	{
		if(routeItems != null){
			for(int i = 0; i < routeItems.size(); i++)
			{
				drawMarker(routeItems.get(i));
			}
		}
	}
	
	private void drawTrack(){		
		if(trackPoints != null && trackPoints.size() > 0){
			PolylineOptions polylineOptions = new PolylineOptions();			
			polylineOptions.width(Config.MAP_TRACK_LINE_WIDTH);
			polylineOptions.color(Color.argb(Config.MAP_TRACK_COLOR_ALPHA, Config.MAP_TRACK_COLOR_RED, Config.MAP_TRACK_COLOR_GREEN, Config.MAP_TRACK_COLOR_BLUE));
						
			polylineOptions.addAll(trackPoints);
			mapView.addPolyline(polylineOptions);		
		}
	}
	
	private void drawMarker(RouteItem item)
	{		
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(new LatLng(item.getLatitude(),item.getLongitude()));
		
		if(item.getTitle() == null || item.getTitle().isEmpty()){
			markerOptions.title(getString(R.string.no_title));
			markerOptions.icon(BitmapDescriptorFactory.fromResource(MAP_MARKER_ROUTE_EMPTY));
		}
		else {
			markerOptions.title(item.getTitle());
			markerOptions.icon(BitmapDescriptorFactory.fromResource(MAP_MARKER_ROUTE_FILLED));
		}
		
		if(item.getDescription() != null && !item.getTitle().isEmpty())
		{
			markerOptions.snippet(item.getDescription());
		}
		
		if(markerId == null)
			markerId = new HashMap<String, Integer>(routeItems.size());
		
		this.markerId.put(this.mapView.addMarker(markerOptions).getId(), item.getRouteItemId());		
	}
		
	public void btnMyLocationClicked(View view){
		if(locationHelper == null){
			locationHelper = new LocationHelper(getApplicationContext());
			locationHelper.setOnLocationFoundListener(new ILocationListener() {
				
				public void locationProviderUnavailable() {
					ToastMessage(getText(R.string.warn_locProvider));
				}
				
				public void locationFound(Location location) {
					drawMarker(new LatLng(location.getLatitude(), location.getLongitude()), MAP_MARKER_CURRENT_LOCATION);
				}
			});
		}			
		
		MessageHelper.ToastMessage(getApplicationContext(), getString(R.string.map_my_location));
		locationHelper.getPoint();
	}
	
	public void btnAddLocationClicked(View view){
		this.isAddNewLocationClicked = true;
		MessageHelper.ToastMessage(getApplicationContext(), getString(R.string.map_add_location_msg));
	}
	
	private void drawMarker(LatLng loc, int markerResource)
	{
		this.mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, Config.MAP_MARKER_ZOOM));

		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(loc);
		
		markerOptions.icon(BitmapDescriptorFactory.fromResource(markerResource));
		markerOptions.title(getString(R.string.no_title));
		
		this.mapView.addMarker(markerOptions);
	}
	
	private boolean zoomMap(){
		boolean zoomCalculated = false;		
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		
		if(routeItems != null && routeItems.size() > 0){
			zoomCalculated = true;
			for (RouteItem routeItem : routeItems) {
				builder.include(new LatLng(routeItem.getLatitude(), routeItem.getLongitude()));
			}
		}
		
		if(trackPoints != null && trackPoints.size() > 0){
			zoomCalculated = true;
			for (LatLng trackPoint : trackPoints) {
				builder.include(trackPoint);
			}
		}
		
		if(zoomCalculated){
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(adjustBoundsForMaxZoomLevel(builder.build()),Config.MAP_ZOOM_PADDING);
			mapView.animateCamera(cameraUpdate);
		}
		
		return zoomCalculated;
	}
	
	private LatLngBounds adjustBoundsForMaxZoomLevel(LatLngBounds bounds) {
		LatLng sw = bounds.southwest;
		LatLng ne = bounds.northeast;
		double deltaLat = Math.abs(sw.latitude - ne.latitude);
		double deltaLon = Math.abs(sw.longitude - ne.longitude);
		
		final double zoomN = 0.005; // minimum zoom coefficient
		if (deltaLat < zoomN) {
			sw = new LatLng(sw.latitude - (zoomN - deltaLat / 2), sw.longitude);
		    ne = new LatLng(ne.latitude + (zoomN - deltaLat / 2), ne.longitude);
		    bounds = new LatLngBounds(sw, ne);
		}
		else if (deltaLon < zoomN) {
			sw = new LatLng(sw.latitude, sw.longitude - (zoomN - deltaLon / 2));
		    ne = new LatLng(ne.latitude, ne.longitude + (zoomN - deltaLon / 2));
		    bounds = new LatLngBounds(sw, ne);
		}
	
		return bounds;
	}
}
