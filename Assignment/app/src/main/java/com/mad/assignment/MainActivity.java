package com.mad.assignment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

        startService(new Intent(this, LocationTrackerService.class));
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
}
