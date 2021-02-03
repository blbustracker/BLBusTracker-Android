package org.unibl.etf.blbustracker.navigationtabs.mapview;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.unibl.etf.blbustracker.Constants;
import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.Bus;
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog.ArrivalTimeFragment;
import org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog.MoreOptionsInterface;
import org.unibl.etf.blbustracker.navigationtabs.mapview.buscontroller.BusController;
import org.unibl.etf.blbustracker.navigationtabs.mapview.destinationmarkeroptions.MarkerCustomInfoWindow;
import org.unibl.etf.blbustracker.navigationtabs.mapview.destinationmarkeroptions.MarkerDialog;
import org.unibl.etf.blbustracker.navigationtabs.mapview.destinationmarkeroptions.MarkerDialogListener;
import org.unibl.etf.blbustracker.navigationtabs.mapview.searchadapter.SearchBusStopAdapter;
import org.unibl.etf.blbustracker.navigationtabs.routes.OnRouteClickedListener;
import org.unibl.etf.blbustracker.navigationtabs.routes.RoutesBottomFragment;
import org.unibl.etf.blbustracker.navigationtabs.settings.SettingsFragment;
import org.unibl.etf.blbustracker.utils.AnimationUtils;
import org.unibl.etf.blbustracker.utils.KeyboardUtils;
import org.unibl.etf.blbustracker.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Main view interactions
 */
