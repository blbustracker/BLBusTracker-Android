package org.unibl.etf.blbustracker.navigationtabs.mapview.buscontroller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.unibl.etf.blbustracker.Constants;
import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.Bus;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.datahandlers.jsonhandlers.BusJSON;
import org.unibl.etf.blbustracker.networkmanager.NetworkManager;
import org.unibl.etf.blbustracker.utils.DrawableUtil;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BusController
{

    public static final int N_THREADS = 2;
    public static final float busZIndex = 1.1f;// -1.0f; for busses under the bus stop (default zIndex is 1.0)

    private final GoogleMap map;

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

    private static final String LINE = "line=";
    private static final String STARTLINEQUERY = "?" + LINE;
    private static final String ANDLINEQUERY = "&" + LINE;

    public BusController(GoogleMap map, Context context)
    {
        this.map = map;
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

    public void startBusTracking(Context context)
    {
        isActive = true;
        activatePoolExecutors();
        twoThreads.execute(() ->
        {
            while (isActive)
            {
                try
                {
                    if (!isBusMarkerClicked)    // if bus is clicked, stop all movement on map
                    {
                        networkManager.GETJsonArray(busLocationUrlQuery
                                , object -> successResponse(object, context)
                                , null);
                    } else
                    {
                        Thread.sleep(Constants.BUS_CLICKED_INTERVAL);
                        isBusMarkerClicked = false;
                    }

                    Thread.sleep(Constants.BUS_REFRESH_INTERVAL);
                } catch (InterruptedException ex)
                {
                }
            }
            mainHandler.post(this::clearBusMarkers);
        });
    }

    /**
     * parse content from server and place(set) buses on map
     */
    public void successResponse(JSONArray object, Context context)
    {
        if (object == null || object.length() == 0)
        {
            clearBusMarkers();
            return;
        }

        activatePoolExecutors();
        twoThreads.execute(() ->
        {
            BusJSON busJSON = new BusJSON();
            List<Bus> busList = busJSON.getAllBuses(object);

            mainHandler.post(() ->
            {
                clearBusMarkers();
                setMarkers(busList, context);

                if (!isActive)
                    clearBusMarkers();
            });
        });
    }

    private void clearBusMarkers()
    {

        if (busMarkers != null && map != null)
            for (Marker busMarker : busMarkers)
            {
                busMarker.remove();
            }
    }

    /**
     * placing busses on map
     */
    private void setMarkers(List<Bus> busList, Context context)
    {
        if (busList != null)
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
                    busColoredIcon = getBusColoredIcon(bus, context);
                    busIconsHashMap.put(bus, busColoredIcon);
                }
                busMarkerOption.icon(busColoredIcon); // set colored icon here;


                Marker busMarker = map.addMarker(busMarkerOption);  // Adding bus marker to Map
                busMarker.setTag(bus);
                busMarkers.add(busMarker);
            }
    }

    private BitmapDescriptor getBusColoredIcon(Bus bus, Context context)
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

    //Note: do NOT add "/location" or "&" in arguemnt
    private String urlEncoder(String url) throws Exception
    {
        String lineURL = URLEncoder.encode(url, "UTF-8");  // Note: encode replaces ' '(space) with '+'
        lineURL = lineURL.replace("+", "%20"); // replace '+' with '%20'
        return lineURL;
    }

    // if routes==null show all busses
    // else show busses on given routes
    public void setListBusLocationUrlQuery(List<Route> routes)
    {
        if (routes == null || routes.isEmpty())
        {
            resetBusLocationUrlQuery();
            return;
        }

        StringBuilder urlAttributes = new StringBuilder();
        urlAttributes.append(STARTLINEQUERY); // starting "?line="
        try
        {
            for (Route route : routes)
            {
                urlAttributes.append(urlEncoder(route.getName()));
                urlAttributes.append(ANDLINEQUERY);
            }
            urlAttributes.setLength(urlAttributes.length() - ANDLINEQUERY.length()); // trim end ANDLINEQUERY
            String lineURL = Constants.BUS_LOCATIONS_PATH + urlAttributes.toString();   // /location + ?line=....
            this.busLocationUrlQuery = lineURL;

        } catch (Exception ex)
        {
            this.resetBusLocationUrlQuery();
        }
    }

    public void setBusLocationUrlQuery(String busLineName)
    {
        String lineQuery = Constants.BUS_LOCATIONS_PATH + STARTLINEQUERY;
        try
        {
            lineQuery += urlEncoder(busLineName);
            this.busLocationUrlQuery = lineQuery;
        } catch (Exception ex)
        {
            this.resetBusLocationUrlQuery();
        }
    }

    public void setActive(boolean active)
    {
        isActive = active;
    }

    public void setIsBusMarkerClicked(boolean busMarkerClicked)
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

    public void resetBusLocationUrlQuery()
    {
        busLocationUrlQuery = Constants.BUS_LOCATIONS_PATH;
    }
}
