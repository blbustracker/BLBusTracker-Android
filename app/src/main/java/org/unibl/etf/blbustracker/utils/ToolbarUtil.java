package org.unibl.etf.blbustracker.utils;

import android.app.Activity;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.unibl.etf.blbustracker.R;

public abstract class ToolbarUtil
{
    public static void initToolbar(Activity activity, View view, String toolbarTitle)
    {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(toolbarTitle);

        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setNavigationOnClickListener(v ->
        {
            ((AppCompatActivity) activity).getSupportActionBar().hide();
            activity.onBackPressed();
        });
    }

}
