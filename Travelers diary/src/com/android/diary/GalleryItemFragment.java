package com.android.diary;

import Helpers.AsyncDrawable;
import Helpers.BitmapWorkerTask;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
		
		if (BitmapWorkerTask.cancelPotentialWork(image, imageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(getActivity(), imageView, false);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(getActivity().getResources(), null, task);
			imageView.setImageDrawable(asyncDrawable);
			
			task.execute(image);		
		}
		
		View view = inflater.inflate(R.layout.gallery_item_fragment, container, false);
		LinearLayout layout = (LinearLayout)view.findViewById(R.id.galleryItemLayout);
		
		layout.addView(imageView);
		
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_gallery_item, menu);
		super.onCreateOptionsMenu(menu, inflater);		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_shareImg:
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("image/*");
			intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(image));
			startActivity(Intent.createChooser(intent, ""));
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
