package com.globalgrupp.greenlight.greenlightclient.classes;

/**
 * Created by п on 28.12.2015.
 */
public class GetEventParams {

    private String URL;

    private SimpleGeoCoords currentCoords;

    private Long eventId;

    public SimpleGeoCoords getCurrentCoords() {
        return currentCoords;
    }

    public void setCurrentCoords(SimpleGeoCoords currentCoords) {
        this.currentCoords = currentCoords;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    private Long radius;

    public Long getRadius() {
        return radius;
    }

    public void setRadius(Long radius) {
        this.radius = radius;
    }

    private Long channelId;

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }
}
