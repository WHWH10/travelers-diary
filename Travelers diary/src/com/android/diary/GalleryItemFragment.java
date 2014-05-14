package com.android.diary;

import java.util.ArrayList;

import Helpers.AsyncDrawable;
import Helpers.BitmapWorkerTask;
import Helpers.IImageDeletedListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
	public static final String KEY_ROUTE_ID = "routeId";
	public static final String KEY_ROUTE_ITEM_ID = "routeItemId";
	
	private ArrayList<IImageDeletedListener> listeners = new ArrayList<IImageDeletedListener>();
	
	private String image;
	private int routeId;
	private int routeItemId;
	
	public static final GalleryItemFragment newInstance(String image, int routeId, int routeItemId)
	{
		GalleryItemFragment fragment = new GalleryItemFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KEY_IMAGE, image);
		bundle.putInt(KEY_ROUTE_ID, routeId);
		bundle.putInt(KEY_ROUTE_ITEM_ID, routeItemId);
		fragment.setArguments(bundle);
		
		return fragment;
	}	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ImageView imageView = new ImageView(getActivity());
		
		image = getArguments() == null ? "" : getArguments().getString(KEY_IMAGE);
		routeId = getArguments() == null ? 0 : getArguments().getInt(KEY_ROUTE_ID, 0);
		routeItemId = getArguments() == null ? 0 : getArguments().getInt(KEY_ROUTE_ITEM_ID);
		
		if (BitmapWorkerTask.cancelPotentialWork(image, imageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(getActivity(), imageView, false);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(getActivity().getResources(), null, task);
			imageView.setImageDrawable(asyncDrawable);
			
			task.execute(image);		
		}
		
		View view = inflater.inflate(R.layout.gallery_item_fragment, container, false);
		LinearLayout layout = (LinearLayout)view.findViewById(R.id.galleryItemLayout);
		
		layout.addView(imageView);
		setHasOptionsMenu(true);
		return view;
	}
	
	public void setOnImageDeletedListener(IImageDeletedListener listener){
		this.listeners.add(listener);
	}
	
	private void notifyImageDeleted(String imageName){
		for (IImageDeletedListener listener : this.listeners) {
			listener.imageDeleted(imageName);
		}
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
		
		case R.id.menu_deleteImg:
			createConfirmDialog();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void createConfirmDialog()
    {
    	new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.dialog_confirmation)).setMessage(getString(R.string.dialog_routeDelete))
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		
			public void onClick(DialogInterface dialog, int which) {
				DatabaseHandler db = new DatabaseHandler(getActivity());
				db.deleteImage(image, routeId, routeItemId);
				db.close();
				
				notifyImageDeleted(image);
			}
		}).setNegativeButton(android.R.string.no, null).show();
    }
	
	public String getImage(){
		return image;
	}
}
