package org.unibl.etf.blbustracker.navigationtabs.routes;

import android.os.Parcelable;

//used when line is clicked in RouteBottomFragment, implemented in MapFragment
public interface OnRouteClickedListener extends Parcelable
{
    void onRouteInDialogClicked(int position);
}
