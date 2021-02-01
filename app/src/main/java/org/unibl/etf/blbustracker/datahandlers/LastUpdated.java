package org.unibl.etf.blbustracker.datahandlers;

import android.content.Context;
import android.content.SharedPreferences;

import org.unibl.etf.blbustracker.utils.Utils;

import org.json.JSONObject;

/**
 * used for checking if there was an update
 * comparing given updated with one in internal memory, and saving new date if there was an update
 */
public class LastUpdated
{
    private SharedPreferences sharedPreferences;

    private static final String LAST_UPDATE_JSONString = "lastUpdate";

    //used as arguemnts for checkIfContentUpdated(...)
    public static final String BUSSTOP_LAST_UPDATE_KEY = "busstop_last_update";
    public static final String ROUTES_LAST_UPDATE_KEY = "routes_last_update";
    public static final String NEWS_LAST_UPDATE_KEY = "announcements_last_update";

    public LastUpdated(Context context)
    {
        sharedPreferences = Utils.getSharedPreferences(context);
    }

    /**
     * Check if date in sharedpreferences (internal memory) is equale as lastUpdateJsonObject
     * @param lastUpdateJsonObject result from networkManager.getJSON when checking lastUpdate
     * @param sharedPreferencesKey key with which we compare JsonObject (BUSSTOP_LAST_UPDATE_KEY, ROUTES_LAST_UPDATE_KEY, NEWS_LAST_UPDATE_KEY)
     * @return true - there was an update, false - no updates
     */
    public boolean isServerContentUpdated(JSONObject lastUpdateJsonObject, String sharedPreferencesKey)
    {
        if(lastUpdateJsonObject==null)
            return false;

        String serverContent= lastUpdateJsonObject.toString();
        String localContent = sharedPreferences.getString(sharedPreferencesKey, "");

        return !localContent.equals(serverContent);

    }

    /**
     * save content (date) in key
     * @param content new date lastUpdate
     * @param sharedPreferencesKey (BUSSTOP_LAST_UPDATE_KEY, ROUTES_LAST_UPDATE_KEY, NEWS_LAST_UPDATE_KEY)
     */
    public void updateSharedPrefenreces(String content,String sharedPreferencesKey)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(sharedPreferencesKey,content);
        editor.apply();
    }

}