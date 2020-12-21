package org.unibl.etf.blbustracker.datahandlers.database;

import android.content.Context;

import androidx.core.util.Pair;

import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStopDAO;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;
import org.unibl.etf.blbustracker.datahandlers.database.joinroutebusstop.JoinRouteBusStopDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used for finding direct and/or indirect route when start and end busStop are selected
 */
public class RouteBusStopConnection
{
    private DBFactory DBFactory;
    private BusStopDAO busStopDAO;
    private JoinRouteBusStopDAO middleDao;

    public RouteBusStopConnection(Context context)
    {
        DBFactory = DBFactory.getInstance(context);
        busStopDAO = DBFactory.getBusStopDAO();
        middleDao = DBFactory.getJoinRouteBusStopDAO();
    }

    /**
     * Find direct Route through BusStop A and B if exists
     *
     * @return all routes containing BusStop A and B, null if there is none
     */
    public List<Route> findDirectRoute(String stringA, String stringB)
    {
        if (stringA == null || stringB == null)
            return null;

        BusStop busStopA = busStopDAO.getBusStopByDesc(stringA);
        BusStop busStopB = busStopDAO.getBusStopByDesc(stringB);

        List<Route> routesWithBusStopA = null;
        List<Route> interceptRoutes = new ArrayList<>();

        if (busStopA != null)
            routesWithBusStopA = middleDao.getRoutesByBusStopId(busStopA.getBusStopId());

        if (routesWithBusStopA != null)
            for (Route routeA : routesWithBusStopA)
            {
                List<BusStop> busStopsOnRouteA = middleDao.getBusStopByRouteId(routeA.getRouteId());
                if (busStopsOnRouteA!=null && busStopsOnRouteA.contains(busStopB))
                {
                    interceptRoutes.add(routeA);
                }
            }

        return interceptRoutes.isEmpty() ? null : interceptRoutes;
    }

    /**
     * Finding indirect route. currently it's O(n^3)
     *
     * @return route pair needed for getting from busStopA to busStopB
     */
    //TODO: still not in use, need to test
    public List<Pair<Route, Route>> findRoutesConnectingBusStops(String stringA, String stringB)
    {
        if (stringA == null || stringB == null)
            return null;

        BusStop busStopA = busStopDAO.getBusStopByDesc(stringA);
        BusStop busStopB = busStopDAO.getBusStopByDesc(stringB);

        //TODO: if(routesWithBusStopA==null...)
        List<Route> routesWithBusStopA = middleDao.getRoutesByBusStopId(busStopA.getBusStopId());
        List<Route> routesWithBusStopB = middleDao.getRoutesByBusStopId(busStopB.getBusStopId());

        List<Pair<Route, Route>> connectingRoutes = new ArrayList<>();

        for (Route routeA : routesWithBusStopA)
        {
            List<BusStop> busStopsOnRouteA = middleDao.getBusStopByRouteId(routeA.getRouteId());

            for (Route routeB : routesWithBusStopB)
            {
                List<BusStop> busStopsOnRouteB = middleDao.getBusStopByRouteId(routeB.getRouteId());

                for (BusStop busStop : busStopsOnRouteA)
                    if (busStopsOnRouteB.contains(busStop))
                    {
                        connectingRoutes.add(new Pair<>(routeA, routeB));
                        break;
                    }
            }
        }

        return connectingRoutes.isEmpty() ? null : connectingRoutes;
    }
}
