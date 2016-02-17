package com.globalgrupp.greenlight.greenlightclient.classes;

import java.io.Serializable;
import java.util.*;


public class Event implements Serializable{

    private Long id;


    private String message;

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    private List<Comment> comments=new ArrayList<Comment>();

    private double longitude;

    private double latitude;

    private double altitude;
    private Long audioId;

    private Long photoId;
    private Long videoId;

    private String streetName;
    private Date createDate;

    private List<Long> photoIds;

    private Long socialType;

    private String userName;

    private List<String> photoPathList;

    private String videoPath;

    public String getSenderAppId() {
        return senderAppId;
    }

    public void setSenderAppId(String senderAppId) {
        this.senderAppId = senderAppId;
    }

    private String senderAppId;

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public List<String> getPhotoPathList() {
        return photoPathList;
    }

    public void setPhotoPathList(List<String> photoPathList) {
        this.photoPathList = photoPathList;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    private String audioPath;

    public String getUniqueGUID() {
        return uniqueGUID;
    }

    public void setUniqueGUID(String uniqueGUID) {
        this.uniqueGUID = uniqueGUID;
    }

    private String uniqueGUID;

    public Long getSocialType() {
        return socialType;
    }

    public void setSocialType(Long socialType) {
        this.socialType = socialType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<Long> getPhotoIds() {
        return photoIds;
    }

    public void setPhotoIds(List<Long> photoIds) {
        this.photoIds = photoIds;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

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

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }




    public Event() {
    }
}
