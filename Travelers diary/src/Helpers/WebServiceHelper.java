package Helpers;

import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.android.diary.BaseApplication;
import com.android.diary.Config;
import com.android.diary.CustomLog;
import com.android.diary.DatabaseHandler;
import com.android.diary.Globals;
import com.android.diary.Route;
import com.android.diary.RouteItem;

import android.content.Context;
import android.os.AsyncTask;

public class WebServiceHelper extends AsyncTask<String, Void, Void>{
	private static final String LOG_TAG = "WebServiceHelper";
	private Context context;
		
	public WebServiceHelper(Context context) {
		super();
		this.context = context;
	}

	@Override
	protected Void doInBackground(String... params) {
		if(Globals.isNetworkAvailable(context)){
			importLog();
		}
		
		return null;
	}
	
	private void importLog(){
		DatabaseHandler db = new DatabaseHandler(context);
		
		try {			
			List<CustomLog> logs = db.getLog();				
			
			for (int i = 0; i < logs.size(); i++) {
				CustomLog customLog = logs.get(i);
				
				SoapObject request = new SoapObject(Config.WEBSERVICE_NAMESPACE, Config.WEBSERVICE_METHOD_LOG_IMPORT);
				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
				envelope.dotNet = true;
				
				request.addProperty(getPropertyInfo("logId", Integer.class, customLog.getLogId()));
				request.addProperty(getPropertyInfo("os", String.class, customLog.getOs()));
				request.addProperty(getPropertyInfo("device", String.class, customLog.getDevice()));
				request.addProperty(getPropertyInfo("model", String.class, customLog.getModel()));
				request.addProperty(getPropertyInfo("product", String.class, customLog.getProduct()));
				request.addProperty(getPropertyInfo("message", String.class, customLog.getMessage()));
				request.addProperty(getPropertyInfo("tag", String.class, customLog.getTag()));
				request.addProperty(getPropertyInfo("username", String.class, customLog.getUsername()));
				request.addProperty(getPropertyInfo("logAddedOn", Long.class, customLog.getDateCreated().getTime()));
				
				envelope.setOutputSoapObject(request);
				SoapPrimitive response = callWebService(Config.WEBSERVICE_URL, Config.WEBSERVICE_NAMESPACE, Config.WEBSERVICE_METHOD_LOG_IMPORT, envelope);
				if(response != null){
					db.deleteLog(Integer.parseInt(response.toString()));
				}
			}
		} catch (Exception e) {
			MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
		}finally{
			db.close();
		}
	}
	
	private void importRoute(){
		DatabaseHandler db = new DatabaseHandler(context);
		String username = ((BaseApplication)context).getUsername();
		
		try {			
			List<Route> routes = db.getRoutes();				
			
			for (int i = 0; i < routes.size(); i++) {
				Route route = routes.get(i);
				
				SoapObject request = new SoapObject(Config.WEBSERVICE_NAMESPACE, Config.WEBSERVICE_METHOD_ROUTE_IMPORT);
				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
				envelope.dotNet = true;
				
				request.addProperty(getPropertyInfo("username", String.class, username));
				request.addProperty(getPropertyInfo("routeId", Integer.class, route.getRouteId()));
				request.addProperty(getPropertyInfo("title", String.class, route.getTitle()));
				request.addProperty(getPropertyInfo("description", String.class, route.getDescription()));
				request.addProperty(getPropertyInfo("dateCreated", Long.class, route.getDateCreated().getTime()));
				request.addProperty(getPropertyInfo("dateModified", Long.class, route.getDateModified().getTime()));
				
				envelope.setOutputSoapObject(request);
				SoapPrimitive response = callWebService(Config.WEBSERVICE_URL, Config.WEBSERVICE_NAMESPACE, Config.WEBSERVICE_METHOD_ROUTE_IMPORT, envelope);
				if(response != null){
					if(importRouteItem(route.getRouteId()))
						db.setRouteImported(Integer.parseInt(response.toString()));
				}
			}
		} catch (Exception e) {
			MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
		}finally{
			db.close();
		}
	}
	
	private boolean importRouteItem(int routeId){
		DatabaseHandler db = new DatabaseHandler(context);
		String username = ((BaseApplication)context).getUsername();
		
		try {			
			List<RouteItem> routeItems = db.getRouteItems(routeId);				
			
			for (int i = 0; i < routeItems.size(); i++) {
				RouteItem routeItem = routeItems.get(i);
				
				SoapObject request = new SoapObject(Config.WEBSERVICE_NAMESPACE, Config.WEBSERVICE_METHOD_ROUTE_ITEM_IMPORT);
				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
				envelope.dotNet = true;
				
				request.addProperty(getPropertyInfo("username", String.class, username));
				request.addProperty(getPropertyInfo("routeId", Integer.class, routeItem.getRouteId()));
				request.addProperty(getPropertyInfo("title", String.class, routeItem.getTitle()));
				request.addProperty(getPropertyInfo("description", String.class, routeItem.getDescription()));
				request.addProperty(getPropertyInfo("address", String.class, routeItem.getAddress().getAddressLine(0)));
				request.addProperty(getPropertyInfo("city", String.class, routeItem.getAddress().getLocality()));
				request.addProperty(getPropertyInfo("country", String.class, routeItem.getAddress().getCountryName()));
				request.addProperty(getPropertyInfo("countryCode", String.class, routeItem.getAddress().getCountryCode()));
				request.addProperty(getPropertyInfo("postalCode", String.class, routeItem.getAddress().getPostalCode()));
				request.addProperty(getPropertyInfo("longitude", Double.class, routeItem.getLongitude()));
				request.addProperty(getPropertyInfo("latitude", Double.class, routeItem.getLatitude()));
				request.addProperty(getPropertyInfo("dateCreated", Long.class, routeItem.getDateCreated().getTime()));
				request.addProperty(getPropertyInfo("dateModified", Long.class, routeItem.getDateModified().getTime()));
				
				envelope.setOutputSoapObject(request);
				SoapPrimitive response = callWebService(Config.WEBSERVICE_URL, Config.WEBSERVICE_NAMESPACE, Config.WEBSERVICE_METHOD_ROUTE_ITEM_IMPORT, envelope);
				if(response != null){
					db.setRouteItemImported(Integer.parseInt(response.toString()));
				}
			}
		} catch (Exception e) {
			MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
			return false;
		}finally{
			db.close();
		}
		
		return true;
	}
	
	private SoapPrimitive callWebService(String webServiceUrl, String namespace, String method, SoapSerializationEnvelope envelope){
		try {
			HttpTransportSE httpTransportSE = new HttpTransportSE(webServiceUrl);
			httpTransportSE.call(namespace + method, envelope);
			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
			return response;
		} catch (Exception e) {
			MessageHelper.LogErrorMessage(context, LOG_TAG, e.toString());
			return null;
		}
	}
	
	private PropertyInfo getPropertyInfo(String name, Object type, Object value){
		PropertyInfo propertyInfo = new PropertyInfo();
		
		propertyInfo.setName(name);
		propertyInfo.setType(type);
		propertyInfo.setValue(value);
		
		return propertyInfo;
	}
}
