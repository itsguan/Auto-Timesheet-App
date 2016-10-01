package com.mad.assignment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LocationsActivity extends AppCompatActivity {

    public static final String LOCATION_PREF = "locationPref";

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

        String[] addresses = getAddressesFromPrefs();

        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, addresses);

        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String[] addresses = getAddressesFromPrefs();

        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, addresses);

        mListView.setAdapter(mAdapter);
        Log.d("tag", "resumed");
    }

    private String[] getAddressesFromPrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences(LOCATION_PREF, Context.MODE_PRIVATE);

        String addressesAsString = sharedPreferences.getString("myList", "Not found");
        return addressesAsString.split(",");
    }
}
