package com.android.diary;

import com.android.diary.R;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class LocationProviderService extends IntentService{
	
	public static final String ROUTE_ID = "routeID";
	public static final String LOG_TAG = "LOC_SERVICE";
	private static final String WAKE_LOCK_TAG = "LOC_SERVICE_WAKE_LOCK";
	
	private boolean stop;
	private LocationManager locationManager;
	private int counter;
	private int routeId;
	private WakeLock wakeLock;
	
	public LocationProviderService() {
		super("Location provider");
	}

	@Override
	protected void onHandleIntent(Intent intent) {	
		if(routeId == 0)
			return;
		while(!stop){}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		routeId = 0;
		if(intent.getExtras() != null)
		{
			routeId = intent.getExtras().getInt(ROUTE_ID, 0);
		}
		
		super.onStart(intent, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		AcquireLock();
		
		this.stop = false;
		counter = 0;		
		
		startLocating();
	}
	
	private void notifyForegroundService()
	{
		NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
		
		Intent notificationIntent = new Intent(this, MapActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setSmallIcon(R.drawable.ic_launcher);
		notification.setContentTitle(getText(R.string.notif_tracking_title));
		notification.setContentText(getText(R.string.notif_tracking_text));
		notification.setContentIntent(pendingIntent);
		notification.setWhen(System.currentTimeMillis());
		notification.setTicker(getText(R.string.notif_tracking_ticker));
		
		startForeground(Notification.FLAG_ONGOING_EVENT, notification.build());
	}
	
	private void startLocating()
	{
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		if(isNetworkAvailable())
		{
			if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			{
				Toast.makeText(this, "Locating network", Toast.LENGTH_SHORT).show();
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
				notifyForegroundService();
				return;
			}
		}
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			Toast.makeText(this, "Locating gps", Toast.LENGTH_SHORT).show();
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
			notifyForegroundService();
		}
		else
		{
			Toast.makeText(this, getText(R.string.warn_locProvider), Toast.LENGTH_LONG).show();
			stopSelf();
		}
	}
	
	private void stopLocating()
	{
		if(locationManager != null)
		{
			locationManager.removeUpdates(locationListener);
		}
	}

	@Override
	public void onDestroy() {
		
		ReleaseLock();
		
		super.onDestroy();
		stop = true;
		stopLocating();		
	}
	
	private void AcquireLock(){
		PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
		wakeLock.acquire();
	}
	
	private void ReleaseLock(){
		wakeLock.release();
	}

	private void pointAdded()
	{
		Toast.makeText(this, "Points added: " + counter, Toast.LENGTH_SHORT).show();
	}
	
	LocationListener locationListener = new LocationListener() {
		
		
		public void onStatusChanged(String provider, int status, Bundle extras) {
			switch (status) {
			case LocationProvider.AVAILABLE:
				startLocating();
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
			if(provider.equals("gps"))
			{
				if(isNetworkAvailable())
				{
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
					return;
				}
				stopLocating();
				locationManager.requestLocationUpdates(provider, Config.LOCATION_UPDATE_TIME, 0, locationListener);					
			}
			else if (provider.equals("network"))
			{
				if(isNetworkAvailable())
				{
					stopLocating();
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
				}
				else
				{
					stopLocating();
					if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
					{
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
					}
					else
					{
						String ns = Context.NOTIFICATION_SERVICE;
						NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
						Context context = getApplicationContext();
						NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
						
						Intent notificationIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
						notification.setSmallIcon(android.R.drawable.stat_sys_warning)
						.setContentTitle(getText(R.string.notif_prov_title))
						.setContentText(getText(R.string.notif_tracking_text))
						.setContentIntent(pendingIntent)
						.setWhen(System.currentTimeMillis())
						.setTicker(getText(R.string.notif_prov_ticker))
						.setAutoCancel(true)
						.setDefaults(Notification.DEFAULT_ALL);
						
						mNotificationManager.notify(1, notification.build());
					}
				}
			}
		}	
		
		public void onProviderDisabled(String provider) {
			if(provider.equals("gps"))
			{
				if(isNetworkAvailable())
				{
					if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
					{
						locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
						return;
					}
					else
					{
						stopLocating();
						Toast.makeText(getApplicationContext(), getText(R.string.notif_prov_title), Toast.LENGTH_LONG).show();
						stopSelf();
					}
				}
			}
			else if (provider.equals("network"))
			{
				stopLocating();
				if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
				{
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
					return;
				}
				else
				{
					Toast.makeText(getApplicationContext(), getText(R.string.notif_prov_title), Toast.LENGTH_LONG).show();
					stopSelf();
				}
			}
		}	
		
		public void onLocationChanged(Location location) {
			Log.i(LOG_TAG, "New Point");
			DatabaseHandler db = new DatabaseHandler(getApplicationContext());			
			db.addLocationInfo(routeId, location.getAltitude(), location.getLatitude(), location.getLongitude());
    		counter++;
    		db.close();
    		pointAdded();
		}
	};	
	
	/**
	 * Checks if network is available.
	 * @return returns true if available, false - otherwise.
	 */
		private boolean isNetworkAvailable() {
		    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		    return activeNetworkInfo != null;
		}
}
