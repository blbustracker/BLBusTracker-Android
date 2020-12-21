package org.unibl.etf.blbustracker.networkmanager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;


public class NetworkStatus
{
    private final Context context;

    // declare it where you need internet onAvailable
    @RequiresApi(api = Build.VERSION_CODES.M)
    private ConnectivityManager.NetworkCallback networkCallback;

    //use listen() when internet check is needed, use stopListening when internet is no longer needed
    private ConnectivityManager connectivityManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public NetworkStatus(Context context, ConnectivityManager.NetworkCallback networkCallback)
    {
        this.context = context;
        this.networkCallback = networkCallback;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }


    //start listener which calls methods from networkCallback like onAvailable, onLost,...
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void listen()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else
        {
            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    .build();
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        }
    }

    //stop listener, call it when internet listener is no longer needed, like in Fragment method OnDestroy(...)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void stopListening()
    {
        try
        {
            if (networkCallback != null)
                connectivityManager.unregisterNetworkCallback(networkCallback);
            networkCallback = null;
        } catch (Exception ex)
        {
        }
    }

    /**
     * Check if there is an internet connection
     *
     * @return true - has internet, false - no internet
     */
    public static boolean isNetworkAvailable(Context context) //for android < 6.0, use callback for >= 6.0
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } else
        {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        }

    }

    private static final long timePeriod = 5 * 1000;
    private static long oldCurrentTime;

    /* error message when volley fails to connect to internet */
    public static void errorConnectingToInternet(VolleyError volleyError, Context context)
    {
        String message = null;
        long currentTime = System.currentTimeMillis();
        if (currentTime - oldCurrentTime > timePeriod)
        {
            oldCurrentTime = currentTime;
            if (volleyError instanceof TimeoutError || volleyError instanceof NoConnectionError)
            {
                message = "Cannot connect to Internet...Please check your connection!";
            } else if (volleyError instanceof ServerError)
            {
                message = "The server could not be found. Please try again after some time!!";
            } else if (volleyError instanceof AuthFailureError)
            {
                message = "Cannot connect to Internet...Please check your connection!";
            } else if (volleyError instanceof ParseError)
            {
                message = "Parsing error! Please try again after some time!!";
            } else if (volleyError instanceof NetworkError)
            {
                message = "Cannot connect to Internet...Please check your connection!";
            }
            String finalMessage = message;
            new Handler(Looper.getMainLooper()).post(() ->
            {
                System.out.println(finalMessage);
//                Toast.makeText(context, finalMessage, Toast.LENGTH_SHORT).show();
            });
        }
    }
}
