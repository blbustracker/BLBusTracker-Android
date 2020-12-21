package org.unibl.etf.blbustracker.datahandlers.jsonhandlers;

import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for creating BusStop object from json object which we get from NetworkManager.get(...) methode
 */
public class BusStopJSON extends JSONHandler
{
    private final String NUMBER = "number";
    private final String BUSSTOP_JSONARRAY = "stops";

    private final String BUSSTOP_ID = "ID";
    private final String LAT = "lat";
    private final String LNG = "lng";
    private final String DESC = "description";


    public BusStopJSON(JSONObject jsonObject)
    {
        super(jsonObject);
    }

    // number of bus stops from server
    public int getNumberOfBusStops() throws JSONException
    {
        return jsonObject.getInt(NUMBER);
    }

    public List<BusStop> getAllBusStops()
    {
        JSONArray busStopsJsonArray = super.getJsonArray(BUSSTOP_JSONARRAY);
        return getAllStaions(busStopsJsonArray);
    }

    //convert JSONArray to List<BusStop>
    public List<BusStop> getAllStaions(JSONArray jsonArray)
    {
        List<BusStop> busStops = null;

        if(jsonArray!=null)
        {
            busStops = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++)
            {
                try
                {
                    JSONObject busStopObj = jsonArray.getJSONObject(i);

                    int busStopID = busStopObj.getInt(BUSSTOP_ID);
                    double lat = busStopObj.getDouble(LAT);
                    double lng = busStopObj.getDouble(LNG);
                    String opis = busStopObj.getString(DESC);

                    busStops.add(new BusStop(busStopID, lat, lng, opis));
                } catch (JSONException ex)
                {
                    ex.printStackTrace();
                }
            }
        }

        return busStops;
    }

}
