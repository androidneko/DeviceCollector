package com.androidcat.biz.manager;

import android.content.Context;
import android.os.Build;

import com.androidcat.api.CommApi;
import com.androidcat.catlibs.log.LogFileTool;
import com.androidcat.catlibs.net.http.FileUpload;
import com.androidcat.catlibs.net.http.HttpCallback;
import com.androidcat.biz.consts.PublicConsts;
import com.androidcat.biz.consts.SDKConsts;
import com.androidcat.catlibs.net.http.entities.Shortcut;
import com.androidcat.catlibs.net.http.request.BaseRequest;
import com.androidcat.catlibs.net.http.request.BindDeviceRequest;
import com.androidcat.catlibs.net.http.request.BindJpushRequest;
import com.androidcat.catlibs.net.http.request.CheckUpdateRequest;
import com.androidcat.catlibs.net.http.request.CrashRequest;
import com.androidcat.catlibs.net.http.request.FeedbackRequest;
import com.androidcat.catlibs.net.http.request.GetShortcutInfoRequest;
import com.androidcat.catlibs.net.http.request.QueryDeviceIdRequest;
import com.androidcat.catlibs.net.http.request.SetCanteenSiteRequest;
import com.androidcat.catlibs.net.http.response.AdListResponse;
import com.androidcat.catlibs.net.http.response.BaseResponse;
import com.androidcat.catlibs.net.http.response.BindDeviceResponse;
import com.androidcat.catlibs.net.http.response.CheckUpdateResponse;
import com.androidcat.catlibs.net.http.response.GetServerTimeResponse;
import com.androidcat.catlibs.net.http.response.QueryDeviceIdResponse;
import com.androidcat.catlibs.net.http.response.ShortcutInfoResponse;
import com.androidcat.catlibs.persistance.SharePreferenceUtil;
import com.androidcat.catlibs.utils.TimeUtil;
import com.androidcat.catlibs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Time;
import java.util.List;

/**
 * Created by androidcat on 2019/8/30.
 */

public class CommManager extends BaseManager {
    public CommManager(Context context, CommApi.CallBack callBack) {
        super(context, callBack);
    }

    public int checkUpdate(String userName,String baseVersion){
        CheckUpdateRequest request = new CheckUpdateRequest();
        request.userName = userName;
        request.baseVersion = baseVersion;
        httpManager.checkUpdate(request, new HttpCallback() {

            @Override
            public void onSuccess(String entity) {
                CheckUpdateResponse response = new Gson().fromJson(entity,CheckUpdateResponse.class);
                onSucceeded(SDKConsts.TYPE_CHECK_UPDATE, response);
            }

            @Override
            public void onFail(String error, String code) {
                onFailed(SDKConsts.TYPE_CHECK_UPDATE, error, code);
            }
        });
        return SDKConsts.SUCCESS;
    }

    public int getServerTime(){

        BaseRequest request = new BaseRequest();
        request.deviceId = PublicConsts.IMEI;
        httpManager.getServerTime(request, new HttpCallback() {

            @Override
            public void onSuccess(String entity) {
                GetServerTimeResponse response=new Gson().fromJson(entity, GetServerTimeResponse.class);
                onSucceeded(SDKConsts.TYPE_GET_SERVER_TIME, response);
            }

            @Override
            public void onFail(String error, String code) {
                onFailed(SDKConsts.TYPE_GET_SERVER_TIME, error, code);
            }
        });
        return SDKConsts.SUCCESS;
    }

    public int getAdList(){
        BaseRequest request = new BaseRequest();
        request.deviceId = PublicConsts.IMEI;
        httpManager.getAdList(request, new HttpCallback() {

            @Override
            public void onSuccess(String entity) {
                AdListResponse response = new Gson().fromJson(entity,AdListResponse.class);
                onSucceeded(SDKConsts.TYPE_GET_ADLIST, response);
            }

            @Override
            public void onFail(String error, String code) {
                onFailed(SDKConsts.TYPE_GET_ADLIST, error, code);
            }
        });
        return SDKConsts.SUCCESS;
    }

    public int bindJpush(String jpushId){

        BindJpushRequest request = new BindJpushRequest();
        request.userId = PublicConsts.IMEI;
        request.registrationId = jpushId;
        request.type = "3";
        request.tags = "android";
        request.alias = PublicConsts.IMEI;
        httpManager.registerJpush(request, new HttpCallback() {

            @Override
            public void onSuccess(String entity) {
                onSucceeded(SDKConsts.TYPE_BIND_JPUSH, new BaseResponse());
            }

            @Override
            public void onFail(String error, String code) {
                onFailed(SDKConsts.TYPE_BIND_JPUSH, error, code);
            }
        });
        return SDKConsts.SUCCESS;
    }

    public int bindDeviceName(final String deviceName){
        BindDeviceRequest request = new BindDeviceRequest();
        request.deviceId = PublicConsts.IMEI;
        request.deviceName=deviceName;
        httpManager.bindDeviceName(request, new HttpCallback() {

            @Override
            public void onSuccess(String entity) {
                BindDeviceResponse response = new Gson().fromJson(entity,BindDeviceResponse.class);
                SharePreferenceUtil.setString(PublicConsts.DEVICE_NAME,deviceName);
                SharePreferenceUtil.setString(PublicConsts.POS_KEY,response.posKey);
                onSucceeded(SDKConsts.TYPE_BIND_DEVICE, response);
            }

            @Override
            public void onFail(String error, String code) {
                onFailed(SDKConsts.TYPE_BIND_DEVICE, error, code);
            }
        });
        return SDKConsts.SUCCESS;
    }

