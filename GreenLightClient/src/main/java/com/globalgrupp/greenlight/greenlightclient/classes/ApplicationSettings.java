package com.globalgrupp.greenlight.greenlightclient.classes;

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
}
