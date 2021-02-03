package org.unibl.etf.blbustracker.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;

import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.phoneoptions.PermissionCode;

import java.util.List;

public abstract class Utils
{
    /**
     * SharedPreferences is used for storing/reading preferences
     *
     * @return SharedPreferences object
     */
    public static SharedPreferences getSharedPreferences(Context context)
    {
        return androidx.preference.PreferenceManager
                .getDefaultSharedPreferences(context); // default mode is MODE_PRIVATE
    }

    /**
     * Find value in SharedPreferences by key
     *
     * @param key          key we want to search by
     * @param defaultValue if value doesnt exist, return this value
     * @return value found with given key or default value
     */
    public static String getSharedPreferencesKeyValue(String key, String defaultValue, Context context)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * Computes the approximate distance in meters between two points
     *
     * @return distance in meters
     */
    public static double distanceBetweenPoints(LatLng start, LatLng end)
    {
        float[] distance = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, distance);
        return (double) distance[0];
    }

    /**
     * Computes the approximate distance in meters between multiple points
     *
     * @return result in meters
     */
    public static double distanceBetweenPoints(List<LatLng> points)
    {
        if (points.size() < 2)
            return 0;

        float[] distance = new float[1];
        double result = 0;
        for (int i = 0; i < points.size() - 1; i++)
        {
            LatLng start = points.get(i);
            LatLng end = points.get(i + 1);
            Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, distance);
            result += distance[0];
        }

        return result;
    }

    /**
     * Check if user has Google Play Serveces and Google Map installed
     * Show dialog for installing if not installed
     *
     * @return true - user has play services, false user doesnt have play services
     */
    public static boolean isGoogleServicesOK(Activity activity)
    {

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);

        if (available == ConnectionResult.SUCCESS)
        {
            //everything is fine and the user can make map requests
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available))
        {
            //an error occured but we can resolve it
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(activity, available, PermissionCode.ERROR_DIALOG_REQUEST);
            dialog.show();
        } else
        {
            Toast.makeText(activity, R.string.playserveces_error, Toast.LENGTH_SHORT).show();
        }
        return false;
    }


}
