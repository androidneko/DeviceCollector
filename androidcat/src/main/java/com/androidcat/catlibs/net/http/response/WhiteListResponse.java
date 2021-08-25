package com.androidcat.catlibs.net.http.response;

import com.androidcat.biz.bean.WhiteList;

import java.util.List;

/**
 * Created by androidcat on 2019/8/28.
 */

public class WhiteListResponse extends BaseResponse{
    public List<WhiteList> whiteListDTOList;
    public String lastUpdateTime;
}
