package org.unibl.etf.blbustracker.datahandlers.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import org.unibl.etf.blbustracker.datahandlers.database.announcement.Announcement;
import org.unibl.etf.blbustracker.datahandlers.database.announcement.AnnouncementDao;
import org.unibl.etf.blbustracker.datahandlers.database.joinroutebusstop.JoinRouteBusStop;
import org.unibl.etf.blbustracker.datahandlers.database.joinroutebusstop.JoinRouteBusStopDAO;
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStopDAO;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.datahandlers.database.route.RouteDao;

// Singleton class used for creating DAO instances of BusStopDao, RouteDao, JoinRouteBusStopDAO and AnnouncementDao class
@Database(entities = {BusStop.class, Route.class, JoinRouteBusStop.class, Announcement.class}, version = 33, exportSchema = false)
public abstract class DBFactory extends RoomDatabase
{
    public static final String DATABASE_NAME = "station_route_db";
    private static DBFactory INSTANCE;

    public abstract BusStopDAO getBusStopDAO();

    public abstract RouteDao getRouteDAO();

    public abstract JoinRouteBusStopDAO getJoinRouteBusStopDAO();

    public abstract AnnouncementDao getAnnouncementDao();


    // Singleton, so we have just one instance of this class
    public static synchronized DBFactory getInstance(Context context)
    {
        if (INSTANCE == null)
        {
            //Room.databaseBuilder for a persistent database. Once a database is built, you should keep a reference to it and re-use it.
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    DBFactory.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    // .allowMainThreadQueries() // we don't want to allow this
                    .build();
        }

        return INSTANCE;
    }

    public void deleteRouteBusStopDatabase(Context context)
    {
        context.deleteDatabase(DATABASE_NAME);
    }

    public static void destroyInstance()
    {
        INSTANCE = null;
    }

}
