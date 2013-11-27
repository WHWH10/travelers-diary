package com.android.diary;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GalleryItemPager extends Fragment {
//	private static final String LOG_TAG = "GALLERY ITEM PAGER";
	public static final String KEY_IMAGE_ARRAY = "ImageArray";
	
	private String[] imageArray;
	private ViewPager viewPager;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.gallery_item, container,  false);
		
		viewPager = (ViewPager) view.findViewById(R.id.galleryItemPager);
		
		imageArray = getArguments().getStringArray(KEY_IMAGE_ARRAY);
		GalleryItemAdapter adapter = new GalleryItemAdapter(getActivity().getSupportFragmentManager());
		adapter.setImages(imageArray);
		viewPager.setAdapter(adapter);
		
		return view;
	}
	
	public static final GalleryItemPager newInstance(String[] images)
	{
		GalleryItemPager fragment = new GalleryItemPager();
		Bundle bundle = new Bundle();
		bundle.putStringArray(KEY_IMAGE_ARRAY, images);
		fragment.setArguments(bundle);
		return fragment;
	}
}
