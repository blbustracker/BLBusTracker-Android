package org.unibl.etf.blbustracker.navigationtabs.routeschedule;

import android.content.Context;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.text.Spanned;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.route.Route;
import org.unibl.etf.blbustracker.utils.TableRowUtil;

import java.time.LocalTime;
import java.util.List;

public class RouteScheduleModel
{
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
        List<ScheduleTime> scheduleTimes = ParseScheduleUtil.splitAndMapSchedule(route.getWorkdaySchedule());
        return splitTextColorByTime(context.getString(R.string.workday), scheduleTimes, context);
    }

    public TextView getSaturdaySchedule(Context context) throws Exception
    {
        List<ScheduleTime> scheduleTimes = ParseScheduleUtil.splitAndMapSchedule(route.getSaturdaySchedule());
        return splitTextColorByTime(context.getString(R.string.saturday), scheduleTimes, context);
    }

    public TextView getSundaySchedule(Context context) throws Exception
    {
        List<ScheduleTime> scheduleTimes = ParseScheduleUtil.splitAndMapSchedule(route.getSundaySchedule());
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

    /**
     * @param inputText color this text
     * @param fontColor hex color (example #cc0029)
     * @return text for TextView
     */
    public static Spanned oneColor(String inputText, String fontColor)
    {
        String text = "<font color=" + fontColor + ">" + inputText + "</font>";
        return HtmlCompat.fromHtml(text,HtmlCompat.FROM_HTML_MODE_LEGACY);
    }
}
