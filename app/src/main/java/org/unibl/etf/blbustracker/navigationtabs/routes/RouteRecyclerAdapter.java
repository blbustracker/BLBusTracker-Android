package org.unibl.etf.blbustracker.navigationtabs.routes;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.utils.DrawableUtil;
import org.unibl.etf.blbustracker.utils.KeyboardUtils;
import org.unibl.etf.blbustracker.utils.languageutil.LatinCyrillicUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for setting and filtering routes(lines)
 */
public class RouteRecyclerAdapter extends RecyclerView.Adapter<RouteRecyclerAdapter.RouteViewHolder> implements Filterable
{
    private List<Route> routes;  // for filtering
    private List<Route> routesFull;
    private OnRouteClickedListener routeClickedListener;
    private Context context;

    // Provide a reference to the views for each data item
    // you provide access to all the views for a data item in a view holder
    public static class RouteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView routeName;
        TextView routeLabel;
        ImageView busImage;
        OnRouteClickedListener routeClickedListener;
        View listView;

        /**
         * Inner Constructor
         *
         * @param routeClickedListener listener from outter constructor
         */
        public RouteViewHolder(View itemView, OnRouteClickedListener routeClickedListener)
        {
            super(itemView);
            listView = itemView;
            routeName = itemView.findViewById(R.id.route_name);
            routeLabel = itemView.findViewById(R.id.route_lable);
            busImage = itemView.findViewById(R.id.bus_image);

            this.routeClickedListener = routeClickedListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            //hide keyboard when route is clicked
            KeyboardUtils.hideKeyboard(v);
            //calling implemented onRoutClicked (implemented in MapFragment class)
            routeClickedListener.onRouteInDialogClicked(getAdapterPosition());
        }
    }

    /**
     * Outter Constructor
     *
     * @param myDataset            set this data in view
     * @param routeClickedListener what happens when line is clicked
     * @param context              used for getting color in color.xml
     */
    public RouteRecyclerAdapter(List<Route> myDataset, OnRouteClickedListener routeClickedListener, Context context)
    {
        routes = myDataset;
        routesFull = new ArrayList<>(routes); //full copy
        this.context = context;
        this.routeClickedListener = routeClickedListener;
    }

    public Route getRoute(int position)
    {
        return routes.get(position);
    }

    public void setRoutes(List<Route> routes)
    {
        if (routes == null)
            routes = new ArrayList<>();
        this.routes = routes;
        routesFull = new ArrayList<>(routes); //copy
        notifyDataSetChanged();
    }

    public void addItem(Route newItem)
    {
        this.routes.add(newItem);
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route, parent, false);

        return new RouteViewHolder(view, routeClickedListener);
    }


    //Set text on UI, and color
    @Override
    public void onBindViewHolder(RouteViewHolder holder, int position)
    {
        Route currentData = routes.get(position);
        // holder.setIsRecyclable(true);
        int colorId = (position % 2 == 0) ? R.color.busline_lighter_blue : R.color.busline_darker_blue;
        holder.listView.setBackgroundColor(ContextCompat.getColor(context, colorId));

        Drawable drawable = DrawableUtil.getColoredBusDrawable(currentData, context);
        holder.busImage.setImageDrawable(drawable);

        holder.routeLabel.setText(currentData.getLabel());
        holder.routeName.setText(currentData.getName());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return routes.size();
    }

    // for filtering RecyclerView
    @Override
    public Filter getFilter()
    {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter()
    {
        @Override
        protected FilterResults performFiltering(CharSequence inputSequence)
        {
            List<Route> filteredList = new ArrayList<>();
            if (inputSequence == null || inputSequence.toString().trim().length() == 0)
                filteredList.addAll(routesFull);
            else
            {
                String filterPattern = inputSequence.toString().toLowerCase().trim();

                for (Route item : routesFull)
                {
                    // filter search of route names and route labels
                    String name = item.getName().toLowerCase().trim();
                    String label = item.getLabel().toLowerCase().trim();
                    if (LatinCyrillicUtil.isMatched(filterPattern, name) || LatinCyrillicUtil.isMatched(filterPattern, label))
                    {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            routes.clear();
            routes.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}