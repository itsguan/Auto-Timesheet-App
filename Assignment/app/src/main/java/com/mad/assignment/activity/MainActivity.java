package com.mad.assignment.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mad.assignment.constants.Constants;
import com.mad.assignment.constants.GlobalGoogleApiClient;
import com.mad.assignment.services.LocationTrackerService;
import com.mad.assignment.R;
import com.mad.assignment.model.WorkSite;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView mActiveWorkSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        GlobalGoogleApiClient globalGoogleApiClient = new GlobalGoogleApiClient(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button setLocationsBtn = (Button) findViewById(R.id.main_activity_set_locations_btn);
        setLocationsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LocationsActivity.class);
                startActivity(intent);
            }
        });

        Button viewCurrentPeriodBtn =
                (Button) findViewById(R.id.main_activity_view_current_period_btn);
        viewCurrentPeriodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CurrentPeriodActivity.class);
                startActivity(intent);
            }
        });

        mActiveWorkSite = (TextView) findViewById(R.id.main_activity_worksite_name);

        firstTimeCheckPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCurrentlyWorkingToActiveSite();
    }

    /**
     * Sets the TextView to show a work site that the user is in.
     */
    private void setCurrentlyWorkingToActiveSite() {

        // Retrieve existing work sites first.
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.LOCATION_PREF, Context.MODE_PRIVATE);
        ArrayList<WorkSite> workSites = new ArrayList<WorkSite>();
        Gson gson = new Gson();
        String jsonSavedWorkSites =
                sharedPreferences.getString(Constants.JSON_TAG, "");
        Type type = new TypeToken<ArrayList<WorkSite>>(){}.getType();

        if (!jsonSavedWorkSites.equals("")) {
            workSites = gson.fromJson(jsonSavedWorkSites, type);
            boolean activeFound = false;

            // Look through all work sites and find the one that is currently active.
            for (WorkSite workSite : workSites) {
                if (workSite.isCurrentlyWorking() == true) {
                    mActiveWorkSite.setText(workSite.getAddress());
                    activeFound = true;
                }
            }

            // If no active sites were found, set text to not at a worksite.
            if (!activeFound) {
                mActiveWorkSite.setText(R.string.main_activity_not_at_worksite);
            }
        }
    }

    /**
     * Checks if user has ACCESS_FINE_LOCATION permission. If they don't, ask for permission.
     * If it is granted, start LocationTrackerService in the callback.
     */
    private void firstTimeCheckPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Required permissions are granted. Start LocationTrackerService.
            startService(new Intent(this, LocationTrackerService.class));
        } else {
            // Ask user to accept access fine location permissions.
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.REQ_PERMISSION
            );
        }
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    startService(new Intent(this, LocationTrackerService.class));

                } else {
                    // Permission denied
                    Toast.makeText(this, "Work Site Location detection will not work",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}
