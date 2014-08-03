package com.android.diary;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import android.database.Cursor;
import android.location.Address;
import android.util.Log;

import com.google.gson.Gson;

public class RouteItem {

    private static final String LOG_TAG = "ROUTE ITEM OBJ";

    private int routeItemId;
    private int routeId;
    private String title;
    private String description;
    private Date dateCreated;
    private Date dateModified;
    private Address address;
    private boolean isImported;

    public RouteItem(Locale locale) {
        this.address = new Address(locale);
    }

    public int getRouteItemId() {
        return routeItemId;
    }

    public void setRouteItemId(int routeItemId) {
        this.routeItemId = routeItemId;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        try {
            this.dateCreated = DateFormat.getDateTimeInstance().parse(dateCreated);
        } catch (ParseException e) {
            Log.e(LOG_TAG, e.toString());
            this.dateCreated = new Date();
        }
    }

    public boolean getIsImported() {
        return this.isImported;
    }

    public void setIsImported(boolean isImported) {
        this.isImported = isImported;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public void setDateModified(String dateModified) {
        try {
            this.dateModified = DateFormat.getDateTimeInstance().parse(dateModified);
        } catch (ParseException e) {
            Log.e(LOG_TAG, e.toString());
            this.dateModified = new Date();
        }
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public double getLatitude() {
        if (this.address.hasLatitude())
            return this.address.getLatitude();
        return 0.0;
    }

    public double getLongitude() {
        if (this.address.hasLongitude())
            return this.address.getLongitude();
        return 0.0;
    }

    public static String toGson(RouteItem routeItem) {
        if (routeItem == null)
            return "";

        return new Gson().toJson(routeItem, routeItem.getClass());
    }

    public static RouteItem parse(Cursor cursor) {
        if (cursor == null || cursor.isClosed())
            return null;

        RouteItem routeItem = new Gson().fromJson(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_ROUTE_ITEM_OBJ)), RouteItem.class);
        if (routeItem != null) {
            routeItem.setRouteItemId(cursor.getInt(cursor.getColumnIndex(DatabaseHandler.KEY_ID)));
            routeItem.setDateCreated(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_DATE_CREATED)));
            routeItem.setDateModified(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_DATE_MODIFIED)));
            routeItem.setIsImported(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_IS_IMPORTED))));
        }

        return routeItem;
    }
}
