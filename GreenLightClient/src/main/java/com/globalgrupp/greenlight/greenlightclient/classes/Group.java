package com.globalgrupp.greenlight.greenlightclient.classes;


import java.io.Serializable;
import java.util.List;

/**
 * Created by Lenovo on 25.02.2016.
 */

public class Group implements Serializable{

    private Long id;

    private String name;

    private Long groupType;

    private List<SocialNetworkUser> socialNetworkUserSet;


    public Group() {
    }

    public List<SocialNetworkUser> getSocialNetworkUserSet() {
        return socialNetworkUserSet;
    }

    public void setSocialNetworkUserSet(List<SocialNetworkUser> socialNetworkUserSet) {
        this.socialNetworkUserSet = socialNetworkUserSet;
    }

    public Long getGroupType() {
        return groupType;
    }

    public void setGroupType(Long groupType) {
        this.groupType = groupType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
