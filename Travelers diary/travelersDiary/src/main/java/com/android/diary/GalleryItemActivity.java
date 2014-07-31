package com.android.diary;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class GalleryItemActivity extends FragmentActivity {
	
	public static final String KEY_IMAGE_ARRAY = "ImageArray";
	public static final String KEY_ROUTE_ID = "RouteId";
	public static final String KEY_ROUTE_ITEM_ID = "RouteItemId";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_gallery_item);
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		
		Bundle bundle = getIntent().getExtras();
		
		GalleryItemPager fragment = GalleryItemPager.newInstance(bundle.getStringArray(KEY_IMAGE_ARRAY), bundle.getInt(KEY_ROUTE_ID, 0), bundle.getInt(KEY_ROUTE_ITEM_ID, 0));
		ft.add(R.id.galleryItemFragment, fragment);
		ft.commit();
	}
}
