package org.unibl.etf.blbustracker.datahandlers.jsonhandlers;

import org.unibl.etf.blbustracker.datahandlers.database.Bus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//for creating List<Bus> from server content
public class BusJSON
{
    private final String LINE = "line";
    private final String LOCATION = "location";
    private final String LAT = "lat";
    private final String LNG = "lng";

    public BusJSON()
    {
    }

    //convert JSONArray to List<Bus>
    public List<Bus> getAllBuses(JSONArray busJsonArray)
    {
        if (busJsonArray == null)
            return null;

        List<Bus> busList = new ArrayList<>();
        for (int i = 0; i < busJsonArray.length(); i++)
        {
            try
            {
                JSONObject busJsonObject = busJsonArray.getJSONObject(i);
                String line = busJsonObject.getString(LINE);
                JSONObject locationJSON = busJsonObject.getJSONObject(LOCATION);
                double lat = locationJSON.getDouble(LAT);
                double lng = locationJSON.getDouble(LNG);

                busList.add(new Bus(line, lat, lng));

            } catch (JSONException ex)
            {
                ex.printStackTrace();
            }
        }
        return busList;

    }

}
