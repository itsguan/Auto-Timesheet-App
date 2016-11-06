package com.mad.assignment.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mad.assignment.R;
import com.mad.assignment.adapters.WorkLogAdapter;
import com.mad.assignment.model.WorkSite;

import java.util.ArrayList;
import java.util.List;

public class CurrentPeriodActivity extends AppCompatActivity {

    private static final String TAG = CurrentPeriodActivity.class.getName();

    private RecyclerView mRecyclerView;
    private WorkLogAdapter mWorkLogAdapter;
    private List<WorkSite> mWorkLogList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_period);

        // Setup the RecyclerView and attach a custom adapter to it.
        mRecyclerView = (RecyclerView) findViewById(R.id.current_period_activity_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWorkLogAdapter = new WorkLogAdapter(this, mWorkLogList);
        mRecyclerView.setAdapter(mWorkLogAdapter);

        // Setup the save to previous period button.
        Button saveToPrevPeriodBtn = (Button) findViewById(R.id.current_period_activity_save_to_prev_btn);
        saveToPrevPeriodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToPreviousPeriod();
            }
        });

        // Create some generic entries for demonstration purposes.
        createFirstEntries();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    /**
     * Updates the adapter and its attached list by retrieving a new updated list.
     */
    private void refreshList() {

        // Clear the entire adapter list in preparation for a refresh.
        mWorkLogList.clear();

        // Retrieve the updated list of ALL work entries with SugarORM.
        List<WorkSite> allWorkSites = WorkSite.listAll(WorkSite.class);

        // Add the entries that are in the currentPeriod to the visible list.
        for (WorkSite currentWorkSite : allWorkSites) {
            if (currentWorkSite.isCurrentPeriod()) {
                mWorkLogList.add(currentWorkSite);
            }
        }

        // Reattach the updated list to the custom list adapter.
        mWorkLogAdapter = new WorkLogAdapter(this, mWorkLogList);
        mRecyclerView.setAdapter(mWorkLogAdapter);
    }

    /**
     * Saves the current entries to the previous period entries.
     */
    private void saveToPreviousPeriod() {
        List<WorkSite> allWorkSites = WorkSite.listAll(WorkSite.class);
        String toastMessage = "";

        // Set currentPeriod to false for ALL entries as new entries are auto true.
        for (WorkSite toBePrevWorkSite : allWorkSites) {
            toBePrevWorkSite.setCurrentPeriod(false);
            toBePrevWorkSite.save();
        }

        // Select an appropriate toast message.
        switch (mWorkLogList.size()) {
            case 0:
                toastMessage = getString(R.string.current_period_activity_no_entries_toast_msg);
                break;
            default:
                toastMessage = getString(R.string.current_period_activity_entries_saved_toast_msg);
                break;
        }

        // Provide visual feedback in the form of a toast.
        Toast toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG);
        TextView toastView = (TextView) toast.getView().findViewById(android.R.id.message);
        if( toastView != null) toastView.setGravity(Gravity.CENTER);
        toast.show();

        refreshList();
    }

    /**
     * Sets up three generic work entries if the DB is new.
     */
    private void createFirstEntries() {
        List<WorkSite> allWorkSites = WorkSite.listAll(WorkSite.class);

        if (allWorkSites.size() == 0) {
            WorkSite genericWorkSite1 = new WorkSite(getString(R.string.generic_address_1),
                    getString(R.string.generic_date_1), 7);
            genericWorkSite1.save();

            WorkSite genericWorkSite2 = new WorkSite(getString(R.string.generic_address_2),
                    getString(R.string.generic_date_2), 6);
            genericWorkSite2.save();

            WorkSite genericWorkSite3 = new WorkSite(getString(R.string.generic_address_3),
                    getString(R.string.generic_date_3), 7);
            genericWorkSite3.save();
        }
    }
}
