package com.android.diary;

import java.io.IOException;
import java.util.List;

import Helpers.SharedPreferenceHelper;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.util.Log;

public class MainService extends IntentService {

	public static final String LOG_TAG = "MAIN_SERVICE";

	public MainService() {
		super("Main service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		startAddressFillService();
	}
	
	private void startAddressFillService()
	{
		SharedPreferenceHelper helper = new SharedPreferenceHelper(this);
		if(helper.getAddressUpdateFlag())
		{
			if(isNetworkAvailable())
			{
				fillAddresses();
				helper.setAddressUpdateFlag(false);
			}				
		}
	}

	private void fillAddresses() {
		DatabaseHandler db = new DatabaseHandler(this);
		Geocoder geocoder = new Geocoder(this);

		try {
			List<RouteItem> routeItems = db.getRouteItems_withoutAddresses();
			for (int i = 0; i < routeItems.size(); i++) {
				List<Address> address = geocoder.getFromLocation(routeItems.get(i).getLatitude(),
						routeItems.get(i).getLongitude(), 1);
				routeItems.get(i).setAddress(address.get(0));
				saveRouteItem(routeItems.get(i), db);
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, e.toString());
		}

		db.close();
	}

	private void saveRouteItem(RouteItem routeItem, DatabaseHandler db) {
		ContentValues cv = new ContentValues();

		cv.put(DatabaseHandler.KEY_TITLE, routeItem.getTitle());
		cv.put(DatabaseHandler.KEY_DESCRIPTION, routeItem.getDescription());
		cv.put(DatabaseHandler.KEY_COUNTRY, routeItem.getAddress().getCountryName());
		cv.put(DatabaseHandler.KEY_ADDRESS_LINE, routeItem.getAddress().getAddressLine(0));
		cv.put(DatabaseHandler.KEY_ADMIN_AREA, routeItem.getAddress().getAdminArea());
		cv.put(DatabaseHandler.KEY_THOROUGHFARE, routeItem.getAddress().getThoroughfare());
		cv.put(DatabaseHandler.KEY_SUB_THOROUGHFARE, routeItem.getAddress().getSubThoroughfare());
		cv.put(DatabaseHandler.KEY_POSTAL_CODE, routeItem.getAddress().getPostalCode());
		cv.put(DatabaseHandler.KEY_FEATURE, routeItem.getAddress().getFeatureName());

		db.updateRouteItem(cv, routeItem.getRouteItemId());
		Log.i(LOG_TAG, "updated");
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
	}
}