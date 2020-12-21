package org.unibl.etf.blbustracker.phoneoptions;

public abstract class PermissionCode
{
    // nije bitna vrijednost, bitno je da su jedinstveni, koriste se u klasi PermissionHelper
    public static final int STORAGE_PERMISSIONS_CODE = 9000;
    public static final int LOCATION_PERMISSIONS_CODE = 9001;
    public static final int GPS_PERMISSIONS_CODE = 9002;

    public static final int ERROR_DIALOG_REQUEST = 9003;
}
