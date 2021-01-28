package org.unibl.etf.blbustracker.navigationtabs.mapview.destinationmarkeroptions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import org.unibl.etf.blbustracker.R;

/**
 * Info Windows that shows up when marker is clicked
 * Window is basically an Image so you CAN'T put buttons in it
 */
public class MarkerCustomInfoWindow implements GoogleMap.InfoWindowAdapter
{
    private final View busStopMarkerWindowView;

    public MarkerCustomInfoWindow(Context context)
    {
        this.busStopMarkerWindowView = LayoutInflater.from(context).inflate(R.layout.custom_marker_infowindow, null);
    }

    private void collectMarkerInformation(Marker marker, View markerWindowView)
    {
        TextView textView = markerWindowView.findViewById(R.id.title);
        textView.setText(marker.getTitle());
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
