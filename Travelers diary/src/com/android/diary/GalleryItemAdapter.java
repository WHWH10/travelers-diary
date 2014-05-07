package com.android.diary;

import Helpers.IImageDeletedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

public class GalleryItemAdapter extends FragmentStatePagerAdapter {

	private String[] images;
	private int routeId;
	private int routeItemId;
	
	public GalleryItemAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public void setImages(String[] images){
		this.images = images;
	}
	
	public void setRouteId(int routeId){
		this.routeId = routeId;
	}
	
	public void setRouteItemId(int routeItemId){
		this.routeItemId = routeItemId;
	}
	
	@Override
	public Fragment getItem(int position) {
		GalleryItemFragment fragment = GalleryItemFragment.newInstance(images[position], routeId, routeItemId);
		fragment.setOnImageDeletedListener(new IImageDeletedListener() {
			
			public void imageDeleted(String imageName) {
				if(images.length > 0){
					String[] newArray = new String[images.length-1];
					int counter = 0;
					for (String image : images) {
						if(image != imageName){
							newArray[counter] = image;
							counter++;
						}
					}
					
					images = newArray;
				}
				
				notifyDataSetChanged();
			}
		});
		
		return fragment;
	}

	@Override
	public int getItemPosition(Object object) {
		int returnVal = POSITION_NONE;
		GalleryItemFragment fragment = (GalleryItemFragment)object;
		String image = fragment.getImage();
		for (int i = 0; i < images.length; i++) {
			if(images[i] == image){
				returnVal = i;
				break;
			}				
		}

		return returnVal;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);
		
		if(position < getCount()){
			FragmentManager manager = ((Fragment)object).getFragmentManager();
			if(manager != null){
				FragmentTransaction transaction = manager.beginTransaction();
				transaction.remove((Fragment)object);
				transaction.commit();
			}
		}		
	}

	@Override
	public int getCount() {
		return images.length;
	}
}
