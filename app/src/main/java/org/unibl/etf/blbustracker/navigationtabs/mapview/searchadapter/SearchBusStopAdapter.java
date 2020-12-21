package org.unibl.etf.blbustracker.navigationtabs.mapview.searchadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;
import org.unibl.etf.blbustracker.utils.languageutil.LatinCyrillicUtil;

import java.util.ArrayList;
import java.util.List;

//For filling AutocompleteTextView with bus stop names
public class SearchBusStopAdapter extends ArrayAdapter<BusStop>
{
    Context context;
    private List<BusStop> busStopListFull;

    public SearchBusStopAdapter(@NonNull Context context, @NonNull List<BusStop> busStopList)
    {
        super(context, 0, busStopList);
        this.context = context;
        busStopListFull = new ArrayList<>(busStopList); // full copy
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
                    .inflate(R.layout.item_busstop_search, parent, false);
        }
        if (position % 2 == 0)
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.busline_lighter_blue));
        else
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.busline_darker_blue));

        TextView textBusStop = convertView.findViewById(R.id.item_busstop_text);


        BusStop busStop = getItem(position);
        if (busStop != null && busStop.getDesc() != null)
        {
            textBusStop.setText(busStop.getDesc());
        }

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter()
    {
        return filter;
    }

    //For Filtering search, need at least 2 chars to show view
    private Filter filter = new Filter()
    {
        @Override
        protected FilterResults performFiltering(CharSequence inputSequence)
        {
            FilterResults results = new FilterResults();
            List<BusStop> suggestions = new ArrayList<>();

            if (inputSequence == null || inputSequence.toString().trim().length() == 0)
                suggestions.addAll(busStopListFull);
            else
            {
                String filterPatern = inputSequence.toString().toLowerCase().trim();

                //finding matches
                for (BusStop busStop : busStopListFull)
                    if (LatinCyrillicUtil.isMatched(filterPatern, busStop.getDesc().toLowerCase()))
                    {
                        suggestions.add(busStop);
                    }
            }

            results.values = suggestions;
            results.count = suggestions.size();
            return results;
        }

        //publish/show filtered results
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue)
        {
            return ((BusStop) resultValue).getDesc();
        }
    };

}
