package com.mad.assignment.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.mad.assignment.R;
import com.mad.assignment.adapters.WorkLogAdapter;
import com.mad.assignment.model.WorkSite;

import java.util.ArrayList;
import java.util.List;

public class PreviousPeriodActivity extends AppCompatActivity {

    private static final String TAG = PreviousPeriodActivity.class.getName();

    private RecyclerView mRecyclerView;
    private WorkLogAdapter mWorkLogAdapter;
    private List<WorkSite> mWorkLogList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_period);

        // Setup the RecyclerView and attach a custom adapter to it.
        mRecyclerView = (RecyclerView) findViewById(R.id.previous_period_activity_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWorkLogAdapter = new WorkLogAdapter(this, mWorkLogList);
        mRecyclerView.setAdapter(mWorkLogAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        mWorkLogList.clear();

        List<WorkSite> allWorkSites = WorkSite.listAll(WorkSite.class);

        for (WorkSite previousWorkSite : allWorkSites) {
            if (!previousWorkSite.isCurrentPeriod()) {
                mWorkLogList.add(previousWorkSite);
            }
        }

        Log.d(TAG, Integer.toString(mWorkLogList.size()));
        mWorkLogAdapter = new WorkLogAdapter(this, mWorkLogList);
        mRecyclerView.setAdapter(mWorkLogAdapter);
    }
}
