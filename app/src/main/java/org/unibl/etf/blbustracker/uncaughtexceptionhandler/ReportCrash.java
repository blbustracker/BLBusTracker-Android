package org.unibl.etf.blbustracker.uncaughtexceptionhandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import org.unibl.etf.blbustracker.BuildConfig;
import org.unibl.etf.blbustracker.Constants;
import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog.TimeUtil;
import org.unibl.etf.blbustracker.navigationtabs.settings.SettingsFragment;
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
    public static final String STACK_TRACE_FILE_NAME = "crash.stacktrace";

    public static final String SUBJECT = "Crash Report ";

    private static final String CATEGORY = "category";
    private static final String CRASH = "crash";
    private static final String TITLE = "title";
    private static final String CRASH_CONTENT = "content";

    //if file "stack.trace" exists send it to server as category: "crash"
    public void sendReportToServer(Context context)
    {
        try
        {
            String crashContent = getStackTraceFile(context); // get file content if exists

            SharedPreferences sharedPreferences = Utils.getSharedPreferences(context);
            boolean isAutoSendChecked = sharedPreferences.getBoolean(SettingsFragment.AUTO_SEND_REPORT, true);
            boolean isCollectReportChecked = sharedPreferences.getBoolean(SettingsFragment.COLLECT_REPORT, true);

            if (isCollectReportChecked)
            {
                if (isAutoSendChecked)
                    sendReport(context, crashContent);
                else    // show alert dialog and buttons: YES - send crash report to server, NO - delete crash report
                    AlertUtil.showAlertDialog(context, context.getString(R.string.send_crash_report)
                            , (dialog, which) -> sendReport(context, crashContent)
                            , (dialog, which) -> context.deleteFile(STACK_TRACE_FILE_NAME));

            } else if (crashContent != null)
                context.deleteFile(STACK_TRACE_FILE_NAME);

        } catch (IOException ex)
        {
        }
    }

    private void sendReport(Context context, String crashContent)
    {
        if (NetworkStatus.isNetworkAvailable(context))
        {
            NetworkManager networkManager = NetworkManager.getInstance(context);
            JSONObject jsonObject = makeCrashJSONBody(crashContent);
            networkManager.POST(Constants.REPORT_PATH,
                    jsonObject,
                    responseObject -> Toast.makeText(context, context.getString(R.string.report_sent), Toast.LENGTH_LONG).show(),
                    error -> NetworkStatus.errorConnectingToInternet(error, context, false));

            context.deleteFile(STACK_TRACE_FILE_NAME);
        } else
        {
            AlertUtil.showWarningAlert(context, context.getString(R.string.no_internet_try_again));
        }
    }

    // if file stack.trace exists ask user to send email of the crash
    // if file stack.trace does NOT exist, then FileNotFoundException will be thrown and rest of the method's code wont be executed
    private String getStackTraceFile(Context context) throws IOException
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

    private JSONObject makeCrashJSONBody(String crashContent)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            int versionCode = BuildConfig.VERSION_CODE;
            String versionName = BuildConfig.VERSION_NAME;
            jsonObject.put(CATEGORY, CRASH);
            String title = SUBJECT + TimeUtil.getCurrentDateAndTime()
                    + " API: " + Build.VERSION.SDK_INT
                    + " versionCode: " + versionCode
                    + " versionName: " + versionName;
            jsonObject.put(TITLE, title);
            jsonObject.put(CRASH_CONTENT, crashContent);

        } catch (JSONException ex)
        {
            //...
        }
        return jsonObject;
    }

}
