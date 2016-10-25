package com.mad.assignment;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Guan on 12/10/2016.
 */

public class WorkSite {

    private String address;
    private String dateWorked;
    private double latitude;
    private double longitude;
    private boolean currentlyWorking = false;


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
}
