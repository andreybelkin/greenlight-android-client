package com.globalgrupp.greenlight.greenlightclient.classes;

import java.io.Serializable;

/**
 * Created by п on 21.12.2015.
 */
public class CreateEventParams implements Serializable {

    private String URL;

    private double longitude;
    private double latitude;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double altitude) {
        this.longitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public CreateEventParams(String URL, double longitude, double latitude, String message) {
        this.URL = URL;
        this.longitude = longitude;
        this.latitude = latitude;
        this.message=message;
    }
}
