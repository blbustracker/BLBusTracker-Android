package org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;
import org.unibl.etf.blbustracker.utils.TableRowUtil;

public class ArrivalTimeFragment extends BottomSheetDialogFragment
{
    private BusStop busStop;
    private ArrivalTimeViewModel arrivalTimeViewModel;

    //UI fields
    private TextView busStopName;
    private Button moreOptionsBtn;
    private MoreOptionsInterface moreOptionsInterface;
    private TableLayout tableLayout;

    public ArrivalTimeFragment(BusStop busStop, MoreOptionsInterface moreOptionsInterface)
    {
        this.busStop = busStop;
        this.moreOptionsInterface = moreOptionsInterface;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View dialogView = inflater.inflate(R.layout.bus_stop_time_popup, container, false);
        //getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        initButtons(dialogView);

        busStopName = dialogView.findViewById(R.id.busstop_name);
        moreOptionsBtn = dialogView.findViewById(R.id.more_options);
        tableLayout = dialogView.findViewById(R.id.tablelayout);

        return dialogView;
    }

    private void initArrivalTimeViewModel()
    {
        arrivalTimeViewModel = new ViewModelProvider(this).get(ArrivalTimeViewModel.class);
        arrivalTimeViewModel.startListening(busStop, getContext());
        arrivalTimeViewModel.getArrivalTimesMLD().observe(getViewLifecycleOwner(), arrivalTimes ->
        {
            tableLayout.removeAllViews();
            TableLayout.LayoutParams layout = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

            if (arrivalTimes != null)
            {
                for (ArrivalTime arrivalTime : arrivalTimes)
                {
                    TableRow tableRow = TableRowUtil.createRow(getContext(), arrivalTime);
                    tableLayout.addView(tableRow, layout);
                }
            } else
            {
                TableRow tableRow = TableRowUtil.createRow(getContext(), getString(R.string.no_busstop_time));
                tableLayout.addView(tableRow, layout);
            }

        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        busStopName.setText(busStop.getDesc());

    }

    private void initButtons(View dialogView)
    {
        dialogView.findViewById(R.id.x).setOnClickListener(l -> this.dismiss());
        moreOptionsBtn = dialogView.findViewById(R.id.more_options);
        moreOptionsBtn.setOnClickListener(l ->
        {
            moreOptionsInterface.onMoreOptionsClicked(busStop);
            this.dismiss();
        });
    }

    @Override
    public void onResume()
    {
        initArrivalTimeViewModel();
        super.onResume();
    }

    @Override
    public void onDestroy()
    {
        busStopName = null;
        moreOptionsInterface = null;
        if (arrivalTimeViewModel != null)
            arrivalTimeViewModel.stopListening();
        super.onDestroy();
    }

}
