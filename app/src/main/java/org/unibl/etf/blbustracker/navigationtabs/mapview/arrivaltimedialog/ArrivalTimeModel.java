package org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TableLayout;
import android.widget.TableRow;

import org.json.JSONArray;
import org.json.JSONObject;
import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.DBFactory;
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;
import org.unibl.etf.blbustracker.datahandlers.database.joinroutebusstop.JoinRouteBusStopDAO;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.datahandlers.jsonhandlers.BusStopJSON;
import org.unibl.etf.blbustracker.navigationtabs.routeschedule.ParseScheduleUtil;
import org.unibl.etf.blbustracker.utils.TableRowUtil;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public void fillTableLayoutWithTimes(List<ArrivalTime> arrivalTimes, TableLayout tableLayout)
    {
        activatePoolExecutorService();
        poolExecutorService.execute(() ->
        {
            DBFactory dbFactory = DBFactory.getInstance(context);
            joinRouteBusStopDAO = dbFactory.getJoinRouteBusStopDAO();

            //routes containing this station
            List<Route> routeList = joinRouteBusStopDAO.getRoutesByBusStopId(busStop.getBusStopId());
            if (routeList == null)
                return;

            List<TableRow> tableRows = new ArrayList<>();
            TableLayout.LayoutParams layout = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
            for (Route route : routeList)
            {
                TableRow tableRow = null;

                int lastBusStopId = getLastBusStopId(route);
                int currentBusStopId = busStop.getBusStopId();
                if (lastBusStopId != currentBusStopId) // only show time if not on last bus stop
                {
                    ArrivalTime tmp = new ArrivalTime(route.getName(), route.getLabel());   // temporary object
                    int index = (arrivalTimes == null || arrivalTimes.size() <= 0) ? -1 : arrivalTimes.indexOf(tmp);    // check if tmp exist and find index in list

                    if (index < 0)  //route does not have time, find in DB depending on day od the week
                    {
                        List<LocalTime> depatureTimes = null;
                        try
                        {
                            depatureTimes = getDepatureTimes(route);

                            if (depatureTimes != null && depatureTimes.size() > 0)
                            {
                                String depatureText = depaturePrintFormat(route, depatureTimes);
                                tableRow = TableRowUtil.createRow(context, depatureText);
                            }

                        } catch (Exception ex)
                        {
                        }
                    } else  // route has time on server
                    {
                        ArrivalTime arrivalTime = arrivalTimes.get(index);
                        tableRow = TableRowUtil.createRow(context, arrivalTime);
                    }
                }
                tableRows.add(tableRow);
            }
            mainHandler.post(() ->
            {
                if (tableRows != null && tableRows.size() > 0)
                {
                    tableLayout.removeAllViews();
                    for (TableRow tableRow : tableRows)
                        tableLayout.addView(tableRow, layout); // place text with UI thread
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

    private List<LocalTime> getDepatureTimes(Route route) throws Exception
    {
        String todaysSchedule = TimeUtil.getTodaySchedule(route);

        List<LocalTime> depatureTimes = ParseScheduleUtil.getFirstDepatureTimes(todaysSchedule, N_DEPARTURE_TIMES, LocalTime.now()); // todays depature times

        if (depatureTimes == null) // last depature has past, try tomorrow
        {
            String tomorrowSchedule = TimeUtil.getTomorrowSchedule(route);
            LocalTime midNight = LocalTime.of(0, 0);
            depatureTimes = ParseScheduleUtil.getFirstDepatureTimes(tomorrowSchedule, N_DEPARTURE_TIMES, midNight);
        }

        return depatureTimes;
    }

    private String depaturePrintFormat(Route route, List<LocalTime> depatureTimes)
    {
        StringBuilder stringBuilder = new StringBuilder();
        String text = context.getString(R.string.route) + " " + route.getLabel() + ": " + context.getString(R.string.next_departure_in);
        stringBuilder.append(text);

        for (int i = 0; i < depatureTimes.size() && i < N_DEPARTURE_TIMES; i++)
            stringBuilder.append(" ").append(depatureTimes.get(i));

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
