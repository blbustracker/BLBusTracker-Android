package org.unibl.etf.blbustracker.navigationtabs.routeschedule;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.navigationtabs.mapview.MapViewModel;

import java.util.List;

public class RouteScheduleFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener, TextWatcher
{
    public static final int DELAY_MILLIS = 100;
    private MapViewModel viewModel;
    private Handler mainHnadler;

    private AutoCompleteTextView routeScheduleInput;
    private TextView routeScheduleTimes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_route_schedule, container, false);
        mainHnadler = new Handler(Looper.getMainLooper());

        populateAutoCompleteInput(view);

        routeScheduleTimes = view.findViewById(R.id.route_schedule_times);
        return view;
    }

    //populate AutoCompleteTextView with routes from database
    public void populateAutoCompleteInput(View view)
    {
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        viewModel.getAndPlaceDBRoute();
        viewModel.getMutableRoutes().observe(getViewLifecycleOwner(), routes -> initAutoCompleteTextView(view, routes));
    }

    public void initAutoCompleteTextView(View view, List<Route> routes)
    {
        RouteScheduleAdapter routeScheduleAdapter = new RouteScheduleAdapter(view.getContext(), routes);
        routeScheduleInput = view.findViewById(R.id.route_schedule_input);
        routeScheduleInput.setAdapter(routeScheduleAdapter);
        routeScheduleInput.showDropDown();

        routeScheduleInput.setOnClickListener(this);
        routeScheduleInput.setOnItemClickListener(this);
        routeScheduleInput.addTextChangedListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Object obj = parent.getItemAtPosition(position);
        if (obj instanceof Route)
            routeScheduleTimes.setText(getSchedules((Route) obj));  //set schedule on screen
    }


    private String getSchedules(Route route)
    {
        if (route.getRouteSchedule() == null)
            return getString(R.string.no_route_schedule);
        String workday = getContext().getString(R.string.workday);
        String saturday = getContext().getString(R.string.saturday);
        String sunday = getContext().getString(R.string.sunday);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(workday).append(":\n").append(route.getWorkdaySchedule()).append("\n\n");
        stringBuilder.append(saturday).append(":\n").append(route.getSaturdaySchedule()).append("\n\n");
        stringBuilder.append(sunday).append(":\n").append(route.getSundaySchedule());

        return stringBuilder.toString();
    }

    @Override
    public void onClick(View v)
    {
        if (routeScheduleInput.getText().toString().trim().length() == 0
                && !routeScheduleInput.isPopupShowing())
        {
            showAllSuggestions();
        }
    }

    private void showAllSuggestions()
    {
        mainHnadler.postDelayed(() -> routeScheduleInput.showDropDown(), DELAY_MILLIS);   //there must be some delay
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
        //nothing to do here
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        //nothing to do here
    }

    @Override
    public void afterTextChanged(Editable s)
    {
        if (s.toString().trim().length() == 0)
            showAllSuggestions();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (viewModel != null)
            viewModel.activatePoolExecutorService();
        if(routeScheduleInput!=null)
            showAllSuggestions();
    }


    @Override
    public void onDestroy()
    {
        if (viewModel != null)
            viewModel.shutdownPoolExecutorService();

        if(routeScheduleInput!=null)
            routeScheduleInput.dismissDropDown();

        super.onDestroy();
    }

}