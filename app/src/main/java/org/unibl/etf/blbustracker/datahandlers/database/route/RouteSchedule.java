package org.unibl.etf.blbustracker.datahandlers.database.route;

//for saving route schedule (timetable)
public class RouteSchedule
{
    private String workday;
    private String saturday;
    private String sunday;

    public RouteSchedule(String workday, String saturday, String sunday)
    {
        this.workday = workday;
        this.saturday = saturday;
        this.sunday = sunday;
    }

    public String getWorkday()
    {
        return workday;
    }

    public void setWorkday(String workday)
    {
        this.workday = workday;
    }

    public String getSaturday()
    {
        return saturday;
    }

    public void setSaturday(String saturday)
    {
        this.saturday = saturday;
    }

    public String getSunday()
    {
        return sunday;
    }

    public void setSunday(String sunday)
    {
        this.sunday = sunday;
    }
}
