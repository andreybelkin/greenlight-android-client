package com.globalgrupp.greenlight.androidclient.model;

/**
 * Created by п on 05.02.2016.
 */

public class UserCredentials {

    private String login;

    private String password;

    private boolean newUser;

    public boolean isNewUser() {
        return newUser;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    public UserCredentials() {
    }

    public String getLogin() {

        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
