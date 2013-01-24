package com.android.diary;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RouteDetailFragment extends Fragment {
	
	public static final String ROUTE_ID = "routeId";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		if(container == null)
			return null;
		
		View view = inflater.inflate(R.layout.fragment_route_detail, container, false);
		
		setValues(view);
		return view;
	}
	
	public static RouteDetailFragment newInstance(int routeID)
	{
		RouteDetailFragment fragment = new RouteDetailFragment();
		
		Bundle args = new Bundle();
		args.putInt(ROUTE_ID, routeID);
		fragment.setArguments(args);
		return fragment;
	}
	
	private int getRouteID()
	{
		if(getArguments() == null)
			return 0;
		return getArguments().getInt(ROUTE_ID, 0);
	}
	
	private void setValues(View view)
	{
		DatabaseHandler db = new DatabaseHandler(getActivity());
		Route route = db.getRoute(getRouteID());
		db.close();
		
		if(route == null)
			return;
		
		TextView title = (TextView) view.findViewById(R.id.route_det_title_textView);
		title.setText(route.getTitle());
		
		TextView description = (TextView) view.findViewById(R.id.route_det_description_textView);
		description.setText(route.getDescription());

		TextView dateCreaded = (TextView) view.findViewById(R.id.route_det_created_textView);
		dateCreaded.setText(route.getDateCreated().toString());
		
		TextView dateModified = (TextView) view.findViewById(R.id.route_det_modified_textView);
		dateModified.setText(route.getDateModified().toString());
	}
}
