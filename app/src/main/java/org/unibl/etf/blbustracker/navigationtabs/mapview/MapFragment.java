package org.unibl.etf.blbustracker.navigationtabs.mapview;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Keep;
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
import org.unibl.etf.blbustracker.navigationtabs.announcements.NewAnnouncementModel;
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
import org.unibl.etf.blbustracker.utils.languageutil.Translator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main view interactions
 */
public class MapFragment extends Fragment implements OnMapReadyCallback
        //        , MarkerDialogListener
        //        , OnRouteClickedListener
        //        , MoreOptionsInterface
        , GoogleMap.OnMapClickListener
        , GoogleMap.OnMarkerClickListener
        , GoogleMap.OnPolylineClickListener
        , TextView.OnEditorActionListener
        , TextWatcher
        , View.OnFocusChangeListener
{
    public static final int CAMERA_SPEED = 1000;
    //initialized in onCreateView(...)
    private transient View mapFragmentView;
    private transient Context context;

    private transient static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private transient GoogleMap map;

    //google MapView, needed for calling onRasume,onStop,...
    private transient MapView mapLayout;

    // for map appearance
    private transient int mapType = 1; // 1 - NORMAL , 2 - SATELLITE, 3 - TERRAIN, 4 - HYBRID
    private transient String mapStyle = "normal";

    //for getting Station and Route onChange information (observers)
    private transient MapViewModel mapViewModel;
    //new announmcent view model for checking if there was a new announcemnt and to show/hide announcemnt icon
    private transient NewAnnouncementModel newAnnouncementModel;

    //for working with map objects such as show/hide routes and/or bus stops
    private transient MapUtils mapUtils;

    //layout for AutocompleteTextView, atribute used for expanding/collapsing
    private transient RelativeLayout collapsableSearchLayout;

    //for showing bus routes dialog
    private transient RoutesBottomFragment routesBottomDialog;

    //maybe move to MapUtil/MapModel
    private transient Map<Integer, Marker> busStopMarkersHash;
    private transient Map<Integer, Polyline> routePolylineHash;

    //fields for entering start and end destination bus stop
    private transient AutoCompleteTextView searchBusStopEditText;
    private transient Button clearBtn; // clear text in searchBusStopEditText


    //start and end destinations text under search
    private transient TextView startBusStopTV;
    private transient TextView endBusStopTV;

    //start and end destination marker
    private transient Marker endDestinationMarker;
    private transient Marker startDestinationMarker;

    //dialog when bus stop is clicked
    private transient MarkerDialog markerDialog;

    //for getting and drawing buses on map
    private transient BusController busController;

    //translate text to latin/cyrillic
    private transient Translator translator;

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

        context = getActivity();
        collapsableSearchLayout = mapFragmentView.findViewById(R.id.search_relativelayout);
        translator = new Translator(getContext());

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
        map.clear();

        mapUtils = new MapUtils(mapFragmentView.getContext(), map, busStopMarkersHash, routePolylineHash);
        mapUtils.setAllMapArguments(mapType, mapStyle, getContext());

        //start tracking buses location
        busController = new BusController(map, context);
        busController.startBusTracking(getContext());

        //observe BusStop list changes
        mapViewModel.getMutableBusStops().observe(getViewLifecycleOwner(), busStops ->
        {
            mapUtils.onBusStopChanged(busStops);
            SearchBusStopAdapter searchBusStopAdapter = new SearchBusStopAdapter(getActivity(), busStops);
            searchBusStopEditText.setAdapter(searchBusStopAdapter);
        });

        //observe Route list changes
        mapViewModel.getMutableRoutes().observe(getViewLifecycleOwner(), routeList ->
        {
            mapUtils.onRouteChanged(routeList);
            routesBottomDialog.setAllRoutes(routeList);
            restartBusController(routeList);
        });

        initializeZoomButtons();

        map.setInfoWindowAdapter(new MarkerCustomInfoWindow(context));
        map.setOnMarkerClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnPolylineClickListener(this);

        mapUtils.setDefaultLocation();  // center map to default location
    }

    private void restartBusController(List<Route> routeList)
    {
        busController.setActive(false);
        busController = null;
        busController = new BusController(map, context);
        busController.setRoutes(routeList);
        busController.startBusTracking(context);
    }
    //*****************************************************************************************************************


    /**
     * @param marker marker that was clicked
     * @return false - move camera so that marker is in the center, true - do not move camera
     */
    private ArrivalTimeFragment timeFragment;

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        Object obj = marker.getTag();

        if (obj instanceof Bus)
        {
            marker.showInfoWindow();
            if (busController != null)
            {
                busController.setIsBusMarkerClicked(true);
            }

        } else if (obj instanceof BusStop)
        {
            isMarkerClicked = true; // used in onMapClicked(...)
            marker.hideInfoWindow();
            BusStop busStop = (BusStop) obj;
            if (timeFragment != null && timeFragment.isAdded())
                timeFragment.dismiss();
            timeFragment = new ArrivalTimeFragment();
            Bundle argsBundle = new Bundle();
            MoreOptionsInterface moreOptionsInterface = new MoreOptionsInterface()
            {
                @Override
                public void onMoreOptionsClicked(BusStop busStop)
                {
                    onMoreOptionsClick(busStop);
                }

                @Override
                public int describeContents()
                {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags)
                {

                }
            };
            argsBundle.putParcelable(Constants.BUSSTOP_PARCELABLE, busStop);
            argsBundle.putParcelable(Constants.MOREOPTIONS_PARCELABLE, moreOptionsInterface);
            timeFragment.setArguments(argsBundle);
            if (!isRemoving())
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

        routesBottomDialog = new RoutesBottomFragment();
        OnRouteClickedListener onRouteClickedListener = new OnRouteClickedListener()
        {
            @Override
            public void onRouteInDialogClicked(int position)
            {
                MapFragment.this.onRouteInDialogClicked(position);
            }

            @Override
            public int describeContents()
            {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags)
            {

            }
        };
        Bundle argsBundle = new Bundle();
        argsBundle.putParcelable(Constants.ONROUTECLICK_LISTENER_PARCELABLE, onRouteClickedListener);
        routesBottomDialog.setArguments(argsBundle);

        routesFloatingButton.setOnClickListener(v ->
        {
            Utils.shortButtonDisableOnClick(getActivity(), routesFloatingButton);
            if (routesBottomDialog.isAdded())
                routesBottomDialog.dismiss();

            routesBottomDialog.show(getActivity().getSupportFragmentManager(), "route_bottom_dialog");
        });
    }

    //___________________________________ Dialog Butoons ______________________________________________

    public void onMoreOptionsClick(BusStop busStop)
    {
        markerDialog = new MarkerDialog();
        MarkerDialogListener markerDialogListener = new MarkerDialogListener()
        {
            @Override
            public void setAsStartDestinationBtn(int stationId)
            {
                MapFragment.this.setAsStartDestinationBtn(stationId);
            }

            @Override
            public void setAsEndDestinationBtn(int stationId)
            {
                MapFragment.this.setAsEndDestinationBtn(stationId);
            }

            @Override
            public void startNavigationBtn(int stationId)
            {
                MapFragment.this.startNavigationBtn(stationId);
            }

            @Override
            public int describeContents()
            {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags)
            {

            }
        };
        Bundle bundle = new Bundle();
        int busStopId = busStop.getBusStopId();
        bundle.putInt(MarkerDialog.DIALOG_BUSSTOP_ID, busStopId);
        bundle.putString(MarkerDialog.DIALOG_BUSSTOP_NAME, busStop.getDesc());
        bundle.putParcelable(Constants.MARKERDIALOG_LISTENER_PARCELABLE, markerDialogListener);
        // more options marker popup window
        markerDialog.setArguments(bundle);
        markerDialog.show(getActivity().getSupportFragmentManager(), "tag");
    }


    /* set as start destination button click listener displayed in custom MarkerDialog  */
    public void setAsStartDestinationBtn(int busStopId)
    {
        startDestinationMarker = setAsDestinationMarker(busStopId, startBusStopTV, startDestinationMarker,
                mapUtils.getStartDestinationIcon());

        checkMarkersForRoute();
        isMarkerClicked = false;
    }

    /* set as end destination button click listener displayed in custom MarkerDialog  */
    public void setAsEndDestinationBtn(int busStopId)
    {
        endDestinationMarker = setAsDestinationMarker(busStopId, endBusStopTV, endDestinationMarker,
                mapUtils.getEndDestinationIcon());

        checkMarkersForRoute();
        isMarkerClicked = false;
    }

    private Marker setAsDestinationMarker(int busStopId, TextView destinationTV, Marker destinationMarker, BitmapDescriptor icon)
    {
        if (destinationMarker != null)
            resetBusStopMarker(destinationMarker);

        Marker marker = busStopMarkersHash.get(busStopId);
        BusStop busStop = (BusStop) marker.getTag();
        String busStopName = translator.translateInput(busStop.getDesc());
        destinationTV.setText(busStopName);
        marker.setIcon(icon);
        return marker;
    }

    //start google navigation to the selected bus stop
    public void startNavigationBtn(int busStopId)
    {
        LatLng position = busStopMarkersHash.get(busStopId).getPosition();
        mapUtils.startGoogleMapNavigation(position, context);
    }

    /**
     * when route in dialog window is clicked. show only this route on map
     * and filter bus stops in search text
     *
     * @param position of clicked item in the list
     */
    public void onRouteInDialogClicked(int position)
    {
        Route route = routesBottomDialog.getRouteFromAdapter(position);

        if (busController != null)
        {
            busController.setBusLocationUrlQuery(route.getName());
            busController.setIsBusMarkerClicked(false);
        }

        mapUtils.onRouteInDialogClicked(route, searchBusStopEditText, getContext());

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

        } else if (searchBusStopEditText.hasFocus())
        {
            searchBusStopEditText.clearFocus();
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
        searchBusStopEditText = (AutoCompleteTextView) mapFragmentView.findViewById(R.id.search_busstop_edittext);
        clearBtn = mapFragmentView.findViewById(R.id.search_clear_btn);
        clearBtn.setOnClickListener(l ->
        {
            searchBusStopEditText.setText("");
            if (searchBusStopEditText.requestFocus())
            {
                KeyboardUtils.showKeyBoard(searchBusStopEditText);
            }
        });

        startBusStopTV = mapFragmentView.findViewById(R.id.start_busstop_text);
        startBusStopTV.setOnClickListener(v ->
        {
            if (startDestinationMarker != null)
                moveCameraToDestination(startDestinationMarker);
        });
        endBusStopTV = mapFragmentView.findViewById(R.id.end_busstop_text);
        endBusStopTV.setOnClickListener(v ->
        {
            if (endDestinationMarker != null)
                moveCameraToDestination(endDestinationMarker);
        });

        initEditTextListeners(searchBusStopEditText);
    }


    //Set all listeners and text watcher to the AutoCompleteTextView
    private void initEditTextListeners(AutoCompleteTextView searchEditText)
    {
        searchEditText.clearFocus();
        searchEditText.setOnEditorActionListener(this);
        searchEditText.setOnFocusChangeListener(this);
        searchEditText.setOnClickListener(l -> showDropDownFunction(searchEditText, true));
        searchEditText.addTextChangedListener(this);

        searchEditText.setOnItemClickListener((parent, view, position, id)
                -> onBusStopItemClick(parent, view, position, searchEditText.getId()));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
        //nothing to do there
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        if ("".equals(s.toString()))
        {
            clearBtn.setVisibility(View.INVISIBLE);
            showDropDownFunction(searchBusStopEditText, true);
        } else
        {
            clearBtn.setVisibility(View.VISIBLE);
        }
        if (startDestinationMarker != null && endDestinationMarker != null)
        {
            resetBothDestinationMarkers();
            if (mapUtils != null)
            {
                mapUtils.setAllRouterPolylinesVisibility(true);
                mapUtils.setAllBusStopMarkersVisibility(true);
            }
            if (busController != null)
            {
                busController.resetBusLocationUrlQuery();
                busController.setIsBusMarkerClicked(false);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s)
    {
        //nothing to do there
    }

    /* Listener for when enter is clicked on keyboard.
     * return true if you have consumed the action, else false.
     */
    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event)
    {

        BusStop busStop = getBusStopOnEditorAction(searchBusStopEditText);
        if (busStop == null)
            return false;
        Marker newDestinationMarker = busStopMarkersHash.get(busStop.getBusStopId());
        moveCameraToDestination(newDestinationMarker);

        textView.clearFocus();
        return true;
    }

    private void checkMarkersForRoute()
    {
        if (startDestinationMarker != null && endDestinationMarker == null)
        {
            mapUtils.onDestinationTextInput((BusStop) startDestinationMarker.getTag(), busController
                    , true, this::updateAdapter, getContext());

        } else if (startDestinationMarker == null && endDestinationMarker != null)
        {
            mapUtils.onDestinationTextInput((BusStop) endDestinationMarker.getTag(), busController
                    , false, this::updateAdapter, getContext());

        } else if (startDestinationMarker != null && endDestinationMarker != null)
        {
            BusStop busStopA = (BusStop) startDestinationMarker.getTag();
            BusStop busStopB = (BusStop) endDestinationMarker.getTag();
            mapUtils.drawRoutesThroughBusStops(busStopA, busStopB, busController
                    , this::onShowAllButtonClicked, this::updateAdapter, getContext());
        }
    }


    public void updateAdapter(List<Integer> busstopIds)
    {
        List<BusStop> reachableBusStop;
        if (busstopIds == null)
        {
            reachableBusStop = busStopMarkersHash.keySet().stream()
                    .map(key -> (BusStop) busStopMarkersHash.get(key).getTag())
                    .collect(Collectors.toList());
        } else
        {
            reachableBusStop = busstopIds.stream()
                    .map(busstopid -> (BusStop) busStopMarkersHash.get(busstopid).getTag())
                    .collect(Collectors.toList());
        }
        SearchBusStopAdapter adapter = new SearchBusStopAdapter(context, reachableBusStop);
        searchBusStopEditText.setAdapter(adapter);
    }

    private Marker setDestinationMarker(BusStop busStop, AutoCompleteTextView destinationEditText, Marker oldDestinationMarker, BitmapDescriptor destinationIcon)
    {
        if (busStop == null)
        {
            if (oldDestinationMarker != null)
                resetBusStopMarker(oldDestinationMarker);
            return null;
        }

        if (oldDestinationMarker != null)
            resetBusStopMarker(oldDestinationMarker);// reset marker icon

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
            showDropDownFunction(searchBusStopEditText, false);
            searchBusStopEditText.clearFocus();
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
        Marker newDestinationMarker = busStopMarkersHash.get(busStop.getBusStopId());
        moveCameraToDestination(newDestinationMarker);
        searchBusStopEditText.setText(translator.translateInput(busStop.getDesc()));
        searchBusStopEditText.clearFocus();

    }

    /* animate move camera to the marker as arguemnt */
    private void moveCameraToDestination(Marker destinationMarker)
    {
        if (destinationMarker != null)
        {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(destinationMarker.getPosition())
                    .zoom(Constants.BIGGER_ZOOM)
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
     * @param hasFocus true if searchbox is focused, else false
     */
    @Override
    public void onFocusChange(View view, boolean hasFocus)
    {
        if (!hasFocus)
            KeyboardUtils.hideKeyboard(getView());

        if (view.getId() == searchBusStopEditText.getId())
        {
            setDestionationTextHint(searchBusStopEditText, hasFocus, getString(R.string.search_station));
        }
    }

    private void setDestionationTextHint(AutoCompleteTextView destinationEditText, boolean hasFocus, String hint)
    {
        if (hasFocus)
            destinationEditText.setHint("");
        else
            destinationEditText.setHint(hint);
    }

    //i hate this method, so much BadTokenExceptions
    private void showDropDownFunction(AutoCompleteTextView searchEditText, boolean isFocused)
    {
        try
        {
            if (isFocused && !searchEditText.isPopupShowing())
            {
                if (getActivity() != null && !getActivity().isFinishing() && !isRemoving())
                    searchEditText.showDropDown();
            } else
            {
                if (getActivity() != null && !getActivity().isFinishing() && !isRemoving())
                    searchEditText.dismissDropDown();
            }
        } catch (Exception ex)
        {
        }

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
        showAllButton.setOnClickListener(v -> onShowAllButtonClicked());
    }

    private void onShowAllButtonClicked()
    {
        if (busController != null)
        {
            busController.resetBusLocationUrlQuery();
            busController.setIsBusMarkerClicked(false);
        }

        if (mapUtils != null)
        {
            mapUtils.setAllRouterPolylinesVisibility(true);
            List<BusStop> busStops = mapUtils.resetBusStopsVisibility(busStopMarkersHash);
            searchBusStopEditText.setAdapter(new SearchBusStopAdapter(getContext(), busStops));
            mapUtils.moveToBounds(routePolylineHash);
        }
        resetStartEndDestinationMarkers();
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
        resetBothDestinationMarkers();

        searchBusStopEditText.getText().clear();
        searchBusStopEditText.clearFocus();
        showDropDownFunction(searchBusStopEditText, false);
    }

    private void resetBothDestinationMarkers()
    {
        if (startDestinationMarker != null)
            resetMarkerAndText(startDestinationMarker, startBusStopTV);
        startDestinationMarker = null;

        if (endDestinationMarker != null)
            resetMarkerAndText(endDestinationMarker, endBusStopTV);
        endDestinationMarker = null;
    }

    private void resetMarkerAndText(Marker destinationMarker, TextView destinationEditText)
    {
        resetBusStopMarker(destinationMarker);
        destinationEditText.setText("");
    }

    private void resetBusStopMarker(Marker destinationMarker)
    {
        boolean wasVisable = true;
        if (!destinationMarker.isVisible())
        {
            wasVisable = false;
            destinationMarker.setVisible(true);
        }
        destinationMarker.setIcon(mapUtils.getDefaultBusStopIcon());

        if (!wasVisable)
            destinationMarker.setVisible(false);
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
        mapLayout.onResume();

        if (mapViewModel != null)
            mapViewModel.activatePoolExecutorService();

        if (busController != null)
        {
            busController.startBusTracking(getContext());
        }

        if (mapUtils != null)
            mapUtils.activatePoolExecutorService();

        if (mapViewModel == null)
            mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        mapViewModel.getFromDBandServer(context);

        if (newAnnouncementModel == null)
            newAnnouncementModel = new NewAnnouncementModel(getActivity(), getActivity().findViewById(R.id.new_announcemnt_icon));
        newAnnouncementModel.startListen();

        resetStartEndDestinationMarkers();
        initShowHideConnectionButton();
    }


    @Override
    public void onStart()
    {
        super.onStart();
        if (mapViewModel != null)
            mapViewModel.activatePoolExecutorService();

        if (mapUtils != null)
            mapUtils.activatePoolExecutorService();

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
        {
            mapViewModel.shutdownPoolExecutorService();
        }
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

        AnimationUtils.resetValue();
        super.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        if (mapLayout != null)
            mapLayout.onLowMemory();
    }

    @Keep
    public MapFragment()
    {
    }
}
