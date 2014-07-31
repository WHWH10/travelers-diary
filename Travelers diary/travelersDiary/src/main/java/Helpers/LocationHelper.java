package Helpers;

import java.util.ArrayList;
import java.util.List;

import com.android.diary.Config;
import com.android.diary.R;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

public class LocationHelper {
	private static final String LOG_TAG = "LOCATION HELPER";	
	private Context context;
	private LocationManager locationManager;
	private List<ILocationListener> locationListeners;
	
	public LocationHelper(Context context){
		this.context = context;
		locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		
//		locationManager.addGpsStatusListener(new Listener() {
//			
//			public void onGpsStatusChanged(int event) {
//				switch (event) {
//				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
//					GpsStatus gpsStatus = locationManager.getGpsStatus(null);
//					Log.i(LOG_TAG, "---------------------");
//					for (GpsSatellite satellite : gpsStatus.getSatellites()) {
//						Log.i(LOG_TAG, satellite.usedInFix() + "");
//					}
//					
//					break;
//				case GpsStatus.GPS_EVENT_FIRST_FIX:
//					GpsStatus gpsStatuss = locationManager.getGpsStatus(null);
//					Log.i(LOG_TAG, "---------------------");
//					for (GpsSatellite satellite : gpsStatuss.getSatellites()) {
//						Log.i(LOG_TAG, satellite.usedInFix() + "");
//					}
//					
//					break;
//
//				default:
//					break;
//				}				
//			}
//		});
		
		locationListeners = new ArrayList<ILocationListener>();
	}
	
	public void setOnLocationFoundListener(ILocationListener listener){
		locationListeners.add(listener);
	}
	
	private void throwLocationFound(Location location){
		for (ILocationListener listener : locationListeners) {
			listener.locationFound(location);
		}
	}
	
	private void throwLocationProvidersUnvailable(){
		for (ILocationListener listener : locationListeners) {
			listener.locationProviderUnavailable();
		}
	}
	
	public void getPoint(){
		initiateLocationUpdates();
	}
	
	public void startTracking(){
		initiateLocationTrackingUptades();
	}
	
	public boolean isGpsProviderEnabled(){
        return this.locationManager != null && this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }
	
	private void initiateLocationUpdates(){
		if(isNetworkAvailable())
		{
			if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			{
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
				return;
			}
		}
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
		}
		else
		{
			MessageHelper.ToastMessage(context, context.getText(R.string.warn_locProvider));
		}
	}
	
	private void initiateLocationTrackingUptades(){		
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationTrackingListener);
		}
		else
		{
			MessageHelper.ToastMessage(context, context.getText(R.string.notif_prov_title_gps));
		}
	}
	
	LocationListener locationListener = new LocationListener() {
	
		public void onStatusChanged(String provider, int status, Bundle extras) {
			switch (status) {
			case LocationProvider.AVAILABLE:
				initiateLocationUpdates();
				break;
			case LocationProvider.OUT_OF_SERVICE:
				stopLocating();
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				stopLocating();
				break;

			default:
				break;
			}
		}	
		
		public void onProviderEnabled(String provider) {
			if(provider.equals(LocationManager.GPS_PROVIDER))
			{
				stopLocating();				
				locationManager.requestLocationUpdates(provider, Config.LOCATION_UPDATE_TIME, 0, locationListener);					
			}
			else if (provider.equals(LocationManager.NETWORK_PROVIDER))
			{
				MessageHelper.LogMessage(context, LOG_TAG, LocationManager.NETWORK_PROVIDER + " enabled.");
				if(isNetworkAvailable())
				{
					stopLocating();
					MessageHelper.LogMessage(context, LOG_TAG, LocationManager.NETWORK_PROVIDER + " enabled. Network available.");
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
				}
				else
				{
					MessageHelper.LogMessage(context, LOG_TAG, LocationManager.NETWORK_PROVIDER + " enabled. Network unavailable.");					
					if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
					{
						stopLocating();
						MessageHelper.LogMessage(context, LOG_TAG, LocationManager.NETWORK_PROVIDER + " enabled. Network unavailable. Gps available.");
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
					}
					else
					{
						MessageHelper.LogMessage(context, LOG_TAG, "No providers available.");
						throwLocationProvidersUnvailable();
					}
				}
			}
		}
		
		public void onProviderDisabled(String provider) {
			if(provider.equals(LocationManager.GPS_PROVIDER))
			{				
				MessageHelper.LogMessage(context, LOG_TAG, LocationManager.GPS_PROVIDER + " disabled.");
				if(isNetworkAvailable())
				{					
					MessageHelper.LogMessage(context, LOG_TAG, LocationManager.GPS_PROVIDER + " disabled. Network available.");
					if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
					{
						stopLocating();
						locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
						return;
					}
					else
					{
						MessageHelper.LogMessage(context, LOG_TAG, LocationManager.GPS_PROVIDER + " disabled. Network not available.");
						MessageHelper.ToastMessageLong(context, context.getText(R.string.notif_prov_title));
						throwLocationProvidersUnvailable();
					}
				}
				else{
					MessageHelper.LogMessage(context, LOG_TAG, LocationManager.GPS_PROVIDER + " disabled. Network not available.");
					MessageHelper.ToastMessageLong(context, context.getText(R.string.notif_prov_title));
					throwLocationProvidersUnvailable();
				}
			}
			else if (provider.equals(LocationManager.NETWORK_PROVIDER))
			{				
				MessageHelper.LogMessage(context, LOG_TAG, LocationManager.NETWORK_PROVIDER + " disabled.");
				if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
				{
					stopLocating();
					MessageHelper.LogMessage(context, LOG_TAG, LocationManager.NETWORK_PROVIDER + " disabled. Gps enabled.");
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
					return;
				}
				else
				{
					MessageHelper.ToastMessageLong(context, context.getText(R.string.notif_prov_title));
					MessageHelper.LogMessage(context, LOG_TAG, context.getText(R.string.notif_prov_title));
					throwLocationProvidersUnvailable();
				}
			}
		}	
		
		public void onLocationChanged(Location location) {
			throwLocationFound(location);    		
    		stopLocating();
		}
	};
	
	LocationListener locationTrackingListener = new LocationListener() {
		
		public void onStatusChanged(String provider, int status, Bundle extras) {
			switch (status) {
			case LocationProvider.AVAILABLE:
				initiateLocationTrackingUptades();
				break;
			case LocationProvider.OUT_OF_SERVICE:
				stopLocating();
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				stopLocating();
				break;

			default:
				break;
			}
		}	
		
		public void onProviderEnabled(String provider) {
			if(provider.equals(LocationManager.GPS_PROVIDER))
			{
				stopLocating();
				locationManager.requestLocationUpdates(provider, Config.LOCATION_UPDATE_TIME, 0, locationTrackingListener);					
			}
		}
		
		public void onProviderDisabled(String provider) {
			if(provider.equals(LocationManager.GPS_PROVIDER))
			{				
				MessageHelper.LogMessage(context, LOG_TAG, LocationManager.GPS_PROVIDER + " disabled.");
				throwLocationProvidersUnvailable();
			}			
		}	
		
		public void onLocationChanged(Location location) {
			throwLocationFound(location);
		}
	};

	public void stopLocating()
	{
		if(locationManager != null){
			locationManager.removeUpdates(locationListener);
			locationManager.removeUpdates(locationTrackingListener);
		}
	}
	
	/**
	 * Checks if network is available.
	 * @return returns true if available, false - otherwise.
	 */
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null;
	}
}