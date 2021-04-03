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

import org.unibl.etf.blbustracker.Constants;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View dialogView = inflater.inflate(R.layout.busstop_marker_popup, container, false);
        //getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        getAndPlaceArguemnts();

        closeBtn = dialogView.findViewById(R.id.x);
        busStopName = dialogView.findViewById(R.id.busstop_name);
        try
        {
            String busStopDesc = getArguments().getString(DIALOG_BUSSTOP_NAME);
            busStopName.setText(busStopDesc);
        } catch (Exception ex)
        {
        }

        setStartDestinationBtn = dialogView.findViewById(R.id.set_start_destination_btn);
        setEndDestinationBtn = dialogView.findViewById(R.id.set_end_destination_btn);
        startNavigationBtn = dialogView.findViewById(R.id.start_navigation_btn);


        return dialogView;
    }

    private void getAndPlaceArguemnts()
    {
        if (markerDialogListener == null)
            markerDialogListener = (MarkerDialogListener) getArguments().getParcelable(Constants.MARKERDIALOG_LISTENER_PARCELABLE);
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
            getAndPlaceArguemnts();
            markerDialogListener.setAsStartDestinationBtn(busStopId);
            dismiss();
        });

        setEndDestinationBtn.setOnClickListener(v ->
        {
            int busStopId = getArguments().getInt(DIALOG_BUSSTOP_ID);
            getAndPlaceArguemnts();
            markerDialogListener.setAsEndDestinationBtn(busStopId);
            dismiss();
        });

        startNavigationBtn.setOnClickListener((v) ->
        {
            int busStopId = getArguments().getInt(DIALOG_BUSSTOP_ID);
            getAndPlaceArguemnts();
            markerDialogListener.startNavigationBtn(busStopId);
            dismiss();
        });

    }

    public void destroyInstance()
    {
        markerDialogListener = null;
    }
}