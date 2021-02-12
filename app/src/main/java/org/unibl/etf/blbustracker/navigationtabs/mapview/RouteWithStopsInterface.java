package org.unibl.etf.blbustracker.navigationtabs.mapview;

import org.unibl.etf.blbustracker.datahandlers.database.route.Route;

import java.util.List;

public interface RouteWithStopsInterface
{
    void showRouteAnStops(Route routes, List<Integer> busStopIds);
}
