package com.androidcat.biz.manager;

import android.content.Context;

import com.androidcat.api.CommApi;
import com.androidcat.biz.database.JlPosDatabase;
import com.androidcat.catlibs.log.LogUtil;
import com.androidcat.catlibs.net.http.HttpCallback;
import com.androidcat.biz.consts.PublicConsts;
import com.androidcat.biz.consts.SDKConsts;
import com.androidcat.catlibs.persistance.SpKey;
import com.androidcat.catlibs.net.http.request.GetWhiteListRequest;
import com.androidcat.catlibs.net.http.response.WhiteListResponse;
import com.androidcat.catlibs.persistance.SharePreferenceUtil;
import com.androidcat.catlibs.utils.Utils;
import com.google.gson.Gson;

/**
 * Created by androidcat on 2019/8/28.
 */

public class WhiteListManager extends BaseManager{

    public WhiteListManager(Context context, CommApi.CallBack callBack) {
        super(context, callBack);
    }

    public int getWhiteList(final String time){
        GetWhiteListRequest request = new GetWhiteListRequest();
        request.deviceId = PublicConsts.IMEI;
        request.lastTime = time;
        httpManager.getWhiteList(request, new HttpCallback() {

            @Override
            public void onSuccess(String entity) {
                WhiteListResponse response = new Gson().fromJson(entity, WhiteListResponse.class);
                if (Utils.isNull(time)){
                    //全量更新
                    long now = System.currentTimeMillis();
                    JlPosDatabase database = JlPosDatabase.getInstance(context);
                    if (database.deleteAllWhite()){
                        database.saveWhiteList(response.whiteListDTOList);
                    }else {
                        database.replaceWhiteList(response.whiteListDTOList);
                    }
                    LogUtil.e("getWhiteList","saveWhiteList costs:"+(System.currentTimeMillis()-now)+"ms");
                }else{
                    //增量更新
                    JlPosDatabase.getInstance(context).updateWhiteList(response.whiteListDTOList);
                    JlPosDatabase.getInstance(context).cleanUpWhiteList();
                }
                SharePreferenceUtil.setString(SpKey.START_TIME,response.lastUpdateTime);
                onSucceeded(SDKConsts.TYPE_WHITE_LIST, response);
            }

            @Override
            public void onFail(String error, String code) {
                onFailed(SDKConsts.TYPE_WHITE_LIST, error, code);
            }
        });
        return SDKConsts.SUCCESS;
    }
}
