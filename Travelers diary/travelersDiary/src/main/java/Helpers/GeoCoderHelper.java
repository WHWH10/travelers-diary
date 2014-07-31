package Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.android.diary.DatabaseHandler;
import com.android.diary.Globals;
import com.android.diary.RouteItem;

import java.io.IOException;
import java.util.List;

public class GeoCoderHelper extends AsyncTask<RouteItem, Void, RouteItem> {
    private Context context;
    private ITaskCompleteListener taskCompleteListener;

    public GeoCoderHelper(Context context, ITaskCompleteListener listener) {
        this.context = context;
        this.taskCompleteListener = listener;
    }

    @Override
    protected RouteItem doInBackground(RouteItem... params) {
        RouteItem routeItem = null;

        if(context != null && Globals.isNetworkAvailable(context) && params.length > 0){
            routeItem = params[0];

            Geocoder geocoder = new Geocoder(context);
            try {
                List<Address> addresses = geocoder.getFromLocation(routeItem.getLatitude(), routeItem.getLongitude(), 1);
                if(addresses != null && addresses.size() > 0){
                    routeItem.setAddress(addresses.get(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return routeItem;
    }

    @Override
    protected void onPostExecute(RouteItem routeItem) {
        super.onPostExecute(routeItem);

        int result = 0;

        if(routeItem != null){
            saveData(routeItem);
            result = routeItem.getRouteId();
        }

        if(taskCompleteListener != null)
            taskCompleteListener.onTaskComplete(result);
    }

    private void saveData(RouteItem routeItem) {
        ContentValues cv = new ContentValues();

        Address address = routeItem.getAddress();

        cv.put(DatabaseHandler.KEY_COUNTRY, address.getCountryName());
        cv.put(DatabaseHandler.KEY_COUNTRY_CODE, address.getCountryCode());
        cv.put(DatabaseHandler.KEY_ADMIN_AREA, address.getAdminArea());
        cv.put(DatabaseHandler.KEY_THOROUGHFARE, address.getSubThoroughfare());
        cv.put(DatabaseHandler.KEY_SUB_THOROUGHFARE, address.getSubThoroughfare());
        cv.put(DatabaseHandler.KEY_POSTAL_CODE, address.getPostalCode());
        cv.put(DatabaseHandler.KEY_FEATURE, address.getFeatureName());
        cv.put(DatabaseHandler.KEY_LOCALITY, address.getLocality());
        cv.put(DatabaseHandler.KEY_LOCALE, address.getLocale().getLanguage());
        if(address.getMaxAddressLineIndex() > -1)
            cv.put(DatabaseHandler.KEY_ADDRESS_LINE, address.getAddressLine(0));
        cv.put(DatabaseHandler.KEY_IS_ADDRESS_UPDATED, 1);

        DatabaseHandler db = new DatabaseHandler(context);
        db.updateRouteItem(cv, routeItem.getRouteItemId());
        db.close();
    }
}
