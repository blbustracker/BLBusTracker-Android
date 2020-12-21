package org.unibl.etf.blbustracker.navigationtabs.mapview.dialogs.markeroptions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.Bus;

/**
 * Info Windows that shows up when marker is clicked
 * Window is basically an Image so you CAN'T put buttons
 */
public class MarkerCustomInfoWindow implements GoogleMap.InfoWindowAdapter
{
    private View busStopMarkerWindowView;
    private Context context;

    public MarkerCustomInfoWindow(Context context)
    {
        this.context = context;
        this.busStopMarkerWindowView = LayoutInflater.from(context).inflate(R.layout.custom_marker_infowindow, null);
    }

    private void collectMarkerInformation(Marker marker, View markerWindowView)
    {
        TextView textView = markerWindowView.findViewById(R.id.title);
        textView.setText(marker.getTitle());
        Object obj = marker.getTag();
        if(obj instanceof Bus)
        {
            busStopMarkerWindowView.findViewById(R.id.more_marker_options).setVisibility(View.GONE);

        }else
        {
            busStopMarkerWindowView.findViewById(R.id.more_marker_options).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View getInfoWindow(Marker marker)
    {
        collectMarkerInformation(marker, busStopMarkerWindowView);
        return busStopMarkerWindowView;
    }

    @Override
    public View getInfoContents(Marker marker)
    {
        collectMarkerInformation(marker, busStopMarkerWindowView);
        return busStopMarkerWindowView;
    }
}
