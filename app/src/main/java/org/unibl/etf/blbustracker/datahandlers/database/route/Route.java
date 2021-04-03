package org.unibl.etf.blbustracker.datahandlers.database.route;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "route_table")
public class Route
{
    @PrimaryKey
    @ColumnInfo(name = "routeId")
    private int routeId;

    @ColumnInfo(name = "color")
    private String color;

    @ColumnInfo(name = "numOfWaypoints")
    private int numOfWaypoints;

    @ColumnInfo(name = "waypointsJSONArray")
    private String waypointsJSONArray; // column will be json string

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "label")
    private String label;

    @Embedded
    public RouteSchedule routeSchedule;

    public Route(int routeId, String color, int numOfWaypoints, String waypointsJSONArray, String name, String label)
    {
        this.routeId = routeId;
        this.color = color;
        this.numOfWaypoints = numOfWaypoints;
        this.waypointsJSONArray = waypointsJSONArray;
        this.name = name;
        this.label = label;
    }

    @Ignore // for testing
    public Route(int routeId, String color, int numOfWaypoints, String waypointsJSONArray, String name, String label, RouteSchedule routeSchedule)
    {
        this.routeId = routeId;
        this.color = color;
        this.numOfWaypoints = numOfWaypoints;
        this.waypointsJSONArray = waypointsJSONArray;
        this.name = name;
        this.label = label;
        this.routeSchedule = routeSchedule;
    }

    @Ignore
    //for testing
    public Route(int routeId, String name, String label)
    {
        this.routeId = routeId;
        this.name = name;
        this.label = label;
    }

    public static String getRouteDepatureName(String routeName)
    {
        String[] splitDestination = routeName.split("-");
        String destination = splitDestination[splitDestination.length - 1];
        return destination.trim();
    }

    public int getRouteId()
    {
        return routeId;
    }

    public void setRouteId(int routeId)
    {
        this.routeId = routeId;
    }

    public String getWorkdaySchedule()
    {
        return routeSchedule.getWorkday();
    }

    public String getSaturdaySchedule()
    {
        return routeSchedule.getSaturday();
    }

    public String getSundaySchedule()
    {
        return routeSchedule.getSunday();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof Route))
            return false;

        Route route = (Route) o;

        if (getRouteId() != route.getRouteId())
            return false;
        if (getNumOfWaypoints() != route.getNumOfWaypoints())
            return false;
        if (!getColor().equals(route.getColor()))
            return false;
        if (!getWaypointsJSONArray().equals(route.getWaypointsJSONArray()))
            return false;
        if (!getName().equals(route.getName()))
            return false;
        return getLabel().equals(route.getLabel());
    }

    @Override
    public int hashCode()
    {
        int result = getRouteId();
        result = 31 * result + getColor().hashCode();
        result = 31 * result + getNumOfWaypoints();
        result = 31 * result + getWaypointsJSONArray().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getLabel().hashCode();
        return result;
    }

    public String getColor()
    {
        return color;
    }

    public void setColor(String color)
    {
        this.color = color;
    }

    public int getNumOfWaypoints()
    {
        return numOfWaypoints;
    }

    public void setNumOfWaypoints(int numOfWaypoints)
    {
        this.numOfWaypoints = numOfWaypoints;
    }

    public String getWaypointsJSONArray()
    {
        return waypointsJSONArray;
    }

    public void setWaypointsJSONArray(String waypointsJSONArray)
    {
        this.waypointsJSONArray = waypointsJSONArray;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public RouteSchedule getRouteSchedule()
    {
        return routeSchedule;
    }

    public void setRouteSchedule(RouteSchedule routeSchedule)
    {
        this.routeSchedule = routeSchedule;
    }
}
