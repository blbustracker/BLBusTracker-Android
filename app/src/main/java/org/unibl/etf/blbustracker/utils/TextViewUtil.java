package org.unibl.etf.blbustracker.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.TableRow;
import android.widget.TextView;

import org.unibl.etf.blbustracker.R;


public abstract class TextViewUtil
{
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
