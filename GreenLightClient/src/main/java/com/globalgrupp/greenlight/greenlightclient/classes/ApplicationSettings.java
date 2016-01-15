package com.globalgrupp.greenlight.greenlightclient.classes;

import twitter4j.auth.AccessToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo on 14.01.2016.
 */
public class ApplicationSettings {
    private static ApplicationSettings instance =null;
    protected ApplicationSettings(){

    }
    public static ApplicationSettings getInstance(){
        if(instance == null) {
            instance = new ApplicationSettings();
        }
        return instance;
    }

    private AuthorizationType authorizationType;

    public AuthorizationType getAuthorizationType() {
        return authorizationType;
    }

    public void setAuthorizationType(AuthorizationType authorizationType) {
        this.authorizationType = authorizationType;
    }

    private AccessToken twitterAccessToken;

    public AccessToken getTwitterAccessToken() {
        return twitterAccessToken;
    }

    public void setTwitterAccessToken(AccessToken twitterAccessToken) {
        this.twitterAccessToken = twitterAccessToken;
    }

    private String oldEventsIdFilePath;

    public String getOldEventsIdFilePath() {
        return oldEventsIdFilePath;
    }

    public void setOldEventsIdFilePath(String oldEventsIdFilePath) {
        this.oldEventsIdFilePath = oldEventsIdFilePath;
    }


    private List<Long> oldEventsId;

    public List<Long> getOldEventsId() {
        if (oldEventsId==null)
            oldEventsId=new ArrayList<Long>();
        return oldEventsId;
    }

    public void setOldEventsId(List<Long> oldEventsId) {
        this.oldEventsId = oldEventsId;
    }
}
