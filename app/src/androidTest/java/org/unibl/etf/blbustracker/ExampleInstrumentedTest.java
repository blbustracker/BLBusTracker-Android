package org.unibl.etf.blbustracker;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.unibl.etf.blbustracker.datahandlers.database.DBFactory;
import org.unibl.etf.blbustracker.datahandlers.database.busstop.BusStop;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest
{

    @Test
    public void useAppContext()
    {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        DBFactory db = DBFactory.getInstance(appContext);
        
        new Thread(()->
        {
            List<BusStop> busStops = db.getJoinRouteBusStopDAO().getBusStopByRouteId(0);
            
            for(BusStop tmp : busStops)
            {
                System.out.println(tmp);
            }
            
        }).start();
//        new Thread(() ->
//        {
//            List<Route> routes = Arrays.asList(new Route("asd", 3, "dsa"),
//                            new Route("qwe", 3, "ewq"),
//                            new Route("zxc", 3, "dsa"));
//            db.getBuslineDAO().insertAll(routes);
//
//            List<StationRoute> crossRefs = Arrays.asList(new StationRoute(0,0),
//                    new StationRoute(0,1),
//                    new StationRoute(1,0),
//                    new StationRoute(1,1),
//                    new StationRoute(1,2)
//            );
//            db.getLineStationDAO().insertAll(crossRefs);
//
//
//        }).start();

//        assertEquals("com.passengerapp", appContext.getPackageName());
    }
}
