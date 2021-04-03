package org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.unibl.etf.blbustracker.Constants;
import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;
import org.unibl.etf.blbustracker.utils.languageutil.Translator;

import java.util.function.Consumer;

public class ArrivalTimeFragment extends BottomSheetDialogFragment implements Consumer<Boolean>
{
    private BusStop busStop;
    private ArrivalTimeViewModel arrivalTimeViewModel;
    private ArrivalTimeModel arrivalTimeModel;
    //UI fields
    private TextView busStopName;
    private Button moreOptionsBtn;
    private MoreOptionsInterface moreOptionsInterface;
    private RecyclerView recyclerView;
    private TextView empty_recycle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View dialogView = inflater.inflate(R.layout.fragment_arrival_time, container, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        moreOptionsInterface = (MoreOptionsInterface) getArguments().getParcelable(Constants.MOREOPTIONS_PARCELABLE);
        busStop = getArguments().getParcelable(Constants.BUSSTOP_PARCELABLE);

        initButtons(dialogView);
        empty_recycle = dialogView.findViewById(R.id.empty_recyleview_text);
        recyclerView = dialogView.findViewById(R.id.arrival_recycler_view);

        String translatedName = new Translator(getContext()).translateInput(busStop.getDesc());
        busStopName = dialogView.findViewById(R.id.busstop_name);
        busStopName.setText(translatedName); // set bus stop name in window
        return dialogView;
    }


    private void initArrivalTimeViewModel()
    {
        arrivalTimeViewModel = new ViewModelProvider(this).get(ArrivalTimeViewModel.class);
        arrivalTimeViewModel.startListening(busStop, getContext());

        arrivalTimeModel = new ArrivalTimeModel(busStop, getContext());
        arrivalTimeViewModel.getArrivalTimesMLD().observe(getViewLifecycleOwner(), arrivalTimes ->
        {
            arrivalTimeModel.fillTableLayoutWithTimes(arrivalTimes, recyclerView, this);   // getting times from server
        });
    }

    @Override
    public void accept(Boolean isEmpty)
    {
        if (isEmpty)
            empty_recycle.setVisibility(View.VISIBLE);
        else
            empty_recycle.setVisibility(View.GONE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

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
        moreOptionsInterface = null;
        if (arrivalTimeViewModel != null)
            arrivalTimeViewModel.stopListening();
        if (arrivalTimeModel != null)
            arrivalTimeModel.shutdownPoolExecutorService();
        arrivalTimeModel = null;
        super.onDestroy();
    }

    @Keep
    public ArrivalTimeFragment()
    {
    }

}
