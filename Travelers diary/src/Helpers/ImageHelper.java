package Helpers;

import com.android.diary.Config;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;

public class ImageHelper {
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {

	        // Calculate ratios of height and width to requested height and width
	        final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);

	        // Choose the smallest ratio as inSampleSize value, this will guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }

	    return inSampleSize;
	}
		
	public static Bitmap decodeSampledBitmapFromResource(String file, DisplayMetrics metrics, boolean isOrientationPortrait) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(file, options);

	    // Calculate inSampleSize
	    options.inSampleSize = getImageSizeForListView(options, metrics, isOrientationPortrait);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(file, options);
	}
	
	public static int getImageSizeForListView(BitmapFactory.Options options, DisplayMetrics metrics, boolean isOrientationPortrait)
	{
		int inSampleSize = 1;
		if(isOrientationPortrait)
		{
			int width = (int) (((double)metrics.widthPixels) * Config.IMAGE_SCALE_X);
			int height = (int) (((double)metrics.heightPixels) * Config.IMAGE_SCALE_Y / 5);
			
			if (options.outHeight > height || options.outWidth > width) {

		        // Calculate ratios of height and width to requested height and width
		        int heightRatio = Math.round((float) options.outHeight / (float) height);
		        int widthRatio = Math.round((float) options.outWidth / (float) width);

		        // Choose the smallest ratio as inSampleSize value, this will guarantee
		        // a final image with both dimensions larger than or equal to the
		        // requested height and width.
		        inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
		    }			
		}
		else {
			int width = (int) (((double)metrics.widthPixels) * Config.IMAGE_SCALE_X);
			int height = (int) (((double)metrics.heightPixels) * Config.IMAGE_SCALE_Y / 5);
			
			if (options.outHeight > height || options.outWidth > width) {

		        // Calculate ratios of height and width to requested height and width
		        int heightRatio = Math.round((float) options.outHeight / (float) height);
		        int widthRatio = Math.round((float) options.outWidth / (float) width);

		        // Choose the smallest ratio as inSampleSize value, this will guarantee
		        // a final image with both dimensions larger than or equal to the
		        // requested height and width.
		        inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
			}
		}
		return inSampleSize;
	}
}
