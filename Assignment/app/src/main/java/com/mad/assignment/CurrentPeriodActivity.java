package com.mad.assignment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class CurrentPeriodActivity extends AppCompatActivity {

    private ListView mListView;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_period);

        mListView = (ListView) findViewById(R.id.current_period_activity_list_view);

        refreshAdapter();
    }

    /**
     * Refreshes the listView's adapter by retrieving the Json string from sharedPrefs.
     */
    private void refreshAdapter() {
        List<WorkSite> workSites = WorkSite.listAll(WorkSite.class);
        List<String> addresses = new ArrayList<>();

        for (WorkSite workSite : workSites) {
            addresses.add(workSite.getAddress());
        }

        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, addresses);

        mListView.setAdapter(mAdapter);
    }
}
