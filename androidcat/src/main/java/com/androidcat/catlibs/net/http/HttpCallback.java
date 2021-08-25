package com.androidcat.catlibs.net.http;

/**
 * Created by Administrator on 2018/4/25.
 */

public interface HttpCallback {
    void onSuccess(String entity);
    void onFail(String error, String code);
}
