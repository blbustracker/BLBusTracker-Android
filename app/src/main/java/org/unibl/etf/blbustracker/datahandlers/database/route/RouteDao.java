package org.unibl.etf.blbustracker.datahandlers.database.route;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RouteDao
{
    @Query("SELECT * FROM route_table")
    List<Route> getAllRoutes();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Route> routes);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Route route);

    @Query("SELECT routeId FROM route_table WHERE name=:name")
    int getRouteIdByName(String name);

    @Query("select * from route_table WHERE routeId = :id")
    Route loadRouteById(int id);

    @Query("SELECT * FROM route_table WHERE name = :name")
    Route getRouteByName(String name);

    @Query("DELETE FROM route_table")
    void deleteAllRoutes();
}
