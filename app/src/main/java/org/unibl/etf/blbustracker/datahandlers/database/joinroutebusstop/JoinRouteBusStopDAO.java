package org.unibl.etf.blbustracker.datahandlers.database.joinroutebusstop;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;

import java.util.List;

@Dao
public interface JoinRouteBusStopDAO
{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(JoinRouteBusStop joinRouteBusStop);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<JoinRouteBusStop> joinRouteBusStop);


    //Return all bus stops that contain route with id routeId
    @Query("SELECT * FROM bus_stop_table INNER JOIN join_route_busStop_table "
            +"ON bus_stop_table.busStopId=join_route_busStop_table.busStopId "
            +"WHERE join_route_busStop_table.routeId=:routeId")
    List<BusStop> getBusStopByRouteId(int routeId);

    //Return all routes that contain bus stop with id busStopId
    @Query("SELECT * FROM route_table INNER JOIN join_route_busStop_table "
            +"ON route_table.routeId=join_route_busStop_table.routeId "
            +"WHERE join_route_busStop_table.busStopId=:busStopId")
    List<Route> getRoutesByBusStopId(int busStopId);


    @Query("DELETE FROM join_route_busStop_table")
    void deleteJoinTable();

}
