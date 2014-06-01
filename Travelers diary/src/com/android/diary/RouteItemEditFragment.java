package com.android.diary;

import java.io.IOException;
import java.util.List;

import BaseClasses.BaseFragment;
import android.content.ContentValues;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.diary.R;

public class RouteItemEditFragment extends BaseFragment {
	
	private static final String LOG_TAG = "ROUTE ITEM EDIT FRAGMENT";
	public static final String ROUTE_ITEM_ID = "routeItemId";
	
	private RouteItem routeItem;
	
	private EditText title;
	private EditText description;
	private EditText country;
	private EditText postalCode;
	private EditText street;
	private EditText city;
	private Button autofillBtn;
	private Button saveBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(container == null)
			return null;
		
		View view = inflater.inflate(R.layout.fragment_route_item_edit, container, false);
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		initControls(view);
		setValues(true);
		
		autofillBtn = (Button) view.findViewById(R.id.ri_edit_autofill_btn);
		autofillBtn.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				autofillPressed();
			}
		});
		
		saveBtn = (Button) view.findViewById(R.id.ri_edit_save_btn);
		saveBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				saveData();				
			}
		});
		
		return view;
	}
	
	public static RouteItemEditFragment newInstance(int routeItemId){
		RouteItemEditFragment fragment = new RouteItemEditFragment();
		
		Bundle args = new Bundle();
		args.putInt(ROUTE_ITEM_ID, routeItemId);
		fragment.setArguments(args);
		return fragment;
	}

	private int getRouteItemId(){
		if(getArguments() == null)
			return 0;
		return getArguments().getInt(ROUTE_ITEM_ID, 0);
	}
	
	private void initControls(View view){
		title = (EditText) view.findViewById(R.id.ri_edit_title);
		description = (EditText) view.findViewById(R.id.ri_edit_description);
		country = (EditText) view.findViewById(R.id.ri_edit_country);
		postalCode = (EditText) view.findViewById(R.id.ri_edit_postal);
		street = (EditText) view.findViewById(R.id.ri_edit_street);
		city = (EditText) view.findViewById(R.id.ri_edit_city);
		
	}
	
	private void setValues(boolean fromDB){
		if(fromDB){
			DatabaseHandler db = new DatabaseHandler(getActivity());
			this.routeItem = db.getRouteItem(getRouteItemId());
			db.close();
		}
		
		if(routeItem != null){
			setText(title, routeItem.getTitle());
			setText(description, routeItem.getDescription());
			setText(country, routeItem.getAddress().getCountryName());
			setText(city, routeItem.getAddress().getLocality());
			setText(street, routeItem.getAddress().getAddressLine(0));
			setText(postalCode, routeItem.getAddress().getPostalCode());
		}		
	}
	
	private void autofillPressed(){
		Geocoder geocoder = new Geocoder(getActivity());
		DatabaseHandler db = new DatabaseHandler(getActivity());
		db.close();
		
		if(!Globals.isNetworkAvailableWithToast(getActivity().getApplicationContext()))		{
			return;
		}
		
		try {
			List<Address> address = geocoder.getFromLocation(this.routeItem.getLatitude(), this.routeItem.getLongitude(), 1);

			this.routeItem.setAddress(address.get(0));
			setValues(false);
		} catch (IOException e) {
			ToastMessage(getString(R.string.warn_unableToFillLocation));
			LogErrorMessage(LOG_TAG, e.toString());
		}
	}
	
	private void saveData() {
		ContentValues cv = new ContentValues();
		
		cv.put(DatabaseHandler.KEY_TITLE, title.getText().toString());
		cv.put(DatabaseHandler.KEY_DESCRIPTION, description.getText().toString());
		cv.put(DatabaseHandler.KEY_COUNTRY, country.getText().toString());
		cv.put(DatabaseHandler.KEY_COUNTRY_CODE, routeItem.getAddress().getCountryCode());
		cv.put(DatabaseHandler.KEY_ADDRESS_LINE, street.getText().toString());
		cv.put(DatabaseHandler.KEY_ADMIN_AREA, routeItem.getAddress().getAdminArea());
		cv.put(DatabaseHandler.KEY_THOROUGHFARE, routeItem.getAddress().getSubThoroughfare());
		cv.put(DatabaseHandler.KEY_SUB_THOROUGHFARE, routeItem.getAddress().getSubThoroughfare());
		cv.put(DatabaseHandler.KEY_POSTAL_CODE, postalCode.getText().toString());
		cv.put(DatabaseHandler.KEY_FEATURE, routeItem.getAddress().getFeatureName());
		cv.put(DatabaseHandler.KEY_LOCALITY, city.getText().toString());
		cv.put(DatabaseHandler.KEY_LOCALE, routeItem.getAddress().getLocale().getLanguage());
		cv.put(DatabaseHandler.KEY_IS_ADDRESS_UPDATED, 1);
		
		DatabaseHandler db = new DatabaseHandler(getActivity());
		db.updateRouteItem(cv, getRouteItemId());
		db.close();
		
		ToastMessage(getString(R.string.ri_edit_saved));
	}
	
	/**
	 * Sets given text value to the given editText.
	 * @param view - fragment view
	 * @param eTId - editText Id (R number)
	 * @param text - text to set on the editText
	 */
	private void setText(EditText editText, String text){
		if(editText == null || text == null || text.isEmpty())
			return;
		
		editText.setText(text);		
	}
}
