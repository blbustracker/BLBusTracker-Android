package org.unibl.etf.blbustracker.networkmanager;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.unibl.etf.blbustracker.Constants;

/**
 * Used for fetching data from the server, methode GETJson(...)
 */
public class NetworkManager
{
    private static NetworkManager singletonInstance = null;
    private RequestQueue requestQueue;

    //private constructor
    private NetworkManager(Context context)
    {
        requestQueue = Volley.newRequestQueue(context);
    }

    // singleton pattern
    public static synchronized NetworkManager getInstance(Context context)
    {
        if (singletonInstance == null)
            singletonInstance = new NetworkManager(context);

        return singletonInstance;
    }

    public synchronized void destroyInstance()
    {
        singletonInstance = null;
    }

    /**
     * Request is executed on seperate Thread, while Response (success/error) is always on Main Thread
     *
     * @param urlPath path that comes after BASE_URL
     * @param body    null for GET methode
     * @param success if response from server was successful, execute this method with server content
     * @param error   if there was an error while fetching the content, execute this method
     */
    public void GETJson(String urlPath, JSONObject body, ResponseCallback success, Response.ErrorListener error)
    {
        request(Request.Method.GET, urlPath, body, success, error);
    }

    public void GETJsonArray(String urlPath, Response.Listener<JSONArray> success, Response.ErrorListener error)
    {
        String fullURl = Constants.BASE_URL + urlPath;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, fullURl, null, success, error);
        requestQueue.add(jsonArrayRequest);
    }

    /**
     * @param urlPath path that comes after BASE_URL
     * @param body    Body for post request that is being sent to the server
     * @param success if response from server was successful, execute this method with server content
     * @param error   if there was an error while fetching the content, execute this method
     */
    public void POST(String urlPath, JSONObject body, ResponseCallback success, Response.ErrorListener error)
    {
        request(Request.Method.POST, urlPath, body, success, error);
    }


    private void request(int code, String urlPath, JSONObject body, ResponseCallback success, Response.ErrorListener error)
    {
        String fullURl = Constants.BASE_URL + urlPath;
        JsonObjectRequest request = new JsonObjectRequest(
                code,
                fullURl,
                body,
                success::successResponse,
                error);

        requestQueue.add(request);
    }


}
