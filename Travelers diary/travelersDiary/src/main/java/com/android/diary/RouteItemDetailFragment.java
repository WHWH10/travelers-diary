package com.android.diary;

import BaseClasses.BaseFragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.diary.R;

public class RouteItemDetailFragment extends BaseFragment {

	public static final String ROUTE_ITEM_ID = "routeItemId";
	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(container == null)
			return null;
		
		view = inflater.inflate(R.layout.fragment_route_item_details, container, false);
				
		if(savedInstanceState == null)
			setHasOptionsMenu(true);
		return view;
	}
	
	@Override
	public void onStart() {
		setValues(view);
		super.onStart();
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
		case R.id.menu_deleteRouteItem:
			createConfirmDialog();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void createConfirmDialog()
    {
    	new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.dialog_confirmation)).setMessage(getString(R.string.dialog_routeItemDelete))
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		
			public void onClick(DialogInterface dialog, int which) {
				DatabaseHandler db = new DatabaseHandler(getActivity());				
				db.deleteRouteItem(getRouteItemID());
				db.close();
				getActivity().finish();
			}
		}).setNegativeButton(android.R.string.no, null).show();
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
			setText(view, R.id.routeItem_det_date_tV, DateFormat.getDateFormat(getActivity()).format(routeItem.getDateCreated()) + " " + 
					DateFormat.getTimeFormat(getActivity()).format(routeItem.getDateCreated()), true);
			setText(view, R.id.routeItem_det_country_tV, routeItem.getAddress().getCountryName(), true);
			setText(view, R.id.routeItem_det_city_tV, routeItem.getAddress().getLocality(), true);
			setText(view, R.id.routeItem_det_street_tV, routeItem.getAddress().getAddressLine(0), true);
			setText(view, R.id.routeItem_det_postalCode_tV, routeItem.getAddress().getPostalCode(), true);
			setText(view, R.id.routeItem_det_coordinatesLat_tV, String.valueOf(routeItem.getLatitude()), true);
			setText(view, R.id.routeItem_det_coordinatesLon_tV, String.valueOf(routeItem.getLongitude()), true);
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
