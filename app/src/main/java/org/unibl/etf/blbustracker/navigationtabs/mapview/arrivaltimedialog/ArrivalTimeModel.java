package org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.unibl.etf.blbustracker.datahandlers.database.DBFactory;
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;
import org.unibl.etf.blbustracker.datahandlers.database.joinroutebusstop.JoinRouteBusStopDAO;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.datahandlers.jsonhandlers.BusStopJSON;
import org.unibl.etf.blbustracker.navigationtabs.routeschedule.ParseScheduleUtil;
import org.unibl.etf.blbustracker.navigationtabs.routeschedule.ScheduleTime;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ArrivalTimeModel
{
    public static final int N_THREADS = 2;
    public static final int N_DEPARTURE_TIMES = 1;

    public static final String IS_BUS_STOP = "stop";

    private final BusStop busStop;
    private final Context context;

    private JoinRouteBusStopDAO joinRouteBusStopDAO;

    private Handler mainHandler;
    private ExecutorService poolExecutorService;

    /**
     * @param busStop clicked bus stop
     */
    public ArrivalTimeModel(BusStop busStop, Context context)
    {
        this.busStop = busStop;
        this.context = context;

        activatePoolExecutorService();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void fillTableLayoutWithTimes(List<ArrivalTime> serverArrivalTimes, RecyclerView recyclerView, Consumer<Boolean> isEmptyData)
    {
        activatePoolExecutorService();
        poolExecutorService.execute(() ->
        {
            DBFactory dbFactory = DBFactory.getInstance(context);
            joinRouteBusStopDAO = dbFactory.getJoinRouteBusStopDAO();

            //routes containing this station
            List<Route> routeList = joinRouteBusStopDAO.getRoutesByBusStopId(busStop.getBusStopId());
            List<ArrivalTime> allArrivalTimes = new ArrayList<>();

            if (routeList != null)
            {
                for (Route route : routeList)
                {
                    int lastBusStopId = getLastBusStopId(route);
                    int currentBusStopId = busStop.getBusStopId();
                    if (lastBusStopId != currentBusStopId) // only show time if not on last bus stop
                    {
                        ArrivalTime tmp = new ArrivalTime(route.getName(), route.getLabel());   // temporary object
                        int index = (serverArrivalTimes == null || serverArrivalTimes.size() <= 0) ? -1 : serverArrivalTimes.indexOf(tmp);    // check if tmp exist and find index in list

                        if (index < 0)  //route does not have time, find in DB depending on day od the week
                        {
                            List<ScheduleTime> depatureTimes = null;
                            try
                            {
                                depatureTimes = getDepatureTimes(route);

                                if (depatureTimes != null && depatureTimes.size() > 0)
                                {
                                    String depatureText = depaturePrintFormat(depatureTimes);
                                    allArrivalTimes.add(new ArrivalTime(route.getName(), route.getLabel(), depatureText));
                                }

                            } catch (Exception ex)
                            {
                            }
                        } else  // route has time on server
                        {
                            ArrivalTime arrivalTime = serverArrivalTimes.get(index);
                            allArrivalTimes.add(arrivalTime);
                        }
                    }
                }
            }

            mainHandler.post(() ->
            {
                if (routeList !=null && !allArrivalTimes.isEmpty())
                {
                    isEmptyData.accept(false);
                    ArrivalAdapter arrivalAdapter = new ArrivalAdapter(context, allArrivalTimes);
                    recyclerView.setAdapter(arrivalAdapter);
                    arrivalAdapter.notifyDataSetChanged();
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                } else
                {
                    isEmptyData.accept(true);
                }
            });
        });
    }

    private int getLastBusStopId(Route route)
    {
        int lastBusStopId = -1;
        try
        {
            JSONArray jsonArray = new JSONArray(route.getWaypointsJSONArray());

            for (int i = jsonArray.length() - 1; i >= 0; i--)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has(IS_BUS_STOP) && jsonObject.has(BusStopJSON.BUSSTOP_ID))
                {
                    lastBusStopId = jsonObject.getInt(BusStopJSON.BUSSTOP_ID);
                    break;
                }
            }
        } catch (Exception ex)
        {
        }

        return lastBusStopId;
    }

    private List<ScheduleTime> getDepatureTimes(Route route) throws Exception
    {
        String todaysSchedule = TimeUtil.getTodaySchedule(route);

        List<ScheduleTime> depatureTimes = ParseScheduleUtil.getFirstDepatureTimes(todaysSchedule, N_DEPARTURE_TIMES, LocalTime.now()); // todays depature times

        if (depatureTimes == null) // last depature has past, try tomorrow
        {
            String tomorrowSchedule = TimeUtil.getTomorrowSchedule(route);
            LocalTime midNight = LocalTime.of(0, 0);
            depatureTimes = ParseScheduleUtil.getFirstDepatureTimes(tomorrowSchedule, N_DEPARTURE_TIMES, midNight);
        }

        return depatureTimes;
    }

    private String depaturePrintFormat(List<ScheduleTime> depatureTimes)
    {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < depatureTimes.size() && i < N_DEPARTURE_TIMES; i++)
            stringBuilder.append(" ").append(depatureTimes.get(i).getLocalTime());

        return stringBuilder.toString();
    }


    public void activatePoolExecutorService()
    {
        if (poolExecutorService == null || poolExecutorService.isShutdown())
            poolExecutorService = Executors.newFixedThreadPool(N_THREADS);
    }

    public void shutdownPoolExecutorService()
    {
        if (poolExecutorService != null && !poolExecutorService.isShutdown())
            poolExecutorService.shutdown();
    }

}
