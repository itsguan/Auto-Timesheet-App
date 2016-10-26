package com.mad.assignment.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ArrayAdapter;

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

        mRecyclerView = (RecyclerView) findViewById(R.id.current_period_activity_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWorkLogAdapter = new WorkLogAdapter(this, mWorkLogList);
        //refreshList();
        mRecyclerView.setAdapter(mWorkLogAdapter);


        //refreshAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //refreshAdapter();
        refreshList();
    }

    private void refreshList() {
        mWorkLogList = WorkSite.listAll(WorkSite.class);
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
