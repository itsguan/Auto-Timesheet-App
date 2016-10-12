package com.mad.assignment;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Guan on 12/10/2016.
 */

public class WorkSite {

    private String address;
    private double latitude;
    private double longitude;

    public WorkSite(String address, LatLng latLng) {
        this.address = address;
        latitude = latLng.latitude;
        longitude = latLng.longitude;
    }

    public String getAddress() {
        return address;
    }
}
