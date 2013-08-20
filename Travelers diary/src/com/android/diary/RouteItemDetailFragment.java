package com.android.diary;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.diary.R;

public class RouteItemDetailFragment extends Fragment {

	public static final String ROUTE_ITEM_ID = "routeItemId";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(container == null)
			return null;
		
		View view = inflater.inflate(R.layout.fragment_route_item_details, container, false);
		
		setValues(view);
		
		if(savedInstanceState == null)
			setHasOptionsMenu(true);
		return view;
	}
	
	public static RouteItemDetailFragment newInstance(int routeItemId)
	{
		RouteItemDetailFragment fragment = new RouteItemDetailFragment();
		 
		Bundle args = new Bundle();
		args.putInt(ROUTE_ITEM_ID, routeItemId);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.activity_route_item_detail, menu);		
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_EditRouteItem:
			Intent intent = new Intent(getActivity(), EditActivity.class);
			intent.putExtra(ROUTE_ITEM_ID, getRouteItemID());
			startActivity(intent);
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private int getRouteItemID()
	{
		if(getArguments() == null)
			return 0;
		return getArguments().getInt(ROUTE_ITEM_ID, 0);
	}
	
	private void setValues(View view)
	{
		DatabaseHandler db = new DatabaseHandler(getActivity());
		RouteItem routeItem = db.getRouteItem(getRouteItemID());
		if(routeItem != null)
		{
			Route route = db.getRoute(routeItem.getRouteId());
			
			if(route != null)
			{
				setText(view, R.id.routeItem_det_route_title_tV, route.getTitle(), true);
			}
			
			setText(view, R.id.routeItem_det_title_tV, routeItem.getTitle(), true);
			setText(view, R.id.routeItem_det_description_tV, routeItem.getDescription(), true);
			setText(view, R.id.routeItem_det_date_tV, routeItem.getDateCreated().toString(), true);
			setText(view, R.id.routeItem_det_country_tV, routeItem.getAddress().getCountryName(), false);
			setText(view, R.id.routeItem_det_adminArea_tV, routeItem.getAddress().getAdminArea(), false);
			setText(view, R.id.routeItem_det_city_tV, routeItem.getAddress().getAddressLine(0), false);
			setText(view, R.id.routeItem_det_thoroughfare_tV, routeItem.getAddress().getThoroughfare(), false);
			setText(view, R.id.routeItem_det_subthoroughfare_tV, routeItem.getAddress().getSubThoroughfare(), false);
			setText(view, R.id.routeItem_det_postalCode_tV, routeItem.getAddress().getPostalCode(), false);
			setText(view, R.id.routeItem_det_feature_tV, routeItem.getAddress().getFeatureName(), false);
			setText(view, R.id.routeItem_det_coordinatesLat_tV, String.valueOf(routeItem.getLatitude()), false);
			setText(view, R.id.routeItem_det_coordinatesLon_tV, String.valueOf(routeItem.getLongitude()), false);
		}
		
		db.close();		
	}
	
	/**
	 * Sets given text value to the given textView.
	 * @param view - fragment view
	 * @param tVId - textView Id (R number)
	 * @param text - text to set on the textView
	 * @param replaceText - if true - replaces all text, if false - appends text
	 */
	private void setText(View view, int tVId, String text, boolean replaceText)
	{
		TextView textView = (TextView) view.findViewById(tVId);
		if(textView == null || text == null || text.isEmpty())
			return;
		
		if(replaceText)
		{
			textView.setText(text);
		}
		else {
			textView.append(" " + text);
		}
	}
}
