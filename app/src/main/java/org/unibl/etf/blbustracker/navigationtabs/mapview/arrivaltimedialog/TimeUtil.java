package org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog;

import org.unibl.etf.blbustracker.datahandlers.database.route.Route;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtil
{
    // get current day in Week (example Calendar.SUNDAY)
    public static int currentDayInWeek()
    {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static int tomorrowDayInWeek()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_WEEK,1);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static String getTomorrowSchedule(Route route)
    {
        int dayInWeek = tomorrowDayInWeek();

        return getScheduleForGivenDay(route, dayInWeek);
    }

    public static String getTodaySchedule(Route route)
    {
        int dayInWeek = currentDayInWeek();

        return getScheduleForGivenDay(route, dayInWeek);
    }

    private static String getScheduleForGivenDay(Route route, int dayInWeek)
    {
        String schedule;
        switch (dayInWeek)
        {
            case Calendar.SUNDAY:
                schedule = route.getSundaySchedule();
            case Calendar.SATURDAY:
                schedule = route.getSaturdaySchedule();
            default:
                schedule = route.getWorkdaySchedule();
        }
        return schedule;
    }


    public static String getCurrentDateAndTime()
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    /**
     * Format date from Server to the format readable to users
     *
     * @param inputDate javascript date as a string
     * @return return date in targetDateFormat
     */
    public static String formatInputDate(String inputDate, String inputDateFormat, String targetDateFormat)
    {
        String dateStr = null;
        try
        {
            dateStr = inputDate;
            DateFormat srcDf = new SimpleDateFormat(inputDateFormat, Locale.ENGLISH);
            // parse the date string into Date object
            Date date = srcDf.parse(dateStr);

            DateFormat destDf = new SimpleDateFormat(targetDateFormat, Locale.ENGLISH);
            // format the date into another format
            dateStr = destDf.format(date);

            return dateStr;

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return inputDate;
    }
}
