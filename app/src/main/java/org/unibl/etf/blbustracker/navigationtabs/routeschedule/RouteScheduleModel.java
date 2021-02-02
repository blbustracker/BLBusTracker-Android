package org.unibl.etf.blbustracker.navigationtabs.routeschedule;

import android.content.Context;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.utils.TableRowUtil;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class RouteScheduleModel
{
    private static final String SPACE_REGEX = "\\s+";
    private static final String NEWLINE = "\n";
    private static final String BLACK_COLOR = "#000000";
    private static final String GRAY_COLOR = "#969696";

    private Route route;
    private LocalTime currentTime;

    public RouteScheduleModel(Route route)
    {
        this.route = route;
        currentTime = LocalTime.now();
    }

    public TextView getWorkDaySchedule(Context context) throws Exception
    {
        List<ScheduleTime> scheduleTimes = splitAndMapSchedule(route.getWorkdaySchedule());
        return splitTextColorByTime(context.getString(R.string.workday), scheduleTimes, context);
    }

    public TextView getSaturdaySchedule(Context context) throws Exception
    {
        List<ScheduleTime> scheduleTimes = splitAndMapSchedule(route.getSaturdaySchedule());
        return splitTextColorByTime(context.getString(R.string.saturday), scheduleTimes, context);
    }

    public TextView getSundaySchedule(Context context) throws Exception
    {
        List<ScheduleTime> scheduleTimes = splitAndMapSchedule(route.getSundaySchedule());
        return splitTextColorByTime(context.getString(R.string.sunday), scheduleTimes, context);
    }

    private TextView splitTextColorByTime(String dayOdWeek, List<ScheduleTime> scheduleTimes, Context context)
    {
        TextView textView = TableRowUtil.getTextView(context);

        dayOdWeek+=":";
        textView.setText(dayOdWeek);
        textView.append(NEWLINE);
        for (ScheduleTime scheduleTime : scheduleTimes)
        {
            LocalTime inputTime = scheduleTime.getInputTime();

            if (currentTime.isAfter(inputTime))
                textView.append(oneColor(inputTime.toString(), GRAY_COLOR));
            else
                textView.append(oneColor(inputTime.toString(), BLACK_COLOR));

            textView.append(" ");
        }
        textView.append(NEWLINE);
        textView.append(NEWLINE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            textView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        return textView;
    }

    //get full schedule in TextView
    public TextView getAllSchedules(Context context)
    {
        TextView textView = TableRowUtil.getTextView(context);

        if (route.getRouteSchedule() == null)
        {
            textView.setText(context.getString(R.string.no_route_schedule));
            return textView;
        }

        String workday = context.getString(R.string.workday);
        String saturday = context.getString(R.string.saturday);
        String sunday = context.getString(R.string.sunday);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(workday).append(":\n").append(route.getWorkdaySchedule()).append("\n\n");
        stringBuilder.append(saturday).append(":\n").append(route.getSaturdaySchedule()).append("\n\n");
        stringBuilder.append(sunday).append(":\n").append(route.getSundaySchedule());

        textView.setText(stringBuilder.toString());
        return textView;
    }

    private List<ScheduleTime> splitAndMapSchedule(String schedule) throws Exception
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
                e.printStackTrace();
            }
            return scheduleTime;
        }).collect(Collectors.toList());

        if(isException.get())
            throw new Exception();

        return scheduleTimes;
    }

    /**
     * @param inputText color this text
     * @param fontColor hex color (example #cc0029)
     * @return text for TextView
     */
    public static Spanned oneColor(String inputText, String fontColor)
    {
        String text = "<font color=" + fontColor + ">" + inputText + "</font>";
        return Html.fromHtml(text);
    }
}
