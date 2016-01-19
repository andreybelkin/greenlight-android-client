package com.globalgrupp.greenlight.greenlightclient.classes;

/**
 * Created by Lenovo on 18.01.2016.
 */
public class Channel {
    private Long id;
    private String channelName;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Channel() {
    }

    @Override
    public String toString() {
        return channelName;
    }

    public Channel(Long id, String channelName) {
        this.id = id;
        this.channelName = channelName;
    }
}
