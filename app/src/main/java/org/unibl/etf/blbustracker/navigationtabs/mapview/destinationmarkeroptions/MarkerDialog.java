package org.unibl.etf.blbustracker.navigationtabs.mapview.destinationmarkeroptions;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.unibl.etf.blbustracker.R;

public class MarkerDialog extends DialogFragment
{
    public static final String DIALOG_BUSSTOP_ID = "busStop_id";
    public static final String DIALOG_BUSSTOP_NAME = "busstop_name";

    //buttons on dialog window
    private TextView closeBtn;
    private TextView busStopName;
    private Button setStartDestinationBtn;
    private Button setEndDestinationBtn;
    private Button startNavigationBtn;

    //interface that is implemented in MapFragment
    private MarkerDialogListener markerDialogListener;

    public MarkerDialog(MarkerDialogListener markerDialogListener)
    {
        this.markerDialogListener = markerDialogListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View dialogView = inflater.inflate(R.layout.busstop_marker_popup, container, false);
        //getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        closeBtn = dialogView.findViewById(R.id.x);
        busStopName = dialogView.findViewById(R.id.busstop_name);
        try
        {
            String busStopDesc = getArguments().getString(DIALOG_BUSSTOP_NAME);
            busStopName.setText(busStopDesc);
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        setStartDestinationBtn = dialogView.findViewById(R.id.set_start_destination_btn);
        setEndDestinationBtn = dialogView.findViewById(R.id.set_end_destination_btn);
        startNavigationBtn = dialogView.findViewById(R.id.start_navigation_btn);


        return dialogView;
    }

    // setting up buttons click listener
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        closeBtn.setOnClickListener(v -> dismiss());

        setStartDestinationBtn.setOnClickListener(v ->
        {
            int busStopId = getArguments().getInt(DIALOG_BUSSTOP_ID);
            markerDialogListener.setAsStartDestinationBtn(busStopId);
            dismiss();
        });

        setEndDestinationBtn.setOnClickListener(v ->
        {
            int busStopId = getArguments().getInt(DIALOG_BUSSTOP_ID);
            markerDialogListener.setAsEndDestinationBtn(busStopId);
            dismiss();
        });

        startNavigationBtn.setOnClickListener((v) ->
        {
            int busStopId = getArguments().getInt(DIALOG_BUSSTOP_ID);
            markerDialogListener.startNavigationBtn(busStopId);
            dismiss();
        });

    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    public void destroyInstance()
    {
        markerDialogListener = null;
    }
}