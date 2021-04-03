package org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog;

import android.os.Parcelable;

import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;

public interface MoreOptionsInterface extends Parcelable
{
    void onMoreOptionsClicked(BusStop busStop);
}
