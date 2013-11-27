package com.android.diary;

import Helpers.ImageHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public final class GalleryItemFragment extends Fragment
{	
	public static final String LOG_TAG = "GALLERY ITEM FRAGMENT";
	public static final String KEY_IMAGE = "image";
	
	private String image;
	
	public static final GalleryItemFragment newInstance(String image)
	{
		GalleryItemFragment fragment = new GalleryItemFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KEY_IMAGE, image);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ImageView imageView = new ImageView(getActivity());
		
		image = getArguments() == null ? "" : getArguments().getString(KEY_IMAGE);
		
		ImageHelper.loadImage(image, imageView, getActivity());
		
		View view = inflater.inflate(R.layout.gallery_item_fragment, container, false);
		LinearLayout layout = (LinearLayout)view.findViewById(R.id.galleryItemLayout);
		
		layout.addView(imageView);
		
		return view;
	}
}