    public int queryDeviceId(){
        final QueryDeviceIdRequest request = new QueryDeviceIdRequest();
        request.deviceId = PublicConsts.IMEI;
        httpManager.queryDeviceId(request, new HttpCallback() {

            @Override
            public void onSuccess(String entity) {
                QueryDeviceIdResponse response = new Gson().fromJson(entity,QueryDeviceIdResponse.class);
                SharePreferenceUtil.setString(PublicConsts.DEVICE_NAME,response.deviceName);
                onSucceeded(SDKConsts.TYPE_QUERY_DEVICEID, response);
            }

            @Override
            public void onFail(String error, String code) {
                onFailed(SDKConsts.TYPE_QUERY_DEVICEID, error, code);
            }
        });
        return SDKConsts.SUCCESS;
    }

    public int setCanteenSite(final String site){
        SetCanteenSiteRequest request = new SetCanteenSiteRequest();
        request.deviceId = PublicConsts.IMEI;
        request.canteenSite=site;
        httpManager.setCanteenSite(request, new HttpCallback() {

            @Override
            public void onSuccess(String entity) {
                SharePreferenceUtil.setString(PublicConsts.ROOM_NAME,site);
                onSucceeded(SDKConsts.TYPE_SET_CANTEEN_SITE, new BaseResponse());
            }

            @Override
            public void onFail(String error, String code) {
                onFailed(SDKConsts.TYPE_SET_CANTEEN_SITE, error, code);
            }
        });
        return SDKConsts.SUCCESS;
    }

    public int getShortcutInfo(){
        final GetShortcutInfoRequest request = new GetShortcutInfoRequest();
        request.deviceId = PublicConsts.IMEI;
        String site=SharePreferenceUtil.getString(PublicConsts.ROOM_NAME);
        request.setCanteenSite(site);
        String mealType="";
        if(TimeUtil.isMorning()){
            mealType="0";
        }else if(TimeUtil.isNoon()){
            mealType="1";
        }else if(TimeUtil.isDinner()){
            mealType="2";
        }else{
            mealType="3";
        }
        request.setMealType(mealType);
        httpManager.getShortcutInfo(request, new HttpCallback() {

            @Override
            public void onSuccess(String entity) {
                Type listType = new TypeToken<List<Shortcut>>(){}.getType();
                List<Shortcut> list = new Gson().fromJson(entity,listType);
                ShortcutInfoResponse response = new ShortcutInfoResponse();
                response.shortcutList = list;
                onSucceeded(SDKConsts.TYPE_GET_SHORTCUT_INFO, response);
            }

            @Override
            public void onFail(String error, String code) {
                onFailed(SDKConsts.TYPE_GET_SHORTCUT_INFO, error, code);
            }
        });
        return SDKConsts.SUCCESS;
    }

    public int uploadLog(){
        //立即保存缓存到日志文件
        LogFileTool.getInstance().flushCache();
        LogFileTool.getInstance().zipFile();
        FileUpload upload=new FileUpload(PublicConsts.SERVER_URL+"/api/file/stream",PublicConsts.IMEI, Utils.getVersionName(context),"");
        upload.upload(LogFileTool.getInstance().getZipLogFile().getAbsolutePath(), new HttpCallback() {
            @Override
            public void onSuccess(String entity) {
                sendFeedback(entity,"日志文件");
            }

            @Override
            public void onFail(String error, String code) {
                onFailed(SDKConsts.TYPE_SEND_FEEDBACK, error, code);
            }
        });
        return SDKConsts.SUCCESS;
    }

    public int uploadDatabase(){
        String databasePath = "/data/data/demo.example.bankpos/databases/JlPosDatabase.db";
        FileUpload upload=new FileUpload(PublicConsts.SERVER_URL+"/api/file/stream",PublicConsts.IMEI, Utils.getVersionName(context),"");
        upload.upload(databasePath, new HttpCallback() {
            @Override
            public void onSuccess(String entity) {
                sendFeedback(entity,"数据库文件");
            }

            @Override
            public void onFail(String error, String code) {
                onFailed(SDKConsts.TYPE_SEND_FEEDBACK, error, code);
            }
        });
        return SDKConsts.SUCCESS;
    }

    public int uploadCrash(String crash){
        final CrashRequest request = new CrashRequest();
        request.deviceId = PublicConsts.IMEI;
        request.crash = crash;
        request.phoneModel = Build.MODEL;
        httpManager.uploadCrash(request, new HttpCallback() {

            @Override
            public void onSuccess(String entity) {
                onSucceeded(SDKConsts.TYPE_UPLOAD_CRASH, new BaseResponse());
            }

            @Override
            public void onFail(String error, String code) {
                onFailed(SDKConsts.TYPE_UPLOAD_CRASH, error, code);
            }
        });
        return SDKConsts.SUCCESS;
    }

    public int sendFeedback(String logUrl,String feedback){
        final FeedbackRequest request = new FeedbackRequest();
        request.deviceId = PublicConsts.IMEI;
        request.content = feedback;
        request.logUrl = logUrl;
        httpManager.sendFeedback(request, new HttpCallback() {

            @Override
            public void onSuccess(String entity) {
                onSucceeded(SDKConsts.TYPE_SEND_FEEDBACK, new BaseResponse());
            }

            @Override
            public void onFail(String error, String code) {
                onFailed(SDKConsts.TYPE_SEND_FEEDBACK, error, code);
            }
        });
        return SDKConsts.SUCCESS;
    }
}
