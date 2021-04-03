package org.unibl.etf.blbustracker.navigationtabs.routeschedule;

import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleTime
{
    private static final String HOUR_REGEX = "([0-9]|0[0-9]|1[0-9]|2[0-3])";
    private static final String TIME_SPLIT_REGEX = "[:,.]";
    private static final String MINUTES_REGEX = "([0-5]?[0-9])";
    private static final String SUFFIX_REGEX = "([a-zA-Z-_]{0,3}),?";
    private static final Pattern pattern = Pattern.compile(HOUR_REGEX + TIME_SPLIT_REGEX + MINUTES_REGEX + SUFFIX_REGEX);

    private final LocalTime localTime;
    private String suffix = "";

    // split hourAndMin
    public ScheduleTime(String hourAndMin) throws Exception
    {
        Matcher matcher = pattern.matcher(hourAndMin);
        matcher.find();
        String hours = matcher.group(1);
        String minutes = matcher.group(2);

        localTime = LocalTime.of(Integer.parseInt(hours), Integer.parseInt(minutes));
        suffix = matcher.group(3);
    }

    public ScheduleTime(int hours, int minutes)
    {
        localTime = LocalTime.of(hours, minutes);
    }

    public LocalTime getLocalTime()
    {
        return localTime;
    }

    public String getTimeWithSufix()
    {
        return localTime.toString() + suffix;
    }
}
