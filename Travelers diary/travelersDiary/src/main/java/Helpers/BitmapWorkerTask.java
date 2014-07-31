package Helpers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.android.diary.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap>{

	private final WeakReference<ImageView> imageViewReference;
	private Context context;
	private String imagePath = "";
	private int position = 0;
	private String imageName;
	private List<IImageLoadedListener> imageLoadedListeners;
	private DisplayMetrics displayMetrics;
	private boolean isList;
	
	public BitmapWorkerTask(Context context, ImageView imageView, int position, String imageName, boolean isList){
		this.context = context;
		this.imageViewReference = new WeakReference<ImageView>(imageView);
		this.position = position;
		this.imageName = imageName;
		this.imageLoadedListeners = new ArrayList<IImageLoadedListener>();
		this.isList = isList;
		
		this.displayMetrics = new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
	}
	
	public BitmapWorkerTask(Context context, ImageView imageView, boolean isList){
		this.context = context;
		this.imageViewReference = new WeakReference<ImageView>(imageView);
		this.position = 0;
		this.imageName = "";
		this.imageLoadedListeners = new ArrayList<IImageLoadedListener>();
		this.isList = isList;
		
		this.displayMetrics = new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
	}
	
	@Override
	protected Bitmap doInBackground(String... imagePaths) {
		imagePath = imagePaths[0];
		if(isList)
			return ImageHelper.decodeSampledBitmapFromResource(imagePath, displayMetrics, IsOrientationPortrait());
		else 
			return ImageHelper.loadImage(imagePath, context);
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if(isCancelled()){
			bitmap = null;
		}
		
		if(imageViewReference != null && bitmap != null){
			final ImageView imageView = imageViewReference.get();
			final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
			
			if(this == bitmapWorkerTask && imageView != null){
				Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_in);
				imageView.setImageBitmap(bitmap);
                imageView.setAnimation(anim);
                anim.start();
                throwImageLoaded(this.imageName, bitmap, this.position);
			}
		}
	}
	
	public void setOnImageLoadedListener(IImageLoadedListener listener){
		imageLoadedListeners.add(listener);
	}
	
	private void throwImageLoaded(String imageName, Bitmap bitmap, int position){
		for (IImageLoadedListener listener : imageLoadedListeners) {
			listener.imageLoaded(imageName, bitmap, position);
		}
	}

	public static boolean cancelPotentialWork(String imagePath, ImageView imageView) {
	    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

	    if (bitmapWorkerTask != null) {
	        final String imagePathData = bitmapWorkerTask.imagePath;
	        // If bitmapData is not yet set or it differs from the new data
	        if (imagePathData == "" || imagePathData != imagePath) {
	            // Cancel previous task
	            bitmapWorkerTask.cancel(true);
	        } else {
	            // The same work is already in progress
	            return false;
	        }
	    }
	    // No task associated with the ImageView, or an existing task was cancelled
	    return true;
	}
	
	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
	   if (imageView != null) {
	       final Drawable drawable = imageView.getDrawable();
	       if (drawable instanceof AsyncDrawable) {
	           final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
	           return asyncDrawable.getBitmapWorkerTask();
	       }
	    }
	    return null;
	}
	
	public boolean IsOrientationPortrait()
	{
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}
}
