package com.mad.assignment.model;

import com.google.android.gms.maps.model.LatLng;
import com.orm.SugarRecord;

/**
 * Created by Guan on 12/10/2016.
 */

public class WorkSite extends SugarRecord {

    private static final double MAX_HOURS_WORKED = 24;

    private String address;
    private String dateWorked;
    private double latitude;
    private double longitude;
    private double hoursWorked;
    private boolean currentlyWorking = false;

    public WorkSite() {
    }

    public WorkSite(String address, LatLng latLng) {
        this.address = address;
        latitude = latLng.latitude;
        longitude = latLng.longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setCurrentlyWorking(boolean currentlyWorking) {
        this.currentlyWorking = currentlyWorking;
    }

    public boolean isCurrentlyWorking() {
        return currentlyWorking;
    }

    public void setHoursWorked(double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public void incrementHoursWorked(double increment) {
        if (hoursWorked < MAX_HOURS_WORKED) {
            hoursWorked += increment;
        } else {
            hoursWorked = MAX_HOURS_WORKED;
        }
    }

    public String getDateWorked() {
        return dateWorked;
    }

    public void setDateWorked(String dateWorked) {
        this.dateWorked = dateWorked;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }
}
