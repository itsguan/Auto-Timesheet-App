package com.mad.assignment.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mad.assignment.constants.Constants;
import com.mad.assignment.model.WorkSite;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Guan Du 98110291
 *
 * This class contains convenient methods related to Gson.
 * It includes methods that can return a list of WorkSites or overwrite this list.
 */

public class GsonHelper {

    private static final String TAG = GsonHelper.class.getName();

    private Gson mGson;
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    /**
     * Creates a GsonHelper instance given an android context.
     */
    public GsonHelper(Context context) {
        mContext = context;
        mGson = new Gson();
        mSharedPreferences =
                mContext.getSharedPreferences(Constants.LOCATION_PREF, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    /**
     * Returns an ArrayList of WorkSite objects saved in the shared prefs.
     */
    public ArrayList<WorkSite> getWorkSitesFromPrefs() {

        // Retrieve the Json String containing a list of WorkSite objects.
        String jsonWorkSites = mSharedPreferences.getString(Constants.JSON_TAG, "");
        Log.d(TAG, "jsonWorkSites = " + jsonWorkSites);
        Type type = new TypeToken<ArrayList<WorkSite>>() {}.getType();

        // Only convert back to a list if the Json string is not empty.
        if (!jsonWorkSites.equals("")) {
            return mGson.fromJson(jsonWorkSites, type);
        }

        // Simply return a new ArrayList if there is nothing in the Json String.
        return new ArrayList<>();
    }

    /**
     * Overwrites the Json list of WorkSite objects given an ArrayList of WorkSites.
     */
    public void overwriteWorkSitesInPrefs(ArrayList<WorkSite> workSites) {

        // Convert the list to a Json string.
        String jsonWorkSites = mGson.toJson(workSites);

        // Overwrite the existing Json WorkSites string with the updated Json string.
        mEditor.putString(Constants.JSON_TAG, jsonWorkSites);
        mEditor.apply();
    }

    /**
     * Clears the list of WorkSites stored in the shared prefs.
     */
    public void clearAllLocations() {
        ArrayList<WorkSite> workSites = new ArrayList<WorkSite>();
        overwriteWorkSitesInPrefs(workSites);
    }

}
