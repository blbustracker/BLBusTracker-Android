package org.unibl.etf.blbustracker.navigationtabs.mapview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

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
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;
import org.unibl.etf.blbustracker.datahandlers.database.joinroutebusstop.JoinRouteBusStopDAO;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.datahandlers.jsonhandlers.pointfactory.PointFactory;
import org.unibl.etf.blbustracker.navigationtabs.mapview.buscontroller.BusController;
import org.unibl.etf.blbustracker.utils.AlertUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

// Map and it's objects interactions
public class MapUtils
{
    private final int N_THREADS = 6;

    //for startig google navigation
    private final String GOOGLEMAP_PACKAGE = "com.google.android.apps.maps";
    private final String MODE_WALK = "&mode=w";

    private final Object lock = new Object();
    private Handler mainHandler;
    private ExecutorService poolExecutorService;

    private BitmapDescriptor defaultBusStopIcon;
    private BitmapDescriptor startDestinationIcon;
    private BitmapDescriptor endDestinationIcon;

    private GoogleMap map;

    private Map<Integer, Marker> busStopMarkersHash;
    private Map<Integer, Polyline> routePolylineHash;

    private RouteBusStopConnection routeBusStopConnection;

    public MapUtils(Context context)
    {
        routeBusStopConnection = new RouteBusStopConnection(context);

        this.mainHandler = new Handler(Looper.getMainLooper());
        poolExecutorService = Executors.newFixedThreadPool(N_THREADS);

        defaultBusStopIcon = BitmapDescriptorFactory.fromResource(R.drawable.busstop_blue_26x26);
        //        int dimension = (int)context.getResources().getDimension(R.dimen.busstop_size);
        //        defaultBusStopIcon = DrawableUtil.resizeDrawable(R.drawable.busstop_blue_256,dimension, context); // TODO: for new version icon resize

        startDestinationIcon = BitmapDescriptorFactory.fromResource(R.drawable.busstop_green_36x36);
        endDestinationIcon = BitmapDescriptorFactory.fromResource(R.drawable.busstop_red_36x36);
    }

    /**
     * Constructor is called in MapFragment.OnMapReady(Google map)
     */
    public MapUtils(Context context, GoogleMap map, Map<Integer, Marker> busStopMarkersHash, Map<Integer, Polyline> routePolylineHash)
    {
        this(context);
        this.map = map;

        this.busStopMarkersHash = busStopMarkersHash;
        this.routePolylineHash = routePolylineHash;
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
        Marker marker = map.addMarker(new MarkerOptions().position(busStop.getLatLng()).icon(defaultBusStopIcon));
        marker.setTag(busStop);
        return marker;
    }

