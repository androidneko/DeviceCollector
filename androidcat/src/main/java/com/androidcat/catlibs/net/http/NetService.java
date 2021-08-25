package com.androidcat.catlibs.net.http;

import com.androidcat.biz.consts.HttpAction;
import com.androidcat.biz.consts.PublicConsts;

public class NetService {
    public interface TOKEN_ERROR_CALLBACK{
        void error(String action, String error);
    }
    private String m_sToken = null;
    private NetManager m_mgNet = null;
    private TOKEN_ERROR_CALLBACK token_callback;

    public NetService(String url, String imei, String versionName,TOKEN_ERROR_CALLBACK callback) {
        token_callback=callback;
        m_mgNet = new NetManager(url, imei, versionName);
    }

    public void setToken(String token) {
        m_sToken = token;
    }


    public  boolean getServerTime(Object object, HttpCallback callback){
        return common(HttpAction.ACTION_GET_SERVER_TIME,object,callback);
    }

    public boolean getWhiteList(Object object, HttpCallback callback){
        return common(HttpAction.ACTION_GET_WHITELIST,object,callback);
    }

    public boolean getAdList(Object object, HttpCallback callback){
        return common(HttpAction.ACTION_GET_AD_LIST,object,callback);
    }

    public boolean registerJpush(Object object, HttpCallback callback){
        return common(HttpAction.ACTION_UPLOAD_JPUSH_INFO,object,callback);
    }

    public boolean queryDeviceId(Object object, HttpCallback callback){
        return common(HttpAction.ACTION_QUERY_DEVICEID,object,callback);
    }

    public boolean bindDeviceName(Object object, HttpCallback callback){
        return common(HttpAction.ACTION_BIND_DEVICENAME,object,callback);
    }

    public boolean setCanteenSite(Object object, HttpCallback callback){
        return common(HttpAction.ACTION_SET_CANTEEN_SITE,object,callback);
    }

    public boolean uploadRecords(Object object, HttpCallback callback){
        return common(HttpAction.ACTION_SWIPE_CARD,object,callback);
    }

    public boolean getShortcutInfo(Object object, HttpCallback callback){
        return common(HttpAction.ACTION_GET_SHORTCUT_INFO,object,callback);
    }

    public boolean sendFeedback(Object object, HttpCallback callback){
        return common(HttpAction.ACTION_FEEDBACK,object,callback);
    }

    public boolean uploadCrash(Object object, HttpCallback callback){
        return common(HttpAction.ACTION_UPLOAD_CRASH,object,callback);
    }

    public boolean checkUpdate(Object object, HttpCallback callback){
        return common(HttpAction.ACTION_APP_VERSION,object,callback);
    }

    private boolean common(final String action, Object object, final HttpCallback callback) {
        if (null == object || null == action) {
            return false;
        }

        if (null == m_mgNet) {
            return false;
        }
        String sParam = m_mgNet.setParam(m_sToken, object);
        m_mgNet.post(sParam, action, new HttpCallback() {

            @Override
            public void onSuccess(String entity) {
                if (callback != null) {
                    callback.onSuccess(entity);
                }
            }

            @Override
            public void onFail(String error, String code) {
                if(PublicConsts.TOKEN_ERROR.equals(code)){
                    if(token_callback!=null){
                        token_callback.error(action,error);
                        return;
                    }
                }
                if (callback != null) {
                    callback.onFail(error, code);
                }
            }
        });

        return true;
    }

}
