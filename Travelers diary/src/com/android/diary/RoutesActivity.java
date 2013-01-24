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
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class RoutesActivity extends ListActivity{
	
//	private static final String LOG_TAG = "ROUTES ACTIVITY";
	private Intent myService;

	private ListView listView;
	private List<Route> routes;
	private int itemSelected;
	private String title;
	private boolean showRoute;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routes);
		
		this.listView = (ListView) findViewById(android.R.id.list);
		this.itemSelected = 0;
		this.title = "";
		this.showRoute = false;
		
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
		
		String from[] = {"route", "date"};
		
		List<Map<String, String>> g = new ArrayList<Map<String, String>>();
		
		for (int i = 0; i < this.routes.size(); i++) {
			Map<String, String> map = new HashMap<String, String>();
									
			String routeTitle = this.routes.get(i).getTitle();
			
			if(routeTitle == null || routeTitle.isEmpty())
				routeTitle = getString(R.string.unnamed);
			map.put("route", routeTitle);
			map.put("date", getResources().getString(R.string.date) + ": " + this.routes.get(i).getDateCreated());
			g.add(map);
		}
		
		ListAdapter adapter = new SimpleAdapter(this, g, android.R.layout.two_line_list_item, from, new int[] {android.R.id.text1, android.R.id.text2});
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
			
		case R.id.menu_showRouteOnMap:
			showRoute = true;
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
	
	@Override
	public void finish() {		
		if(showRoute)
			setResult(itemSelected);
		else 
			setResult(0);
		super.finish();
	}
	
	private void showRouteOnMap()
	{
		SharedPreferences settings = getSharedPreferences(MapActivity.APP_PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();		
		editor.putInt(MapActivity.LAST_ROUTE, MapActivity.ROUTES_CTRL);
		editor.commit();
		
		if(this.routes == null || this.routes.size() <= itemSelected)
		{
			itemSelected = -1;
			this.finish();
		}
		
		itemSelected = this.routes.get(itemSelected).getRouteId();
		
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
}
