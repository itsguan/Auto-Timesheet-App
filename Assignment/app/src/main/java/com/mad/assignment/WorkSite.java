package com.mad.assignment;

/**
 * Created by Guan on 12/10/2016.
 */

public class WorkSite {

    private String address;
    private double latitude;
    private double longitude;

    public WorkSite(String address, double lat, double lng) {
        this.address = address;
        latitude = lat;
        longitude = lng;
    }

    public String getAddress() {
        return address;
    }
}
