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
import android.widget.Button;
import android.widget.TableLayout;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.navigationtabs.mapview.MapViewModel;

import java.util.List;

public class RouteScheduleFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener, TextWatcher
{
    private static final int DELAY_MILLIS = 100;
    private MapViewModel viewModel;
    private TableLayout scheduleTableLayout;

    private AutoCompleteTextView routeScheduleInput;
    private Button clearBtn;
    private Handler mainHnadler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_route_schedule, container, false);

        mainHnadler = new Handler(Looper.getMainLooper());

        populateAutoCompleteInput(view);
        scheduleTableLayout = view.findViewById(R.id.tablelayout);
        clearBtn = view.findViewById(R.id.clear_btn);
        clearBtn.setOnClickListener(l-> routeScheduleInput.setText(""));

        return view;
    }

    //populate AutoCompleteTextView with routes from database
    public void populateAutoCompleteInput(View view)
    {
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        viewModel.getAndPlaceDBRoute(getContext());
        viewModel.getMutableRoutes().observe(getViewLifecycleOwner(), routes -> initAutoCompleteTextView(view, routes));
    }

    public void initAutoCompleteTextView(View view, List<Route> routes)
    {
        RouteScheduleAdapter routeScheduleAdapter = new RouteScheduleAdapter(view.getContext(), routes);
        routeScheduleInput = view.findViewById(R.id.route_schedule_input);
        routeScheduleInput.setAdapter(routeScheduleAdapter);

        showAllSuggestions();

        routeScheduleInput.setOnClickListener(this);
        routeScheduleInput.setOnItemClickListener(this);
        routeScheduleInput.addTextChangedListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Object obj = parent.getItemAtPosition(position);
        if (obj instanceof Route)
        {
            Route route = (Route) obj;
            RouteScheduleModel routeScheduleModel = new RouteScheduleModel(route);

            scheduleTableLayout.removeAllViews();
            try
            {
                scheduleTableLayout.addView(routeScheduleModel.getWorkDaySchedule(getContext()));
                scheduleTableLayout.addView(routeScheduleModel.getSaturdaySchedule(getContext()));
                scheduleTableLayout.addView(routeScheduleModel.getSundaySchedule(getContext()));

            } catch (Exception ex)
            {
                scheduleTableLayout.removeAllViews();
                scheduleTableLayout.addView(routeScheduleModel.getAllSchedules(getContext()));
            }
        }
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
        if (routeScheduleInput != null && getActivity() != null && !getActivity().isFinishing() && !isRemoving())
        {
            mainHnadler.postDelayed(() -> routeScheduleInput.showDropDown(), DELAY_MILLIS);   //there must be some delay
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
        //nothing to do here
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        if ("".equals(s.toString()))
        {
            clearBtn.setVisibility(View.INVISIBLE);
            showAllSuggestions();
        }
        else
            clearBtn.setVisibility(View.VISIBLE);

    }

    @Override
    public void afterTextChanged(Editable s)
    {
//        if (s.toString().trim().length() == 0)
//            showAllSuggestions();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (viewModel != null)
            viewModel.activatePoolExecutorService();

        showAllSuggestions();
    }


    @Override
    public void onDestroy()
    {
        if (viewModel != null)
            viewModel.shutdownPoolExecutorService();

        if (routeScheduleInput != null)
            routeScheduleInput.dismissDropDown();

        super.onDestroy();
    }

}