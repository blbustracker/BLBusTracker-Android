package org.unibl.etf.blbustracker.navigationtabs.mapview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import org.unibl.etf.blbustracker.Constants;
import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.DBFactory;
import org.unibl.etf.blbustracker.datahandlers.database.RouteBusStopConnection;
import org.unibl.etf.blbustracker.datahandlers.database.joinroutebusstop.JoinRouteBusStopDAO;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;
import org.unibl.etf.blbustracker.datahandlers.jsonhandlers.pointfactory.PointFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

// Map and it's objects interactions
public class MapUtils
{
    private final int N_THREADS = 5;

    //for startig google navigation
    private final String GOOGLEMAP_PACKAGE = "com.google.android.apps.maps";
    private final String MODE_WALK = "&mode=w";

    private View homeView;
    private Context context;
    private Activity activity; //used for popUp window marker

    private final Object lock = new Object();
    private Handler mainHandler;
    private ExecutorService poolExecutorService;

    private BitmapDescriptor busIcon;
    private BitmapDescriptor startDestinationIcon;
    private BitmapDescriptor endDestinationIcon;

    private GoogleMap map;

    private Map<Integer, Marker> busStopMarkersHash;
    private Map<Integer, Polyline> routePolylineHash;

    private RouteBusStopConnection routeBusStopConnection;

    /**
     * Constructor is called in MapFragment.OnMapReady(Google map)
     */
    public MapUtils(Context context, GoogleMap map, Map<Integer, Marker> busStopMarkersHash, Map<Integer, Polyline> routePolylineHash)
    {
        this.context = context;
        this.map = map;

        routeBusStopConnection = new RouteBusStopConnection(context);

        this.busStopMarkersHash = busStopMarkersHash;
        this.routePolylineHash = routePolylineHash;

        this.mainHandler = new Handler(Looper.getMainLooper());
        poolExecutorService = Executors.newFixedThreadPool(N_THREADS);

        busIcon = BitmapDescriptorFactory.fromResource(R.drawable.busstop_blue_32x32);

        startDestinationIcon = BitmapDescriptorFactory.fromResource(R.drawable.busstop_green_40x40);
        endDestinationIcon = BitmapDescriptorFactory.fromResource(R.drawable.busstop_red_40x40);
    }

