package org.unibl.etf.blbustracker.navigationtabs.reportproblem;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.unibl.etf.blbustracker.Constants;
import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.networkmanager.NetworkManager;
import org.unibl.etf.blbustracker.networkmanager.NetworkStatus;

import org.json.JSONException;
import org.json.JSONObject;

public class ReportProblemModel
{
    private static final String TAG = "ReportProblemModel";

    private static final String CATEGORY = "category";
    private static final String REPORT = "report";
    private static final String REPORT_TITLE = "title";
    private static final String REPORT_CONTENT = "content";
    public static final String RESPONSE_MESSAGE = "message";
    private Context context;

    private NetworkManager networkManager;
    private NetworkStatus networkStatus;

    private JSONObject jsonPOSTBody;

    //called when internet is available
    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback()
    {
        @Override
        public void onAvailable(@NonNull Network network)
        {
            super.onAvailable(network);
            networkManager.POST(Constants.REPORT_PATH, jsonPOSTBody,
                    response -> successfulResponse(response),
                    error -> NetworkStatus.errorConnectingToInternet(error,context));
        }
    };

    public ReportProblemModel(Context context)
    {
        this.context = context;
        networkManager = NetworkManager.getInstance(context);
    }

    public void successfulResponse(JSONObject object)
    {
        String message = null;
        try
        {
            message = object.getString(RESPONSE_MESSAGE);
        } catch (JSONException ex)
        {
            ex.printStackTrace();
        }
        if (RESPONSE_MESSAGE.equals(message))
            message = context.getString(R.string.problem_reported);

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        Log.d(TAG, "successfulResponse: Problem reported!");
    }

    void startListening()
    {
        if (networkStatus == null)
            networkStatus = new NetworkStatus(context, networkCallback);
        networkStatus.listen();
    }

    void stopListening()
    {
        if(networkCallback!=null)
            networkStatus.stopListening();

    }

    public void setPOSTBody(String title,String message)
    {
        this.jsonPOSTBody = makeJSONBody(title,message);
    }

    private JSONObject makeJSONBody(String title,String message)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put(CATEGORY,REPORT);
            jsonObject.put(REPORT_TITLE, title);
            jsonObject.put(REPORT_CONTENT, message);
        } catch (JSONException ex)
        {
            ex.printStackTrace();
        }
        return jsonObject;
    }
}
