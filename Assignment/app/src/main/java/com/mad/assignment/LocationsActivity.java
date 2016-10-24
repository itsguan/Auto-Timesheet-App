package com.mad.assignment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;

public class LocationsActivity extends AppCompatActivity {

    public static final String LOCATION_PREF = "locationPref";
    public static final String JSON_TAG = "myList";

    ListView mListView;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LocationsActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        mListView = (ListView) findViewById(R.id.locations_activity_list_view);

        ArrayList<String> addresses = getAddressesFromPrefs();

        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, addresses);

        mListView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.locations_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_clear_all:
                clearAllLocations();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAdapter();
        Log.d("tag", "resumed");
    }

    private void refreshAdapter() {
        ArrayList<String> addresses = getAddressesFromPrefs();
        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, addresses);

        mListView.setAdapter(mAdapter);
    }

    private ArrayList<String> getAddressesFromPrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences(LOCATION_PREF, Context.MODE_PRIVATE);

        /*
        String addressesAsString = sharedPreferences.getString("myList", "Not found");
        return addressesAsString.split(",");
        */
        String jsonWorkSites = sharedPreferences.getString(JSON_TAG, "");
        Log.d("JSONTAG", "jsonWorkSites = " + jsonWorkSites);

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<WorkSite>>(){}.getType();
        ArrayList<String> workSiteAddresses = new ArrayList<String>();

        if (jsonWorkSites != null && jsonWorkSites != "") {
            ArrayList<WorkSite> workSites = gson.fromJson(jsonWorkSites, type);

            for (WorkSite workSite : workSites) {
                workSiteAddresses.add(workSite.getAddress());
            }
        }

        return workSiteAddresses;
    }

    /**
     * Clears the Json list of work sites stored in the SharedPrefs
     */
    private void clearAllLocations() {
        Gson gson = new Gson();
        ArrayList<WorkSite> workSites = new ArrayList<WorkSite>();
        String jsonWorkSites = gson.toJson(workSites);
        SharedPreferences.Editor editor =
                getSharedPreferences(LocationsActivity.LOCATION_PREF, MODE_PRIVATE).edit();

        editor.putString(JSON_TAG, jsonWorkSites);
        editor.commit();
        refreshAdapter();
    }
}
