package org.unibl.etf.blbustracker.navigationtabs.mapview.bus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.unibl.etf.blbustracker.Constants;
import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.Bus;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.datahandlers.jsonhandlers.BusJSON;
import org.unibl.etf.blbustracker.networkmanager.NetworkManager;
import org.unibl.etf.blbustracker.networkmanager.NetworkStatus;
import org.unibl.etf.blbustracker.utils.DrawableUtil;

import org.json.JSONArray;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BusController
{
    private static final String TAG = "BusController";
    public static final int N_THREADS = 2;
    public static final float busZIndex = 1.1f; // default zIndex is 1.0, to bring busMarker to front

    private GoogleMap map;
    private Context context;

    private boolean isActive;
    private Boolean isBusMarkerClicked;

    private NetworkManager networkManager;

    private ExecutorService twoThreads;
    private Handler mainHandler;

    private List<Route> routeList;

    // default bus icon which changes color to the corresponding route
    private Drawable busDrawable;
    private HashMap<Bus, BitmapDescriptor> busIconsHashMap;  // key == buses, value == there corresponding icons
    private List<Marker> busMarkers;

    private String busLocationUrlQuery;
    private static final String LINEQUERY = "?line=";

    public BusController(GoogleMap map, Context context)
    {
        this.map = map;
        this.context = context;
        isActive = true;
        isBusMarkerClicked = false;
        busMarkers = new ArrayList<>();
        networkManager = NetworkManager.getInstance(context);
        twoThreads = Executors.newFixedThreadPool(N_THREADS);
        mainHandler = new Handler(Looper.getMainLooper());
        busDrawable = ContextCompat.getDrawable(context, R.drawable.bus_tracking_icon);

        busLocationUrlQuery = Constants.BUS_LOCATIONS_PATH;
        busIconsHashMap = new HashMap<>();
    }

    public void startBusTracking()
    {
        isActive = true;
        activatePoolExecutors();
        twoThreads.execute(() ->
        {
            Log.d(TAG, "startBusTracking: created");
            while (isActive)
            {
                try
                {
                    if (!isBusMarkerClicked)    // if bus is clicked, stop all movement on map
                    {
//                        Log.d(getClass().getSimpleName(), "startBusTracking: " + busLocationUrlQuery);
                        networkManager.GETJsonArray(busLocationUrlQuery
                                , this::successResponse
                                , error ->
                                {
                                    System.out.println("buscontroller network error");
                                    NetworkStatus.errorConnectingToInternet(error, context);
                                });
                    }

                    Thread.sleep(Constants.BUS_REFRESH_INTERVAL);
                } catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }
            }
            Log.d(TAG, "startBusTracking: destroyed");
        });
    }

    /**
     * parse content from server and place(set) buses on map
     */
    public void successResponse(JSONArray object)
    {
        if (object == null || object.length() == 0)
        {
            clearMarker();
            return;
        }

        twoThreads.execute(() ->
        {
            BusJSON busJSON = new BusJSON();
            List<Bus> busList = busJSON.getAllBuses(object);

            mainHandler.post(() ->
            {
                clearMarker();
                setMarkers(busList);
            });
        });
    }

    /**
     * placing busses on map
     */
    private void setMarkers(List<Bus> busList)
    {
        for (Bus bus : busList)
        {

            MarkerOptions busMarkerOption = new MarkerOptions()
                    .position(bus.getLocation())
                    .title(bus.getLine())
                    .zIndex(busZIndex);

            BitmapDescriptor busColoredIcon;

            if (busIconsHashMap.containsKey(bus))
                busColoredIcon = busIconsHashMap.get(bus);
            else
            {
                busColoredIcon = getBusColoredIcon(bus);
                busIconsHashMap.put(bus, busColoredIcon);
            }
            busMarkerOption.icon(busColoredIcon); // set colored icon here;

            Marker busMarker = map.addMarker(busMarkerOption);
            busMarker.setTag(bus);
            busMarkers.add(busMarker);
        }
    }

    private BitmapDescriptor getBusColoredIcon(Bus bus)
    {
        String busLineName = bus.getLine();
        if (routeList != null && !routeList.isEmpty())
        {
            Optional<Route> matchingRoute = routeList.parallelStream()
                    .filter(route -> busLineName.equals(route.getName()))
                    .findAny();

            if (matchingRoute.isPresent())
            {
                Route route = matchingRoute.get();
                Drawable coloredDrawable = DrawableUtil.getColoredBusDrawable(route, context);
                return getMarkerIconFromDrawable(coloredDrawable);
            }
        }
        return getMarkerIconFromDrawable(busDrawable);
    }

    private void clearMarker()
    {
        //        Log.d(TAG, "clearMarker");
        for (Marker busMarker : busMarkers)
        {
            busMarker.remove();
        }
    }

    //create busicon from drawable
    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable)
    {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void setActive(boolean active)
    {
        Log.d(TAG, "setActive " + active);
        isActive = active;
    }

    public void setBusMarkerClicked(boolean busMarkerClicked)
    {
        isBusMarkerClicked = busMarkerClicked;
    }

    public boolean isBusMarkerClicked()
    {
        return isBusMarkerClicked;
    }

    public synchronized void setRoutes(List<Route> routeList)
    {
        this.routeList = routeList;
        busIconsHashMap.clear();
    }

    void activatePoolExecutors()
    {
        if (twoThreads == null || twoThreads.isShutdown())
            twoThreads = Executors.newFixedThreadPool(N_THREADS);
    }

    public String getBusLocationUrlQuery()
    {
        return busLocationUrlQuery;
    }

    public void setBusLocationUrlQuery(String busLineName)
    {
        String lineURL;
        this.resetBusLocationUrlQuery();
        try
        {
            lineURL = URLEncoder.encode(busLineName, "UTF-8");  // Note: encode replaces ' '(space) with '+'
        } catch (Exception ex)
        {
            return;
        }
        this.busLocationUrlQuery += (LINEQUERY + lineURL).replace("+", "%20"); // replace '+' with '%20'

    }

    public void resetBusLocationUrlQuery()
    {
        busLocationUrlQuery = Constants.BUS_LOCATIONS_PATH;
    }
}
