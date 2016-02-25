package com.globalgrupp.greenlight.greenlightclient.classes;



import java.io.Serializable;
import java.util.List;


/**
 * Created by Lenovo on 25.02.2016.
 */

public class SocialNetworkUser implements Serializable{

    private Long id;

    private Long socialNetworkuserId;

    private Long socialNetworkId;

    private String userName;

    private List<Group> groups;



    public SocialNetworkUser() {
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSocialNetworkId() {
        return socialNetworkId;
    }

    public void setSocialNetworkId(Long socialNetworkId) {
        this.socialNetworkId = socialNetworkId;
    }

    public Long getSocialNetworkuserId() {
        return socialNetworkuserId;
    }

    public void setSocialNetworkuserId(Long socialNetworkuserId) {
        this.socialNetworkuserId = socialNetworkuserId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
