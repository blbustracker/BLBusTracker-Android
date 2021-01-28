package org.unibl.etf.blbustracker.navigationtabs.announcements;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.unibl.etf.blbustracker.Constants;
import org.unibl.etf.blbustracker.datahandlers.LastUpdated;
import org.unibl.etf.blbustracker.datahandlers.database.DBFactory;
import org.unibl.etf.blbustracker.datahandlers.database.announcement.Announcement;
import org.unibl.etf.blbustracker.datahandlers.database.announcement.AnnouncementDao;
import org.unibl.etf.blbustracker.datahandlers.jsonhandlers.AnnouncementJSON;
import org.unibl.etf.blbustracker.networkmanager.NetworkManager;
import org.unibl.etf.blbustracker.networkmanager.NetworkStatus;
import org.unibl.etf.blbustracker.networkmanager.ResponseCallback;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Used for getting announcemnts from database and/or Server,
 * and notifying AnnouncemntsFragment when there is data change
 */
public class AnnouncementsViewModel extends AndroidViewModel implements ResponseCallback
{
    private final String TAG = "AnnouncementsViewModel";
    private final Context context;
    private final int N_THREADS = 5;

    private boolean isFragmentAlive = true;
    private NetworkStatus networkStatus;

    private ExecutorService poolExecutorService;
    private final Object locker = new Object();
    private boolean isDBDone = false;

    //data to observe on change
    private MutableLiveData<Boolean> isUpdating;    // Loading screen is only shown on first run when there is no internet and no announcments in database. App is to fast :(
    private MutableLiveData<List<Announcement>> announcementsMutable;

    //used for checking data and saving it if there was an update
    private LastUpdated lastUpdated;
    private String lastUpdateDateString;

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback()
    {
        @Override
        public void onAvailable(@NonNull Network network)
        {
            getAnnouncementsFromServer();
            super.onAvailable(network);
            Log.d(TAG, "onAvailable: intenet IS available");
        }

    };

    public AnnouncementsViewModel(@NonNull Application application)
    {
        super(application);
        context = application.getApplicationContext();
        announcementsMutable = new MutableLiveData<>();
        isUpdating = new MutableLiveData<>(true);
        poolExecutorService = Executors.newFixedThreadPool(N_THREADS);
        getAnnouncementsFromDB();
        startListening();
    }

    public void startListening()
    {
        if (networkStatus == null)
            networkStatus = new NetworkStatus(context, networkCallback);
        networkStatus.listen();
    }

    /**
     * get annoucenemnts from data base if there is any and notify "server thread"
     */
    private void getAnnouncementsFromDB()
    {
        activatePoolExecutors();
        poolExecutorService.execute(() ->
        {
            List<Announcement> announcementList = null;
            try
            {
                DBFactory announcementDB = DBFactory.getInstance(context);
                AnnouncementDao announcementDao = announcementDB.getAnnouncementDao();
                announcementList = announcementDao.getAllAnnouncement();

            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
            if (announcementList != null && !announcementList.isEmpty())
            {
                List<Announcement> finalAnnouncementList = announcementList;
                new Handler(Looper.getMainLooper()).post(() ->
                {
                    announcementsMutable.setValue(finalAnnouncementList);
                    isUpdating.setValue(false);
                });
            }
            synchronized (locker)
            {
                isDBDone = true;
                locker.notify();
            }

        });
    }

    //get announcements from server
    void getAnnouncementsFromServer()
    {
        activatePoolExecutors();
        poolExecutorService.execute(() ->
        {
            //checking and waiting for internet connection
            if (isFragmentAlive)
            {
                NetworkManager networkManager = NetworkManager.getInstance(context);
                networkManager.GETJson(Constants.NEWS_LAST_UPDATE_PATH, null, (dateObject) ->
                {
                    activatePoolExecutors();
                    poolExecutorService.execute(() ->
                    {
                        lastUpdated = new LastUpdated(context);
                        if (lastUpdated.isServerContentUpdated(dateObject, LastUpdated.NEWS_LAST_UPDATE_KEY))
                        //There was an update
                        {
                            lastUpdateDateString = dateObject.toString();
                            networkManager.GETJson(Constants.NEWS_PATH, null, this // this is successResponse(...)
                                    , error -> NetworkStatus.errorConnectingToInternet(error, context,false));
                        }

                    });
                }, error -> NetworkStatus.errorConnectingToInternet(error, context,true));
            }
        });

    }

    /**
     * execute this when server gives successful response
     *
     * @param object announcements are in JSON format
     */
    @Override
    public void successResponse(JSONObject object)
    {
        if (object == null || object.length() == 0)
            return;

        poolExecutorService.execute(() ->
        {
            AnnouncementJSON announcementJSON = new AnnouncementJSON(object);
            List<Announcement> announcementsList = announcementJSON.getAllAnnouncements();

            synchronized (locker)
            {
                try
                {
                    while (!isDBDone)
                        locker.wait();
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try
                {
                    new Handler(Looper.getMainLooper()).post(() ->
                    {
                        announcementsMutable.setValue(announcementsList);
                        if (isUpdating != null && isUpdating.getValue())
                            isUpdating.setValue(false);
                    });
                    poolExecutorService.execute(() -> updateAnnouncementsDB(announcementsList));
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });

    }

    /**
     * Update data baes with new content from server
     */
    private void updateAnnouncementsDB(List<Announcement> announcementList)
    {
        DBFactory announcementDB = DBFactory.getInstance(context);
        AnnouncementDao announcementDao = announcementDB.getAnnouncementDao();
        announcementDao.deleteAllAnnouncements();
        announcementDao.insertAll(announcementList);
        lastUpdated.updateSharedPrefenreces(lastUpdateDateString, LastUpdated.NEWS_LAST_UPDATE_KEY);
    }

    /**
     * for showing/hiding progressbar
     *
     * @return true - still getting data, false - data is not null
     */
    public LiveData<Boolean> getIsUpdating()
    {
        return isUpdating;
    }

    /**
     * this method uses AnnouncementsFragment to set observers
     * so that AnnaouncementsFragment could update View with given data
     */
    LiveData<List<Announcement>> getAnnouncementsMutable()
    {
        //returns LiveData which is mutable
        return announcementsMutable;
    }

    void activatePoolExecutors()
    {
        if (poolExecutorService == null || poolExecutorService.isShutdown())
            poolExecutorService = Executors.newFixedThreadPool(N_THREADS);
    }

    void shutdownPoolExecutorService()
    {
        isFragmentAlive = false;
        if (poolExecutorService != null && !poolExecutorService.isShutdown())
            poolExecutorService.shutdown();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && networkStatus != null)
            networkStatus.stopListening();
    }
}
