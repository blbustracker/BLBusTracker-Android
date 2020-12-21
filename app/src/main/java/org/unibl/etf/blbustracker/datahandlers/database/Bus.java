package org.unibl.etf.blbustracker.datahandlers.database;

import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

//Used for placing bus on map
public class Bus
{
    private String line;
    private LatLng location;

    public Bus(String line, double lat, double lng)
    {
        this.line = line;
        location = new LatLng(lat, lng);
    }

    public String getLine()
    {
        return line;
    }

    public LatLng getLocation()
    {
        return location;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bus bus = (Bus) o;
        return line.equals(bus.line);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(line);
    }
}
