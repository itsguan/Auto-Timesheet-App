package com.mad.assignment.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;
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
    private Button mSaveToPrevPeriodBtn;

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
        mSaveToPrevPeriodBtn = (Button) findViewById(R.id.current_period_activity_save_to_prev_btn);
        mSaveToPrevPeriodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<WorkSite> allWorkSites = WorkSite.listAll(WorkSite.class);

                String addresses = "";
                for (WorkSite workSite : allWorkSites) {
                    addresses += workSite.getAddress() + ", ";
                }

                Log.d(TAG, "Before: " +addresses);

                List<WorkSite> toBePrevWorkSites = new ArrayList<WorkSite>();

                // Set currentPeriod to false for ALL entries as new entries are auto true.
                for (WorkSite toBePrevWorkSite : allWorkSites) {
                    toBePrevWorkSite.setCurrentPeriod(false);
                    toBePrevWorkSite.save();
                }

                allWorkSites = WorkSite.listAll(WorkSite.class);

                String addresses2 = "";
                for (WorkSite workSite : allWorkSites) {
                    addresses2 += workSite.getAddress() + ", ";
                }

                Log.d(TAG, "After: " +addresses2);

                refreshList();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //refreshAdapter();
        refreshList();
    }

    private void refreshList() {
        //mWorkLogList = WorkSite.listAll(WorkSite.class);
        mWorkLogList.clear();



        for (int i = 0; i < 5; i++) {
            //mWorkLogList.add(tempWorkSite);
            WorkSite tempWorkSite = new WorkSite("Test" + i, new LatLng(100, 100));
            tempWorkSite.save();
        }

        List<WorkSite> allWorkSites = WorkSite.listAll(WorkSite.class);

        for (WorkSite currentWorkSite : allWorkSites) {
            if (currentWorkSite.isCurrentPeriod()) {
                mWorkLogList.add(currentWorkSite);
            }
        }

        Log.d(TAG, Integer.toString(mWorkLogList.size()));
        mWorkLogAdapter = new WorkLogAdapter(this, mWorkLogList);
        mRecyclerView.setAdapter(mWorkLogAdapter);

        //mWorkLogAdapter.notifyDataSetChanged();
    }

    /**
     * Refreshes the listView's adapter by retrieving the Json string from sharedPrefs.
     */
    /*
    private void refreshAdapter() {
        List<WorkSite> workSites = WorkSite.listAll(WorkSite.class);
        List<String> addresses = new ArrayList<>();

        for (WorkSite workSite : workSites) {
            addresses.add(workSite.getAddress());
        }

        mWorkLogAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, addresses);

        mRecyclerView.setAdapter(mWorkLogAdapter);
    }*/
}
