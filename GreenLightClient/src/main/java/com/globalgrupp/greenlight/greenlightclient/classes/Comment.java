package com.globalgrupp.greenlight.greenlightclient.classes;

import java.util.Date;
import java.util.List;


public class Comment {

    private Long id;

    private Event event;


    private String message;

    private Date createDate;

    private Long audioId;

    private Long videoId;

    public Long getAudioId() {
        return audioId;
    }

    public void setAudioId(Long audioId) {
        this.audioId = audioId;
    }

    public List<Long> getPhotoIds() {
        return photoIds;
    }

    public void setPhotoIds(List<Long> photoIds) {
        this.photoIds = photoIds;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    private List<Long> photoIds;

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Comment() {
    }

    public Event getEvent() {

        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
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


}
