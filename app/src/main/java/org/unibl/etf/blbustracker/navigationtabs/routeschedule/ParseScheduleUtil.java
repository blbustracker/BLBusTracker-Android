package org.unibl.etf.blbustracker.navigationtabs.routeschedule;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public abstract class ParseScheduleUtil
{
    private static final String SPACE_REGEX = "\\s+";

    public static List<ScheduleTime> splitAndMapSchedule(String schedule) throws Exception
    {
        List<String> times = Arrays.asList(schedule.trim().split(SPACE_REGEX));
        AtomicBoolean isException = new AtomicBoolean(false);
        List<ScheduleTime> scheduleTimes = times.stream().map(x ->
        {
            ScheduleTime scheduleTime = null;
            try
            {
                scheduleTime = new ScheduleTime(x);
            } catch (Exception e)
            {
                isException.set(true);
            }
            return scheduleTime;
        }).collect(Collectors.toList());

        if (isException.get())
            throw new Exception();

        return scheduleTimes;
    }

    public static List<ScheduleTime> getFirstDepatureTimes(String schedule, final int numberOfDepatureTimes, LocalTime timeToCompare) throws Exception
    {
        List<ScheduleTime> scheduleTimes = splitAndMapSchedule(schedule);

        if (scheduleTimes == null)
            return null;

        int numOftimes = 0;
        List<ScheduleTime> departureTimes = new ArrayList<>();

        for (int i = 0; i < scheduleTimes.size() && numOftimes < numberOfDepatureTimes; i++)
        {
            ScheduleTime scheduleTime = scheduleTimes.get(i);
            if (timeToCompare.isBefore(scheduleTime.getLocalTime()))
            {
                departureTimes.add(scheduleTime);
                numOftimes++;
            }
        }

        return departureTimes.isEmpty() ? null : departureTimes;
    }
}
