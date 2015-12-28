package com.globalgrupp.greenlight.greenlightclient.classes;

/**
 * Created by п on 28.12.2015.
 */
public class GetEventParams {

    private String URL;

    private SimpleGeoCoords currentCoords;

    public SimpleGeoCoords getCurrentCoords() {
        return currentCoords;
    }

    public void setCurrentCoords(SimpleGeoCoords currentCoords) {
        this.currentCoords = currentCoords;
    }

    public String getURL() {
        return URL;
    }

    public void setUrl(String url) {
        URL = url;
    }
}
