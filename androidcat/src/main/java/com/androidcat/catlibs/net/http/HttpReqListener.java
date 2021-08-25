package com.androidcat.catlibs.net.http;

/**
 * Project: bletravel_remote
 * Author: androidcat
 * Email:androidcat@126.com
 * Created at: 2016-5-16 15:15:17
 * add function description here...
 */
public interface HttpReqListener{


    void onStart(String url);

    void onResponse(String entity);

     void onFailure(Exception e);
}
