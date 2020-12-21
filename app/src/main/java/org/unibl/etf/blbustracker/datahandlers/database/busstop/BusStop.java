package org.unibl.etf.blbustracker.datahandlers.database.busstop;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

@Entity(tableName = "bus_stop_table")
public class BusStop
{
    @PrimaryKey
    @ColumnInfo(name="busStopId")
    private int busStopId;

    @ColumnInfo(name="lat")
    private double lat;
    @ColumnInfo(name="lng")
    private double lng;
    @ColumnInfo(name="desc")
    private String desc;

    public BusStop(int busStopId, double lat, double lng, String desc)
    {
        this.busStopId = busStopId;
        this.lat = lat;
        this.lng = lng;
        this.desc = desc;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof BusStop))
            return false;

        BusStop busStop = (BusStop) o;

        if (getBusStopId() != busStop.getBusStopId())
            return false;
        if (Double.compare(busStop.getLat(), getLat()) != 0)
            return false;
        if (Double.compare(busStop.getLng(), getLng()) != 0)
            return false;
        return getDesc().equals(busStop.getDesc());
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = getBusStopId();
        temp = Double.doubleToLongBits(getLat());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getLng());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + getDesc().hashCode();
        return result;
    }

    public int getBusStopId()
    {
        return busStopId;
    }

    public void setBusStopId(int busStopId)
    {
        this.busStopId = busStopId;
    }

    public LatLng getLatLng()
    {
        return new LatLng(lat,lng);
    }

    public double getLat()
    {
        return lat;
    }

    public void setLat(double lat)
    {
        this.lat = lat;
    }

    public double getLng()
    {
        return lng;
    }

    public void setLng(double lng)
    {
        this.lng = lng;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    @Override
    public String toString()
    {
        return "BusStop{" +
                "id=" + busStopId +
                ", lat=" + lat +
                ", lng=" + lng +
                ", opis='" + desc + '\'' +
                '}';
    }

}
