package com.globalgrupp.greenlight.greenlightclient.classes;

import java.io.Serializable;

/**
 * Created by п on 21.12.2015.
 */
public class CreateEventParams implements Serializable {

    private String URL;

    private double longitude;
    private double latitude;

    private Long audioId;

    private Long photoId;

    private Long videoId;

    private String streetName;

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public Long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public Long getAudioId() {
        return audioId;
    }

    public void setAudioId(Long audioId) {
        this.audioId = audioId;
    }

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

    private String senderAppId;

    public String getSenderAppId() {
        return senderAppId;
    }

    public void setSenderAppId(String senderAppId) {
        this.senderAppId = senderAppId;
    }

    public CreateEventParams(String URL, double longitude, double latitude, String message) {
        this.URL = URL;
        this.longitude = longitude;
        this.latitude = latitude;
        this.message=message;
    }

    public CreateEventParams() {
    }
}
