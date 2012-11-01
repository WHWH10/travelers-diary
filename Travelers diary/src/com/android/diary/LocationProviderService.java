package com.android.diary;

import com.android.diary.R;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class LocationProviderService extends IntentService{
	
	private boolean stop;
	private boolean newLocationAdded;
	private LocationManager locationManager;
	private int counter;
	private int route;
	public LocationProviderService() {
		super("Location provider");
	}

	@Override
	protected void onHandleIntent(Intent intent) {		
		while(!stop){}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.newLocationAdded = false;
		this.stop = false;
		this.load();
		this.route++;		
		counter = 0;		
		
		startLocating();
	}
	
	private void notifyForegroundService()
	{
		NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
		
		Intent notificationIntent = new Intent(this, MainActivity.class);
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
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, locationListener);
				notifyForegroundService();
				return;
			}
		}
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
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
		super.onDestroy();
		stop = true;
		this.save();
		stopLocating();
	}

	private void pointAdded()
	{
		Toast.makeText(this, "Points added: " + counter, Toast.LENGTH_SHORT).show();
	}
	
	LocationListener locationListener = new LocationListener() {
		
		@Override
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
		
		@Override
		public void onProviderEnabled(String provider) {
			if(provider.equals("gps"))
			{
				if(isNetworkAvailable())
				{
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, locationListener);
					return;
				}
				stopLocating();
				locationManager.requestLocationUpdates(provider, 10000, 0, locationListener);					
			}
			else if (provider.equals("network"))
			{
				if(isNetworkAvailable())
				{
					stopLocating();
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, locationListener);
				}
				else
				{
					stopLocating();
					if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
					{
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
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
		
		@Override
		public void onProviderDisabled(String provider) {
			if(provider.equals("gps"))
			{
				if(isNetworkAvailable())
				{
					if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
					{
						locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, locationListener);
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
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
					return;
				}
				else
				{
					Toast.makeText(getApplicationContext(), getText(R.string.notif_prov_title), Toast.LENGTH_LONG).show();
					stopSelf();
				}
			}
		}
		
		@Override
		public void onLocationChanged(Location location) {
			DatabaseHandler db = new DatabaseHandler(getApplicationContext());
			
			db.addLocationInfo(new LocationInfo(location, route));
    		counter++;
    		db.close();
    		newLocationAdded = true;
    		pointAdded();
		}
	};
	
	private void save()
	{
		SharedPreferences settings = getSharedPreferences(MainActivity.APP_PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();
		if(newLocationAdded)
			editor.putInt(MainActivity.ROUTE_ID, this.route);
		else
		{
			this.route--;
			editor.putInt(MainActivity.ROUTE_ID, this.route);
		}
		editor.commit();
	}
	
	private void load()
	{
		SharedPreferences settings = getSharedPreferences(MainActivity.APP_PREFS, 0);
		route = settings.getInt(MainActivity.ROUTE_ID, 0);
	}
	
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
