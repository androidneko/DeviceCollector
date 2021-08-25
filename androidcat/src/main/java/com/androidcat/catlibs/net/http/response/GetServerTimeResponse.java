package com.androidcat.catlibs.net.http.response;

public class GetServerTimeResponse extends BaseResponse{
    private String serverTime;

    public String getServerTime() {
        return serverTime;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }
}
