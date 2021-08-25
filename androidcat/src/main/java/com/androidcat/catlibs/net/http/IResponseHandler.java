package com.androidcat.catlibs.net.http;

/**
 * Created by tsy on 16/8/15.
 */
public interface IResponseHandler {

    void onFailure(int statusCode, Exception e);

    void onProgress(long currentBytes, long totalBytes);
}
