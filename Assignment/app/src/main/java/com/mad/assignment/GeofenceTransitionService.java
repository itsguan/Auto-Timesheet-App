package com.mad.assignment;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class GeofenceTransitionService extends IntentService {

    private static final String TAG = GeofenceTransitionService.class.getSimpleName();

    public static final int GEOFENCE_NOTIFICATION_ID = 0;

    public GeofenceTransitionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        // Handling errors
        if ( geofencingEvent.hasError() ) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
            Log.e( TAG, errorMsg );
            return;
        }

        int geoFenceTransition = geofencingEvent.getGeofenceTransition();

        // Check if the triggered transition is either entering or exiting.
        switch (geoFenceTransition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                // Set the work location to currently working.
                handleTransitionEvent(geofencingEvent, geoFenceTransition, true);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                // Set the currently worked location to false as user is leaving.
                handleTransitionEvent(geofencingEvent, geoFenceTransition, false);
                break;
            default:
                Log.d(TAG, "Transition not known");
                break;
        }
    }

    /**
     * Finds the work site object that has the same address as the triggered geofence.
     * Sets the work site to active or inactive depending on the transition state.
     */
    private void handleTransitionEvent(GeofencingEvent geofencingEvent, int geoFenceTransition,
                                       boolean activeState) {

        Log.d(TAG, "handleTransitionEvent()");

        // Retrieve triggered geofences.
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

        // Find the work site object with the address of the geofence.
        for (Geofence geofence : triggeringGeofences) {
            Log.d(TAG, "GEOID: " + geofence.getRequestId());
            WorkSite activeWorkSite = findWorkSiteWithAddress(geofence.getRequestId());

            if (activeWorkSite != null) {
                saveActiveWorkSiteToSharedPrefs(activeWorkSite, activeState);
            }
            else {
                Log.d(TAG, "Cannot find a work site on the geofence");
            }
        }

        String geofenceTransitionDetails =
                getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences );

        // Send notification details as a String
        sendNotification( geofenceTransitionDetails );
        Log.d(TAG, "triggered geofence");
    }

    private void saveActiveWorkSiteToSharedPrefs(WorkSite activeWorkSite, boolean activeState) {
        // Retrieve all active work sites from shared preferences first.
        SharedPreferences sharedPreferences =
                getSharedPreferences(LocationsActivity.LOCATION_PREF, Context.MODE_PRIVATE);
        ArrayList<WorkSite> workSites = new ArrayList<WorkSite>();
        Gson gson = new Gson();
        String jsonSavedWorkSites =
                sharedPreferences.getString(LocationsActivity.JSON_TAG, "");
        Type type = new TypeToken<ArrayList<WorkSite>>(){}.getType();

        if (!jsonSavedWorkSites.equals("")) {
            // Convert the json string back into a list of work site objects.
            workSites = gson.fromJson(jsonSavedWorkSites, type);

            // Find the index of the work site with the address of triggered site.
            int indexOfTriggeredWorkSite = findIndexOfWorkSite(workSites, activeWorkSite);

            // Set it to currently working within the list.
            workSites.get(indexOfTriggeredWorkSite).setCurrentlyWorking(activeState);

            // Convert the new work site list into a Json string.
            String jsonWorkSites = gson.toJson(workSites);

            // Overwrite the old Json string with the new updated list.
            SharedPreferences.Editor editor =
                    getSharedPreferences(LocationsActivity.LOCATION_PREF, MODE_PRIVATE).edit();
            editor.putString(LocationsActivity.JSON_TAG, jsonWorkSites);
            editor.commit();
        }
    }

    /**
     * Finds the index of a work site within a list by matching their address.
     */
    private int findIndexOfWorkSite(List<WorkSite> sites, WorkSite activeSite) {

        for(int i = 0; i < sites.size(); i++) {

            if (sites.get(i).getAddress().equals(activeSite.getAddress())) {
                return i;
            }
        }

        return -1;
    }

    private void showToastAtGeofence(GeofencingEvent geofencingEvent) {
        Location locationOfGeofence = geofencingEvent.getTriggeringLocation();
        LatLng latLng = new LatLng(locationOfGeofence.getLatitude(), locationOfGeofence.getLongitude());
        String latLngMsg = "Lat: " + latLng.latitude + "Lng: " + latLng.longitude;
        Toast.makeText(getApplicationContext(), latLngMsg, Toast.LENGTH_LONG).show();
        Log.d(TAG, latLngMsg);
    }

    /**
     * Finds a work site object from the shared prefs with the given address.
     */
    private WorkSite findWorkSiteWithAddress(String address) {

        // Retrieve all active work sites from shared preferences first.
        SharedPreferences sharedPreferences =
                getSharedPreferences(LocationsActivity.LOCATION_PREF, Context.MODE_PRIVATE);
        ArrayList<WorkSite> workSites = new ArrayList<WorkSite>();
        Gson gson = new Gson();
        String jsonSavedWorkSites =
                sharedPreferences.getString(LocationsActivity.JSON_TAG, "");
        Type type = new TypeToken<ArrayList<WorkSite>>(){}.getType();

        if (!jsonSavedWorkSites.equals("")) {
            Log.d(TAG, "Searching for Json work site.");
            // Convert the json string back into a list of work site objects.
            workSites = gson.fromJson(jsonSavedWorkSites, type);

            // Look through all work sites and find the one with the same address.
            for (WorkSite workSite : workSites) {
                Log.d(TAG, "'" + workSite.getAddress() + "'");
                if (workSite.getAddress().equals(address)) {
                    return workSite;
                }
            }
        }
        return null;
    }

    private String getGeofenceTrasitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for ( Geofence geofence : triggeringGeofences ) {
            triggeringGeofencesList.add( geofence.getRequestId() );
        }

        String status = null;
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER )
            status = "Entering ";
        else if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT )
            status = "Exiting ";
        return status + TextUtils.join( ", ", triggeringGeofencesList);
    }

    private void sendNotification( String msg ) {
        Log.i(TAG, "sendNotification: " + msg );

        // Intent to start the main Activity
        Intent notificationIntent = MapsActivity.makeNotificationIntent(
                getApplicationContext(), msg
        );

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MapsActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificatioMng.notify(
                GEOFENCE_NOTIFICATION_ID,
                createNotification(msg, notificationPendingIntent));

    }

    // Create notification
    private Notification createNotification(String msg, PendingIntent notificationPendingIntent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder
                .setSmallIcon(R.drawable.ic_action_location)
                .setColor(Color.RED)
                .setContentTitle(msg)
                .setContentText("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }


    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }
}