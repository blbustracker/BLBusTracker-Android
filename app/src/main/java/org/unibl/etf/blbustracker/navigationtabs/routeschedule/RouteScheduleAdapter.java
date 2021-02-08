package org.unibl.etf.blbustracker.navigationtabs.routeschedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.utils.DrawableUtil;
import org.unibl.etf.blbustracker.utils.languageutil.LatinCyrillicUtil;

import java.util.ArrayList;
import java.util.List;

//Adapter from filtering search results in Route Schedule
public class RouteScheduleAdapter extends ArrayAdapter<Route>
{
    private List<Route> routeListFull;

    private final String TIME_SPLIT_REGEX = "( :|,|\\.)";

    public RouteScheduleAdapter(@NonNull Context context, @NonNull List<Route> routeList)
    {
        super(context, 0, routeList);
        routeListFull = new ArrayList<>(routeList); // deep copy
    }

    //Need at least 2 chars to show view
    //Fill that view with result
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_route, parent, false);
        }
        if (position % 2 == 0)
            convertView.setBackgroundColor(ContextCompat.getColor(convertView.getContext(), R.color.busline_lighter_blue));
        else
            convertView.setBackgroundColor(ContextCompat.getColor(convertView.getContext(), R.color.busline_darker_blue));

        TextView routeName = convertView.findViewById(R.id.route_name);
        TextView routeLabel = convertView.findViewById(R.id.route_lable);
        ImageView busImage = convertView.findViewById(R.id.bus_image);


        Route route = getItem(position);
        if (route != null && route.getName() != null && route.getLabel() != null)
        {
            routeLabel.setText(route.getLabel());
            routeName.setText(route.getName());
            busImage.setImageDrawable(DrawableUtil.getColoredBusDrawable(route,convertView.getContext()));
        }

        return convertView;
    }

    //For Filtering search, need at least 2 chars to show view
    private final Filter filter = new Filter()
    {
        @Override
        protected FilterResults performFiltering(CharSequence inputSequence)
        {
            FilterResults results = new FilterResults();
            List<Route> suggestions = new ArrayList<>();
            if (inputSequence == null || inputSequence.toString().trim().length() == 0)
                suggestions.addAll(routeListFull);
            else
            {
                String filterPatern = inputSequence.toString().toLowerCase().trim();
                for (Route route : routeListFull)
                    if (LatinCyrillicUtil.isMatched(filterPatern, route.getName().toLowerCase())
                            || LatinCyrillicUtil.isMatched(filterPatern, route.getLabel().toLowerCase()))
                    {
                        suggestions.add(route);
                    }
            }

            results.values = suggestions;
            results.count = suggestions.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged(); // to update view
        }

        @Override
        public CharSequence convertResultToString(Object resultValue)
        {
            Route route = (Route) resultValue;
            return route.getLabel().trim() + " " + route.getName().trim();
        }
    };

    @NonNull
    @Override
    public Filter getFilter()
    {
        return filter;
    }

}

