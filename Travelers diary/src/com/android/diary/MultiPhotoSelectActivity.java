package com.android.diary;

import java.util.ArrayList;

import BaseClasses.BaseImageLoader;
import android.content.Context;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
 
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
    
    public static final String ROUTE_ID = "routeId";
    public static final String ROUTE_ITEM_ID = "routeItemId";
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_image_grid);
        
        loadData();
        
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        
        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        Cursor imagecursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy + " DESC");
 
        this.imageUrls = new ArrayList<String>();
 
        for (int i = 0; i < imagecursor.getCount(); i++) {
            imagecursor.moveToPosition(i);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
            imageUrls.add(imagecursor.getString(dataColumnIndex));
        }
 
        options = new DisplayImageOptions.Builder()
            .showStubImage(R.drawable.ic_launcher)
            .showImageForEmptyUri(R.drawable.ic_launcher)
            .cacheInMemory()
            .cacheOnDisc()
            .build();
 
        imageAdapter = new ImageAdapter(this, imageUrls);
 
        GridView gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(imageAdapter);
    }
    
    private void loadData(){
    	Bundle bundle = getIntent().getExtras();
    	if(bundle != null)
        {
        	routeId = bundle.getInt(ROUTE_ID);
        	routeItemId = bundle.getInt(ROUTE_ITEM_ID);
        }
    	else {
			LogErrorMessage(LOG_TAG, "No data passed!");
			this.finish();
		}
    	
    	if(routeId == 0 && routeItemId == 0){
    		LogErrorMessage(LOG_TAG, "Route and routeItem ids are empty!");
    		this.finish();
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
 
        public ImageAdapter(Context context, ArrayList<String> imageList) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mSparseBooleanArray = new SparseBooleanArray();
            mList = new ArrayList<String>();
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
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.multiphoto_imgView);
 
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
