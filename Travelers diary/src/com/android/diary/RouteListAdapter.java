package com.android.diary;

import java.io.File;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RouteListAdapter extends ArrayAdapter<Map<String, String>> {
	
	static class RouteListViewHolder {
		TextView title;
		TextView date;
		ImageView image;
	}
	
	private final Context context;
	private final List<Map<String, String>> values;
	private final int resource;
	private final DisplayMetrics displayMetrics;
	private boolean isOrientationPortrait;
	private LruCache<String, Bitmap> memoryCache;

	public RouteListAdapter(Context context, int resource, List<Map<String, String>> values, DisplayMetrics displayMetrics, boolean isOrientationPortrait) {
		super(context, resource, values);
		this.context = context;
		this.values = values;
		this.resource = resource;
		this.displayMetrics = displayMetrics;
		this.isOrientationPortrait = isOrientationPortrait;
		
		this.memoryCache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 1024 / 8)){
			@Override
			protected int sizeOf(String key, Bitmap bitmap){
				return bitmap.getByteCount()/1024;
			}
		};
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RouteListViewHolder viewHolder;
		
		if(convertView == null || convertView.getTag() == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resource,  parent, false);
			
			viewHolder = new RouteListViewHolder();
			viewHolder.title = (TextView) convertView.findViewById(R.id.route_item_title);
			viewHolder.date = (TextView) convertView.findViewById(R.id.route_item_date);
			viewHolder.image = (ImageView) convertView.findViewById(R.id.route_item_image);
			
			convertView.setTag(viewHolder);
		}
		else{
			viewHolder = (RouteListViewHolder) convertView.getTag();
		}
		
		viewHolder.title.setText(values.get(position).get("route"));
		viewHolder.date.setText(values.get(position).get("date"));
		
		String imagePath = getImagePathFromDatabase(position);
		if(imagePath != "")
		{
			File img = new File(imagePath);
			if(img.exists())
			{
				if(getBitMapFromMemoryCache(img.getName()) != null)
				{
					viewHolder.image.setImageBitmap(getBitMapFromMemoryCache(img.getName()));
				}
				else {
					Bitmap bitmap = ImageHelper.decodeSampledBitmapFromResource(img.getAbsolutePath(), this.displayMetrics, this.isOrientationPortrait);
					addBitmapToMemoryCache(img.getName(), bitmap, position);
					viewHolder.image.setImageBitmap(bitmap);
				}								
			}
			else {
				viewHolder.image.setImageResource(R.drawable.ic_launcher);
			}
		}
		else
			viewHolder.image.setImageResource(R.drawable.ic_launcher);
		
		return convertView;
	}
	
	private String getImagePathFromDatabase(int position){
		DatabaseHandler db = new DatabaseHandler(this.context);
		String imagePath = db.getImageByRouteId(Integer.parseInt(values.get(position).get("routeId")));
		db.close();
		return imagePath;
	}
	
	private void addBitmapToMemoryCache(String key, Bitmap bitmap, int position)
	{
		try {
			if(getBitMapFromMemoryCache(key) == null)
			{
				this.memoryCache.put(key, bitmap);
			}
		} catch (OutOfMemoryError e) {
			if(this.getCount() / 2 > position)
			{
				String imagePath = getImagePathFromDatabase(0);
				if(imagePath != ""){
					File file = new File(imagePath);
					if(file.exists()){
						this.memoryCache.get(file.getName()).recycle();
					}
				}
			}
			else {
				String imagePath = getImagePathFromDatabase(this.getCount()-1);
				if(imagePath != ""){
					File file = new File(imagePath);
					if(file.exists()){
						this.memoryCache.get(file.getName()).recycle();
					}
				}
			}
		}		
	}
	
	private Bitmap getBitMapFromMemoryCache(String key){
		return this.memoryCache.get(key);
	}
}
