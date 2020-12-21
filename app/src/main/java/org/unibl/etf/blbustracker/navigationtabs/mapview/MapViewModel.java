package org.unibl.etf.blbustracker.navigationtabs.mapview;

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
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStopDAO;
import org.unibl.etf.blbustracker.datahandlers.database.joinroutebusstop.JoinRouteBusStop;
import org.unibl.etf.blbustracker.datahandlers.database.joinroutebusstop.JoinRouteBusStopDAO;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.datahandlers.database.route.RouteDao;
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;
import org.unibl.etf.blbustracker.datahandlers.jsonhandlers.RouteJSON;
import org.unibl.etf.blbustracker.datahandlers.jsonhandlers.BusStopJSON;
import org.unibl.etf.blbustracker.datahandlers.jsonhandlers.pointfactory.PointFactory;
import org.unibl.etf.blbustracker.networkmanager.NetworkManager;
import org.unibl.etf.blbustracker.networkmanager.NetworkStatus;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Data interactions. Getting data from database/server and notifying MapFragment on data change
 */
public class MapViewModel extends AndroidViewModel
{
    private static final String TAG = "MapViewModel";
    private final int N_THREADS = 10;
    private Context context;
    private boolean isFragmentAlive = true;

    ConnectivityManager.NetworkCallback networkCallback;

    // for getting content from server
    private NetworkManager networkManager;
    //for checking if internet is available
    private NetworkStatus networkStatus;

    // LiveData used for sending information on change to MapFragment
    private MutableLiveData<List<BusStop>> mutableBusStops;
    private MutableLiveData<List<Route>> mutableRoutes;
    private MutableLiveData<Boolean> isInternetAvailable;

    // database objects
    private DBFactory DBFactory;
    private BusStopDAO busStopDAO;
    private RouteDao routeDao;
    private JoinRouteBusStopDAO joinRouteBusStopDAO;

    // checking last updated
    private LastUpdated lastUpdated;
    private String busStopDateUpdated;
    private String routeDateUpdated;

    // Threads
    private Handler mainHandler;
    private ExecutorService poolExecutorService;

    // for thread synchronization
    private final Object lockBusStop = new Object();
    private final Object lockRoute = new Object();
    private boolean isBusStopDBdone = false;
    private boolean isRouteDBdone = false;

    public MapViewModel(@NonNull Application application)
    {
        super(application);
        context = application.getApplicationContext();
        mutableBusStops = new MutableLiveData<>();
        mutableRoutes = new MutableLiveData<>();
        isInternetAvailable = new MutableLiveData<>();

        networkManager = NetworkManager.getInstance(context);
        poolExecutorService = Executors.newFixedThreadPool(N_THREADS);
        mainHandler = new Handler(Looper.getMainLooper());

        networkCallback = initNetworkCallback();
        networkStatus = new NetworkStatus(context, networkCallback);
        lastUpdated = new LastUpdated(context);
    }

    public LiveData<List<BusStop>> getMutableBusStops()
    {
        return mutableBusStops;
    }

    public LiveData<List<Route>> getMutableRoutes()
    {
        return mutableRoutes;
    }

    public LiveData<Boolean> getIsInternetAvailable()
    {
        return isInternetAvailable;
    }


    public void getFromDBandServer()
    {
        activatePoolExecutorService();
        getFromDB();
        getFromServer();
    }

    //WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW GETTING DATA FROM DATABASE WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW

    public void getFromDB()
    {
        getAndPlaceDBBusStops();
        getAndPlaceDBRoute();
    }

    public void getAndPlaceDBRoute()
    {
        poolExecutorService.submit(() ->
        {
            List<Route> routeList = getRoutesFromDB();
            if (routeList != null && !routeList.isEmpty())
                mainHandler.post(() -> mutableRoutes.setValue(routeList));

            synchronized (lockRoute)
            {
                isRouteDBdone = true;
                lockRoute.notify();
            }
        });
    }

    private void getAndPlaceDBBusStops()
    {
        poolExecutorService.submit(() ->
        {
            List<BusStop> busStopList = getBusStopsFromDB();
            if (busStopList != null && !busStopList.isEmpty())
                mainHandler.post(() -> mutableBusStops.setValue(busStopList));
            synchronized (lockBusStop)
            {
                isBusStopDBdone = true;
                lockBusStop.notify();
            }
        });
    }

