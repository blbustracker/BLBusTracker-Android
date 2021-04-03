package org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArrivalTime
{
    private final String routeName;
    private final String routeLabel;
    boolean isFromServer = true;
    private List<Integer> arrivaleTimes;
    private String scheduleTime;

    public ArrivalTime(String routeName, String routeLabel)
    {
        this.routeName = routeName;
        this.routeLabel = routeLabel;
        arrivaleTimes = new ArrayList<>();
    }

    public ArrivalTime(String routeName, String routeLabel, String routeScheduleTime)
    {
        this.routeName = routeName;
        this.routeLabel = routeLabel;
        isFromServer = false;
        scheduleTime = routeScheduleTime;
    }

    public ArrivalTime(String routeName, String routeLabel, int time)
    {
        this.routeName = routeName;
        this.routeLabel = routeLabel;
        arrivaleTimes = new ArrayList<>();
        arrivaleTimes.add(time);
    }
    public ArrivalTime(String routeName, String routeLabel, List<Integer> arrivaleTimes)
    {
        this.routeName = routeName;
        this.routeLabel = routeLabel;
        this.arrivaleTimes = arrivaleTimes;
    }

    public void addTime(int time)
    {
        if (arrivaleTimes == null)
            arrivaleTimes = new ArrayList<>();
        arrivaleTimes.add(time);
    }

    public String getScheduleTime()
    {
        return scheduleTime;
    }

    public int numberOfTimes()
    {
        return (arrivaleTimes == null) ? 0 : arrivaleTimes.size();
    }

    public String getRouteName()
    {
        return routeName;
    }

    public String getRouteLabel()
    {
        return routeLabel;
    }

    public List<Integer> getArrivaleTimes()
    {
        return arrivaleTimes;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrivalTime that = (ArrivalTime) o;
        return routeName.equals(that.routeName) &&
                routeLabel.equals(that.routeLabel);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(routeName, routeLabel);
    }
}
