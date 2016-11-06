package com.mad.assignment.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mad.assignment.constants.Constants;
import com.mad.assignment.database.GsonHelper;
import com.mad.assignment.model.WorkSite;
import com.mad.assignment.services.LocationTrackerService;
import com.mad.assignment.R;

import java.util.ArrayList;

/**
 * Created by Guan Du 98110291
 * <p>
 * This class handles the MainActivity, which is also the launch activity.
 * It checks user permissions which can start the essential LocationTrackerService.
 * Also sets up the buttons that starts the other activities.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private TextView mActiveWorkSite;
    private TextView mHoursWorked;
    private BroadcastReceiver mHoursWorkedReceiver;
    private BroadcastReceiver mActiveWorkAddressReceiver;
    private GsonHelper mGsonHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mGsonHelper = new GsonHelper(this);

        setupButtons();
        setupTextViews();
        firstTimeCheckPermissions();

        Log.d(TAG, "onCreate()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCurrentlyWorkingToActiveSite();

        // Register the two broadcast receivers.
        LocalBroadcastManager.getInstance(this).registerReceiver(mHoursWorkedReceiver,
                new IntentFilter(Constants.INTENT_FILTER_HOURS_WORKED));

        LocalBroadcastManager.getInstance(this).registerReceiver(mActiveWorkAddressReceiver,
                new IntentFilter(Constants.INTENT_FILTER_ACTIVE_ADDRESS));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the hours worked broadcast receiver when activity is paused.
        if (mHoursWorkedReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mHoursWorkedReceiver);
        }
    }

    /**
     * Sets up the buttons shown in the MainActivity and give them clickListeners.
     */
    private void setupButtons() {

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

        Button viewPeriodPeriodBtn =
                (Button) findViewById(R.id.main_activity_view_previous_period_btn);
        viewPeriodPeriodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PreviousPeriodActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Sets up the TextViews and create broadcast receivers to update their text.
     */
    private void setupTextViews() {
        mActiveWorkSite = (TextView) findViewById(R.id.main_activity_worksite_name);
        mHoursWorked = (TextView) findViewById(R.id.main_activity_hours_worked_timer);

        mHoursWorkedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Updates the hours worked TextView when the user is within a work site.
                double hoursWorked = intent.getDoubleExtra(Constants.EXTRA_HOURS_WORKED, 0);
                mHoursWorked.setText(Double.toString(hoursWorked));
                //Log.d(TAG, "" + hoursWorked);
            }
        };

        mActiveWorkAddressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Displays the location of the work site when the user is in one.
                String activeWorkAddress = intent.getStringExtra(Constants.EXTRA_ACTIVE_ADDRESS);
                mActiveWorkSite.setText(activeWorkAddress);

                // Reset hours worked back to 0 if not in a work site.
                if (activeWorkAddress.equals(getString(R.string.main_activity_not_at_worksite))) {
                    mHoursWorked.setText(getString(R.string.hours_worked_placeholder));
                }

                Log.d(TAG, activeWorkAddress);
            }
        };
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

    /**
     * Callback method after user allows/disable location tracker service.
     * If allowed, start location tracker service.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Start background location tracker service if allowed.
                    startService(new Intent(this, LocationTrackerService.class));

                } else {
                    // Create visual feedback to tell user that app will not work.
                    Toast.makeText(this, getString(R.string.main_activity_perm_not_granted_effects),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    /**
     * Creates an intent by the notification in GeofenceTransitionService.
     */
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Constants.NOTIFICATION_MSG, msg);
        return intent;
    }

    /**
     * Sets the TextView to show a work site that the user is in.
     */
    private void setCurrentlyWorkingToActiveSite() {
        ArrayList<WorkSite> workSites = mGsonHelper.getWorkSitesFromPrefs();
        boolean activeFound = false;

        // Look through all work sites and find the one that is currently active.
        for (WorkSite workSite : workSites) {
            if (workSite.isCurrentlyWorking()) {
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

