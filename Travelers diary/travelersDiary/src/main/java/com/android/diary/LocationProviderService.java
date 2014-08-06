package com.android.diary;

import Helpers.ILocationListener;
import Helpers.LocationHelper;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationProviderService extends Service {

    public static final String ROUTE_ID = "routeID";
    public static final String LOG_TAG = "LOC_SERVICE";

    public static final String ACTION_START_LOCATION_PROVIDER = "com.android.diary.action.START_LOCATION_PROVIDER";
    public static final String ACTION_STOP_LOCATION_PROVIDER = "com.android.diary.action.STOP_LOCATION_PROVIDER";

    private ExecutorService executorService;

    private LocationHelper locationHelper;
    private int routeId;

    public LocationProviderService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(LocationProviderService.ACTION_START_LOCATION_PROVIDER)) {
            routeId = 0;
            if (intent.getExtras() != null) {
                routeId = intent.getExtras().getInt(ROUTE_ID, 0);
            }

            LocationTrackingRunnable locationTrackingRunnable = new LocationTrackingRunnable(this);
            executorService.execute(locationTrackingRunnable);
        } else if (intent.getAction().equals(LocationProviderService.ACTION_STOP_LOCATION_PROVIDER)) {
            stopSelf();
        } else
            stopSelf();

        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    private void notifyForegroundService() {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_tracking);
        remoteViews.setTextViewText(R.id.notification_title, getText(R.string.notif_tracking_title));
        remoteViews.setTextViewText(R.id.noticication_message, getText(R.string.notif_tracking_text));

        Intent resultIntent = new Intent(getApplicationContext(), RoutesActivity.class);
        resultIntent.putExtra(RoutesActivity.KEY_CLOSE_APP, true);
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

    private void startLocating() {
        if (locationHelper == null)
            this.locationHelper = new LocationHelper(getApplicationContext());

        if (locationHelper.isGpsProviderEnabled()) {
            notifyForegroundService();

            locationHelper.setOnLocationFoundListener(new ILocationListener() {

                public void locationProviderUnavailable() {
                    notifyOnError(getString(R.string.notif_prov_title_gps));
                }

                public void locationFound(Location location) {
                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                    db.addTrackPoint(routeId, location.getLatitude(), location.getLongitude());
                    db.close();
                }
            });

            locationHelper.startTracking();
        } else {
            notifyOnError(getApplicationContext().getText(R.string.notif_prov_title_gps).toString());
            stopSelf();
        }
    }

    private void notifyOnError(String message) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        Context context = getApplicationContext();
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context);

        Intent notificationIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(message)
                .setContentText(getText(R.string.notif_prov_text))
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setTicker(getText(R.string.notif_prov_ticker))
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        mNotificationManager.notify(1, notification.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (this.locationHelper != null) {
            this.locationHelper.stopLocating();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class LocationTrackingRunnable implements Runnable {
        LocationProviderService locationProviderService;

        public LocationTrackingRunnable(LocationProviderService locationProviderService) {
            this.locationProviderService = locationProviderService;
        }

        @Override
        public void run() {
            startLocating();
        }
    }
}
