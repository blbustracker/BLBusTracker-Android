package org.unibl.etf.blbustracker;

public abstract class Constants
{
    public static final double STARTING_LAT = 44.7691468567798;  // Banja Luka center lat
    public static final double STARTING_LNG = 17.18835644423962; // Banja Luka center lng

    public static final float POLYLINE_WIDTH_23 = 5.5f;         //googles default is 10.0f
    public static final float POLYLINE_WIDTH_24 = 8.5f;

    public static final int CAMERA_PADDING = 20;

    public static final int POLYLINE_ALPHA_COLOR = 230;         // route alpha (transparency) value

    public static final float DEFAULT_ZOOM = 16.0f;             // default zoom value

    public static final double ONE_KM_IN_DEGREE = 0.0091;       //approximation for our region

    public static final int LAYOUT_ANIMATION_DURATION = 500;    // duration of expand/collapse search layout

    public static final int RETRY_CONNECTION_TIME = 3000;       //duration of checking internet connection

    public static final int BUS_REFRESH_INTERVAL = 1000;        //update bus position every 1s

    public static final String SERVER_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"; //date format on server
    public static final String MY_DATE_FORMAT = "dd-MM-yyyy";                //date format for showing
    //public static final String MY_DATE_FORMAT = "dd-MM-yyyy  HH:mm";       //date format for showing with time

    public static final String BASE_URL = "https://blbustracker.etf.unibl.org/api";

    public static final String LAST_UPDATE = "/lastUpdate";
    //////////////////////////////////////////////////////////////////////////////////////////
    public static final String BUSSTOPS_PATH = "/stops";
    public static final String BUSSTOPS_FULL_URL = BASE_URL + BUSSTOPS_PATH;

    public static final String BUSSTOP_LAST_UPDATE_PATH = BUSSTOPS_PATH + LAST_UPDATE;
    public static final String BUSSTOP_LAST_UPDATE_FULL_URL = BASE_URL + BUSSTOP_LAST_UPDATE_PATH;
    //////////////////////////////////////////////////////////////////////////////////////////
    public static final String ROUTES_PATH = "/routes";
    public static final String ROUTES_FULL_URL = BASE_URL + ROUTES_PATH;

    public static final String ROUTES_LAST_UPDATE_PATH = ROUTES_PATH + LAST_UPDATE;
    public static final String ROUTES_LAST_UPDATE_FULL_URL = BASE_URL + ROUTES_LAST_UPDATE_PATH;
    //////////////////////////////////////////////////////////////////////////////////////////
    public static final String NEWS_PATH = "/news";
    public static final String NEWS_FULL_URL = BASE_URL + NEWS_PATH;

    public static final String NEWS_LAST_UPDATE_PATH = NEWS_PATH + LAST_UPDATE;
    public static final String NEWS_LAST_UPDATE_FULL_URL = BASE_URL + NEWS_LAST_UPDATE_PATH;
    //////////////////////////////////////////////////////////////////////////////////////////
    public static final String REPORT_PATH = "/report";
    public static final String REPORT_FULL_URL = BASE_URL + REPORT_PATH;
    //////////////////////////////////////////////////////////////////////////////////////////
    public static final String BUS_LOCATIONS_PATH = "/location";

    public static final String ARRIVAL_TIME = "/time";
}
