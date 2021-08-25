package com.androidcat.catlibs.net.http.entities;

import java.util.List;

/**
 * Created by androidcat on 2019/9/5.
 */

public class AdField {
    public String id;
    public String code;
    public String name;
    public String picture;
    public String url;
    public List<AdItem> adItemList;

    public List<AdItem> getAdItemList() {
        return adItemList;
    }

    public void setAdItemList(List<AdItem> adItemList) {
        this.adItemList = adItemList;
    }
}
