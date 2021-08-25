package com.androidcat.catlibs.net.http.response;

import com.androidcat.catlibs.net.http.entities.AdField;

import java.util.List;

/**
 * Created by androidcat on 2019/9/5.
 */

public class AdListResponse extends BaseResponse{
    private List<AdField> adFieldList;

    public List<AdField> getAdFieldList() {
        return adFieldList;
    }

    public void setAdFieldList(List<AdField> adFieldList) {
        this.adFieldList = adFieldList;
    }
}
