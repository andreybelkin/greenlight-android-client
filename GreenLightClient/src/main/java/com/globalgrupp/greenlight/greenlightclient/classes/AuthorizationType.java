package com.globalgrupp.greenlight.greenlightclient.classes;

/**
 * Created by Lenovo on 14.01.2016.
 */
public enum AuthorizationType {
    VK(1),
    FACEBOOK(2),
    TWITTER(3),
    GREENLIGHT(4),
    NONE(0);

    private final int value;
    private AuthorizationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
