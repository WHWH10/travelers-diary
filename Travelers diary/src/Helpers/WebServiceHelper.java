package Helpers;

import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.android.diary.Config;
import com.android.diary.CustomLog;
import com.android.diary.DatabaseHandler;
import com.android.diary.Globals;

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
