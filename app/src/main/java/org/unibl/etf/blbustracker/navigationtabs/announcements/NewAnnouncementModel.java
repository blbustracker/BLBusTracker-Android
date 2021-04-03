package org.unibl.etf.blbustracker.navigationtabs.announcements;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.unibl.etf.blbustracker.Constants;
import org.unibl.etf.blbustracker.datahandlers.LastUpdated;
import org.unibl.etf.blbustracker.networkmanager.NetworkManager;
import org.unibl.etf.blbustracker.networkmanager.NetworkStatus;
import org.unibl.etf.blbustracker.networkmanager.ResponseCallback;
import org.unibl.etf.blbustracker.utils.Utils;

public class NewAnnouncementModel implements ResponseCallback
{
    //using to check last saved update date with server date
    private final SharedPreferences sharedPreferences;
    private final NetworkManager networkManager;
    private final ImageView notificationIcon;

    private final NetworkStatus networkStatus;

    public NewAnnouncementModel(Context context, ImageView notificationIcon)
    {
        this.notificationIcon = notificationIcon;

        sharedPreferences = Utils.getSharedPreferences(context);
        networkManager = NetworkManager.getInstance(context);

        networkStatus = new NetworkStatus(context, new ConnectivityManager.NetworkCallback()
        {
            @Override
            public void onAvailable(@NonNull Network network)
            {
                super.onAvailable(network);
                checkForNewUpdate();
            }
        });
    }

    public void startListen()
    {
        networkStatus.listen();
    }


    public void checkForNewUpdate()
    {
        new Thread(() ->
        {
            networkManager.GETJson(Constants.ANNOUNCEMENT_LAST_UPDATE_PATH, null, this, null);
        }).start();
    }

    @Override
    public void successResponse(JSONObject object)
    {
        String currentLastUpdate = sharedPreferences.getString(LastUpdated.NEWS_LAST_UPDATE_KEY, "");
        String serverLastUpdate = object.toString();
        boolean hasupdate = (!currentLastUpdate.equals(serverLastUpdate));    // false for no new announmcents, true has new announcment
        showNotification(hasupdate);
    }

    private void showNotification(boolean hasUpdate)
    {
        if(hasUpdate)
            notificationIcon.setVisibility(View.VISIBLE);
        else
            notificationIcon.setVisibility(View.INVISIBLE);
    }
}
