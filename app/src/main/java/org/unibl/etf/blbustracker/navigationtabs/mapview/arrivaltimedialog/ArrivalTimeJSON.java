package org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ArrivalTimeJSON
{
    public static final String ARRIVING = "arrivingIn";
    public static final String LINE_NAME = "line";
    public static final String LINE_LBL = "lineLabel";

    public List<ArrivalTime> getArrivalTimes(JSONArray jsonArray)
    {
        if (jsonArray == null || jsonArray.length() == 0)
            return null;

        List<ArrivalTime> arrivalTimes = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++)
        {
            try
            {
                JSONObject arrivalTimeJSON = jsonArray.getJSONObject(i);
                String routeName = arrivalTimeJSON.getString(LINE_NAME);
                String lineLbl = arrivalTimeJSON.getString(LINE_LBL);
                int time = arrivalTimeJSON.getInt(ARRIVING);

                ArrivalTime arrivalTime = new ArrivalTime(routeName, lineLbl, time);

                int index = arrivalTimes.indexOf(arrivalTime);
                if (index < 0)
                    arrivalTimes.add(arrivalTime);
                else
                {
                    arrivalTimes.get(index).addTime(time);
                }

            } catch (JSONException ex)
            {
                ex.printStackTrace();
            }

        }
        return arrivalTimes;
    }
}
