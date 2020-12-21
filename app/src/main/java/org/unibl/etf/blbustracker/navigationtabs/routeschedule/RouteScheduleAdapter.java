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
    Context context;
    private List<Route> routeListFull;

    public RouteScheduleAdapter(@NonNull Context context, @NonNull List<Route> routeList)
    {
        super(context, 0, routeList);
        this.context = context;
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
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.busline_lighter_blue));
        else
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.busline_darker_blue));

        TextView routeName = convertView.findViewById(R.id.route_name);
        TextView routeLabel = convertView.findViewById(R.id.route_lable);
        ImageView busImage = convertView.findViewById(R.id.bus_image);


        Route route = getItem(position);
        if (route != null && route.getName() != null && route.getLabel() != null)
        {
            routeLabel.setText(route.getLabel());
            routeName.setText(route.getName());
            busImage.setImageDrawable(DrawableUtil.getColoredBusDrawable(route,context));
        }

        return convertView;
    }

    //For Filtering search, need at least 2 chars to show view
    private Filter filter = new Filter()
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

                //                Log.d(getClass().getSimpleName(), "performFiltering: START checkin languge");
                //                System.out.println("input: " + filterPatern);
                //                if (LatinUtils.isInputLatin(filterPatern))
                //                    System.out.println("LAT: " + LatinUtils.stripAccent(filterPatern));
                //                else if (CyrillicUtils.isCyrillic(filterPatern))
                //                    System.out.println("CYR: " + CyrillicUtils.convertCyrToLat(filterPatern));
                //                else
                //                    System.out.println("NIJE NI JEDNO NI DRUGO");
                //                //
                //                Log.d(getClass().getSimpleName(), "performFiltering: END checkin languge");


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

