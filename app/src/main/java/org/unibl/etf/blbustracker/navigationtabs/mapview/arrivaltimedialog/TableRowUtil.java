package org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog;

import android.content.Context;
import android.graphics.Color;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.unibl.etf.blbustracker.R;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TableRowUtil
{
    public static final int MAX_NUM_OF_TIMES = 2;
    public static final String TILDA = "~";

    public static TableRow createRow(Context context, @NonNull ArrivalTime arrivalTime)
    {
        String name = context.getString(R.string.route);
        name += " " + arrivalTime.getRouteLabel().trim() + ": ";

        return createRow(context, name, arrivalTime.getArrivaleTimes());
    }

    public static TableRow createRow(Context context, String lblName, List<Integer> times)
    {
        StringBuilder rowContent = new StringBuilder();
        rowContent.append(lblName);

        if (times != null)
        {
            List<Integer> sortedTimes = times.stream().distinct().sorted().limit(MAX_NUM_OF_TIMES).collect(Collectors.toList());

            for (Integer time : sortedTimes)
            {
                long timeInMinutes = TimeUnit.SECONDS.toMinutes(time);

                String timeEst = ((timeInMinutes == 0) ? "<1" : TILDA + timeInMinutes) + context.getString(R.string.minutes);
                rowContent.append(timeEst).append(", ");
            }

            rowContent.setLength(rowContent.length() - 2);
        }

        TableRow tableRow = new TableRow(context);
        tableRow.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));

        TextView lblTextView = getTextView(context);
        lblTextView.setText(rowContent.toString());

        tableRow.addView(lblTextView);
        return tableRow;
    }

    private static TextView getTextView(Context context)
    {
        TextView textView = new TextView(context);
        textView.setTextAppearance(R.style.TextAppearance_AppCompat_Medium);
        textView.setTextColor(Color.BLACK);
        textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        return textView;
    }
}
