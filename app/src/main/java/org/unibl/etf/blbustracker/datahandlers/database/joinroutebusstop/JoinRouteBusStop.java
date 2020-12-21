package org.unibl.etf.blbustracker.datahandlers.database.joinroutebusstop;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "join_route_busStop_table",
        primaryKeys = {"busStopId","routeId"})
public class JoinRouteBusStop
{
    @ColumnInfo(name="busStopId")
    private int busStopId;

    @ColumnInfo(name="routeId")
    private int routeId;

    public JoinRouteBusStop(int busStopId, int routeId)
    {
        this.busStopId = busStopId;
        this.routeId = routeId;
    }

    public int getBusStopId()
    {
        return busStopId;
    }

    public void setBusStopId(int busStopId)
    {
        this.busStopId = busStopId;
    }

    public int getRouteId()
    {
        return routeId;
    }

    public void setRouteId(int routeId)
    {
        this.routeId = routeId;
    }
}
