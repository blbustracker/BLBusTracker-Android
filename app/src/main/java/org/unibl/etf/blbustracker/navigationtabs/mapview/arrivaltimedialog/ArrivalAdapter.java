package org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.utils.languageutil.Translator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ArrivalAdapter extends RecyclerView.Adapter<ArrivalAdapter.ArrivalViewHolder>
{
    private static final int MAX_NUM_OF_TIMES = 2;
    private static final String TILDA = "~";

    private Context context;
    private Translator translator;
    private List<ArrivalTime> arrivalTimes;

    static class ArrivalViewHolder extends RecyclerView.ViewHolder
    {
        View cardView;  // to change colour
        TextView routeLblTV;
        TextView routeDestinationTV;
        TextView arrivalTimeTV;

        //inner constructor
        public ArrivalViewHolder(@NonNull View itemView)
        {
            super(itemView);
            this.cardView = itemView;
            routeLblTV = itemView.findViewById(R.id.route_lable);
            routeDestinationTV = itemView.findViewById(R.id.route_destination);
            arrivalTimeTV = itemView.findViewById(R.id.route_arrival_time);
        }
    }

    //outter constructor
    public ArrivalAdapter(Context context, List<ArrivalTime> arrivalTimes)
    {
        this(context);
        this.arrivalTimes = arrivalTimes;
        if (this.arrivalTimes == null)
            this.arrivalTimes = new ArrayList<>();
        notifyDataSetChanged();
    }

    //outter constructor
    public ArrivalAdapter(Context context)
    {
        this.context = context;
        translator = new Translator(context);
    }

    public void clearAllTimes()
    {
        if (arrivalTimes == null)
            return;
        int size = arrivalTimes.size();
        arrivalTimes.clear();
        notifyItemRangeRemoved(0, size);
    }

    @NonNull
    @Override
    public ArrivalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_arrival_time, parent, false);
        return new ArrivalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArrivalViewHolder holder, int position)
    {
        ArrivalTime currentArrivalTime = arrivalTimes.get(position);
        int colorId = (position % 2 == 0) ? R.color.busline_lighter_blue : R.color.busline_darker_blue;
        holder.itemView.setBackgroundColor(ContextCompat.getColor(context, colorId));

        holder.routeLblTV.setText(context.getString(R.string.route));
        holder.routeLblTV.append(" " + currentArrivalTime.getRouteLabel());
        String routeDestination = Route.getRouteDepatureName(currentArrivalTime.getRouteName());
        String transplatedDestination = translator.translateInput(routeDestination);
        holder.routeDestinationTV.setText(transplatedDestination);
        if (currentArrivalTime.isFromServer)
        {
            holder.arrivalTimeTV.setText(printTimes(currentArrivalTime.getArrivaleTimes()));
        } else
        {
            holder.arrivalTimeTV.setText(currentArrivalTime.getScheduleTime());
            holder.arrivalTimeTV.append("\n" + context.getString(R.string.from_starting));
        }
    }

    private String printTimes(List<Integer> times)
    {
        StringBuilder rowContent = new StringBuilder();
        if (times != null)
        {
            List<Integer> sortedTimes = times.stream().distinct().sorted().limit(MAX_NUM_OF_TIMES).collect(Collectors.toList());

            for (Integer time : sortedTimes)
            {
                long timeInMinutes = TimeUnit.SECONDS.toMinutes(time);

                String timeEst = ((timeInMinutes == 0) ? "<1" : TILDA + timeInMinutes) + context.getString(R.string.minutes);
                rowContent.append(timeEst).append(", ");
            }
            rowContent.setLength(rowContent.length() - 2);
        }
        return rowContent.toString();
    }

    @Override
    public int getItemCount()
    {
        return arrivalTimes.size();
    }

}
