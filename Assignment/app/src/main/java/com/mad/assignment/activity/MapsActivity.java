package com.mad.assignment.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mad.assignment.constants.Constants;
import com.mad.assignment.database.GsonHelper;
import com.mad.assignment.services.GeofenceTransitionService;
import com.mad.assignment.R;
import com.mad.assignment.model.WorkSite;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        ResultCallback<Status> {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private static final float CAMERA_ZOOM = 15f;

    private GoogleApiClient googleApiClient;
    private GoogleMap mMap;
    private GsonHelper mGsonHelper;

    private MapFragment mapFragment;

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initialiseGoogleApiClient();
        mGsonHelper = new GsonHelper(this);

        final EditText searchBar = (EditText) findViewById(R.id.maps_activity_search_et);
        Button searchBtn = (Button) findViewById(R.id.maps_activity_search_btn);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchAddress = searchBar.getText().toString();
                LatLng latLng = findLatLngFromAddress(searchAddress);

                if (latLng != null) {

                    // Add a red marker pointing to the address.
                    mMap.addMarker(new MarkerOptions().position(latLng).title(searchAddress));
                    animateMapCamera(CAMERA_ZOOM, latLng);
                } else {
                    showCantFindAddressToast();
                }
            }
        });

        Button saveBtn = (Button) findViewById(R.id.maps_activity_save_btn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchAddress = searchBar.getText().toString();

                // Checks if an address was actually entered.
                if (!searchAddress.equals("")) {
                    LatLng latLng = findLatLngFromAddress(searchAddress);

                    // Checks if the address was actually found.
                    if (latLng != null) {
                        String latLngMsg = "Lat: " + latLng.latitude + "Lng: " + latLng.longitude;
                        Log.d(TAG, latLngMsg);

                        // Only create geofence and finish if address was saved properly.
                        if (saveLocationToSharedPrefs(searchAddress)) {
                            startGeofence(latLng, searchAddress);
                            finish();
                        }
                    } else {
                        showCantFindAddressToast();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.no_address_entered_warning,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    /**
     * Create an instance of GoogleApiClient if there is none.
     */
    private void initialiseGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    /**
     * Provide visual feedback in the form of a toast when a search fails.
     */
    private void showCantFindAddressToast() {
        Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.search_timed_out),
                Toast.LENGTH_LONG);
        TextView toastView = (TextView) toast.getView().findViewById(android.R.id.message);
        if (toastView != null) toastView.setGravity(Gravity.CENTER);
        toast.show();
    }

    /**
     * Saves a location to the locations shared preferences.
     * Returns true if this is a new location.
     * Returns false if this is an existing location.
     */
    private boolean saveLocationToSharedPrefs(String location) {

        // Retrieve existing work sites first.
        ArrayList<WorkSite> workSites = mGsonHelper.getWorkSitesFromPrefs();

        // Create a work site object using the new address.
        LatLng latLng = findLatLngFromAddress(location);
        WorkSite workSite = new WorkSite(location, latLng);

        // Add first entry to a newly created shared prefs.
        if (workSites.size() == 0) {
            workSites.add(workSite);
        } else {

            // Controls whether a new entry is needed if the same address is already added.
            boolean existingWorkSiteFound = false;

            // Checks if the new address is already saved.
            for (WorkSite existingWorkSite : workSites) {
                if (existingWorkSite.getAddress().equals(workSite.getAddress())) {
                    existingWorkSiteFound = true;
                }
            }

            // Display a toast if the address was already saved or save as a new address.
            // Prevents concurrent modification exception by using the extra boolean.
            if (existingWorkSiteFound) {
                Toast.makeText(this, R.string.address_already_saved_warning,
                        Toast.LENGTH_LONG).show();
                return false;
            } else {
                workSites.add(workSite);
            }
        }

        // Overwrite the list of WorkSites in the shared prefs.
        mGsonHelper.overwriteWorkSitesInPrefs(workSites);

        // Provide feedback to the user with a toast message.
        Toast.makeText(getApplicationContext(), location + " is added.", Toast.LENGTH_LONG).show();
        return true;
    }

    /**
     * Returns a LatLng when given an address.
     */
    private LatLng findLatLngFromAddress(String searchAddress) {
        List<Address> addressList = null;

        if (searchAddress != null || !searchAddress.equals("")) {
            Geocoder geocoder = new Geocoder(getApplicationContext());

            try {
                addressList = geocoder.getFromLocationName(searchAddress, 1);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Could not find address " + searchAddress);
            }

            // Retrieve the first address in the list if there were multiple.
            if (addressList != null && addressList.size() != 0) {
                Address address = addressList.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            }
        }
        return null;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            // Permissions should be given during app startup.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    /**
     * Animates the Google Map camera to the position with the zoom level.
     */
    private void animateMapCamera(float zoom, LatLng latLng) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.animateCamera(cameraUpdate);
    }

    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra(NOTIFICATION_MSG, msg);
        return intent;
    }

    // Start Geofence creation process
    private void startGeofence(LatLng latLng, String geoID) {
        Log.i(TAG, "startGeofence()");
        Geofence geofence = createGeofence(latLng, GEOFENCE_RADIUS, geoID);
        GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
        addGeofence(geofenceRequest);

        List<Geofence> geofences = geofenceRequest.getGeofences();

        for (Geofence geofence1 : geofences) {
            Log.d(TAG, "Geofences: " + geofence1.toString());
        }
    }

    private static final float GEOFENCE_RADIUS = 500.0f; // in meters

    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, float radius, String geoID) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(geoID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent;

        Intent intent = new Intent(this, GeofenceTransitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        // Return false if permission was somehow revoked after accepting in MainActivity.
        return false;
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if (status.isSuccess()) {
            //saveGeofence();
            //drawGeofence();
        } else {
            // inform about fail
        }
    }
}
