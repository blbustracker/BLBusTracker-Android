package org.unibl.etf.blbustracker.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog.ArrivalTime;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class TableRowUtil
{
    private static final int MAX_NUM_OF_TIMES = 2;

    private static final String TILDA = "~";

    public static TableRow createRow(Context context, @NonNull ArrivalTime arrivalTime)
    {
        StringBuilder rowContent = new StringBuilder();
        String routeLbl = context.getString(R.string.route);
        routeLbl += " " + arrivalTime.getRouteLabel().trim() + ": ";
        rowContent.append(routeLbl);

        List<Integer> times = arrivalTime.getArrivaleTimes();
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

    public static TableRow createRow(Context context, @NonNull String rowContent)
    {
        TextView textView = getTextView(context);
        textView.setText(rowContent);

        TableRow tableRow = new TableRow(context);
        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        tableRow.addView(textView);

        return tableRow;
    }

    public static TextView getTextView(Context context)
    {
        TextView textView = new TextView(context);
        textView.setTextAppearance(R.style.TextAppearance_AppCompat_Medium);
        textView.setTextColor(Color.BLACK);
        textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);

        return textView;
    }
}
