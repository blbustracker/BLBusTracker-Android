package org.unibl.etf.blbustracker.navigationtabs.mapview;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONObject;
import org.unibl.etf.blbustracker.Constants;
import org.unibl.etf.blbustracker.datahandlers.LastUpdated;
import org.unibl.etf.blbustracker.datahandlers.database.DBFactory;
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStopDAO;
import org.unibl.etf.blbustracker.datahandlers.database.joinroutebusstop.JoinRouteBusStop;
import org.unibl.etf.blbustracker.datahandlers.database.joinroutebusstop.JoinRouteBusStopDAO;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.datahandlers.database.route.RouteDao;
import org.unibl.etf.blbustracker.datahandlers.jsonhandlers.BusStopJSON;
import org.unibl.etf.blbustracker.datahandlers.jsonhandlers.RouteJSON;
import org.unibl.etf.blbustracker.datahandlers.jsonhandlers.pointfactory.PointFactory;
import org.unibl.etf.blbustracker.networkmanager.NetworkManager;
import org.unibl.etf.blbustracker.networkmanager.NetworkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Data interactions. Getting data from database/server and notifying MapFragment on data change
 */
public class MapViewModel extends AndroidViewModel
{
    private final int N_THREADS = 10;
    private static final int WAIT_THRESHOOLD = 3000;
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
    private DBFactory dbFactory;
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
        mutableBusStops = new MutableLiveData<>();
        mutableRoutes = new MutableLiveData<>();
        isInternetAvailable = new MutableLiveData<>();

        networkManager = NetworkManager.getInstance(application);
        poolExecutorService = Executors.newFixedThreadPool(N_THREADS);
        mainHandler = new Handler(Looper.getMainLooper());

        networkCallback = initNetworkCallback(application);
        networkStatus = new NetworkStatus(application, networkCallback);
        lastUpdated = new LastUpdated(application);
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


    public void getFromDBandServer(Context context)
    {
        activatePoolExecutorService();
        getFromDB(context);
        getFromServer(context);
    }

    //WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW GETTING DATA FROM DATABASE WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW

    public void getFromDB(Context context)
    {
        getAndPlaceDBBusStops(context);
        getAndPlaceDBRoute(context);
    }

    public void getAndPlaceDBRoute(Context context)
    {
        activatePoolExecutorService();
        poolExecutorService.submit(() ->
        {
            List<Route> routeList = getRoutesFromDB(context);
            if (routeList != null && !routeList.isEmpty())
                mainHandler.post(() -> mutableRoutes.setValue(routeList));

            synchronized (lockRoute)
            {
                isRouteDBdone = true;
                lockRoute.notify();
            }
        });
    }

    private void getAndPlaceDBBusStops(Context context)
    {
        activatePoolExecutorService();
        poolExecutorService.submit(() ->
        {
            List<BusStop> busStopList = getBusStopsFromDB(context);
            if (busStopList != null && !busStopList.isEmpty())
                mainHandler.post(() -> mutableBusStops.setValue(busStopList));
            synchronized (lockBusStop)
            {
                isBusStopDBdone = true;
                lockBusStop.notify();
            }
        });
    }

    private List<BusStop> getBusStopsFromDB(Context context)
    {
        dbFactory = DBFactory.getInstance(context);
        if (busStopDAO == null)
            busStopDAO = dbFactory.getBusStopDAO();
        List<BusStop> busStopList = null;
        try
        {
            busStopList = busStopDAO.getAllBusStops();
        } catch (Exception ex)
        {
        }
        return busStopList;
    }

    private List<Route> getRoutesFromDB(Context context)
    {
        dbFactory = DBFactory.getInstance(context);
        if (routeDao == null)
            routeDao = dbFactory.getRouteDAO();

        List<Route> routeList = null;
        try
        {
            routeList = routeDao.getAllRoutes();
        } catch (Exception ex)
        {
        }
        return routeList;
    }

    //WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW GETTING DATA FROM SERVER WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW


    public void getFromServer(Context context)
    {
        if (networkCallback == null)
        {
            networkCallback = initNetworkCallback(context);
            networkStatus = new NetworkStatus(context, networkCallback);
        }
        if (networkStatus == null)
            networkStatus = new NetworkStatus(context, networkCallback);
        networkStatus.listen();

        if (!NetworkStatus.isNetworkAvailable(context))
            mainHandler.post(() -> isInternetAvailable.setValue(false));

    }

