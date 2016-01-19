package com.globalgrupp.greenlight.greenlightclient.classes;

import android.location.Location;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import twitter4j.auth.AccessToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

    private SimpleGeoCoords previousCoord;

    public SimpleGeoCoords getPreviousCoord() {
        return previousCoord;
    }

    public void setPreviousCoord(SimpleGeoCoords previousCoord) {
        this.previousCoord = previousCoord;
    }

    public void startLocationTimer(){
        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            ApplicationSettings.getInstance().getmGoogleApiClient());
                    SimpleGeoCoords currentCoord=new SimpleGeoCoords(mLastLocation.getLongitude(),mLastLocation.getLatitude(),mLastLocation.getAltitude());
                    if (ApplicationSettings.getInstance().getPreviousCoord().equals(currentCoord)){
                        ApplicationSettings.getInstance().setPreviousCoord(currentCoord);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        },4000);
    }

    private GoogleApiClient mGoogleApiClient;

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setmGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    private Long channelId;

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }
}
