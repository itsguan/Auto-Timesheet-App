package com.mad.assignment.constants;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Guan on 28/10/2016.
 */

public class GlobalGoogleApiClient implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = GlobalGoogleApiClient.class.getName();

    private static GoogleApiClient sGoogleApiClient;
    private Context mContext;

    public GlobalGoogleApiClient(Context context) {
        mContext = context;

        createGoogleApi();
    }

    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (sGoogleApiClient == null) {
            sGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public static GoogleApiClient getSingletonGoogleApiClient() {
        if (sGoogleApiClient != null) {
            return sGoogleApiClient;
        } else return null;
    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        //getLastKnownLocation();
        //recoverGeofenceMarker();
    }

    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }
}
