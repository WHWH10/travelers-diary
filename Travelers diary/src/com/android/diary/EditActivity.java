package com.android.diary;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.android.diary.R;

import android.app.Activity;
import android.content.ContentValues;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditActivity extends Activity{
	
	private int id;
	private RouteItem routeItem;
	private EditText title;
	private EditText description;
	private EditText country;
	private EditText city;
	private EditText street;
	private EditText postal;
	private EditText feature;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		Bundle bundle = getIntent().getExtras();
		this.id = bundle.getInt(MapActivity.MARKER_ID);
		
		DatabaseHandler db = new DatabaseHandler(this);
		routeItem = db.getRouteItem(id);
		db.close();
		
		TextView routeName = (TextView) findViewById(R.id.edit_route_name);
		if(routeItem.getTitle().equals(""))
			routeName.setText(getString(R.string.route) + ": " + routeItem.getRouteItemId());
		else
			routeName.setText(routeItem.getTitle());
		
		TextView date = (TextView) findViewById(R.id.edit_date);
		Date dt = routeItem.getDateCreated();
		date.setText(dt.toString());
		
		loadViews();
	}
	
	private void loadViews()
	{
		title = (EditText) findViewById(R.id.edit_title);
		description = (EditText) findViewById(R.id.edit_description);
		country = (EditText) findViewById(R.id.edit_coutry);
		city = (EditText) findViewById(R.id.edit_city);
		street = (EditText) findViewById(R.id.edit_street);
		postal = (EditText) findViewById(R.id.edit_postal);
		feature = (EditText) findViewById(R.id.edit_feature);
		
		if(!routeItem.getTitle().equals(""))
			title.setText(routeItem.getTitle());
		if(!routeItem.getDescription().equals(""))
			description.setText(routeItem.getDescription());
		Address address = routeItem.getAddress();
		
		fillAddressFields(address);
	}
	
	private void fillAddressFields(Address address)
	{
		if(address == null)
			return;
		
		if(address.getCountryName() != null)
			if(!address.getCountryName().equals(""))
				country.setText(address.getCountryName());
		
		if(address.getAdminArea() != null)
			if(!address.getAdminArea().equals(""))
				city.setText(address.getAdminArea());
		
		if(address.getAddressLine(0) != null)
			if(!address.getAddressLine(0).equals(""))
				street.setText(address.getAddressLine(0));
		
		if(address.getPostalCode() != null)
			if(!address.getPostalCode().equals(""))
				postal.setText(address.getPostalCode());
		
		if(address.getFeatureName() != null)
			if(!address.getFeatureName().equals(""))
				feature.setText(address.getFeatureName());
	}

	public void onAutofillPressed(View view)
	{
		Geocoder geocoder = new Geocoder(this);
		DatabaseHandler db = new DatabaseHandler(this);
		RouteItem routeItem = db.getRouteItem(id);
		db.close();
		
		try {			
			List<Address> address = geocoder.getFromLocation(routeItem.getLatitude(), routeItem.getLongitude(), 1);
			routeItem.setAddress(address.get(0));
			fillAddressFields(address.get(0));
//			Toast.makeText(this, address.get(0).getAdminArea(), Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Override
	protected void onPause() {
		saveData();
		super.onPause();
	}
	
	private void saveData()
	{
		ContentValues cv = new ContentValues();
		if(!title.getText().equals(""))
			cv.put(DatabaseHandler.KEY_TITLE, title.getText().toString());
		if(!description.getText().equals(""))
			cv.put(DatabaseHandler.KEY_DESCRIPTION, description.getText().toString());
		if(!country.getText().equals(""))
			cv.put(DatabaseHandler.KEY_COUNTRY, country.getText().toString());
		if(!city.getText().equals(""))
			cv.put(DatabaseHandler.KEY_ADMIN_AREA, city.getText().toString());
		if(!street.getText().equals(""))
			cv.put(DatabaseHandler.KEY_ADDRESS_LINE, street.getText().toString());
		if(!postal.getText().equals(""))
			cv.put(DatabaseHandler.KEY_POSTAL_CODE, postal.getText().toString());
		if(!feature.getText().equals(""))
			cv.put(DatabaseHandler.KEY_FEATURE, feature.getText().toString());
				
		DatabaseHandler db = new DatabaseHandler(this);
		db.updateRouteItem(cv, id);
		db.close();
		
		Toast.makeText(this, getText(R.string.edit_saved), Toast.LENGTH_SHORT).show();
	}
}
