package com.androidcat.catlibs.net.http.entities;

/**
 * Created by androidcat on 2019/9/5.
 */

public class AdItem {
    public String id;
    public String adFieldId;
    public String adName;
    public String adPicture;
    public String adLink;
    public String orderNum;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdFieldId() {
        return adFieldId;
    }

    public void setAdFieldId(String adFieldId) {
        this.adFieldId = adFieldId;
    }

    public String getAdName() {
        return adName;
    }

    public void setAdName(String adName) {
        this.adName = adName;
    }

    public String getAdPicture() {
        return adPicture;
    }

    public void setAdPicture(String adPicture) {
        this.adPicture = adPicture;
    }

    public String getAdLink() {
        return adLink;
    }

    public void setAdLink(String adLink) {
        this.adLink = adLink;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }
}
