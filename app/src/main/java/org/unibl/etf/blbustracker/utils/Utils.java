package org.unibl.etf.blbustracker.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.unibl.etf.blbustracker.Constants;
import org.unibl.etf.blbustracker.R;

public abstract class Utils
{
    public static final int ERROR_DIALOG_REQUEST = 9003;

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

    public static void shortButtonDisableOnClick(Activity activity, ImageButton button)
    {
        if (activity != null && !activity.isFinishing())
        {
            Handler mainHandler = new Handler(activity.getMainLooper());
            button.setEnabled(false);
            mainHandler.postDelayed(() ->
            {
                if (activity != null && !activity.isFinishing())
                    button.setEnabled(true);
            }, Constants.MINOR_BUTTON_DELAY);
        }
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

    //set view size based on size_percentage
    public static void ajustViewSize(View view, float size_percentage, Activity activity)
    {
        Rect displayRectangle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        view.setMinimumHeight((int) (displayRectangle.height() * size_percentage));
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
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(activity, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else
        {
            Toast.makeText(activity, R.string.playserveces_error, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}
