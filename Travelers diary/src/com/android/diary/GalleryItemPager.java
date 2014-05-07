package com.android.diary;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GalleryItemPager extends Fragment {
//	private static final String LOG_TAG = "GALLERY ITEM PAGER";
	
	private String[] imageArray;
	private int routeId;
	private int routeItemId;
	private ViewPager viewPager;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.gallery_item, container,  false);
		
		viewPager = (ViewPager) view.findViewById(R.id.galleryItemPager);
		
		imageArray = getArguments().getStringArray(GalleryItemActivity.KEY_IMAGE_ARRAY);
		routeId = getArguments().getInt(GalleryItemActivity.KEY_ROUTE_ID);
		routeItemId = getArguments().getInt(GalleryItemActivity.KEY_ROUTE_ITEM_ID);
		
		GalleryItemAdapter adapter = new GalleryItemAdapter(getActivity().getSupportFragmentManager());
		adapter.setImages(imageArray);
		adapter.setRouteId(routeId);
		adapter.setRouteItemId(routeItemId);
		viewPager.setAdapter(adapter);
		
		return view;
	}
	
	public static final GalleryItemPager newInstance(String[] images, int routeId, int routeItemId)
	{
		GalleryItemPager fragment = new GalleryItemPager();
		Bundle bundle = new Bundle();
		bundle.putStringArray(GalleryItemActivity.KEY_IMAGE_ARRAY, images);
		bundle.putInt(GalleryItemActivity.KEY_ROUTE_ID, routeId);
		bundle.putInt(GalleryItemActivity.KEY_ROUTE_ITEM_ID, routeItemId);
		fragment.setArguments(bundle);
		return fragment;
	}
}
