package com.android.diary;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helpers.IImageClickListener;
import Helpers.ImageHelper;
import Helpers.MessageHelper;
import Helpers.SharedPreferenceHelper;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

public class RoutesActivity extends ListActivity {

    //	private static final String LOG_TAG = "ROUTES ACTIVITY";
    private Intent myService;

    private ListView listView;
    private List<Route> routes;
    private int itemSelected;
    private String title;

    private final static int REQ_CODE_GET_DEFAULT_IMAGE = 1;
    public static final String KEY_CLOSE_APP = "keyCloseApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        this.listView = (ListView) findViewById(android.R.id.list);
        this.itemSelected = 0;
        this.title = "";

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getBoolean(KEY_CLOSE_APP)) {
                if (isLocationProviderServiceRunning(this)) {
                    Intent s = new Intent(LocationProviderService.ACTION_STOP_LOCATION_PROVIDER);
                    startService(s);
                }

                finish();
            }
        }

        startMainService();
        registerForContextMenu(listView);
    }

    private void startMainService() {
        if (!isMainServiceRunning(this)) {
            Intent myService = new Intent(this, MainService.class);
            startService(myService);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_route_option, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.menu_showGallery:
                intent = new Intent(this, MultiPhotoSelectActivity.class);
                intent.putExtra(MultiPhotoSelectActivity.SHOW_GALLERY, true);
                startActivity(intent);
                break;
            case R.id.menu_addNewRoute:
                addNewRoute();
                break;
            case R.id.menu_settings:
                intent = new Intent(this, LogInActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public static boolean isMainServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.android.diary.MainService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLocationProviderServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.android.diary.LocationProviderService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        saveImages();
        prepareList();
        super.onStart();
    }

    private void saveImages() {
        SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(getApplicationContext());
        long time = sharedPreferenceHelper.getCameraStartTime();

        if (time != 0) {
            List<String> images = ImageHelper.getImagePathsByDate(time, getApplicationContext());
            if (images.size() > 0) {
                if (routes != null) {
                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                    int routeId = routes.get(itemSelected).getRouteId();
                    for (String image : images) {
                        db.insertImage(routeId, 0, image);
                    }
                }
            }

            sharedPreferenceHelper.removeValue(Config.CAMERA_START_TIME);
        }
    }

    private void prepareList() {
        DatabaseHandler db = new DatabaseHandler(this);
        this.routes = db.getRoutes();
        db.close();

        List<Map<String, String>> g = new ArrayList<Map<String, String>>();

        for (Route route : this.routes) {
            Map<String, String> map = new HashMap<String, String>();

            String routeTitle = route.getTitle();

            if (routeTitle == null || routeTitle.isEmpty())
                routeTitle = getString(R.string.unnamed);
            map.put("title", routeTitle);
            map.put("date", getResources().getString(R.string.date) + ": " +
                    DateFormat.getDateFormat(getApplicationContext()).format(route.getDateCreated()) + " " +
                    DateFormat.getTimeFormat(getApplicationContext()).format(route.getDateCreated()));
            map.put("id", String.valueOf(route.getRouteId()));
            g.add(map);
        }

        RouteListAdapter adapter = new RouteListAdapter(this, g);
        adapter.setOnImageClickListener(new IImageClickListener() {

            public void imageClicked(int id) {
                itemSelected = id;
                Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                imageIntent.setType("image/*");
                startActivityForResult(imageIntent, REQ_CODE_GET_DEFAULT_IMAGE);
            }
        });

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

            case R.id.menu_takePicture:
                Intent takePictureIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);

                SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(getApplicationContext());
                sharedPreferenceHelper.setCameraStartTime(new Date().getTime());

                startActivity(takePictureIntent);
                break;

            case R.id.menu_addImage:
                Intent intent = new Intent(this, MultiPhotoSelectActivity.class);
                intent.putExtra(MultiPhotoSelectActivity.ROUTE_ID, this.routes.get(itemSelected).getRouteId());
                startActivity(intent);
                break;

            case R.id.menu_showRouteOnMap:
                showRouteOnMap();
                break;

            case R.id.menu_showGallery:
                Intent galeryIntent = new Intent(this, MultiPhotoSelectActivity.class);
                galeryIntent.putExtra(MultiPhotoSelectActivity.SHOW_GALLERY, true);
                galeryIntent.putExtra(MultiPhotoSelectActivity.ROUTE_ID, routes.get(itemSelected).getRouteId());
                startActivity(galeryIntent);
                break;

            case R.id.menu_startTracking:
                if (this.routes == null || this.routes.size() <= itemSelected)
                    break;

                myService = new Intent(LocationProviderService.ACTION_START_LOCATION_PROVIDER);
                myService.putExtra(LocationProviderService.ROUTE_ID, this.routes.get(itemSelected).getRouteId());
                startService(myService);
                break;

            case R.id.menu_stopTracking:
                if (isLocationProviderServiceRunning(this) && myService != null) {
                    stopService(myService);
                } else {
                    myService = new Intent(LocationProviderService.ACTION_STOP_LOCATION_PROVIDER);
                    startService(myService);
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
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_CODE_GET_DEFAULT_IMAGE) {
                DatabaseHandler db = new DatabaseHandler(this);
                String imagePath;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    imagePath = ImageHelper.getPathFromUriKitKat(getApplicationContext(), data.getData());
                } else {
                    imagePath = ImageHelper.getImagePathFromURI(getApplicationContext(), data.getData());
                }

                if (imagePath == null) {
                    MessageHelper.ToastMessage(getApplicationContext(), getString(R.string.warn_imageNotAdded));
                    return;
                }

                db.insertDefaultImage(routes.get(itemSelected).getRouteId(), 0, imagePath, true);
                db.close();
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_routes_context, menu);
        if (isLocationProviderServiceRunning(this)) {
            menu.findItem(R.id.menu_startTracking).setVisible(false);
            menu.findItem(R.id.menu_stopTracking).setVisible(true);
        } else {
            menu.findItem(R.id.menu_startTracking).setVisible(true);
            menu.findItem(R.id.menu_stopTracking).setVisible(false);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        itemSelected = position;
        openContextMenu(listView);
        super.onListItemClick(l, v, position, id);
    }

    private void showRouteOnMap() {
        if (this.routes == null || this.routes.size() <= itemSelected) {
            return;
        }

        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(MapActivity.ROUTE_ID, this.routes.get(itemSelected).getRouteId());
        startActivity(intent);
    }

    private void showRouteDetail() {
        Intent intent = new Intent(getApplicationContext(), RouteDetailActivity.class);
        if (this.routes != null && this.routes.size() > itemSelected) {
            intent.putExtra(RouteDetailFragment.ROUTE_ID, this.routes.get(itemSelected).getRouteId());
        }
        startActivity(intent);
    }

    private void changeRouteTitle() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        editText.setHint(R.string.hint_title);
        if (!routes.get(itemSelected).getTitle().isEmpty())
            editText.setText(routes.get(itemSelected).getTitle());
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
        editText.setSelection(editText.getText().length());
    }

    private void addNewRoute() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        editText.setHint(R.string.routes_title_hint);

        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        final InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        dialog.setView(editText);
        dialog.setNeutralButton(R.string.dialog_add, new OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if (editText.getText().length() > 0) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DatabaseHandler.KEY_TITLE, editText.getText().toString());

                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                    db.insertRoute(contentValues);
                    db.close();

                    prepareList();
                }

                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
        });
        dialog.setNegativeButton(R.string.dialog_cancel, new OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
        editText.setSelection(editText.getText().length());
    }

    private void changeTitle() {
        if (!title.isEmpty() && this.routes != null && itemSelected < this.routes.size()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHandler.KEY_TITLE, title);

            DatabaseHandler db = new DatabaseHandler(this);
            db.updateRoute(contentValues, this.routes.get(itemSelected).getRouteId());
            db.close();
            prepareList();
        }
    }

    private void createConfirmDialog() {
        new AlertDialog.Builder(this).setTitle(getString(R.string.dialog_confirmation)).setMessage(getString(R.string.dialog_routeDelete))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                        db.deleteRoute(routes.get(itemSelected).getRouteId());
                        db.close();
                        prepareList();
                    }
                }).setNegativeButton(android.R.string.no, null).show();
    }

    private void deleteRoute() {
        if (this.routes != null && this.routes.size() > itemSelected) {
            createConfirmDialog();
        }
    }
}
