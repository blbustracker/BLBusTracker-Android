package org.unibl.etf.blbustracker.datahandlers.jsonhandlers.pointfactory;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/* For getting points and bus stops from route waypoints arguemnt */
public class PointFactory
{
    private final String IS_BUSSTOP = "stop";

    private final String LAT = "lat";
    private final String LNG = "lng";
    private final String BUSSTOP_ID = "ID";

    private List<LatLng> points;
    private List<Integer> busStopIds;

    public PointFactory()
    {
        this.points = new ArrayList<>();
        this.busStopIds = new ArrayList<>();
    }

    public void devidePointsAndBusStops(String routePoints)
    {
        points.clear(); // clear in case this method gets called twice
        busStopIds.clear();
        try
        {
            if (routePoints == null)
                return;

            JSONArray jsonArray = new JSONArray(routePoints);
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                try
                {
                    double lat = jsonObject.getDouble(LAT);
                    double lng = jsonObject.getDouble(LNG);
                    points.add(new LatLng(lat, lng));

                    if (jsonObject.has(IS_BUSSTOP))
                    {
                        int busStopId = jsonObject.getInt(BUSSTOP_ID);
                        busStopIds.add(busStopId);
                    }

                } catch (JSONException ex)
                {
                }
            }
        } catch (JSONException e)
        {
        }
    }

    public List<LatLng> getPoints()
    {
        return points;
    }

    public List<Integer> getBusStopIds()
    {
        return busStopIds;
    }
}
