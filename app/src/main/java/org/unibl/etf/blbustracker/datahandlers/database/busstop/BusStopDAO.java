package org.unibl.etf.blbustracker.datahandlers.database.busstop;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import static androidx.room.OnConflictStrategy.IGNORE;
@Dao
public interface BusStopDAO
{
    @Insert(onConflict = IGNORE)
    void insert(BusStop busStop);

    @Insert(onConflict = IGNORE)
    void insertAll(List<BusStop> busStops);

    @Query("SELECT * FROM bus_stop_table WHERE `desc` = :desc")
    BusStop getBusStopByDesc(String desc);

    @Query("SELECT busStopId FROM bus_stop_table WHERE `desc` = :desc")
    int getBusStopIDByDesc(String desc);

    @Query("select * from bus_stop_table where busStopId = :id")
    BusStop getBusStopByID(int id);

    @Query("select * from bus_stop_table " +
            "where lat = :lat and lng = :lng")
    BusStop getBusStopByLatLng(double lat, double lng);

    @Update
    void update(BusStop busStop);

    @Delete
    void delete(BusStop busStop);

    @Query("SELECT * FROM bus_stop_table")
    List<BusStop> getAllBusStops();

    @Query("DELETE FROM bus_stop_table")
    void deleteAllBusStops();

}

