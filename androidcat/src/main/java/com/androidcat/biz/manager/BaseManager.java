package com.androidcat.biz.manager;

import android.content.Context;

import com.androidcat.api.CommApi;
import com.androidcat.catlibs.net.http.NetService;
import com.androidcat.catlibs.net.http.response.BaseResponse;

/**
 * Created by androidcat on 2019/8/28.
 */

public class BaseManager {
    protected NetService httpManager;
    protected Context context;
    protected CommApi.CallBack callBack;

    public BaseManager (Context context,CommApi.CallBack callBack){
        this.context = context;
        this.httpManager = HttpManager.getInstance(context);
        this.callBack = callBack;
    }

    protected void onSucceeded(int type,BaseResponse entity){
        if (callBack != null){
            callBack.onSuccess(type,entity);
        }
    }

    protected void onFailed(int type,String err, String code){
        if (callBack != null){
            callBack.onFail(type,err,code);
        }
    }
}