    private List<BusStop> getBusStopsFromDB()
    {
        DBFactory = DBFactory.getInstance(context);
        if (busStopDAO == null)
            busStopDAO = DBFactory.getBusStopDAO();
        List<BusStop> busStopList = null;
        try
        {
            busStopList = busStopDAO.getAllBusStops();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return busStopList;
    }

    private List<Route> getRoutesFromDB()
    {
        DBFactory = DBFactory.getInstance(context);
        if (routeDao == null)
            routeDao = DBFactory.getRouteDAO();

        List<Route> routeList = null;
        try
        {
            routeList = routeDao.getAllRoutes();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return routeList;
    }

    //WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW GETTING DATA FROM SERVER WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW


    public void getFromServer()
    {
        if (networkCallback == null)
        {
            networkCallback = initNetworkCallback();
            networkStatus = new NetworkStatus(context, networkCallback);
        }
        if (networkStatus == null)
            networkStatus = new NetworkStatus(context, networkCallback);
        networkStatus.listen();

        if (!NetworkStatus.isNetworkAvailable(context))
            mainHandler.post(() -> isInternetAvailable.setValue(false));

    }

    private ConnectivityManager.NetworkCallback initNetworkCallback()
    {
        return new ConnectivityManager.NetworkCallback()
        {
            @Override
            public void onAvailable(@NonNull Network network)
            {
                super.onAvailable(network);
                Log.d(TAG, "onAvailable: intenet IS available");
                activatePoolExecutorService();
                getDataFromServer();
                mainHandler.post(() -> isInternetAvailable.setValue(true));
            }

            @Override
            public void onLost(@NonNull Network network)
            {
                super.onLost(network);
                mainHandler.post(() -> isInternetAvailable.setValue(false));
            }
        };
    }

    //is called in OnAvailable (Networkstatus)
    private void getDataFromServer()
    {
        this.activatePoolExecutorService();
        poolExecutorService.execute(() ->
        {
            if (isFragmentAlive)
            {
                checkAndGetFromServer(true, Constants.BUSSTOP_LAST_UPDATE_PATH, LastUpdated.BUSSTOP_LAST_UPDATE_KEY);
                checkAndGetFromServer(false, Constants.ROUTES_LAST_UPDATE_PATH, LastUpdated.ROUTES_LAST_UPDATE_KEY);
            }

        });
    }

    private void checkAndGetFromServer(boolean isBusStop, String lastUpdatePath, String shraedPreferencesKEY)
    {
        networkManager.GETJson(lastUpdatePath, null, (lastUpdateJSON) ->
                poolExecutorService.execute(() ->
                {
                    if (lastUpdated.isServerContentUpdated(lastUpdateJSON, shraedPreferencesKEY))
                    {
                        if (isBusStop)
                        {
                            busStopDateUpdated = lastUpdateJSON.toString();
                            networkManager.GETJson(Constants.BUSSTOPS_PATH, null, this::getBusStopsFromServer
                                    , error -> NetworkStatus.errorConnectingToInternet(error, context));

                        } else
                        {
                            routeDateUpdated = lastUpdateJSON.toString();
                            networkManager.GETJson(Constants.ROUTES_PATH, null, this::getRoutesFromServer
                                    , error -> NetworkStatus.errorConnectingToInternet(error, context));
                        }
                    }
                }), error -> NetworkStatus.errorConnectingToInternet(error, context));
    }

    private void getBusStopsFromServer(JSONObject busStopServerJSON)
    {
        if (busStopServerJSON == null || busStopServerJSON.length() == 0)
            return;
        ;
        poolExecutorService.execute(() ->
        {
            BusStopJSON busStopJSON = new BusStopJSON(busStopServerJSON);
            List<BusStop> busStopList = busStopJSON.getAllBusStops();

            synchronized (lockBusStop)
            {
                try
                {
                    // making sure that bus stop db finishes first
                    while (!isBusStopDBdone)
                        lockBusStop.wait();
                } catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }
                mainHandler.post(() -> mutableBusStops.setValue(busStopList));
                poolExecutorService.execute(() -> updateBusStopDB(busStopList));
            }
        });
    }

    private void getRoutesFromServer(JSONObject routeServerContent)
    {
        if (routeServerContent == null || routeServerContent.length() == 0)
            return;
        poolExecutorService.execute(() ->
        {
            RouteJSON routeJSON = new RouteJSON(routeServerContent);
            List<Route> routeList = routeJSON.getAllRoutes();

            synchronized (lockRoute)
            {
                try
                {   // making sure that route db finishes first
                    while (!isRouteDBdone)
                        lockRoute.wait();
                } catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }
                mainHandler.post(() -> mutableRoutes.setValue(routeList));
                poolExecutorService.execute(() -> updateRouteAndJoinDB(routeList));
            }
        });
    }

    //WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW DATABASE UPDATE WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW

    private void updateBusStopDB(List<BusStop> busStopList)
    {
        DBFactory = DBFactory.getInstance(context);
        if (busStopDAO == null)
            busStopDAO = DBFactory.getBusStopDAO();

        busStopDAO.deleteAllBusStops();
        busStopDAO.insertAll(busStopList);
        lastUpdated.updateSharedPrefenreces(busStopDateUpdated, LastUpdated.BUSSTOP_LAST_UPDATE_KEY);

    }

    private void updateRouteAndJoinDB(List<Route> routeList)
    {
        DBFactory = DBFactory.getInstance(context);
        if (routeDao == null)
            routeDao = DBFactory.getRouteDAO();
        joinRouteBusStopDAO = DBFactory.getJoinRouteBusStopDAO();

        routeDao.deleteAllRoutes();
        joinRouteBusStopDAO.deleteJoinTable();

        for (Route route : routeList)
        {
            //get all bus stops on route
            PointFactory pointFactory = new PointFactory();
            pointFactory.devidePointsAndBusStops(route.getWaypointsJSONArray());
            List<Integer> busStopIds = pointFactory.getBusStopIds();

            routeDao.insert(route);
            List<JoinRouteBusStop> joinList = new ArrayList<>();
            for (Integer busStopId : busStopIds)
            {
                joinList.add(new JoinRouteBusStop(busStopId, route.getRouteId()));
            }
            if (!joinList.isEmpty())
                joinRouteBusStopDAO.insertAll(joinList);
        }
        //saves route last update date
        lastUpdated.updateSharedPrefenreces(routeDateUpdated, LastUpdated.ROUTES_LAST_UPDATE_KEY);
    }

    public void activatePoolExecutorService()
    {
        if (poolExecutorService == null || poolExecutorService.isShutdown())
            poolExecutorService = Executors.newFixedThreadPool(N_THREADS);
    }

    public void shutdownPoolExecutorService()
    {
        isFragmentAlive = false;
        DBFactory.destroyInstance();
        networkManager.destroyInstance();
        if (poolExecutorService != null && !poolExecutorService.isShutdown())
            poolExecutorService.shutdown();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && networkStatus != null)
            networkStatus.stopListening();
        networkCallback = null;
    }

    public void setFragmentAlive(boolean fragmentAlive)
    {
        isFragmentAlive = fragmentAlive;
    }

}