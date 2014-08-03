package com.android.diary;

import java.io.IOException;
import java.util.List;

import Helpers.SharedPreferenceHelper;
import Helpers.WebServiceHelper;
import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

public class MainService extends IntentService {

	public static final String LOG_TAG = "MAIN_SERVICE";

	public MainService() {
		super("Main service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		startAddressFillService();
				
		if(canMainServiteRun()){
			WebServiceHelper webServiceHelper = new WebServiceHelper(getApplicationContext());
			webServiceHelper.execute("");
		}		
	}
	
	private boolean canMainServiteRun(){
		if(!Globals.isUserLoggedIn(getApplicationContext()))
			return false;
		
		SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(getApplicationContext());
		
		if(sharedPreferenceHelper.getUploadOnWifiFlag() && Globals.isConnectedToWifi(getApplicationContext()))
			return true;
		else
			Globals.isNetworkAvailable(getApplicationContext());			
		
		return false;
	}
	
	private void startAddressFillService()
	{
		SharedPreferenceHelper helper = new SharedPreferenceHelper(this);
		if(helper.getAddressUpdateFlag()){
			if(Globals.isNetworkAvailable(getApplicationContext())){
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
            for (RouteItem routeItem : routeItems) {
                List<Address> address = geocoder.getFromLocation(routeItem.getLatitude(),
                        routeItem.getLongitude(), 1);
                routeItem.setAddress(address.get(0));
                saveRouteItem(routeItem, db);
            }
		} catch (IOException e) {
			Log.e(LOG_TAG, e.toString());
		}

		db.close();
	}

	private void saveRouteItem(RouteItem routeItem, DatabaseHandler db) {
		db.updateRouteItem(routeItem, true, true);
	}
}
