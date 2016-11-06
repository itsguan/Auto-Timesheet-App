package com.mad.assignment.model;

import com.google.android.gms.maps.model.LatLng;
import com.orm.SugarRecord;

/**
 * Created by Guan Du 98110291
 *
 * This is a POJO that contains the fields, getters and setters for a Work Site.
 * Used by Gson and SugarORM APIs.
 */

public class WorkSite extends SugarRecord {

    private static final double MAX_HOURS_WORKED = 24;

    private String mAddress;
    private String mDateWorked;
    private double mLatitude;
    private double mLongitude;
    private double mHoursWorked;
    private boolean mCurrentlyWorking = false;
    private boolean mCurrentPeriod = true;

    /**
     * Constructor required by SugarORM
     */
    public WorkSite() {
    }

    /**
     * Creates a WorkSite using only an mAddress and a LatLng. Used for geofences.
     */
    public WorkSite(String address, LatLng latLng) {
        this.mAddress = address;
        mLatitude = latLng.latitude;
        mLongitude = latLng.longitude;
    }

    /**
     * Creates a WorkSite that contains date and hours worked. Used for databases.
     */
    public WorkSite (String address, String dateWorked, double hoursWorked) {
        this.mAddress = address;
        this.mDateWorked = dateWorked;
        this.mHoursWorked = hoursWorked;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setCurrentlyWorking(boolean currentlyWorking) {
        this.mCurrentlyWorking = currentlyWorking;
    }

    public boolean isCurrentlyWorking() {
        return mCurrentlyWorking;
    }

    public void setHoursWorked(double hoursWorked) {
        this.mHoursWorked = hoursWorked;
    }

    /**
     * Increases the hours worked. Cannot pass MAX_HOURS.
     */
    public void incrementHoursWorked(double increment) {
        if (mHoursWorked + increment < MAX_HOURS_WORKED) {
            mHoursWorked += increment;
        } else {
            mHoursWorked = MAX_HOURS_WORKED;
        }
    }

    public String getDateWorked() {
        return mDateWorked;
    }

    public void setDateWorked(String dateWorked) {
        this.mDateWorked = dateWorked;
    }

    public double getHoursWorked() {
        return mHoursWorked;
    }

    public boolean isCurrentPeriod() {
        return mCurrentPeriod;
    }

    public void setCurrentPeriod(boolean currentPeriod) {
        this.mCurrentPeriod = currentPeriod;
    }
}
