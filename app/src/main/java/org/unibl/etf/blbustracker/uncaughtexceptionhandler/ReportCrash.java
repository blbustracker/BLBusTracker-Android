package org.unibl.etf.blbustracker.uncaughtexceptionhandler;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import org.unibl.etf.blbustracker.Constants;
import org.unibl.etf.blbustracker.networkmanager.NetworkManager;
import org.unibl.etf.blbustracker.networkmanager.NetworkStatus;
import org.unibl.etf.blbustracker.utils.AlertUtil;
import org.unibl.etf.blbustracker.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReportCrash
{
    //TODO: maybe make nonstatic methods
    // check settings option for sending report

    public static final String STACK_TRACE_FILE_NAME = "crash.stacktrace";

    public static final String SUBJECT = "Crash Report ";

    private static final String CATEGORY = "category";
    private static final String CRASH = "crash";
    private static final String TITLE = "title";
    private static final String CRASH_CONTENT = "content";


    //if file "stack.trace" exists send it to server as category: "crash"
    public static void sendReportToServer(Context context)
    {
        try
        {
            String crashContent = getStackTraceFile(context);
            AlertUtil.showAlertDialog(context, "Send crash report?", (dialog, which) ->
                    {
                        if (NetworkStatus.isNetworkAvailable(context))
                        {
                            NetworkManager networkManager = NetworkManager.getInstance(context);
                            JSONObject jsonObject = makeCrashJSONBody(crashContent);
                            networkManager.POST(Constants.REPORT_PATH,
                                    jsonObject,
                                    responseObject -> Toast.makeText(context, "Report sent!", Toast.LENGTH_LONG).show(),
                                    error -> NetworkStatus.errorConnectingToInternet(error, context));

                            context.deleteFile(STACK_TRACE_FILE_NAME);
                        } else
                        {
                            AlertUtil.showWarningAlert(context, "Current there is no internet connection please try again later");
                        }
                    }
                    , (dialog, which) -> context.deleteFile(STACK_TRACE_FILE_NAME));

        } catch (IOException ex)
        {
//            ex.printStackTrace();
        }

    }

    // if file stack.trace exists ask user to send email of the crash
    // if file stack.trace does NOT exist, then FileNotFoundException will be thrown and rest of the method's code wont be executed
    private static String getStackTraceFile(Context context) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.openFileInput(STACK_TRACE_FILE_NAME)));
        String line;
        StringBuilder stringBuilder = new StringBuilder();

        while ((line = reader.readLine()) != null)
        {
            stringBuilder.append(line).append("\n");
        }

        reader.close();
        return stringBuilder.toString();
    }

    private static JSONObject makeCrashJSONBody(String crashContent)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put(CATEGORY, CRASH);
            String title = SUBJECT + Utils.getCurrentDateAndTime() + "; API: " + Build.VERSION.SDK_INT;
            jsonObject.put(TITLE, title);
            jsonObject.put(CRASH_CONTENT, crashContent);

        } catch (JSONException ex)
        {
            //...
        }
        return jsonObject;
    }

}