    private ConnectivityManager.NetworkCallback initNetworkCallback(Context context)
    {
        return new ConnectivityManager.NetworkCallback()
        {
            @Override
            public void onAvailable(@NonNull Network network)
            {
                super.onAvailable(network);
                activatePoolExecutorService();
                getDataFromServer(context);
                mainHandler.post(() -> isInternetAvailable.setValue(true));
            }

            @Override
            public void onLost(@NonNull Network network)
            {
                super.onLost(network);
                mainHandler.post(() -> isInternetAvailable.setValue(false));
            }

            @Override
            public void onUnavailable()
            {
                super.onUnavailable();
                mainHandler.post(() -> isInternetAvailable.setValue(false));
            }
        };
    }

    //is called in OnAvailable (Networkstatus)
    private void getDataFromServer(Context context)
    {
        this.activatePoolExecutorService();
        poolExecutorService.execute(() ->
        {
            if (isFragmentAlive)
            {
                checkAndGetFromServer(true, Constants.BUSSTOP_LAST_UPDATE_PATH, LastUpdated.BUSSTOP_LAST_UPDATE_KEY, context);
                checkAndGetFromServer(false, Constants.ROUTES_LAST_UPDATE_PATH, LastUpdated.ROUTES_LAST_UPDATE_KEY, context);
            }

        });
    }

    private void checkAndGetFromServer(boolean isBusStop, String lastUpdatePath, String shraedPreferencesKEY, Context context)
    {
        networkManager.GETJson(lastUpdatePath, null, (lastUpdateJSON) ->
        {
            activatePoolExecutorService();
            poolExecutorService.execute(() ->
            {
                if (lastUpdated.isServerContentUpdated(lastUpdateJSON, shraedPreferencesKEY))
                {
                    if (isBusStop)
                    {
                        busStopDateUpdated = lastUpdateJSON.toString();
                        networkManager.GETJson(Constants.BUSSTOPS_PATH, null, obj -> getBusStopsFromServer(obj, context)
                                , error -> NetworkStatus.errorConnectingToInternet(error, context, false));

                    } else
                    {
                        routeDateUpdated = lastUpdateJSON.toString();
                        networkManager.GETJson(Constants.ROUTES_PATH, null, object -> getRoutesFromServer(object, context)
                                , error -> NetworkStatus.errorConnectingToInternet(error, context, false));
                    }
                }
            });
        }, error -> NetworkStatus.errorConnectingToInternet(error, context, true));
    }

    private void getBusStopsFromServer(JSONObject busStopServerJSON, Context context)
    {
        if (busStopServerJSON == null || busStopServerJSON.length() == 0)
            return;

        activatePoolExecutorService();
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
                        lockBusStop.wait(WAIT_THRESHOOLD);
                } catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }
                mainHandler.post(() -> mutableBusStops.setValue(busStopList));
                activatePoolExecutorService();
                poolExecutorService.execute(() -> updateBusStopDB(busStopList, context));
            }
        });
    }

    private void getRoutesFromServer(JSONObject routeServerContent, Context context)
    {
        if (routeServerContent == null || routeServerContent.length() == 0)
            return;

        activatePoolExecutorService();
        poolExecutorService.execute(() ->
        {
            RouteJSON routeJSON = new RouteJSON(routeServerContent);
            List<Route> routeList = routeJSON.getAllRoutes();

            synchronized (lockRoute)
            {
                try
                {   // making sure that route db finishes first
                    while (!isRouteDBdone)
                        lockRoute.wait(WAIT_THRESHOOLD);
                } catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }
                mainHandler.post(() -> mutableRoutes.setValue(routeList));
                activatePoolExecutorService();
                poolExecutorService.execute(() -> updateRouteAndJoinDB(routeList, context));
            }
        });
    }

    //WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW DATABASE UPDATE WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW

    private void updateBusStopDB(List<BusStop> busStopList, Context context)
    {
        dbFactory = DBFactory.getInstance(context);
        if (busStopDAO == null)
            busStopDAO = dbFactory.getBusStopDAO();

        busStopDAO.deleteAllBusStops();
        busStopDAO.insertAll(busStopList);
        lastUpdated.updateSharedPrefenreces(busStopDateUpdated, LastUpdated.BUSSTOP_LAST_UPDATE_KEY);

    }

    private void updateRouteAndJoinDB(List<Route> routeList, Context context)
    {
        dbFactory = DBFactory.getInstance(context);
        if (routeDao == null)
            routeDao = dbFactory.getRouteDAO();
        joinRouteBusStopDAO = dbFactory.getJoinRouteBusStopDAO();

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