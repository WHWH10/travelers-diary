package com.android.diary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.diary.R;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

public class RoutesActivity extends ListActivity{
	
	private static final String LOG_TAG = "ROUTES ACTIVITY";
	private Intent myService;

	private ListView listView;
	private List<Route> routes;
	private int itemSelected;
	private String title;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routes);
		
		this.listView = (ListView) findViewById(android.R.id.list);
		this.itemSelected = 0;
		this.title = "";
		
		prepareList();
		
		registerForContextMenu(listView);
	}
	
	private void prepareList()
	{		
		DatabaseHandler db = new DatabaseHandler(this);
		this.routes = db.getRoutes();
		db.close();
		
		if(this.routes == null || this.routes.isEmpty())
			return;	
				
		List<Map<String, String>> g = new ArrayList<Map<String, String>>();
		
		for (int i = 0; i < this.routes.size(); i++) {
			Map<String, String> map = new HashMap<String, String>();
									
			String routeTitle = this.routes.get(i).getTitle();
			
			if(routeTitle == null || routeTitle.isEmpty())
				routeTitle = getString(R.string.unnamed);
			map.put("route", routeTitle);
			map.put("date", getResources().getString(R.string.date) + ": " + this.routes.get(i).getDateCreated());
			map.put("routeId", String.valueOf(routes.get(i).getRouteId()));
			g.add(map);
		}
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		RouteListAdapter adapter = new RouteListAdapter(this, R.layout.route_list_item, g, displayMetrics, IsOrientationPortrait());
		setListAdapter(adapter);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_changeRouteTtitle:
			changeRouteTitle();
			break;
		case R.id.menu_deleteRoute:
			deleteRoute();
			break;
			
		case R.id.menu_showRouteDetails:
			showRouteDetail();
			break;
			
		case R.id.menu_addImage:
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(intent, 1);
			break;
			
		case R.id.menu_showRouteOnMap:
			showRouteOnMap();			
			break;
			
		case R.id.menu_startTracking:
			if(this.routes == null || this.routes.size() <= itemSelected)
				break;
						
			myService = new Intent(this, LocationProviderService.class);
			myService.putExtra(LocationProviderService.ROUTE_ID, this.routes.get(itemSelected).getRouteId());
			startService(myService);
			break;
			
		case R.id.menu_stopTracking:
			if(isMyServiceRunning() && myService != null)
			{
				stopService(myService);
			}
			else
			{
				myService = new Intent(this, LocationProviderService.class);
				startService(myService);
				stopService(myService);			
			}
			break;

		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK)
		{
			Log.i(LOG_TAG, getImagePathFromURI(data.getData()));
			DatabaseHandler db = new DatabaseHandler(this);
			db.insertImage(routes.get(itemSelected).getRouteId(), 0, getImagePathFromURI(data.getData()));
			Log.i(LOG_TAG, db.getImageByRouteId(routes.get(itemSelected).getRouteId()));
			db.close();
		}
	}
	
	public String getImagePathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_routes_context, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		itemSelected = position;
		openContextMenu(listView);
		super.onListItemClick(l, v, position, id);
	}
	
	private void showRouteOnMap()
	{
		if(this.routes == null || this.routes.size() <= itemSelected)
		{
			return;
		}
		
		itemSelected = this.routes.get(itemSelected).getRouteId();
		Intent intent = new Intent(this, MapActivity.class);
		intent.putExtra(MapActivity.ROUTE_ID, itemSelected);
		startActivity(intent);		
		
		this.finish();
	}
	
	private void showRouteDetail()
	{
		Intent intent = new Intent(getApplicationContext(), RouteDetailActivity.class);
		if(this.routes != null && this.routes.size() > itemSelected)
		{
			intent.putExtra(RouteDetailFragment.ROUTE_ID, this.routes.get(itemSelected).getRouteId());
		}
		startActivity(intent);
	}

	private void changeRouteTitle()
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		final EditText editText = new EditText(this);
		editText.setHint(R.string.hint_title);
		editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		final InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		
		dialog.setView(editText);
		dialog.setNeutralButton(R.string.dialog_set, new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				title = editText.getText().toString();
				changeTitle();
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}
		});
		dialog.setNegativeButton(R.string.dialog_cancel, new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		dialog.show();		
	}
	
	public void addRoute(View view)
	{
		EditText et = (EditText) findViewById(R.id.routes_title_editText);
		if(et.getText().length() > 0)
		{
			ContentValues contentValues = new ContentValues();
			contentValues.put(DatabaseHandler.KEY_TITLE, et.getText().toString());
			
			DatabaseHandler db = new DatabaseHandler(this);
			db.insertRoute(contentValues);
			db.close();
			
			prepareList();
		}
	}
	
	private void changeTitle()
	{
		if(!title.isEmpty() && this.routes != null && itemSelected < this.routes.size())
		{
			ContentValues contentValues = new ContentValues();
			contentValues.put(DatabaseHandler.KEY_TITLE, title);
			
			DatabaseHandler db = new DatabaseHandler(this);
			db.updateRoute(contentValues, this.routes.get(itemSelected).getRouteId());
			db.close();
			prepareList();
		}		
	}
	
	private void deleteRoute()
	{
		if(this.routes != null && this.routes.size() > itemSelected)
		{				
			DatabaseHandler db = new DatabaseHandler(this);
			db.deleteRoute(this.routes.get(itemSelected).getRouteId());
			db.close();
			prepareList();			
		}	
	}
	
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	
	        if ("com.android.diary.LocationProviderService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public boolean IsOrientationPortrait()
	{
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}
}
