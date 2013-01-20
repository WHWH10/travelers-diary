package com.android.diary;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.android.diary.R;

import android.app.Activity;
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
	private LocationInfo locationInfo;
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
		locationInfo = db.getLocationInfo(id);
		db.close();
		
		TextView routeName = (TextView) findViewById(R.id.edit_route_name);
		if(locationInfo.getRoute().equals(""))
			routeName.setText(getString(R.string.route) + ": " + locationInfo.getRoute_id());
		else
			routeName.setText(locationInfo.getRoute());
		
		TextView date = (TextView) findViewById(R.id.edit_date);
		Date dt = new Date(locationInfo.getTime());
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
		
		if(!locationInfo.getTitle().equals(""))
			title.setText(locationInfo.getTitle());
		if(!locationInfo.getDescription().equals(""))
			description.setText(locationInfo.getDescription());
		Address address = locationInfo.getAddress();
		
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
		LocationInfo locationInfo = db.getLocationInfo(id);
		db.close();
		
		try {			
			List<Address> address = geocoder.getFromLocation(locationInfo.getAddress().getLatitude(), locationInfo.getAddress().getLongitude(), 1);
			locationInfo.setAddress(address.get(0));
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
		Address address = locationInfo.getAddress();
		if(!title.getText().equals(""))
			locationInfo.setTitle(title.getText().toString());
		if(!description.getText().equals(""))
			locationInfo.setDescription(description.getText().toString());
		if(!country.getText().equals(""))
			address.setCountryName(country.getText().toString());
		if(!city.getText().equals(""))
			address.setAdminArea(city.getText().toString());
		if(!street.getText().equals(""))
			address.setAddressLine(0, street.getText().toString());
		if(!postal.getText().equals(""))
			address.setPostalCode(postal.getText().toString());
		if(!feature.getText().equals(""))
			address.setFeatureName(feature.getText().toString());
		
		locationInfo.setAddress(address);
		
		DatabaseHandler db = new DatabaseHandler(this);
		db.updateLocationInfo(locationInfo);
		db.close();
		
		Toast.makeText(this, getText(R.string.edit_saved), Toast.LENGTH_SHORT).show();
	}
}
