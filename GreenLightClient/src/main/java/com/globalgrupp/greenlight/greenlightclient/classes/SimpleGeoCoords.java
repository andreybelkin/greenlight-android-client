package com.globalgrupp.greenlight.greenlightclient.classes;

import java.io.Serializable;

/**
 * Created by п on 24.12.2015.
 */
public class SimpleGeoCoords implements Serializable {
    private double longtitude;

    private double latitude;

    private double altitude;

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public SimpleGeoCoords(double longtitude, double latitude, double altitude) {
        this.longtitude = longtitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }
}
