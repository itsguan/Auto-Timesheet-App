package com.mad.assignment.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mad.assignment.R;
import com.mad.assignment.database.GsonHelper;
import com.mad.assignment.model.WorkSite;

import java.util.ArrayList;

/**
 * Created by Guan Du 98110291
 *
 * This class handles the LocationsActivity by managing its ListView and updating its adapter
 * with the appropriate List of WorkSites from the Shared Preferences.
 */

public class LocationsActivity extends AppCompatActivity {

    private ListView mListView;
    private GsonHelper mGsonHelper;

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

        mGsonHelper = new GsonHelper(this);
        mListView = (ListView) findViewById(R.id.locations_activity_list_view);

        // Clicking a name in the ListView will remove that entry from the shared prefs.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Retrieve the work site to be deleted from the shared prefs.
                ArrayList<WorkSite> activeWorkSites = mGsonHelper.getWorkSitesFromPrefs();
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
        refreshList();
    }

    /**
     * Refreshes the listView's adapter by retrieving the Json string from sharedPrefs.
     */
    private void refreshList() {
        ArrayList<String> addresses = getAddressesFromPrefs();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, addresses);

        mListView.setAdapter(adapter);
    }

    /**
     * Create a confirmation pop-up to ask user if they want to delete a location.
     */
    private void createConfirmationWindow(final String address, final ArrayList<WorkSite> activeWorkSites,
                                          final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.locations_activity_popup_confirmation_msg)
                + address + "?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                getString(R.string.locations_activity_popup_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // Remove the entry from the given ArrayList.
                        activeWorkSites.remove(position);

                        // Use the helper class to overwrite the old Json list.
                        mGsonHelper.overwriteWorkSitesInPrefs(activeWorkSites);

                        // Update the list view by refreshing the adapter.
                        refreshList();

                        // Close the pop-up.
                        dialog.cancel();

                        // Provide visual feedback of deletion.
                        Toast.makeText(getApplicationContext(), address +
                                getString(R.string.locations_activity_toast_feedback_suffix),
                                Toast.LENGTH_LONG).show();
                    }
                });

        builder.setNegativeButton(
                getString(R.string.locations_activity_popup_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Returns a list of addresses of the work sites stored in the shared prefs.
     */
    private ArrayList<String> getAddressesFromPrefs() {

        // Retrieve the entire list of active work sites first using the helper class.
        ArrayList<WorkSite> activeWorkSites = mGsonHelper.getWorkSitesFromPrefs();
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
        mGsonHelper.clearAllLocations();
        refreshList();
    }
}
