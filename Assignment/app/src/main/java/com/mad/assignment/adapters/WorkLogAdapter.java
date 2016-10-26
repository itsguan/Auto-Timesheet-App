package com.mad.assignment.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mad.assignment.R;
import com.mad.assignment.model.WorkSite;

import java.util.List;

/**
 * Created by Guan on 26/10/2016.
 */

public class WorkLogAdapter extends RecyclerView.Adapter<WorkLogAdapter.ViewHolder> {

    private Context mContext;
    private List<WorkSite> mWorkSiteList;

    private static final String LATE = "Late";
    private static final int SINGLE_SLEEP_TIME = 2000;

    /**
     * Represents a single row that will display a work site object's fields.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView date, location, hours;

        /**
         * References the text views that will hold a work site's information.
         */
        public ViewHolder(View view) {
            super(view);

            date = (TextView) view.findViewById(R.id.work_log_date_tv);
            location = (TextView) view.findViewById(R.id.work_log_location_tv);
            hours = (TextView) view.findViewById(R.id.work_log_hours_tv);
        }
    }


    /**
     * Sets the fields of the class with the arguments supplied by the caller.
     */
    public WorkLogAdapter(Context context, List workSites) {

        mContext = context;
        mWorkSiteList = workSites;
    }

    /**
     * Returns a train object by giving it a position on the Recycler View
     */
    public WorkSite getItem(int position) {
        return mWorkSiteList != null ? mWorkSiteList.get(position) : null;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.work_log_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WorkSite workSite = mWorkSiteList.get(position);

        holder.date.setText(workSite.getDateWorked());
        holder.location.setText(workSite.getAddress());
        holder.hours.setText(Double.toString(workSite.getHoursWorked()));
    }

    @Override
    public int getItemCount() {
        return mWorkSiteList.size();
    }
}