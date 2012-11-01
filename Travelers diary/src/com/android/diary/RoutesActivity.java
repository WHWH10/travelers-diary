package com.android.diary;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.diary.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class RoutesActivity extends ListActivity{

	private ListView listView;
	private List<LocationInfo> routes;
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
		this.routes = db.getRouteTitles();
		db.close();
		
		String from[] = {"route", "date"};
		
		List<Map<String, String>> g = new ArrayList<Map<String, String>>();
		for(int i = 0; i < this.routes.size(); i++)
		{
			Map<String, String> map = new HashMap<String, String>();
			Date date = new Date();
			date.setTime(routes.get(i).getTime());
			String route = routes.get(i).getRoute();
			if(routes.get(i).getRoute().equals(""))
				route = getResources().getString(R.string.route) + ": " + routes.get(i).getRoute_id();
			map.put("route", route);
			map.put("date", getResources().getString(R.string.date) + ": " + date.toString());
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
			
			break;
			
		case R.id.menu_showRouteOnMap:
			showRoute = true;
			showRouteOnMap();			
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_route_options, menu);
		return super.onCreateOptionsMenu(menu);
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
		SharedPreferences settings = getSharedPreferences(MainActivity.APP_PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();		
		editor.putInt(MainActivity.LAST_ROUTE, MainActivity.ROUTES_CTRL);
		editor.commit();
		itemSelected = this.routes.get(itemSelected).getRoute_id();
		this.finish();
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
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				title = editText.getText().toString();
				changeTitle();
			}
		});
		dialog.setNegativeButton(R.string.dialog_cancel, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		dialog.show();		
	}
	
	private void changeTitle()
	{
		if(!title.equals("") || !title.equals(" "))
			routes.get(itemSelected).setRoute(title);
		updateDatabase();
		prepareList();
	}
	
	private void deleteRoute()
	{
		DatabaseHandler db = new DatabaseHandler(this);
		List<LocationInfo> loc = db.getRoute(routes.get(itemSelected).getRoute_id());
		for(int i = 0; i < loc.size(); i++)
		{
			db.deleteLocationInfo(loc.get(i));
		}
		db.close();
		prepareList();
	}
	
	private void updateDatabase()
	{
		DatabaseHandler db = new DatabaseHandler(this);
		List<LocationInfo> loc = db.getRoute(routes.get(itemSelected).getRoute_id());
		for(int i = 0; i < loc.size(); i++)
		{
			loc.get(i).setRoute(title);
			db.updateLocationInfo(loc.get(i));
		}
		db.close();
	}
}
