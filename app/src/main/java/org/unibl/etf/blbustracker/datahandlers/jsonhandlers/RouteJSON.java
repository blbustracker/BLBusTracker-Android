package org.unibl.etf.blbustracker.datahandlers.jsonhandlers;

import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.datahandlers.database.route.RouteSchedule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for creating Route object from json object which we get from NetworkManager.get(...) methode
 */
public class RouteJSON extends JSONHandler
{
    private final String NUMBER = "number";
    private final String ROUTES_JSONARRAY = "routes";

    private final String ROUTE_ID = "ID";
    private final String COLOR = "color";
    private final String NUM_OF_WAYPOINTS = "count";
    private final String WAYPOINTS = "points";
    private final String ROUTE_NAME = "name";
    private final String LABEL = "label";

    private final String SCHEDULE = "schedule";
    private final String WORKDAY = "workday";
    private final String SATURDAY = "weekend";
    private final String SUNDAY = "holiday";


    public RouteJSON(JSONObject jsonObject)
    {
        super(jsonObject);
    }

    public int getNumberOfRoutes() throws JSONException
    {
        return jsonObject.getInt(NUMBER);
    }

    //convert JSONArray to List<Route>
    public List<Route> getAllRoutes()
    {
        List<Route> allRoutes = null;

        JSONArray routesJsonArray = super.getJsonArray(ROUTES_JSONARRAY);

        if (routesJsonArray != null)
        {
            allRoutes = new ArrayList<>();
            for (int i = 0; i < routesJsonArray.length(); i++)
            {
                try
                {
                    JSONObject routeObj = routesJsonArray.getJSONObject(i);

                    int routeID = routeObj.getInt(ROUTE_ID);
                    String name = routeObj.getString(ROUTE_NAME);
                    int numOfWaypoints = routeObj.getInt(NUM_OF_WAYPOINTS);
                    String waypointsJSONArray = routeObj.getString(WAYPOINTS);
                    String color = routeObj.getString(COLOR);
                    String label = routeObj.getString(LABEL);

                    if (routeObj.has(SCHEDULE))
                    {
                        JSONObject scheduleJSONObj = routeObj.getJSONObject(SCHEDULE);
                        String workDay = scheduleJSONObj.getString(WORKDAY);
                        String saturday = scheduleJSONObj.getString(SATURDAY);
                        String sunday = scheduleJSONObj.getString(SUNDAY);
                        RouteSchedule routeSchedule = new RouteSchedule(workDay, saturday, sunday);
                        allRoutes.add(new Route(routeID, color, numOfWaypoints, waypointsJSONArray, name, label, routeSchedule));
                    } else
                    {
                        allRoutes.add(new Route(routeID, color, numOfWaypoints, waypointsJSONArray, name, label));
                    }

                } catch (JSONException ex)
                {
                    ex.printStackTrace();
                }
            }
        }

        return allRoutes;
    }

}
