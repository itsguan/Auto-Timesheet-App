package com.mad.assignment.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mad.assignment.constants.Constants;
import com.mad.assignment.R;
import com.mad.assignment.model.WorkSite;

import java.util.ArrayList;
import java.lang.reflect.Type;

public class LocationsActivity extends AppCompatActivity {

    private ListView mListView;
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

        refreshAdapter();

        // Clicking a name in the ListView will remove that entry from the shared prefs.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Retrieve the work site to be deleted from the shared prefs.
                ArrayList<WorkSite> activeWorkSites = getWorkSitesFromPrefs();
                WorkSite toBeDeletedWorkSite = activeWorkSites.get(position);

                // Create a pop-up asking for confirmation.
                createConfirmationWindow(toBeDeletedWorkSite.getAddress(), activeWorkSites,
                        position);
            }
        });
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

    /**
     * Refreshes the listView's adapter by retrieving the Json string from sharedPrefs.
     */
    private void refreshAdapter() {
        ArrayList<String> addresses = getAddressesFromPrefs();
        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, addresses);

        mListView.setAdapter(mAdapter);
    }

    /**
     * Create a confirmation pop-up to ask user if they want to delete a location.
     */
    private void createConfirmationWindow(String address, final ArrayList<WorkSite> activeWorkSites,
                                          final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to delete " + address + "?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // Remove the entry from the given ArrayList.
                        activeWorkSites.remove(position);

                        // Retrieve Json WorkSite list and open connection to shared prefs.
                        Gson gson = new Gson();
                        String jsonWorkSites = gson.toJson(activeWorkSites);
                        SharedPreferences.Editor editor =
                                getSharedPreferences(Constants.LOCATION_PREF, MODE_PRIVATE).edit();

                        // Overwrite the existing Json WorkSite list with the updated list.
                        editor.putString(Constants.JSON_TAG, jsonWorkSites);
                        editor.apply();

                        // Update the list view by refreshing the adapter.
                        refreshAdapter();

                        // Close the pop-up.
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Returns an ArrayList of WorkSite objects stored in the shared prefs.
     */
    private ArrayList<WorkSite> getWorkSitesFromPrefs() {

        // Retrieve the Json String containing a list of WorkSite objects.
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.LOCATION_PREF, Context.MODE_PRIVATE);
        String jsonWorkSites = sharedPreferences.getString(Constants.JSON_TAG, "");
        Log.d("JSONTAG", "jsonWorkSites = " + jsonWorkSites);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<WorkSite>>() {}.getType();

        // Only convert back to a list if the Json string is not empty.
        if (jsonWorkSites != null && jsonWorkSites != "") {
            return gson.fromJson(jsonWorkSites, type);
        }

        // Simple return a new ArrayList if there is nothing in the Json String.
        return new ArrayList<>();
    }

    /**
     * Returns a list of addresses of the work sites stored in the shared prefs.
     */
    private ArrayList<String> getAddressesFromPrefs() {

        // Retrieve the entire list of active work sites first from the shared prefs.
        ArrayList<WorkSite> activeWorkSites = getWorkSitesFromPrefs();
        ArrayList<String> activeWorkSiteAddresses = new ArrayList<>();

        // Retrieve the addresses and store them in a list of Strings.
        for (WorkSite workSite : activeWorkSites) {
            activeWorkSiteAddresses.add(workSite.getAddress());
        }

        return activeWorkSiteAddresses;
    }

    /**
     * Clears the Json list of work sites stored in the SharedPrefs
     */
    private void clearAllLocations() {
        Gson gson = new Gson();
        ArrayList<WorkSite> workSites = new ArrayList<WorkSite>();
        String jsonWorkSites = gson.toJson(workSites);
        SharedPreferences.Editor editor =
                getSharedPreferences(Constants.LOCATION_PREF, MODE_PRIVATE).edit();

        editor.putString(Constants.JSON_TAG, jsonWorkSites);
        editor.apply();
        refreshAdapter();
    }
}