    //default camera position on start
    void setDefaultLocation()
    {
        LatLng trgBL = new LatLng(Constants.STARTING_LAT, Constants.STARTING_LNG);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(trgBL, Constants.DEFAULT_ZOOM));
    }

    /**
     * Method that is called when MapViewModel gets and sets routeList
     *
     * @param routeList data fetched from Database/Server
     */
    synchronized void onRouteChanged(List<Route> routeList)
    {
        if (routePolylineHash == null)
            routePolylineHash = new HashMap<>();

        removeOldRoutePolylines(routePolylineHash);

        for (Route route : routeList)
        {
            this.drawRoutePolylineOnMap(route); // drawing all routes
        }
    }

    /**
     * Method that is called when MapViewModel gets and sets busStopList
     *
     * @param busStopList data fetched from Database/Server
     */
    synchronized void onBusStopChanged(List<BusStop> busStopList)
    {
        if (busStopMarkersHash == null)
            busStopMarkersHash = new HashMap<>();

        removeOldBusStopMarkers(busStopMarkersHash);

        if (busStopList != null)
            for (BusStop busStop : busStopList)
            {
                Marker marker = placeBusStopMarkerOnMap(busStop);
                busStopMarkersHash.put(busStop.getBusStopId(), marker);
            }
    }

    private Marker placeBusStopMarkerOnMap(BusStop busStop)
    {
        Marker marker = map.addMarker(new MarkerOptions().position(busStop.getLatLng()).icon(busIcon));
        marker.setTitle(busStop.getDesc());
        marker.setTag(busStop);
        return marker;
    }

    //draw given route on map
    void drawRoutePolylineOnMap(Route route)
    {
        poolExecutorService.execute(() ->
        {
            PointFactory pointFactory = new PointFactory();
            pointFactory.devidePointsAndBusStops(route.getWaypointsJSONArray());

            List<LatLng> wayPoints = pointFactory.getPoints();

            String colorCode = route.getColor();
            if (colorCode == null)
                return;
            int colorId = ColorUtils.setAlphaComponent(Color.parseColor(colorCode), Constants.POLYLINE_ALPHA_COLOR); // adding alpha to server color

            PolylineOptions roadOverlay = new PolylineOptions()
                    //                    .width(polylineWidth)
                    .color(colorId)
                    .addAll(wayPoints);


            mainHandler.post(() ->
            {
                synchronized (lock)
                {
                    Polyline routePolyline = map.addPolyline(roadOverlay);
                    routePolyline.setTag(route);
                    routePolyline.setClickable(true);
                    routePolylineHash.put(route.getRouteId(), routePolyline);
                }
            });
        });
    }

    /**
     * Delete/remove polyline that are not needed anymore
     * When there is an update delete polylines that are from database
     */
    void removeOldRoutePolylines(Map<Integer, Polyline> routePolylinesHash)
    {
        if (routePolylinesHash != null)
            for (Map.Entry<Integer, Polyline> polylineEntry : routePolylinesHash.entrySet())
            {
                polylineEntry.getValue().remove();
            }
    }

    /**
     * Delete/remove marker that are not needed anymore
     * When there is an update delete markers that are from database
     */
    void removeOldBusStopMarkers(Map<Integer, Marker> busStopMarkersHash)
    {
        if (busStopMarkersHash != null)
            for (Map.Entry<Integer, Marker> markerEntry : busStopMarkersHash.entrySet())
            {
                markerEntry.getValue().remove();
            }
    }

    //Initialize thread pool if it is null or shutdown
    //for example when phone is turned off and on again
    public void checkPoolState()
    {
        if (poolExecutorService == null || poolExecutorService.isShutdown())
            poolExecutorService = Executors.newFixedThreadPool(N_THREADS);
    }

    void shutdownPoolExecutorService()
    {
        if (poolExecutorService != null && !poolExecutorService.isShutdown())
            poolExecutorService.shutdown();
    }

    public BitmapDescriptor getStartDestinationIcon()
    {
        return startDestinationIcon;
    }

    public BitmapDescriptor getEndDestinationIcon()
    {
        return endDestinationIcon;
    }

    public BitmapDescriptor getBusIcon()
    {
        return busIcon;
    }

    /**
     * Apply all map settings, style, type,...
     */
    void setAllMapArguments(int mapType, String mapStyle)
    {
        // so that google default navigation icon do NOT show when marker is clicked
        map.getUiSettings().setMapToolbarEnabled(false);

        setMapStyle(mapStyle);
        map.setMapType(mapType); // 1 - NORMAL , 2 - SATELLITE, 3 - TERRAIN, 4 - HYBRID
        map.setIndoorEnabled(false);
        //map.setTrafficEnabled(true); // map with traffic density information superimposed on top of it
    }

    private final String NIGHT_MAP_STYLE = "mapstyle_night";
    private final String DARK_MAP_STYLE = "mapstyle_dark";
    private final String GRAYSCALE_MAP_STYLE = "mapstyle_grayscale";
    private final String RETRO_MAP_STYLE = "mapstyle_retro";
    private final String AUBERGINE_MAP_STYLE = "mapstyle_aubergine";

    void setMapStyle(String mapStyle)
    {
        switch (mapStyle)
        {
            case NIGHT_MAP_STYLE:
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.mapstyle_night));
                break;
            case DARK_MAP_STYLE:
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.mapstyle_dark));
                break;
            case GRAYSCALE_MAP_STYLE:
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.mapstyle_grayscale));
                break;
            case RETRO_MAP_STYLE:
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.mapstyle_retro));
                break;
            case AUBERGINE_MAP_STYLE:
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.mapstyle_aubergine));
        }
    }

    /* used for setting up camera bounds such that all points are in the view */
    private CameraUpdate setCameraBounds(List<LatLng> points)
    {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points)
        {
            builder.include(point);
        }
        LatLngBounds bounds = builder.build();
        return CameraUpdateFactory.newLatLngBounds(bounds, Constants.CAMERA_PADDING);
    }

    /**
     * Minimize this app to start GoogleMap turn-by-turn directions/navigation.
     * By default start location is 'you current location' which user can change in GoogleMap app
     *
     * @param latLng direction to this position
     */
    void startGoogleMapNavigation(LatLng latLng, Context context)
    {
        // some use cases:
        //        google.navigation:q=a+street+address
        //        google.navigation:q=latitude,longitude
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latLng.latitude + "," + latLng.longitude + MODE_WALK);
        Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        intent.setPackage(GOOGLEMAP_PACKAGE);
        context.startActivity(intent);
    }

    /**
     * either show all bus stops or hide all bus stops
     * must be called on main thread
     *
     * @param isVisible if true show all bus stops, if false hide all
     */
    public void setAllBusStopMarkersVisibility(boolean isVisible)
    {
        for (Map.Entry<Integer, Marker> busStopMarkerEntry : busStopMarkersHash.entrySet())
        {
            setMarkerVisibility(busStopMarkerEntry.getValue(), isVisible);
        }
    }

    private void setBusStopVisibility(int busStopId, boolean isVisible)
    {
        setMarkerVisibility(busStopMarkersHash.get(busStopId), isVisible);

    }

    private void setBusStopVisibility(@NonNull BusStop busStop, boolean isVisible)
    {
        setMarkerVisibility(busStopMarkersHash.get(busStop.getBusStopId()), isVisible);
    }

    private void setMarkerVisibility(@NonNull Marker busStopMarker, boolean isVisible)
    {
        if (isVisible)
            busStopMarker.setVisible(true);
        else
            busStopMarker.setVisible(false);
    }

    public void setAllRouterPolylinesVisibility(boolean isVisible)
    {
        for (Map.Entry<Integer, Polyline> routePolylineEntry : routePolylineHash.entrySet())
        {
            setPolylineVisibility(routePolylineEntry.getValue(), isVisible);
        }
    }

    /**
     * set visibility and clicable to same value.
     * Note: even though the polyline is invisible it can still be clicked
     * must be called on main thread
     *
     * @param polyline
     * @param isVisible set visible and clickable to this value
     */
    private void setPolylineVisibility(@NonNull Polyline polyline, boolean isVisible)
    {
        if (isVisible)
        {
            polyline.setVisible(true);
            polyline.setClickable(true);
        } else
        {
            polyline.setVisible(false);
            polyline.setClickable(false);
        }
    }

    private void setRouteVisibility(@NonNull Route route, boolean isVisible)
    {
        setPolylineVisibility(routePolylineHash.get(route.getRouteId()), isVisible);
    }

    /**
     * traverse all routes(polylines) and set visable those who contain given clickedBusStop (@code clickedBusStop)
     * and hide those who dont contain given clickedBusStop (@code clickedBusStop).
     * If the given clickedBusStop (@code clickedBusStop) doesnt have any routes, hide all routes
     *
     * @param clickedBusStop clickedBusStop (marker) that was clicked
     */
    public void onBusStopClicked(BusStop clickedBusStop)
    {
        poolExecutorService.execute(() ->
        {
            JoinRouteBusStopDAO joinRouteBusStopDAO = DBFactory.getInstance(context).getJoinRouteBusStopDAO();
            List<Route> routesWithBusStop = joinRouteBusStopDAO.getRoutesByBusStopId(clickedBusStop.getBusStopId());
            if (routesWithBusStop != null && routesWithBusStop.size() > 0)
            {
                for (Map.Entry<Integer, Polyline> routePolylineEntry : routePolylineHash.entrySet())    // treverse all routes (polylines)
                {
                    mainHandler.post(() ->
                    {
                        Route route = (Route) routePolylineEntry.getValue().getTag();
                        if (routesWithBusStop.contains(route))
                            setPolylineVisibility(routePolylineEntry.getValue(), true);
                        else
                            setPolylineVisibility(routePolylineEntry.getValue(), false);
                    });
                }
            } else
            {
                mainHandler.post(() -> setAllRouterPolylinesVisibility(false));
            }
        });
    }

    /**
     * only show bus stops on routes that contain destinationBusStop,
     * all routes that contain destinationBusStop should've been visable with onBusStopClicked method
     */
    public void onSetAsDestinationClicked(BusStop destinationBusStop)
    {
        poolExecutorService.execute(() ->
        {
            JoinRouteBusStopDAO joinRouteBusStopDAO = DBFactory.getInstance(context).getJoinRouteBusStopDAO();
            List<Route> routesWithDestinationBusStop = joinRouteBusStopDAO.getRoutesByBusStopId(destinationBusStop.getBusStopId());
            if (routesWithDestinationBusStop != null && routesWithDestinationBusStop.size() > 0)
            {
                mainHandler.post(() -> setAllBusStopMarkersVisibility(false)); //hide all bus stops

                for (Route route : routesWithDestinationBusStop)
                {
                    List<Integer> busStopIds = getBusStopIdsOnRoute(route); // get all busStopIds on this route
                    mainHandler.post(()-> setBusStopIdsVisability(busStopIds,true));
                }
            }
        });
    }

    //traverse all waypoints on route and collect all bus stop IDs
    private List<Integer> getBusStopIdsOnRoute(Route route)
    {
        PointFactory pointFactory = new PointFactory();
        pointFactory.devidePointsAndBusStops(route.getWaypointsJSONArray());
        return pointFactory.getBusStopIds();
    }

    //doesn't go through all bus stops just the ones given as input
    private void setBusStopIdsVisability(List<Integer> busStopIds, boolean isVisable)
    {
        for (Integer busStopId : busStopIds)
        {
            setBusStopVisibility(busStopId, isVisable);
        }
    }

    public void onRouteInDialogClicked(Route route)
    {
        poolExecutorService.execute(() ->
        {
            PointFactory pointFactory = new PointFactory();
            pointFactory.devidePointsAndBusStops(route.getWaypointsJSONArray());

            List<LatLng> wayPoints = pointFactory.getPoints();
            CameraUpdate cu = setCameraBounds(wayPoints);

            mainHandler.post(()->
            {
                setAllBusStopMarkersVisibility(false);
                setAllRouterPolylinesVisibility(false);

                setRouteVisibility(route,true);
                setBusStopIdsVisability(pointFactory.getBusStopIds(),true);
                map.animateCamera(cu);
            });
        });
    }

    //when user inputs bus stop in search box
    public void onDestinationTextInput(BusStop busStop)
    {
        onBusStopClicked(busStop);
        onSetAsDestinationClicked(busStop);
    }

    /**
     * Show only specific route polylines on the map
     * run this on main/UI thread
     *
     * @param routeIds route ids to show on map, if NULL show all routes
     */
    void showSpecificRoutesOnly(List<Integer> routeIds)
    {
        for (Map.Entry<Integer, Polyline> routePolylineEntry : routePolylineHash.entrySet())
        {
            if (routeIds == null)
            {
                routePolylineEntry.getValue().setVisible(true);
                routePolylineEntry.getValue().setClickable(true);
            } else if (routeIds.contains(routePolylineEntry.getKey()))
            {
                routePolylineEntry.getValue().setVisible(true);
                routePolylineEntry.getValue().setClickable(true);
            } else
            {
                routePolylineEntry.getValue().setVisible(false);
                routePolylineEntry.getValue().setClickable(false);
            }
        }
    }

    //used for drawing routes that contains busStopA and busStopB
    @Deprecated
    public void drawRoutesThroughBusStops(String busStopA, String busStopB)
    {
        poolExecutorService.execute(() ->
        {
            List<Route> routeList = routeBusStopConnection.findDirectRoute(busStopA, busStopB);
            if (routeList != null)
            {
                List<Integer> routeIds = routeList.stream().mapToInt(Route::getRouteId).boxed().collect(Collectors.toList());
                mainHandler.post(() ->
                {
                    showSpecificRoutesOnly(routeIds);
                });
            } else
            {
                mainHandler.post(() ->
                {
                    showSpecificRoutesOnly(null);
                    Toast.makeText(context, R.string.no_direct_route_msg, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void drawRoutesThroughBusStops(BusStop busStopA, BusStop busStopB)
    {
        poolExecutorService.execute(() ->
        {
            List<Route> routeList = routeBusStopConnection.findDirectRoute(busStopA, busStopB);
            if (routeList != null)
            {
                List<Integer> routeIds = routeList.stream().mapToInt(Route::getRouteId).boxed().collect(Collectors.toList());
                mainHandler.post(() ->
                {
                    showSpecificRoutesOnly(routeIds);
                });
            } else
            {
                mainHandler.post(() ->
                {
                    showSpecificRoutesOnly(null);
                    Toast.makeText(context, R.string.no_direct_route_msg, Toast.LENGTH_SHORT).show();
                });
            }
        });

    }


}
