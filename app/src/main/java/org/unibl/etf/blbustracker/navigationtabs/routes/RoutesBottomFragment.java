package org.unibl.etf.blbustracker.navigationtabs.routes;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.unibl.etf.blbustracker.Constants;
import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.utils.KeyboardUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for working with route UI when "Show routes button" is clicked
 */
public class RoutesBottomFragment extends BottomSheetDialogFragment
{
    private RecyclerView recyclerView;
    private RouteRecyclerAdapter routeRecyclerAdapter;

    private OnRouteClickedListener onRouteClickedListener;
    private List<Route> allRoutes;

    //call this method in onRouteChange
    public void setAllRoutes(List<Route> allRoutes)
    {
        this.allRoutes = allRoutes;
        if(routeRecyclerAdapter!=null && recyclerView!=null && allRoutes!=null)
        {
            routeRecyclerAdapter.setRoutes(new ArrayList<>(allRoutes));
            recyclerView.setAdapter(routeRecyclerAdapter);
        }
    }

    //executes after calling .showDialog() in MapFragment
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // better view of a Dialog when keyboard is shown
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View routeDialogView = inflater.inflate(R.layout.fragment_routes_dialog, container, false);
        recyclerView = (RecyclerView) routeDialogView.findViewById(R.id.street_recycler_view);

        getAndPlaceArgs();
        initRecyclerView(routeDialogView.getContext());
        initSearchView(routeDialogView);

        return routeDialogView;
    }

    private void getAndPlaceArgs()
    {
        if(onRouteClickedListener==null)
            onRouteClickedListener = getArguments().getParcelable(Constants.ONROUTECLICK_LISTENER_PARCELABLE);
    }

    /**
     * Makes sure that keyboard is under the BottomSheetDialogFragment
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog instanceof BottomSheetDialog)
        {
            ((BottomSheetDialog) dialog).getBehavior().setSkipCollapsed(true);
            ((BottomSheetDialog) dialog).getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        return dialog;
    }

    //initialize RecyclerView
    private void initRecyclerView(Context context)
    {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        // temporary empty array, while waiting for data
        if (allRoutes == null) allRoutes = new ArrayList<>();
        getAndPlaceArgs();
        routeRecyclerAdapter = new RouteRecyclerAdapter(new ArrayList<>(allRoutes), onRouteClickedListener, context);
        recyclerView.setAdapter(routeRecyclerAdapter);

    }

    // used for filtering/searching routes from RecyclerView
    private void initSearchView(View routeDialogView)
    {
        SearchView routeSearchView = routeDialogView.findViewById(R.id.route_search_editText);
        routeSearchView.setIconifiedByDefault(false);
        routeSearchView.setOnSearchClickListener(KeyboardUtils::hideKeyboard);  // hides keyboard when enter pressed
        routeSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                routeRecyclerAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    //get clicked route from RecyclerView
    public Route getRouteFromAdapter(int position)
    {
        return routeRecyclerAdapter.getRoute(position);
    }

    @Keep
    public RoutesBottomFragment()
    {
    }
}