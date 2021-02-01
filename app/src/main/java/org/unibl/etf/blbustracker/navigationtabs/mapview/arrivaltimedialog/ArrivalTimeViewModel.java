package org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Response;

import org.json.JSONArray;
import org.unibl.etf.blbustracker.Constants;
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;
import org.unibl.etf.blbustracker.networkmanager.NetworkManager;
import org.unibl.etf.blbustracker.networkmanager.NetworkStatus;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArrivalTimeViewModel extends AndroidViewModel
{
    public static final String TIMEQUERY = "?id=";
    private static final int N_THREADS = 2;
    private static final int TIME_REFRESH_PERIOD = 10_000;

    private MutableLiveData<List<ArrivalTime>> arrivalTimesMLD;
    // for getting content from server
    private NetworkManager networkManager;

    private boolean isFragmentAlive = true;
    private Integer stationId;

    private ExecutorService poolExecutorService;

    public ArrivalTimeViewModel(@NonNull Application application)
    {
        super(application);
        arrivalTimesMLD = new MutableLiveData<>();
    }

    public LiveData<List<ArrivalTime>> getArrivalTimesMLD()
    {
        return arrivalTimesMLD;
    }

    public void startListening(BusStop busStop, Context context)
    {
        this.startListening(busStop.getBusStopId(), context);
    }

    public void startListening(int stationId, Context context)
    {
        activatePoolExecutorService();
        poolExecutorService.execute(() ->
        {
            while (isFragmentAlive)
            {
                try
                {
                    networkManager = NetworkManager.getInstance(context);
                    String query = Constants.ARRIVAL_TIME + TIMEQUERY + stationId;
                    networkManager.GETJsonArray(query, this::onSuccessfulResponse
                            , error -> NetworkStatus.errorConnectingToInternet(error, context, false));

                    Thread.sleep(TIME_REFRESH_PERIOD);

                }catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }
            }
            Log.d(getClass().getSimpleName(), "startListening: killing the LOOP");
        });
    }

    private void onSuccessfulResponse(JSONArray response)
    {

        System.out.println(response);
        ArrivalTimeJSON arrivalTimeJSON = new ArrivalTimeJSON();
        List<ArrivalTime> arrivalTimes = arrivalTimeJSON.getArrivalTimes(response);
        arrivalTimesMLD.setValue(arrivalTimes);
    }

    public void stopListening()
    {
        stationId = null;
        poolExecutorService.shutdown();
        isFragmentAlive = false;
    }

    private void activatePoolExecutorService()
    {
        if (poolExecutorService == null || poolExecutorService.isShutdown())
            poolExecutorService = Executors.newFixedThreadPool(N_THREADS);
    }

}
