package Helpers;

import java.util.ArrayList;
import java.util.List;

import com.android.diary.Config;
import com.android.diary.Globals;
import com.android.diary.R;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

public class LocationHelper implements Handler.Callback {
    private static final String LOG_TAG = "LOCATION HELPER";
    private static final String HANDLER_THREAD_NAME = "LOCATION_HELPER_HANDLER_THREAD";
    private Context context;
    private LocationManager locationManager;
    private List<ILocationListener> locationListeners;
    private Looper looper;
    private Location lastLocation;
    private long lastLocationUpdateInMillis;
    private boolean isGpsFix;

    public LocationHelper(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        HandlerThread handlerThread = new HandlerThread(LocationHelper.HANDLER_THREAD_NAME);
        handlerThread.start();

        looper = handlerThread.getLooper();

        Handler handler = new Handler(looper, this);
        if (looper.getThread().isAlive()) {
            handler.sendEmptyMessage(0);
        }

        locationListeners = new ArrayList<ILocationListener>();
    }

    public void setOnLocationFoundListener(ILocationListener listener) {
        locationListeners.add(listener);
    }

    private void throwLocationFound(Location location) {
        for (ILocationListener listener : locationListeners) {
            listener.locationFound(location);
        }
    }

    private void throwLocationProvidersUnavailable() {
        for (ILocationListener listener : locationListeners) {
            listener.locationProviderUnavailable();
        }
    }

    public boolean isGpsProviderEnabled() {
        return this.locationManager != null && this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void stopLocating() {
        if (looper != null) {
            looper.quit();
        }

        if (locationManager != null) {
            locationManager.removeGpsStatusListener(gpsStatusListener);
            locationManager.removeUpdates(locationListener);
            locationManager.removeUpdates(locationTrackingListener);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        locationManager.addGpsStatusListener(gpsStatusListener);
        return true;
    }

    //region Get point

    public void getPoint() {
        initiateLocationUpdates();
    }

    private void initiateLocationUpdates() {
        if (Globals.isNetworkAvailable(context)) {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
                return;
            }
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
        } else {
            MessageHelper.ToastMessage(context, context.getText(R.string.warn_locProvider));
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
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                stopLocating();
                locationManager.requestLocationUpdates(provider, Config.LOCATION_UPDATE_TIME, 0, locationListener);
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                MessageHelper.LogMessage(context, LOG_TAG, LocationManager.NETWORK_PROVIDER + " enabled.");
                if (Globals.isNetworkAvailable(context)) {
                    stopLocating();
                    MessageHelper.LogMessage(context, LOG_TAG, LocationManager.NETWORK_PROVIDER + " enabled. Network available.");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
                } else {
                    MessageHelper.LogMessage(context, LOG_TAG, LocationManager.NETWORK_PROVIDER + " enabled. Network unavailable.");
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        stopLocating();
                        MessageHelper.LogMessage(context, LOG_TAG, LocationManager.NETWORK_PROVIDER + " enabled. Network unavailable. Gps available.");
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
                    } else {
                        MessageHelper.LogMessage(context, LOG_TAG, "No providers available.");
                        throwLocationProvidersUnavailable();
                    }
                }
            }
        }

        public void onProviderDisabled(String provider) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                MessageHelper.LogMessage(context, LOG_TAG, LocationManager.GPS_PROVIDER + " disabled.");
                if (Globals.isNetworkAvailable(context)) {
                    MessageHelper.LogMessage(context, LOG_TAG, LocationManager.GPS_PROVIDER + " disabled. Network available.");
                    if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        stopLocating();
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
                    } else {
                        MessageHelper.LogMessage(context, LOG_TAG, LocationManager.GPS_PROVIDER + " disabled. Network not available.");
                        MessageHelper.ToastMessageLong(context, context.getText(R.string.notif_prov_title));
                        throwLocationProvidersUnavailable();
                    }
                } else {
                    MessageHelper.LogMessage(context, LOG_TAG, LocationManager.GPS_PROVIDER + " disabled. Network not available.");
                    MessageHelper.ToastMessageLong(context, context.getText(R.string.notif_prov_title));
                    throwLocationProvidersUnavailable();
                }
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                MessageHelper.LogMessage(context, LOG_TAG, LocationManager.NETWORK_PROVIDER + " disabled.");
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    stopLocating();
                    MessageHelper.LogMessage(context, LOG_TAG, LocationManager.NETWORK_PROVIDER + " disabled. Gps enabled.");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationListener);
                } else {
                    MessageHelper.ToastMessageLong(context, context.getText(R.string.notif_prov_title));
                    MessageHelper.LogMessage(context, LOG_TAG, context.getText(R.string.notif_prov_title));
                    throwLocationProvidersUnavailable();
                }
            }
        }

        public void onLocationChanged(Location location) {
            lastLocation = location;
            lastLocationUpdateInMillis = SystemClock.elapsedRealtime();
            throwLocationFound(location);
            stopLocating();
        }
    };

    //endregion

    //region Location tracking

    public void startTracking() {
        initiateLocationTrackingUpdates();
    }

    private void initiateLocationTrackingUpdates() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.LOCATION_UPDATE_TIME, 0, locationTrackingListener, looper);
        } else {
            MessageHelper.ToastMessage(context, context.getText(R.string.notif_prov_title_gps));
        }
    }

    LocationListener locationTrackingListener = new LocationListener() {

        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    initiateLocationTrackingUpdates();
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
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                stopLocating();
                locationManager.requestLocationUpdates(provider, Config.LOCATION_UPDATE_TIME, 0, locationTrackingListener);
            }
        }

        public void onProviderDisabled(String provider) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                MessageHelper.LogMessage(context, LOG_TAG, LocationManager.GPS_PROVIDER + " disabled.");
                throwLocationProvidersUnavailable();
            }
        }

        public void onLocationChanged(Location location) {
            lastLocation = location;
            lastLocationUpdateInMillis = SystemClock.elapsedRealtime();
            throwLocationFound(location);
        }
    };

    //endregion

    GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    if (lastLocation != null) {
                        isGpsFix = (SystemClock.elapsedRealtime() - lastLocationUpdateInMillis) < 3000;
                    }

                    if (isGpsFix) {
                        Log.i(LOG_TAG, "GPS is fix");
                    } else {
                        Log.i(LOG_TAG, "GPS is not fix");
                    }

                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    isGpsFix = true;

                    break;

                default:
                    break;
            }
        }
    };
}