public class MapFragment extends Fragment implements OnMapReadyCallback
        , MarkerDialogListener
        , MoreOptionsInterface
        , OnRouteClickedListener
        , GoogleMap.OnMapClickListener
        , GoogleMap.OnMarkerClickListener
        , GoogleMap.OnPolylineClickListener
        , TextView.OnEditorActionListener
        //        , GoogleMap.OnInfoWindowClickListener
        , View.OnFocusChangeListener
{
    public static final int CAMERA_SPEED = 1000;
    //initialized in onCreateView(...)
    private View mapFragmentView;
    private Context context;

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private GoogleMap map;

    //google MapView, needed for calling onRasume,onStop,...
    private MapView mapLayout;

    // for map appearance
    private int mapType = 1; // 1 - NORMAL , 2 - SATELLITE, 3 - TERRAIN, 4 - HYBRID
    private String mapStyle = "normal";

    //for getting Station and Route onChange information (observers)
    private MapViewModel mapViewModel;

    //for working with map objects such as show/hide routes and/or bus stops
    private MapUtils mapUtils;

    //fields for entering start and end destination bus stop
    private AutoCompleteTextView startDestinationEditText;
    private AutoCompleteTextView endDestinationEditText;

    //layout for AutocompleteTextView, atribute used for expanding/collapsing
    private RelativeLayout collapsableSearchLayout;

    //for showing bus routes dialog
    private RoutesBottomFragment routesBottomDialog;

    //maybe move to MapUtil/MapModel
    private Map<Integer, Marker> busStopMarkersHash;
    private Map<Integer, Polyline> routePolylineHash;

    private Marker endDestinationMarker;
    private Marker startDestinationMarker;

    //dialog when bus stop is clicked
    private MarkerDialog markerDialog;

    //for getting and drawing buses on map
    private BusController busController;

    Handler handler;
    private static final int SHORT_DELAY = 100;
    private boolean isMarkerClicked = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        busStopMarkersHash = new HashMap<>();
        routePolylineHash = new HashMap<>();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mapFragmentView = inflater.inflate(R.layout.fragment_mapview, container, false);

        context = mapFragmentView.getContext();
        collapsableSearchLayout = mapFragmentView.findViewById(R.id.search_linearLayout);

        handler = new Handler();

        checkFragmentArguemnts();
        initializeRoutesButton();
        initGoogleMap(savedInstanceState);
        showAllRoutesAndBusStopButton();
        initSearchDestinationTextArea();
        initShowHideConnectionButton();

        return mapFragmentView;
    }

    /**
     * Initialize mapView
     * Do NOT do anything with the map until onMapReady() method is called
     */
    private void initGoogleMap(Bundle savedInstanceState)
    {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null)
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);

        mapLayout = (MapView) mapFragmentView.findViewById(R.id.map);
        mapLayout.onCreate(mapViewBundle);

        mapLayout.getMapAsync(this);
    }


    /* When map is created this method is called */
    //************************* MAP READY *****************************************************************************
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        this.map = googleMap;
        mapUtils = new MapUtils(mapFragmentView.getContext(), map, busStopMarkersHash, routePolylineHash);
        mapUtils.setAllMapArguments(mapType, mapStyle, getContext());

        //start tracking buses location
        busController = new BusController(map, context);
        busController.startBusTracking(getContext());

        //observe BusStop list changes
        mapViewModel.getMutableBusStops().observe(getViewLifecycleOwner(), busStops ->
        {
            mapUtils.onBusStopChanged(busStops);
            SearchBusStopAdapter searchBusStopAdapter = new SearchBusStopAdapter(context, busStops);
            startDestinationEditText.setAdapter(searchBusStopAdapter);
            endDestinationEditText.setAdapter(searchBusStopAdapter);
        });

        //observe Route list changes
        mapViewModel.getMutableRoutes().observe(getViewLifecycleOwner(), routeList ->
        {
            mapUtils.onRouteChanged(routeList);
            busController.setRoutes(routeList);
            routesBottomDialog.setAllRoutes(routeList);
        });

        initializeZoomButtons();

        map.setInfoWindowAdapter(new MarkerCustomInfoWindow(context));
        map.setOnMarkerClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnPolylineClickListener(this);

        mapUtils.setDefaultLocation();  // center map to default location
    }
    //*****************************************************************************************************************


    /**
     * @param marker marker that was clicked
     * @return false - move camera so that marker is in the center, true - do not move camera
     */
    ArrivalTimeFragment timeFragment;

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        Object obj = marker.getTag();

        if (obj instanceof Bus)
        {
            marker.showInfoWindow();
            //TODO: set which bus is clicked so only this bus marker doesnt move
            if (busController != null)
            {
                busController.setIsBusMarkerClicked(true);
                busController.setClickedBus((Bus)obj);
            }

        } else if (obj instanceof BusStop)
        {
            isMarkerClicked = true; // used in onMapClicked(...)
            marker.hideInfoWindow();
            BusStop busStop = (BusStop) obj;
            timeFragment = new ArrivalTimeFragment(busStop, this);
            timeFragment.show(getActivity().getSupportFragmentManager(), "timeFragment");
            return true;
        }

        return false;
    }

    // init button for showing all routes, uses RoutesBottomDialog class
    private void initializeRoutesButton()
    {
        FloatingActionButton routesFloatingButton = (FloatingActionButton) mapFragmentView.findViewById(R.id.fab_show_routes);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            routesFloatingButton.setTooltipText(getString(R.string.show_all_routes));

        routesBottomDialog = new RoutesBottomFragment(this);

        routesFloatingButton.setOnClickListener(v ->
        {
            routesBottomDialog.show(getActivity().getSupportFragmentManager(), "nepotreban_tag");
        });
    }

    //___________________________________ Dialog Butoons ______________________________________________

    @Override
    public void onMoreOptionsClicked(BusStop busStop)
    {
        markerDialog = new MarkerDialog(this);
        Bundle bundle = new Bundle();
        int busStopId = busStop.getBusStopId();
        bundle.putInt(MarkerDialog.DIALOG_BUSSTOP_ID, busStopId);
        bundle.putString(MarkerDialog.DIALOG_BUSSTOP_NAME, busStop.getDesc());

        // more options marker popup window
        markerDialog.setArguments(bundle);
        markerDialog.show(getActivity().getSupportFragmentManager(), "tag");
    }

    /* set as start destination button click listener displayed in custom MarkerDialog  */
    @Override
    public void setAsStartDestinationBtn(int busStopId)
    {
        startDestinationMarker = setAsDestinationMarker(busStopId, startDestinationEditText, startDestinationMarker,
                mapUtils.getStartDestinationIcon());
        checkMarkersForRoute();
        isMarkerClicked = false;
    }

    /* set as end destination button click listener displayed in custom MarkerDialog  */
    @Override
    public void setAsEndDestinationBtn(int busStopId)
    {
        endDestinationMarker = setAsDestinationMarker(busStopId, endDestinationEditText, endDestinationMarker,
                mapUtils.getEndDestinationIcon());
        checkMarkersForRoute();
        isMarkerClicked = false;
    }

    private Marker setAsDestinationMarker(int busStopId, AutoCompleteTextView editText, Marker destinationMarker, BitmapDescriptor icon)
    {
        if (destinationMarker != null)
            destinationMarker.setIcon(mapUtils.getBusIcon());

        Marker marker = busStopMarkersHash.get(busStopId);
        BusStop busStop = (BusStop) marker.getTag();
        String busStopName = busStop.getDesc();
        editText.setText(busStopName);
        marker.setIcon(icon);
        editText.requestFocus();
        editText.clearFocus();
        return marker;
    }

    //start google navigation to the selected bus stop
    @Override
    public void startNavigationBtn(int busStopId)
    {
        LatLng position = busStopMarkersHash.get(busStopId).getPosition();
        mapUtils.startGoogleMapNavigation(position, context);
    }

    /**
     * when route in dialog window is clicked. show only this route on map
     *
     * @param position of clicked item in the list
     */
    @Override
    public void onRouteInDialogClicked(int position)
    {
        Route route = routesBottomDialog.getRouteFromAdapter(position);

        if (busController != null)
        {
            busController.setBusLocationUrlQuery(route.getName());
            busController.setIsBusMarkerClicked(false);
        }

        mapUtils.onRouteInDialogClicked(route);

        isMarkerClicked = false;
        routesBottomDialog.dismiss();
    }

    /**
     * hide keyboard if shown and if keyboard is not shown
     * Either expand Map/collapse SearchLayout or shrink Map/expand SearchLayout
     */
    @Override
    public void onMapClick(LatLng latLng)
    {
        KeyboardUtils.hideKeyboard(mapFragmentView);

        if (busController != null && busController.isBusMarkerClicked())
        {
            busController.setIsBusMarkerClicked(false);
            isMarkerClicked = false;
        } else if (isMarkerClicked)
        {
            isMarkerClicked = false;

        } else if (startDestinationEditText.hasFocus() || endDestinationEditText.hasFocus())
        {
            startDestinationEditText.clearFocus();
            endDestinationEditText.clearFocus();

        } else if (AnimationUtils.isIsExpanded())
        {
            AnimationUtils.collapse(collapsableSearchLayout);

        } else
        {
            AnimationUtils.expand(collapsableSearchLayout);
        }
    }

    /**
     * gets called when polyline is clicked
     * Note: polyline can still be clicked if it is invisable (setVisable(false);) and if
     * clickable is set to true (setClickable(true);)
     */
    @Override
    public void onPolylineClick(Polyline polyline)
    {
        Object obj = polyline.getTag();
        if (obj instanceof Route)
        {
            Route route = (Route) obj;
            Toast.makeText(context, route.getLabel() + " " + route.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    //________________________________________ SEARCH TEXTBOX methods __________________________________________________________________________________________________

    /**
     * Initialize Search and Swap Buttons
     */
    private void initSearchDestinationTextArea()
    {
        startDestinationEditText = (AutoCompleteTextView) mapFragmentView.findViewById(R.id.start_destination_edittext);
        endDestinationEditText = (AutoCompleteTextView) mapFragmentView.findViewById(R.id.end_destination_edittext);

        initEditTextListeners(startDestinationEditText);
        initEditTextListeners(endDestinationEditText);
    }

    //Set all listeners and text watcher to the AutoCompleteTextView
    private void initEditTextListeners(AutoCompleteTextView destinationEditText)
    {
        destinationEditText.setOnEditorActionListener(this);
        destinationEditText.setOnFocusChangeListener(this);

        destinationEditText.setOnItemClickListener((parent, view, position, id)
                -> onBusStopItemClick(parent, view, position, destinationEditText.getId()));
    }

    /* Listener for when enter is clicked on keyboard.
     * return true if you have consumed the action, else false.
     */
    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event)
    {
        BusStop busStop;

        if (textView.getId() == startDestinationEditText.getId())
        {
            busStop = getBusStopOnEditorAction(startDestinationEditText);
            startDestinationMarker = setDestinationMarker(busStop,
                    startDestinationEditText,
                    startDestinationMarker,
                    mapUtils.getStartDestinationIcon());

            if (startDestinationMarker != null)
                moveCameraToDestination(startDestinationMarker);

        } else if (textView.getId() == endDestinationEditText.getId())
        {
            busStop = getBusStopOnEditorAction(endDestinationEditText);
            endDestinationMarker = setDestinationMarker(busStop,
                    endDestinationEditText,
                    endDestinationMarker,
                    mapUtils.getEndDestinationIcon());

            if (endDestinationMarker != null)
                moveCameraToDestination(endDestinationMarker);
        }

        checkMarkersForRoute();
        textView.clearFocus();
        return true;
    }

    private void checkMarkersForRoute()
    {
        if (startDestinationMarker != null && endDestinationMarker == null)
            mapUtils.onDestinationTextInput((BusStop) startDestinationMarker.getTag(), getContext());
        else if (startDestinationMarker == null && endDestinationMarker != null)
            mapUtils.onDestinationTextInput((BusStop) endDestinationMarker.getTag(), getContext());
        else if (startDestinationMarker != null && endDestinationMarker != null)
        {
            BusStop busStopA = (BusStop) startDestinationMarker.getTag();
            BusStop busStopB = (BusStop) endDestinationMarker.getTag();
            mapUtils.drawRoutesThroughBusStops(busStopA, busStopB, getContext());
        }
    }

    private Marker setDestinationMarker(BusStop busStop, AutoCompleteTextView destinationEditText, Marker oldDestinationMarker, BitmapDescriptor destinationIcon)
    {
        if (busStop == null)
        {
            if (oldDestinationMarker != null)
                oldDestinationMarker.setIcon(mapUtils.getBusIcon());
            return null;
        }

        if (oldDestinationMarker != null)
            oldDestinationMarker.setIcon(mapUtils.getBusIcon()); // reset marker icon

        destinationEditText.setText(busStop.getDesc());

        Marker newDestinationMarker = busStopMarkersHash.get(busStop.getBusStopId());
        newDestinationMarker.setIcon(destinationIcon);

        return newDestinationMarker;
    }


    /**
     * @param destinationTextView startDestinationEditText or endDestinationEditText
     * @return null if bus stop was not found, else first match from dropDown menu
     */
    private BusStop getBusStopOnEditorAction(AutoCompleteTextView destinationTextView)
    {
        String inputText = destinationTextView.getText().toString().trim();
        if ("".equals(inputText))   // if string is empty, reset destination marker
        {
            destinationTextView.getText().clear();
            return null;
        }

        ArrayAdapter<BusStop> arrayAdapter = (ArrayAdapter<BusStop>) destinationTextView.getAdapter();
        BusStop busStop = null;

        if (arrayAdapter == null || arrayAdapter.getCount() == 0)
        {
            Toast.makeText(context, context.getString(R.string.unknown_busstop), Toast.LENGTH_SHORT).show();
        } else
        {
            busStop = arrayAdapter.getItem(0); //get first item from list
        }

        return busStop;
    }

    /* When item in autocomplete suggestion list is clicked */
    private void onBusStopItemClick(AdapterView<?> parent, View view, int position, int editTextid)
    {
        Object item = parent.getItemAtPosition(position);
        BusStop busStop = (BusStop) item;
        switch (editTextid)
        {
            case R.id.start_destination_edittext:
                startDestinationMarker = setDestinationMarker(busStop,
                        startDestinationEditText,
                        startDestinationMarker,
                        mapUtils.getStartDestinationIcon());

                if (startDestinationMarker != null)
                    moveCameraToDestination(startDestinationMarker);

                startDestinationEditText.clearFocus();
                break;

            case R.id.end_destination_edittext:
                endDestinationMarker = setDestinationMarker(busStop,
                        endDestinationEditText,
                        endDestinationMarker,
                        mapUtils.getEndDestinationIcon());

                if (endDestinationMarker != null)
                    moveCameraToDestination(endDestinationMarker);

                endDestinationEditText.clearFocus();
                break;
        }
        checkMarkersForRoute();
    }

    /* animate move camera to the marker as arguemnt */
    private void moveCameraToDestination(Marker destinationMarker)
    {
        if (destinationMarker != null)
        {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(destinationMarker.getPosition())
                    .zoom(Constants.DEFAULT_ZOOM)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), CAMERA_SPEED, null);
            destinationMarker.setVisible(true);
        } else
            Toast.makeText(context, context.getResources().getString(R.string.no_busstop_found), Toast.LENGTH_SHORT).show();
    }

    /**
     * Used when edittext loses focus to check if bouth start and end destination are set,
     * and draw routes containing entered bus stops
     *
     * @param view
     * @param hasFocus true if searchbox is focused, else false
     */
    @Override
    public void onFocusChange(View view, boolean hasFocus)
    {
        if (!hasFocus)
            KeyboardUtils.hideKeyboard(getView());

        if (view.getId() == startDestinationEditText.getId())
        {
            showDropdownOnFocusChange(hasFocus, startDestinationEditText);

        } else if (view.getId() == endDestinationEditText.getId())
        {
            showDropdownOnFocusChange(hasFocus, endDestinationEditText);
        }
    }

    private void showDropdownOnFocusChange(boolean hasFocus, AutoCompleteTextView startDestinationEditText)
    {
        SearchBusStopAdapter searchBusStopAdapter = ((SearchBusStopAdapter) startDestinationEditText.getAdapter());
        if ("".contentEquals(startDestinationEditText.getText()) && searchBusStopAdapter != null)
            searchBusStopAdapter.getFilter().filter(startDestinationEditText.getText());
        showDropDown(startDestinationEditText, hasFocus);
    }


    private void showDropDown(AutoCompleteTextView destinationEditText, boolean isFocused)
    {
        if (isFocused)
        {
            handler.postDelayed(destinationEditText::showDropDown, SHORT_DELAY);
        } else
            destinationEditText.dismissDropDown();
    }

    //__________________________________________________________________________________________________________________________________________

    //show warning icon if there is no internet, else hide it
    public void initShowHideConnectionButton()
    {
        if (mapViewModel != null)
            mapViewModel.getIsInternetAvailable().observe(getViewLifecycleOwner(), this::setShowNoConnectionButton);
    }

    private void setShowNoConnectionButton(boolean isNetworkAvailable)
    {
        // if there is internet connection => hide button, else show button
        if (!isNetworkAvailable)
            ((FloatingActionButton) mapFragmentView.findViewById(R.id.connection_fab)).show();
        else
            ((FloatingActionButton) mapFragmentView.findViewById(R.id.connection_fab)).hide();
    }

    /**
     * initialize show all routes and bus stops button
     */
    private void showAllRoutesAndBusStopButton()
    {
        FloatingActionButton showAllButton = (FloatingActionButton) mapFragmentView.findViewById(R.id.show_all_routes_busstops);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            showAllButton.setTooltipText(getString(R.string.show_all_button));
        }
        showAllButton.setOnClickListener(v ->
        {
            resetStartEndDestinationMarkers();

            mapUtils.setAllRouterPolylinesVisibility(true);
            mapUtils.setAllBusStopMarkersVisibility(true);

            if (busController != null)
            {
                busController.resetBusLocationUrlQuery();
                busController.setIsBusMarkerClicked(false);
            }
        });
    }

    /**
     * Buttons for zooming in and out, has to be called after map is created ( in OnMapReady method)
     */
    private void initializeZoomButtons()
    {
        FloatingActionButton zoomInBtn = (FloatingActionButton) mapFragmentView.findViewById(R.id.fab_zoom_in);
        zoomInBtn.setOnClickListener(v -> map.animateCamera(CameraUpdateFactory.zoomIn()));

        FloatingActionButton zoomOutBtn = (FloatingActionButton) mapFragmentView.findViewById(R.id.fab_zoom_out);
        zoomOutBtn.setOnClickListener(v -> map.animateCamera(CameraUpdateFactory.zoomOut()));
    }

    private void resetStartEndDestinationMarkers()
    {
        if (startDestinationMarker != null)
        {
            startDestinationMarker.setIcon(mapUtils.getBusIcon());
            startDestinationEditText.getText().clear();
            startDestinationEditText.clearFocus();
        }
        startDestinationMarker = null;

        if (endDestinationMarker != null)
        {
            endDestinationMarker.setIcon(mapUtils.getBusIcon());
            endDestinationEditText.getText().clear();
            endDestinationEditText.clearFocus();
        }
        endDestinationMarker = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null)
        {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        if (mapLayout != null)
            mapLayout.onSaveInstanceState(mapViewBundle);
    }

    /* Check for saved settings from sharedPreferences */
    private void checkFragmentArguemnts()
    {
        String mapTypeSelected = Utils.getSharedPreferencesKeyValue(SettingsFragment.SELECTED_MAP_TYPE, "1", context);
        mapType = Integer.parseInt(mapTypeSelected);
        mapStyle = Utils.getSharedPreferencesKeyValue(SettingsFragment.SELECTED_MAP_STYLE, "normal", context);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (mapViewModel != null)
            mapViewModel.activatePoolExecutorService();

        if (busController != null)
        {
            busController.startBusTracking(getContext());
        }

        if (mapUtils != null)
            mapUtils.checkPoolState();

        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        mapViewModel.getFromDBandServer(getContext());
        initShowHideConnectionButton();
        mapLayout.onResume();

    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (mapViewModel != null)
            mapViewModel.activatePoolExecutorService();

        if (mapUtils != null)
            mapUtils.checkPoolState();

        mapLayout.onStart();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mapLayout.onStop();
        if (markerDialog != null)
            markerDialog.destroyInstance();
        if (mapUtils != null)
            mapUtils.shutdownPoolExecutorService();
        if (mapViewModel != null)
            mapViewModel.shutdownPoolExecutorService();
        if (busController != null)
            busController.setActive(false);

    }

    @Override
    public void onPause()
    {
        if (mapLayout != null)
            mapLayout.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        if (mapLayout != null)
            mapLayout.onDestroy();

        super.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        if (mapLayout != null)
            mapLayout.onLowMemory();
    }

}