    //draw given route on map
    void drawRoutePolylineOnMap(Route route)
    {
        activatePoolExecutorService();
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
                    .width(Constants.POLYLINE_WIDTH)
                    .color(colorId)
                    .addAll(wayPoints);


            mainHandler.post(() ->
            {
                Polyline routePolyline = map.addPolyline(roadOverlay);
                routePolyline.setTag(route);
                routePolyline.setClickable(true);
                routePolylineHash.put(route.getRouteId(), routePolyline);
            });
        });
    }

    /**
     * Delete/remove polyline that are not needed anymore
     * When there is an update delete polylines that are from database
     */
    void removeOldRoutePolylines(Map<Integer, Polyline> routePolylinesHash)
    {
        if (routePolylinesHash != null && !routePolylinesHash.isEmpty())
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
        if (busStopMarkersHash != null && !busStopMarkersHash.isEmpty())
            for (Map.Entry<Integer, Marker> markerEntry : busStopMarkersHash.entrySet())
            {
                markerEntry.getValue().remove();
            }
    }

    //Initialize thread pool if it is null or shutdown
    //for example when phone is turned off and on again
    public void activatePoolExecutorService()
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

    public BitmapDescriptor getDefaultBusStopIcon()
    {
        return defaultBusStopIcon;
    }

    /**
     * Apply all map settings, style, type,...
     */
    void setAllMapArguments(int mapType, String mapStyle, Context context)
    {
        // so that google default navigation icon do NOT show when marker is clicked
        map.getUiSettings().setMapToolbarEnabled(false);

        setMapStyle(mapStyle, context);
        map.setMapType(mapType); // 1 - NORMAL , 2 - SATELLITE, 3 - TERRAIN, 4 - HYBRID
        map.setIndoorEnabled(false);
        //map.setTrafficEnabled(true); // map with traffic density information superimposed on top of it
    }

    private final String NIGHT_MAP_STYLE = "mapstyle_night";
    private final String DARK_MAP_STYLE = "mapstyle_dark";
    private final String GRAYSCALE_MAP_STYLE = "mapstyle_grayscale";
    private final String RETRO_MAP_STYLE = "mapstyle_retro";
    private final String AUBERGINE_MAP_STYLE = "mapstyle_aubergine";

    void setMapStyle(String mapStyle, Context context)
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

    public void moveToBounds(Map<Integer, Polyline> routePolylineHash)
    {
        if (routePolylineHash == null || routePolylineHash.isEmpty())
            return;

        List<List<LatLng>> listOfLists = routePolylineHash.values()
                .stream()
                .map(Polyline::getPoints)
                .collect(Collectors.toList());

        activatePoolExecutorService();
        poolExecutorService.execute(() ->
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (List<LatLng> polylineList : listOfLists)
            {
                for (LatLng latLng : polylineList)
                {
                    builder.include(latLng);
                }
            }
            LatLngBounds bounds = builder.build();
            int padding = Constants.CAMERA_PADDING; // offset from edges of the map in pixels

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mainHandler.post(() -> map.animateCamera(cu));
        });
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
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage(GOOGLEMAP_PACKAGE);
        if (mapIntent.resolveActivity(context.getPackageManager()) != null)
        {
            context.startActivity(mapIntent);
        } else
        {
            AlertUtil.showWarningAlert(context, context.getString(R.string.no_google_maps_api));
        }

    }

    /**
     * either show all bus stops or hide all bus stops
     * must be called on main thread
     *
     * @param isVisible if true show all bus stops, if false hide all
     */
    public void setAllBusStopMarkersVisibility(boolean isVisible)
    {
        if (busStopMarkersHash != null)
            for (Map.Entry<Integer, Marker> busStopMarkerEntry : busStopMarkersHash.entrySet())
            {
                setMarkerVisibility(busStopMarkerEntry.getValue(), isVisible);
            }
    }

    private void setBusStopVisibility(int busStopId, boolean isVisible)
    {
        Marker busstopMarker = busStopMarkersHash.get(busStopId);
        if (busstopMarker != null)
            setMarkerVisibility(busstopMarker, isVisible);

    }

    private void setBusStopVisibility(BusStop busStop, boolean isVisible)
    {
        setMarkerVisibility(busStopMarkersHash.get(busStop.getBusStopId()), isVisible);
    }

    private void setMarkerVisibility(Marker busStopMarker, boolean isVisible)
    {
        if (busStopMarker == null)
            return;
        busStopMarker.setVisible(isVisible);
    }

    public void setAllRouterPolylinesVisibility(boolean isVisible)
    {
        if (routePolylineHash != null)
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
    private void setPolylineVisibility(Polyline polyline, boolean isVisible)
    {
        if (polyline == null)
            return;

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

    /**
     * traverse all routes(polylines) and set visable those who contain given clickedBusStop (@code clickedBusStop)
     * and hide those who dont contain given clickedBusStop (@code clickedBusStop).
     * If the given clickedBusStop (@code clickedBusStop) doesnt have any routes, hide all routes
     *
     * @param clickedBusStop clickedBusStop (marker) that was clicked
     */
    public void onBusStopClicked(BusStop clickedBusStop, BusController busController, Context context)
    {
        activatePoolExecutorService();
        poolExecutorService.execute(() ->
        {
            JoinRouteBusStopDAO joinRouteBusStopDAO = DBFactory.getInstance(context).getJoinRouteBusStopDAO();
            List<Route> routesWithBusStop = joinRouteBusStopDAO.getRoutesByBusStopId(clickedBusStop.getBusStopId());
            if (routesWithBusStop != null && routesWithBusStop.size() > 0)
            {
                busController.setListBusLocationUrlQuery(routesWithBusStop);
                for (Map.Entry<Integer, Polyline> routePolylineEntry : routePolylineHash.entrySet())    // treverse all routes (polylines)
                {
                    mainHandler.post(() ->
                    {
                        Route route = (Route) routePolylineEntry.getValue().getTag();
                        setPolylineVisibility(routePolylineEntry.getValue(), routesWithBusStop.contains(route));
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
    public void onSetAsDestinationClicked(BusStop destinationBusStop, boolean isStartDest, Context context)
    {
        activatePoolExecutorService();
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
                    int destinationIndex = busStopIds.indexOf(destinationBusStop.getBusStopId());
                    if (isStartDest)
                    {
                        busStopIds = busStopIds.subList(destinationIndex, busStopIds.size());
                    } else
                    {
                        busStopIds = busStopIds.subList(0, destinationIndex + 1);
                    }
                    List<Integer> finalBusStopIds = busStopIds;
                    mainHandler.post(() -> setBusStopIdsVisability(finalBusStopIds, true));
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
        if (busStopIds != null)
            for (Integer busStopId : busStopIds)
            {
                setBusStopVisibility(busStopId, isVisable);
            }
    }

    public void onRouteInDialogClicked(Route route)
    {
        activatePoolExecutorService();
        poolExecutorService.execute(() ->
        {
            PointFactory pointFactory = new PointFactory();
            pointFactory.devidePointsAndBusStops(route.getWaypointsJSONArray());

            List<LatLng> wayPoints = pointFactory.getPoints();
            CameraUpdate cu = setCameraBounds(wayPoints);

            mainHandler.post(() ->
            {
                setAllBusStopMarkersVisibility(false);
                setAllRouterPolylinesVisibility(false);

                setPolylineVisibility(routePolylineHash.get(route.getRouteId()), true);
                setBusStopIdsVisability(pointFactory.getBusStopIds(), true);
                map.animateCamera(cu);
            });
        });
    }

    //when user inputs bus stop in search box
    public void onDestinationTextInput(BusStop busStop, BusController busController, boolean isStartDest, Context context)
    {
        onBusStopClicked(busStop, busController, context);
        onSetAsDestinationClicked(busStop, isStartDest, context);
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

    public synchronized void setAllRoutesAndBusStopsVisibilty(boolean visible)
    {
        setAllRouterPolylinesVisibility(visible);
        setAllBusStopMarkersVisibility(visible);
    }

    //used for drawing routes that contains startBusStop and endBusStop
    public void drawRoutesThroughBusStops(BusStop startBusStop, BusStop endBusStop, BusController busController, ResetInterface resetStartEnd, Context context)
    {
        setAllRoutesAndBusStopsVisibilty(false);
        activatePoolExecutorService();
        poolExecutorService.execute(() ->
        {
            List<Route> routeList = routeBusStopConnection.findDirectRoute(startBusStop, endBusStop, (route, busStopIds) -> mainHandler.post(() ->
            {
                setPolylineVisibility(routePolylineHash.get(route.getRouteId()), true);
                setBusStopIdsVisability(busStopIds, true);
            }));

            busController.setListBusLocationUrlQuery(routeList);

            if (routeList == null || routeList.isEmpty())
            {
                mainHandler.post(() ->
                {
                    resetStartEnd.resetStartEndDestination();
                    Toast.makeText(context, R.string.no_direct_route_msg, Toast.LENGTH_SHORT).show();
                });
            }
        });

    }

}
