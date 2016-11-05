package com.mad.assignment.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.google.gson.reflect.TypeToken;
import com.mad.assignment.constants.Constants;
import com.mad.assignment.services.LocationTrackerService;
import com.mad.assignment.R;
import com.mad.assignment.model.WorkSite;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private TextView mActiveWorkSite;
    private TextView mHoursWorked;
    private BroadcastReceiver mHoursWorkedReceiver;
    private BroadcastReceiver mActiveWorkAddressReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        setupButtons();
        setupTextViews();
        firstTimeCheckPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();

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
                double hoursWorked = intent.getDoubleExtra(Constants.EXTRA_HOURS_WORKED, 0);
                mHoursWorked.setText(Double.toString(hoursWorked));
                Log.d("MAIN", ""+hoursWorked);
            }
        };

        mActiveWorkAddressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String activeWorkAddress = intent.getStringExtra(Constants.EXTRA_ACTIVE_ADDRESS);
                mActiveWorkSite.setText(activeWorkAddress);

                if (activeWorkAddress.equals(getString(R.string.main_activity_not_at_worksite))) {
                    mHoursWorked.setText("0");
                }
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
                    Toast.makeText(this, R.string.main_activity_perm_not_granted_effects,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}
