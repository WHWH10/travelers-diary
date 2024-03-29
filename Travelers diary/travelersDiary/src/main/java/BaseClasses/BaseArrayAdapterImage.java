package BaseClasses;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.android.diary.DatabaseHandler;
import com.android.diary.R;

import Helpers.AsyncDrawable;
import Helpers.BitmapWorkerTask;
import Helpers.IImageClickListener;
import Helpers.IImageLoadedListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BaseArrayAdapterImage extends ArrayAdapter<Map<String, String>> {
	static class RouteListViewHolder {
		TextView title;
		TextView date;
		ImageView image;
	}
	
	protected final Context context;
	protected final List<Map<String, String>> values;
	private LruCache<String, Bitmap> memoryCache;
	private List<IImageClickListener> imageClickListeners;
	
	public BaseArrayAdapterImage(Context context, List<Map<String, String>> values) {
		super(context, R.layout.list_item_with_image, values);
		this.context = context;
		this.values = values;
		
		imageClickListeners = new ArrayList<IImageClickListener>();
		
		this.memoryCache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 1024 / 8)){
			@Override
			protected int sizeOf(String key, Bitmap bitmap){
				return bitmap.getByteCount()/1024;
			}
		};
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final RouteListViewHolder viewHolder;
		
		if(convertView == null || convertView.getTag() == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_item_with_image,  parent, false);
			
			viewHolder = new RouteListViewHolder();
			viewHolder.title = (TextView) convertView.findViewById(R.id.list_item_title);
			viewHolder.date = (TextView) convertView.findViewById(R.id.list_item_date);
			viewHolder.image = (ImageView) convertView.findViewById(R.id.list_item_image);
			
			convertView.setTag(viewHolder);
		}
		else{
			viewHolder = (RouteListViewHolder) convertView.getTag();
		}
		
		viewHolder.title.setText(values.get(position).get("title"));
		viewHolder.date.setText(values.get(position).get("date"));
		
		String imagePath = getImagePathFromDatabase(position);
		if(imagePath != null && imagePath != "")
		{
			final File img = new File(imagePath);
			if(img != null && img.exists())
			{
				if(getBitMapFromMemoryCache(img.getName()) != null)
				{
					viewHolder.image.setImageBitmap(getBitMapFromMemoryCache(img.getName()));					
				}
				else {
					if (BitmapWorkerTask.cancelPotentialWork(img.getAbsolutePath(), viewHolder.image)) {
						final BitmapWorkerTask task = new BitmapWorkerTask(this.context, viewHolder.image, position, img.getName(), true);
						final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), null, task);
						viewHolder.image.setImageDrawable(asyncDrawable);
						task.setOnImageLoadedListener(new IImageLoadedListener() {
							
							public void imageLoaded(String imageName, Bitmap bitmap, int position) {
								addBitmapToMemoryCache(imageName, bitmap, position);
							}
						});
						
						task.execute(img.getAbsolutePath());		
					}
				}							
			}
			else {
				viewHolder.image.setImageResource(R.drawable.ic_launcher);
			}
		}
		else
			viewHolder.image.setImageResource(R.drawable.ic_launcher);
		
		viewHolder.image.setTag(position);
		viewHolder.image.setOnClickListener(new OnClickListener() {						
			public void onClick(View v) {
				throwImageClicked(Integer.parseInt(v.getTag().toString()));
			}
		});
		
		return convertView;
	}	
	
	protected String getImagePathFromDatabase(int position){
		DatabaseHandler db = new DatabaseHandler(this.context);
		String imagePath = db.getDefaultRouteImage(Integer.parseInt(values.get(position).get("id")));
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
	
	public void setOnImageClickListener(IImageClickListener imageClickListener){
		imageClickListeners.add(imageClickListener);
	}
	
	private void throwImageClicked(int id){
		for (IImageClickListener listener : imageClickListeners) {
			listener.imageClicked(id);
		}
	}
}
