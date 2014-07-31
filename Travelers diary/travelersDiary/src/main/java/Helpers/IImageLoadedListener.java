package Helpers;

import android.graphics.Bitmap;

public interface IImageLoadedListener {
	public void imageLoaded(String imageName, Bitmap bitmap, int position);
}
