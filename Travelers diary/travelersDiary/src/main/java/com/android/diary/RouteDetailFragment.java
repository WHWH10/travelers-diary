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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class RouteDetailFragment extends ListFragment {

    public static final String ROUTE_ID = "routeId";
    private String title;
    private String routeItemTitle;
    private String description;
    private TextView tvTitle;
    private TextView tvDescription;
    private List<RouteItem> routeItems;
    private ListView listView;
    private int itemSelected;

    private final static int REQ_CODE_GET_DEFAULT_IMAGE = 1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null)
            return null;

        View view = inflater.inflate(R.layout.fragment_route_detail, container, false);
        this.listView = (ListView) view.findViewById(android.R.id.list);

        this.title = "";
        this.routeItemTitle = "";
        this.itemSelected = 0;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().addHeaderView(getActivity().getLayoutInflater().inflate(R.layout.route_detail_header, null));
        initializeButtons(getListView());
        setValues(getListView());
        registerForContextMenu(listView);
    }

    @Override
    public void onStart() {
        saveImages();
        prepareList();
        super.onStart();
    }

    private void saveImages() {
        SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(getActivity());
        long time = sharedPreferenceHelper.getCameraStartTime();

        if (time != 0) {
            List<String> images = ImageHelper.getImagePathsByDate(time, getActivity());
            if (images.size() > 0) {
                if (routeItems != null) {
                    DatabaseHandler db = new DatabaseHandler(getActivity());
                    int routeId = routeItems.get(itemSelected).getRouteId();
                    int routeItemId = routeItems.get(itemSelected).getRouteItemId();
                    for (String image : images) {
                        db.insertImage(routeId, routeItemId, image);
                    }
                }
            }

            sharedPreferenceHelper.removeValue(Config.CAMERA_START_TIME);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        itemSelected = position - 1;
        getActivity().openContextMenu(listView);
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.activity_route_detail_context, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_changeRouteItemTtitle:
                changeRouteItemTitle();
                break;
            case R.id.menu_deleteRouteItem:
                deleteRoute();
                break;

            case R.id.menu_showRouteItemDetails:
                showRouteItemDetail();
                break;

            case R.id.menu_takePicture:
                Intent takePictureIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);

                SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(getActivity());
                sharedPreferenceHelper.setCameraStartTime(new Date().getTime());

                startActivity(takePictureIntent);
                break;

            case R.id.menu_addImage:
                Intent intent = new Intent(getActivity(), MultiPhotoSelectActivity.class);
                intent.putExtra(MultiPhotoSelectActivity.ROUTE_ID, this.routeItems.get(itemSelected).getRouteId());
                intent.putExtra(MultiPhotoSelectActivity.ROUTE_ITEM_ID, this.routeItems.get(itemSelected).getRouteItemId());
                startActivity(intent);
                break;

            case R.id.menu_showGallery:
                Intent galeryIntent = new Intent(getActivity(), MultiPhotoSelectActivity.class);
                galeryIntent.putExtra(MultiPhotoSelectActivity.SHOW_GALLERY, true);
                galeryIntent.putExtra(MultiPhotoSelectActivity.ROUTE_ID, routeItems.get(itemSelected).getRouteId());
                galeryIntent.putExtra(MultiPhotoSelectActivity.ROUTE_ITEM_ID, routeItems.get(itemSelected).getRouteItemId());
                startActivity(galeryIntent);
                break;

            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void prepareList() {
        DatabaseHandler db = new DatabaseHandler(getActivity());
        this.routeItems = db.getRouteItems(getRouteID());
        db.close();

        List<Map<String, String>> g = new ArrayList<Map<String, String>>();

        for (RouteItem routeItem : this.routeItems) {
            Map<String, String> map = new HashMap<String, String>();

            String routeItemTitle = routeItem.getTitle();

            if (routeItemTitle == null || routeItemTitle.isEmpty())
                routeItemTitle = getString(R.string.unnamed);
            map.put("title", routeItemTitle);
            map.put("date", getResources().getString(R.string.date) + ": " +
                    DateFormat.getDateFormat(getActivity()).format(routeItem.getDateCreated()) + " " +
                    DateFormat.getTimeFormat(getActivity()).format(routeItem.getDateCreated()));
            map.put("id", String.valueOf(routeItem.getRouteItemId()));
            g.add(map);
        }

        RouteListItemAdapter adapter = new RouteListItemAdapter(getActivity(), g);

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

    private void initializeButtons(View view) {
        ImageButton btnEditTitle = (ImageButton) view.findViewById(R.id.route_det_title_btnEdit);
        btnEditTitle.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(View v) {
                changeRouteTitlePressed();
            }
        });

        ImageButton btnEditDescription = (ImageButton) view.findViewById(R.id.route_det_description_btnEdit);
        btnEditDescription.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(View v) {
                changeRouteDescriptionPressed();
            }
        });
    }

    private void changeRouteItemTitle() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        final EditText editText = new EditText(getActivity());
        editText.setHint(R.string.hint_routeItemtitle);
        if (routeItems.get(itemSelected).getTitle() != null && !routeItems.get(itemSelected).getTitle().isEmpty())
            editText.setText(routeItems.get(itemSelected).getTitle());
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        dialog.setView(editText);
        dialog.setNeutralButton(R.string.dialog_set, new OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                routeItemTitle = editText.getText().toString();

                if (!routeItemTitle.isEmpty() && routeItems != null && itemSelected < routeItems.size()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DatabaseHandler.KEY_TITLE, routeItemTitle);

                    routeItems.get(itemSelected).setTitle(routeItemTitle);
                    DatabaseHandler db = new DatabaseHandler(getActivity());
                    db.updateRouteItem(routeItems.get(itemSelected), false, false);
                    db.close();
                    prepareList();
                }

                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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

    private void deleteRoute() {
        if (this.routeItems != null && this.routeItems.size() > itemSelected) {
            createConfirmDialog();
        }
    }

    private void createConfirmDialog() {
        new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.dialog_confirmation)).setMessage(getString(R.string.dialog_routeItemDelete))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseHandler db = new DatabaseHandler(getActivity());
                        db.deleteRouteItem(routeItems.get(itemSelected).getRouteItemId());
                        db.close();
                        prepareList();
                    }
                }).setNegativeButton(android.R.string.no, null).show();
    }

    public static RouteDetailFragment newInstance(int routeId) {
        RouteDetailFragment fragment = new RouteDetailFragment();

        Bundle args = new Bundle();
        args.putInt(ROUTE_ID, routeId);
        fragment.setArguments(args);
        return fragment;
    }

    private int getRouteID() {
        if (getArguments() == null)
            return 0;
        return getArguments().getInt(ROUTE_ID, 0);
    }

    private void setValues(View view) {
        DatabaseHandler db = new DatabaseHandler(getActivity());
        Route route = db.getRoute(getRouteID());
        db.close();

        if (route == null)
            return;

        tvTitle = (TextView) view.findViewById(R.id.route_det_title_textView);
        tvTitle.setText(route.getTitle());

        tvDescription = (TextView) view.findViewById(R.id.route_det_description_textView);
        tvDescription.setText(route.getDescription());

        TextView dateCreaded = (TextView) view.findViewById(R.id.route_det_created_textView);
        dateCreaded.setText(DateFormat.getDateFormat(getActivity()).format(route.getDateCreated()) + " " +
                DateFormat.getTimeFormat(getActivity()).format(route.getDateCreated()));
    }

    private void changeRouteTitlePressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        final EditText editText = new EditText(getActivity());
        editText.setHint(R.string.hint_title);
        editText.setText(tvTitle.getText());
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        dialog.setView(editText);
        dialog.setNeutralButton(R.string.dialog_set, new OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                title = editText.getText().toString();
                changeTitle();
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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

    private void showRouteItemDetail() {
        Intent intent = new Intent(getActivity(), RouteItemDetailsActivity.class);
        if (this.routeItems != null && this.routeItems.size() > itemSelected) {
            intent.putExtra(RouteItemDetailFragment.ROUTE_ITEM_ID, this.routeItems.get(itemSelected).getRouteItemId());
        }
        startActivity(intent);
    }

    private void changeRouteDescriptionPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        final EditText editText = new EditText(getActivity());
        editText.setHint(R.string.hint_description);
        editText.setText(tvDescription.getText());
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        dialog.setView(editText);
        dialog.setNeutralButton(R.string.dialog_set, new OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                description = editText.getText().toString();
                changeDescription();
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
        if (!title.isEmpty() && getRouteID() != 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHandler.KEY_TITLE, title);

            DatabaseHandler db = new DatabaseHandler(getActivity());
            db.updateRoute(contentValues, getRouteID());
            db.close();

            tvTitle.setText(title);
        }
    }

    private void changeDescription() {
        if (!description.isEmpty() && getRouteID() != 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHandler.KEY_DESCRIPTION, description);

            DatabaseHandler db = new DatabaseHandler(getActivity());
            db.updateRoute(contentValues, getRouteID());
            db.close();

            tvDescription.setText(description);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQ_CODE_GET_DEFAULT_IMAGE) {
                DatabaseHandler db = new DatabaseHandler(getActivity());
                String imagePath;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    imagePath = ImageHelper.getPathFromUriKitKat(getActivity(), data.getData());
                } else {
                    imagePath = ImageHelper.getImagePathFromURI(getActivity(), data.getData());
                }

                if (imagePath == null) {
                    MessageHelper.ToastMessage(getActivity(), getString(R.string.warn_imageNotAdded));
                    return;
                }

                db.insertDefaultImage(routeItems.get(itemSelected).getRouteId(), routeItems.get(itemSelected).getRouteItemId(), imagePath, false);
                db.close();
            }
        }
    }
}
