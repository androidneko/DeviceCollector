package com.androidcat.catlibs.net.http;

import android.os.Build;

import com.androidcat.catlibs.log.LogUtil;
import com.androidcat.biz.consts.PublicConsts;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FileUpload {
    private static final String TAG="FileUpload";
    private final String APP_ID = "8a88a80f65b2e4b80165b2e7633b0000";
    private String url;
    private String imei;
    private String versionName;
    private String token;

    public FileUpload(String url,String imei,String versionName,String token){
        this.url=url;
        this.imei=imei;
        this.versionName=versionName;
        this.token=token;
    }
    public void upload(final String filePath,final HttpCallback callback){
        new Thread(){
            @Override
            public void run() {
                StringBuilder sb=new StringBuilder(url);
                if (null != APP_ID) {
                    sb.append("?appId="+APP_ID);
                }
                sb.append("&deviceId="+imei);
                sb.append("&msgId="+random());
                sb.append("&brand="+Build.BRAND);
                sb.append("&model="+Build.MODEL);
                sb.append("&sdkVersion="+versionName);
                //sb.append("&authorization="+token);
                String param = sb.toString();
                param = param.replaceAll(" ","");
                LogUtil.e(TAG,"文件上传参数:"+param);
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .connectionPool(new ConnectionPool(10, 5l, TimeUnit.SECONDS)).build();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", filePath,
                                RequestBody.create(MediaType.parse("multipart/form-data"), new File(filePath)))
                        .build();
                Request request = new Request.Builder()
                        .header("Authorization", "Client-ID " + UUID.randomUUID())
                        .url(param)
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if(response.isSuccessful()){
                        String body=response.body().string();
                        LogUtil.e(TAG,"文件上传返回结果:"+body);
                        try {
                            JSONObject object=new JSONObject(body);
                            String res=object.getString("body");
                            String code=object.getString("returnCode");
                            String des=object.getString("returnDes");
                            if(PublicConsts.SUCCESS.equals(code)){
                                callback.onSuccess(res);
                            }else{
                                callback.onFail(des,code);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.onFail(e.getMessage(),"-1");
                        }

                    }else{
                        callback.onFail(response.message(),response.code()+"");
                    }
                } catch (Exception e) {
                    callback.onFail("当前网络不可用，请检查你的网络设置","-1");
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private String random() {
        Date date = new Date();
        SimpleDateFormat dfFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String sDate = dfFormat.format(date);
        return sDate + String.valueOf((int) (Math.random() * 9000 + 1000));
    }
}
