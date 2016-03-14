package com.globalgrupp.greenlight.androidclient.model;

import android.location.Location;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import twitter4j.auth.AccessToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Andrey Belkin on 14.03.2016.
 */
public class ApplicationSettings {

    private List<String> oldGuidList;

    public List<String> getOldGuidList() {
        if (oldGuidList==null)
        {
            oldGuidList=new ArrayList<String>();
        }
        return oldGuidList;
    }

    public void setOldGuidList(List<String> oldGuidList1) {
        oldGuidList = oldGuidList1;
    }

    private static AuthorizationType authorizationType;

    public static AuthorizationType getAuthorizationType() {
        return authorizationType;
    }

    public static void setAuthorizationType(AuthorizationType authorizationType1) {
        authorizationType = authorizationType1;
    }

    private static AccessToken twitterAccessToken;

    public static AccessToken getTwitterAccessToken() {
        return twitterAccessToken;
    }

    public static void setTwitterAccessToken(AccessToken twitterAccessToken1) {
        twitterAccessToken = twitterAccessToken1;
    }

    private String oldEventsIdFilePath;

    public String getOldEventsIdFilePath() {
        return oldEventsIdFilePath;
    }

    public void setOldEventsIdFilePath(String oldEventsIdFilePath) {
        this.oldEventsIdFilePath = oldEventsIdFilePath;
    }


    private static List<Long> oldEventsId;

    public List<Long> getOldEventsId() {
        if (oldEventsId==null)
            oldEventsId=new ArrayList<Long>();
        return oldEventsId;
    }

    public static void setOldEventsId(List<Long> oldEventsId1) {
        oldEventsId = oldEventsId1;
    }

    private static SimpleGeoCoords previousCoord;

    public static SimpleGeoCoords getPreviousCoord() {
        return previousCoord;
    }

    public static void setPreviousCoord(SimpleGeoCoords previousCoord1) {
        previousCoord = previousCoord1;
    }

    public static void startLocationTimer(){
        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            getMGoogleApiClient());
                    SimpleGeoCoords currentCoord=new SimpleGeoCoords(mLastLocation.getLongitude(),mLastLocation.getLatitude(),mLastLocation.getAltitude());
                    if (getPreviousCoord().equals(currentCoord)) {
                        setPreviousCoord(currentCoord);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        },4000);
    }

    private static GoogleApiClient mGoogleApiClient;

    public static GoogleApiClient getMGoogleApiClient() {
        return mGoogleApiClient;
    }

    public static void setMGoogleApiClient(GoogleApiClient mGoogleApiClient1) {
        mGoogleApiClient = mGoogleApiClient1;
    }

    private static Long channelId;

    public static Long getChannelId() {
        return channelId;
    }

    public static void setChannelId(Long channelId1) {
        channelId = channelId1;
    }
}
