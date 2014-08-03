package com.android.diary;

import BaseClasses.BaseFragment;
import Helpers.GeoCoderHelper;
import Helpers.ITaskCompleteListener;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class RouteItemEditFragment extends BaseFragment {

    //	private static final String LOG_TAG = "ROUTE ITEM EDIT FRAGMENT";
    public static final String ROUTE_ITEM_ID = "routeItemId";

    private RouteItem routeItem;

    private EditText title;
    private EditText description;
    private EditText country;
    private EditText postalCode;
    private EditText street;
    private EditText city;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null)
            return null;

        View view = inflater.inflate(R.layout.fragment_route_item_edit, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        initControls(view);
        setValues(true);

        Button autofillBtn = (Button) view.findViewById(R.id.ri_edit_autofill_btn);
        autofillBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                autofillPressed();
            }
        });

        Button saveBtn = (Button) view.findViewById(R.id.ri_edit_save_btn);
        saveBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                saveData();
            }
        });

        return view;
    }

    public static RouteItemEditFragment newInstance(int routeItemId) {
        RouteItemEditFragment fragment = new RouteItemEditFragment();

        Bundle args = new Bundle();
        args.putInt(ROUTE_ITEM_ID, routeItemId);
        fragment.setArguments(args);
        return fragment;
    }

    private int getRouteItemId() {
        if (getArguments() == null)
            return 0;
        return getArguments().getInt(ROUTE_ITEM_ID, 0);
    }

    private void initControls(View view) {
        title = (EditText) view.findViewById(R.id.ri_edit_title);
        description = (EditText) view.findViewById(R.id.ri_edit_description);
        country = (EditText) view.findViewById(R.id.ri_edit_country);
        postalCode = (EditText) view.findViewById(R.id.ri_edit_postal);
        street = (EditText) view.findViewById(R.id.ri_edit_street);
        city = (EditText) view.findViewById(R.id.ri_edit_city);
    }

    private void setValues(boolean fromDB) {
        if (fromDB) {
            DatabaseHandler db = new DatabaseHandler(getActivity());
            this.routeItem = db.getRouteItem(getRouteItemId());
            db.close();
        }

        if (routeItem != null) {
            setText(title, routeItem.getTitle());
            setText(description, routeItem.getDescription());
            setText(country, routeItem.getAddress().getCountryName());
            setText(city, routeItem.getAddress().getLocality());
            setText(street, routeItem.getAddress().getAddressLine(0));
            setText(postalCode, routeItem.getAddress().getPostalCode());
        }
    }

    private void autofillPressed() {
        if (Globals.isNetworkAvailableWithToast(getActivity().getApplicationContext())) {
            GeoCoderHelper geoCoderHelper = new GeoCoderHelper(getActivity(), new ITaskCompleteListener() {
                @Override
                public void onTaskComplete(int routeItemId) {
                    if (routeItemId == 0) {
                        ToastMessage(getString(R.string.warn_unableToFillLocation));
                    } else {
                        DatabaseHandler db = new DatabaseHandler(getActivity());
                        routeItem = db.getRouteItem(routeItemId);
                        db.close();
                        setValues(false);
                    }
                }
            });

            geoCoderHelper.execute(this.routeItem);
        }
    }

    private void saveData() {
        routeItem.setTitle(title.getText().toString());
        routeItem.setDescription(description.getText().toString());
        routeItem.getAddress().setCountryName(country.getText().toString());
        routeItem.getAddress().setPostalCode(postalCode.getText().toString());
        routeItem.getAddress().setLocality(city.getText().toString());
        routeItem.getAddress().setAddressLine(0, street.getText().toString());

        DatabaseHandler db = new DatabaseHandler(getActivity());
        db.updateRouteItem(routeItem, true, true);
        db.close();

        ToastMessage(getString(R.string.ri_edit_saved));
    }

    /**
     * Sets given text value to the given editText.
     *
     * @param editText - editText Id (R number)
     * @param text     - text to set on the editText
     */
    private void setText(EditText editText, String text) {
        if (editText == null || text == null || text.isEmpty())
            return;

        editText.setText(text);
    }
}
