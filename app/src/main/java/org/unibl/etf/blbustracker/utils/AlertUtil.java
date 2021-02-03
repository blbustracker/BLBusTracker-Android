package org.unibl.etf.blbustracker.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.unibl.etf.blbustracker.R;

public abstract class AlertUtil
{
    /**
     * show alarm/warning message with positive and negative button,
     *
     * @param title         title of the alarm/warning message
     * @param positiveClick what will happen if user clicks "yes"
     * @param negativeClick what will happen if user clicks "no", if null just dismiss dialog
     */
    public static void showAlertDialog(Context context, String title, @NonNull DialogInterface.OnClickListener positiveClick, DialogInterface.OnClickListener negativeClick)
    {
        AlertDialog.Builder alertBuilder = getBuilder(context, title);

        alertBuilder.setPositiveButton(
                context.getString(R.string.yes),
                positiveClick);

        if (negativeClick == null)
            alertBuilder.setNegativeButton(context.getString(R.string.no), (dialog, id) -> dialog.cancel());
        else
            alertBuilder.setNegativeButton(context.getString(R.string.no), negativeClick);

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        formatPositiveButton(alertDialog, context);
    }

    //show warning message
    public static void showWarningAlert(Context context, String title)
    {
        AlertDialog.Builder alertBuilder = getBuilder(context, title);

        alertBuilder.setPositiveButton(
                context.getString(R.string.ok_button),
                (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        formatPositiveButton(alertDialog, context);
    }

    /**
     * Must be called after alertDialog.show();
     * Make Positive button little pritier
     *
     * @param alertDialog
     * @param context
     */
    private static void formatPositiveButton(AlertDialog alertDialog, Context context)
    {
        final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setBackgroundColor(context.getColor(R.color.google_blue));
        positiveButton.setTextColor(context.getColor(R.color.white));

        LinearLayout parent = (LinearLayout) positiveButton.getParent();
        parent.setGravity(Gravity.CENTER_HORIZONTAL);
        View leftSpacer = parent.getChildAt(1);
        leftSpacer.setVisibility(View.GONE); //removes leftSpacer so OK button can be in center
    }

    private static AlertDialog.Builder getBuilder(Context context, String title)
    {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setMessage(title + "\n");
        alertBuilder.setCancelable(true);
        return alertBuilder;
    }
}
