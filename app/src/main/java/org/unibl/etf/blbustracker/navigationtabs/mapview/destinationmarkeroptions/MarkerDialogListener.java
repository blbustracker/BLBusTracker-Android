package org.unibl.etf.blbustracker.navigationtabs.mapview.destinationmarkeroptions;

import android.os.Parcelable;

public interface MarkerDialogListener extends Parcelable
{
    void setAsStartDestinationBtn(int stationId);
    void setAsEndDestinationBtn(int stationId);
    void startNavigationBtn(int stationId);
}
