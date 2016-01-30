package com.peter.georeminder.models;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.google.android.gms.maps.model.LatLng;
import com.peter.georeminder.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Peter on 10/6/15.
 * TODO: encapsulate all fields
 */
public class Reminder {

    private String Uid;             // Uid = title_lat_lng_createDate_createTime

    private Date createDate;
    private Date startingDate;
    private Date endDate;
    private long timeFromNow;

    private LatLng createLocation;
    private LatLng remindLocation;
    private LatLng lastKnownLocation;
    private int locationAccuracy;
    private double distanceToHere;

    private String title;
    private String description;
    private String additional;
    private int importance;         // importance: 1, 2, 3, 4
    private int colorInt;

    private boolean withLocation;
    private boolean withTime;

    // how to remind
    private boolean vibrate;
    private int notification;
    private int repeatTimes;

    // reminder type
    private int type;       // 0 = notes, 1 = geo, 2 = normal

    // logistics
    private Context context;


    // utilities
    Calendar cal = Calendar.getInstance();

    public Reminder(Context context){
        initialise(context);
    }

    private void initialise(Context context) {
        startingDate = cal.getTime();
//        endDate

        setTitle(context.getString(R.string.reminder_default_title));

        distanceToHere = 0;
        startingDate = null;
        endDate = null;
        createDate = new Date();
        colorInt = R.color.colorPrimary;
        vibrate = true;
    }

    // Title
    public String getTitle() {
        return title;
    }

    public Reminder setTitle(String title) {
        this.title = title;
        return this;
    }

    // Description
    public String getDescription() {
        return description;
    }

    public Reminder setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getAdditional() {
        return additional;
    }

    public Reminder setAdditional(String additional) {
        this.additional = additional;
        return this;
    }

    // withLocation
    public boolean isWithLocation() {
        return withLocation;
    }

    public Reminder withLocation(boolean withLocation) {
        this.withLocation = withLocation;
        return this;
    }

    // withTime
    public boolean isWithTime() {
        return withTime;
    }

    public Reminder withTime(boolean withTime) {
        this.withTime = withTime;
        return this;
    }

    // distanceToHere
    public double getDistanceToHere() {
        return distanceToHere;
    }

    public Reminder setDistanceToHere(double distanceToHere) {
        this.distanceToHere = distanceToHere;
        return this;
    }

    // timeFromNow
    public long getTimeFromNow() {
        return timeFromNow;
    }

    public Reminder setTimeFromNow(long timeFromNow) {
        this.timeFromNow = timeFromNow;
        return this;
    }

    // startingDate
    public Date getStartingDate() {
        return startingDate;
    }

    public Reminder setStartingDate(Date startingDate) {
        this.startingDate = startingDate;
        return this;
    }

    // endDate
    public Date getEndDate() {
        return endDate;
    }

    public Reminder setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    // color
    public int getColorInt() {
        return colorInt;
    }

    public Reminder setColorInt(@ColorInt int colorInt) {
        this.colorInt = colorInt;
        return this;
    }
}
