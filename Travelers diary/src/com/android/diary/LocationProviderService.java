package com.android.diary;

import com.android.diary.R;

import Helpers.ILocationListener;
import Helpers.LocationHelper;
import Helpers.MessageHelper;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class LocationProviderService extends IntentService{
	
	public static final String ROUTE_ID = "routeID";
	public static final String LOG_TAG = "LOC_SERVICE";
	private static final String WAKE_LOCK_TAG = "LOC_SERVICE_WAKE_LOCK";
	
	private LocationHelper locationHelper;
	private boolean stop;
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
		
		startLocating();
		super.onStart(intent, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		AcquireLock();
		
		this.stop = false;		
	}
	
	private void notifyForegroundService()
	{
		NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
		
		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_tracking);
		remoteViews.setTextViewText(R.id.notification_title, getText(R.string.notif_tracking_title));
		remoteViews.setTextViewText(R.id.noticication_message, getText(R.string.notif_tracking_text));
		
		Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
		resultIntent.putExtra(MainActivity.KEY_CLOSE_APP, true);
		PendingIntent pendigResultIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.notification_button, pendigResultIntent);
		
		Intent notificationIntent = new Intent(this, MapActivity.class);
		notificationIntent.putExtra(MapActivity.ROUTE_ID, routeId);
		
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setSmallIcon(R.drawable.ic_launcher);
		notification.setContentIntent(pendingIntent);
		notification.setWhen(System.currentTimeMillis());
		notification.setTicker(getText(R.string.notif_tracking_ticker));
		notification.setContent(remoteViews);
		
		startForeground(Notification.FLAG_ONGOING_EVENT, notification.build());
	}
	
	private void startLocating()
	{
		if(locationHelper == null)
			this.locationHelper = new LocationHelper(getApplicationContext());
		
		if(locationHelper.isGpsProviderEnabled()){
			notifyForegroundService();	
			
			locationHelper.setOnLocationFoundListener(new ILocationListener() {
				
				public void locationProviderUnavailable() {
					String ns = Context.NOTIFICATION_SERVICE;
					NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
					Context context = getApplicationContext();
					NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
					
					Intent notificationIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
					notification.setSmallIcon(android.R.drawable.stat_sys_warning)
					.setContentTitle(getText(R.string.notif_prov_title_gps))
					.setContentText(getText(R.string.notif_tracking_text))
					.setContentIntent(pendingIntent)
					.setWhen(System.currentTimeMillis())
					.setTicker(getText(R.string.notif_prov_ticker))
					.setAutoCancel(true)
					.setDefaults(Notification.DEFAULT_ALL);
					
					mNotificationManager.notify(1, notification.build());
				}
				
				public void locationFound(Location location) {
					DatabaseHandler db = new DatabaseHandler(getApplicationContext());
					db.addLocationInfo(routeId, location.getAltitude(), location.getLatitude(), location.getLongitude());
					db.close();
				}
			});
			
			locationHelper.startTracking();
		}
		else {
			MessageHelper.ToastMessageLong(getApplicationContext(), getApplicationContext().getText(R.string.notif_prov_title_gps));
			stopSelf();
		}				
	}

	@Override
	public void onDestroy() {		
		ReleaseLock();
		
		super.onDestroy();
		stop = true;
		
		if(this.locationHelper != null){
			this.locationHelper.stopLocating();
		}
	}
	
	private void AcquireLock(){
		PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
		wakeLock.acquire();
	}
	
	private void ReleaseLock(){
		wakeLock.release();
	}
}
