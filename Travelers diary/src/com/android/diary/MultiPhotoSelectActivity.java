package com.android.diary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BaseClasses.BaseImageLoader;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
 
public class MultiPhotoSelectActivity extends BaseImageLoader {
 
	private static final String LOG_TAG = "MULTI_PHOTO_SELECT_ACTIVITY";
	
    private ArrayList<String> imageUrls;
    private DisplayImageOptions options;
    private ImageAdapter imageAdapter;
    
    private int routeId;
    private int routeItemId;
    private boolean isGallery;
    
    public static final String ROUTE_ID = "routeId";
    public static final String ROUTE_ITEM_ID = "routeItemId";
    public static final String SHOW_GALLERY = "galleryView";
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_image_grid);
        
        loadData();
        handleButtonVisibility();
        
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        
        loadImages();
        
        if(imageUrls == null || imageUrls.isEmpty())
        {
        	ToastMessage(getString(R.string.warn_noImagesToDisplay));
        	this.finish();
        }
 
        options = new DisplayImageOptions.Builder()
            .showStubImage(R.drawable.ic_launcher)
            .showImageForEmptyUri(R.drawable.ic_launcher)
            .cacheInMemory()
            .cacheOnDisc()
            .build();
 
        imageAdapter = new ImageAdapter(this, imageUrls, isGallery);
 
        GridView gridView = (GridView) findViewById(R.id.gridview);        
        gridView.setAdapter(imageAdapter);        
    }
    
    public void onImageClick(View view)
    {
    	if(isGallery)
    	{
    		Intent intent = new Intent(this, GalleryItemActivity.class);    		
    		
    		ImageView imageView = (ImageView)((RelativeLayout) view.getParent()).findViewById(R.id.multiphoto_imgView);
    		    		
    		int pos = Integer.parseInt(imageView.getTag().toString());
    		
    		String[] array = new String[imageUrls.size()];
    		int couner = 0;
    		for (int i = pos; i < imageUrls.size(); i++) {
				array[couner] = imageUrls.get(i);
				couner++;
			}
    		
    		for (int i = 0; i < pos; i++) {
				array[couner] = imageUrls.get(i);
				couner++;
			}

    		intent.putExtra(GalleryItemActivity.KEY_IMAGE_ARRAY, array);
    		intent.putExtra(GalleryItemActivity.KEY_ROUTE_ID, routeId);
    		intent.putExtra(GalleryItemActivity.KEY_ROUTE_ITEM_ID, routeItemId);
    		startActivity(intent);
    	}
    	else
    	{
    		CheckBox checkBox = (CheckBox)((RelativeLayout) view.getParent()).findViewById(R.id.multiphoto_chk);
        	checkBox.setChecked(!checkBox.isChecked());
    	}
    }
    
    private void loadData(){
    	isGallery = false;
    	
    	Bundle bundle = getIntent().getExtras();
    	if(bundle != null)
        {
        	routeId = bundle.getInt(ROUTE_ID);
        	routeItemId = bundle.getInt(ROUTE_ITEM_ID);
        	isGallery = bundle.getBoolean(SHOW_GALLERY);
        }
    	else {
			LogErrorMessage(LOG_TAG, "No data passed!");
			this.finish();
		}
    	
    	if(routeId == 0 && routeItemId == 0 && !isGallery){
    		LogErrorMessage(LOG_TAG, "Route and routeItem ids are empty!");
    		this.finish();
    	}
    }
    
    private void loadImages()
    {
    	final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        
        Cursor imagecursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy + " DESC");
 
        this.imageUrls = new ArrayList<String>();
        
        if(isGallery)
        	addImagesForGallery(imagecursor);
        else
        	addImagesForSelection(imagecursor);        
    }
    
    private void addImagesForGallery(Cursor cursor)
    {
    	DatabaseHandler db = new DatabaseHandler(this);    	
    	List<String> imageList = db.getImagesPath(routeId, routeItemId);    	
    	db.close();
    	
    	String[] images = new String[imageList.size()];    	
    	imageList.toArray(images);
    	    	
    	Arrays.sort(images);
    	
    	for (int i = 0; i < cursor.getCount(); i++) {
    		cursor.moveToPosition(i);
            int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            
            if(Arrays.binarySearch(images, cursor.getString(dataColumnIndex)) >= 0)
            	imageUrls.add(cursor.getString(dataColumnIndex));
        }
    }
    
    private void addImagesForSelection(Cursor cursor)
    {
    	for (int i = 0; i < cursor.getCount(); i++) {
    		cursor.moveToPosition(i);
            int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            imageUrls.add(cursor.getString(dataColumnIndex));
        }
    }
    
    private void handleButtonVisibility()
    {
    	if(isGallery){
    		((Button)findViewById(R.id.image_grid_btnAddPhotos)).setVisibility(View.GONE);
    	}
    }
 
    @Override
    protected void onStop() {
        imageLoader.stop();
        super.onStop();
    }
 
    public void btnChoosePhotosClick(View v){
    	try {
    		ArrayList<String> selectedItems = imageAdapter.getCheckedItems();            
            DatabaseHandler db = new DatabaseHandler(this);
            
            for (String imagePath : selectedItems) {
    			db.insertImage(routeId, routeItemId, imagePath);
    		}
            
            db.close();            
            ToastMessage(getString(R.string.multi_photo_select_success));
            this.finish();
            
		} catch (Exception e) {
			LogErrorMessage(LOG_TAG, e.toString());
		}
    }
 
    public class ImageAdapter extends BaseAdapter {
 
        ArrayList<String> mList;
        LayoutInflater mInflater;
        Context mContext;
        SparseBooleanArray mSparseBooleanArray;
        boolean isGallery;
 
        public ImageAdapter(Context context, ArrayList<String> imageList, boolean isGallery) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mSparseBooleanArray = new SparseBooleanArray();
            mList = new ArrayList<String>();
            this.isGallery = isGallery;
            this.mList = imageList;
        }
 
        public ArrayList<String> getCheckedItems() {
            ArrayList<String> mTempArry = new ArrayList<String>();
 
            for(int i=0;i<mList.size();i++) {
                if(mSparseBooleanArray.get(i)) {
                    mTempArry.add(mList.get(i));
                }
            }
 
            return mTempArry;
        }
 
        public int getCount() {
            return imageUrls.size();
        }
 
        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
 
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.row_multiphoto_item, null);
            }
 
            CheckBox mCheckBox = (CheckBox) convertView.findViewById(R.id.multiphoto_chk);
            mCheckBox.setVisibility(!isGallery ? View.VISIBLE : View.GONE);
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.multiphoto_imgView);
            imageView.setTag(position);
 
            imageLoader.displayImage("file://"+imageUrls.get(position), imageView, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(Bitmap loadedImage) {
                    Animation anim = AnimationUtils.loadAnimation(MultiPhotoSelectActivity.this, R.anim.fade_in);
                    imageView.setAnimation(anim);
                    anim.start();
                }
            });
 
            mCheckBox.setTag(position);
            mCheckBox.setChecked(mSparseBooleanArray.get(position));
            mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);
 
            return convertView;
        }
 
        OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
            }
        };
    }
}
