package org.unibl.etf.blbustracker.navigationtabs.routeschedule;

import java.time.LocalTime;

public class ScheduleTime
{
    private static final String TIME_SPLIT_REGEX = "[:|,|\\.]";

    private LocalTime inputTime;

    // split hourAndMin
    public ScheduleTime(String hourAndMin) throws Exception
    {
        String[] splitedTime = hourAndMin.trim().split(TIME_SPLIT_REGEX);
        if(splitedTime==null || splitedTime.length!=2)
            throw new ParseException();

        inputTime = LocalTime.of(Integer.parseInt(splitedTime[0]),Integer.parseInt(splitedTime[1]));
    }

    public ScheduleTime(int hours, int minutes)
    {
        inputTime = LocalTime.of(hours, minutes);
    }

    public LocalTime getInputTime()
    {
        return inputTime;
    }

}
