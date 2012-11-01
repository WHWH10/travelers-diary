package com.android.diary;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.android.diary.R;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private MainActivity mContext;
	private long systemTime = System.currentTimeMillis();

	public MapOverlay(Drawable marker, MainActivity context) {
		super(boundCenterBottom(marker));
		mContext = context;
		populate();
	}

	@Override
	protected OverlayItem createItem(int index) {
		return mOverlays.get(index);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
	
	public void clear()
	{
		mOverlays.clear();
		populate();
	}

	public void addOverlayItem(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}
	
	public void refresh()
	{
		populate();
	}
	
	@Override
	protected boolean onTap(int index) {
		if(mOverlays.isEmpty())
			return false;
		final int ind = index;
//		if(item.getTitle().equals("") || item.getSnippet().equals(""))
//		{
//			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
//			dialog.setNeutralButton(R.string.dialog_addInfo, new OnClickListener() {
//				
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					mContext.onInfoPressed(ind);
//				}
//			});
//			dialog.show();
//			return false;
//		}
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setIcon(R.drawable.mark_empty);
		dialog.setTitle(mContext.getText(R.string.select_action));
		dialog.setNeutralButton(R.string.dialog_edit, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mContext.onEditPressed(ind);
			}
		});
		dialog.setNegativeButton(R.string.dialog_cancel, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		dialog.setPositiveButton(R.string.dialog_details, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mContext.onDetailsPressed(ind);
			}
		});
		dialog.show();
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView map) {
		switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if ((System.currentTimeMillis() - systemTime) < ViewConfiguration.getDoubleTapTimeout()) {
                map.getController().zoomInFixing((int) event.getX(), (int) event.getY());
            }
            break;
        case MotionEvent.ACTION_UP:
            systemTime = System.currentTimeMillis();
            break;
        }

        return false;

	}
	
}
