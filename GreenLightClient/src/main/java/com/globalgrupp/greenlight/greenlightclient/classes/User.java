package com.globalgrupp.greenlight.greenlightclient.classes;


/**
 * Created with IntelliJ IDEA.
 * User: Andrey Belkin
 * Date: 02.12.2015
 * Time: 11:21
 */
public class User {
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User() {
    }

    private String pushAppId;

    public String getPushAppId() {
        return pushAppId;
    }

    public void setPushAppId(String pushAppId) {
        this.pushAppId = pushAppId;
    }
}
