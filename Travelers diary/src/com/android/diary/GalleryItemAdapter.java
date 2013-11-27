package com.android.diary;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class GalleryItemAdapter extends FragmentPagerAdapter {

	private String[] images;
	public GalleryItemAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public void setImages(String[] images){
		this.images = images;
	}
	
	@Override
	public Fragment getItem(int position) {
		return GalleryItemFragment.newInstance(images[position]);
	}

	@Override
	public int getCount() {
		return images.length;
	}
}
