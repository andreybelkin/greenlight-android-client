package com.globalgrupp.greenlight.greenlightclient.classes;

/**
 * Created by Lenovo on 25.02.2016.
 */
public class UsersFilter {

    private String name;

    private boolean fbUser;

    private boolean vkUser;

    private boolean twUser;

    public UsersFilter() {
    }

    public boolean isFbUser() {
        return fbUser;
    }

    public void setFbUser(boolean fbUser) {
        this.fbUser = fbUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTwUser() {
        return twUser;
    }

    public void setTwUser(boolean twUser) {
        this.twUser = twUser;
    }

    public boolean isVkUser() {
        return vkUser;
    }

    public void setVkUser(boolean vkUser) {
        this.vkUser = vkUser;
    }
}
