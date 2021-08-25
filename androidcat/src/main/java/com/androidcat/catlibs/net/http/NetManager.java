package com.androidcat.catlibs.net.http;

import android.os.Build;

import com.androidcat.biz.consts.GConfig;
import com.androidcat.catlibs.log.LogUtil;
import com.androidcat.biz.consts.PublicConsts;
import com.androidcat.catlibs.utils.ConvertUtil;
import com.androidcat.catlibs.utils.JlEncodingUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by Administrator on 2018/4/25.
 */

public class NetManager {

    private static final String TAG = "NetManager";

    private String m_sHost = null;

    private Map<String, String> m_mapCommonParam = new HashMap<>();

    public NetManager(String host, String imei, String versionName) {
        m_sHost = host;
        setCommonParam(imei, versionName);
    }

    private synchronized String random() {
        Date date = new Date();
        SimpleDateFormat dfFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        dfFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String sDate = dfFormat.format(date);
        String da= sDate + (new Random().nextInt(8999)+1000);
        return da;
    }

    private List<String> sort(Map<String, String> mapData) {
        List<String> lstSort = new ArrayList<>();

        Set<String> sets = mapData.keySet();
        Iterator<String> iterator = sets.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = mapData.get(key);
            if (null != value && value.length() > 0) {
                lstSort.add(key + "=" + value + "&");
            }
        }
        Collections.sort(lstSort);
        return lstSort;
    }

    private String getSort(List<String> lstSrc, String key) {
        StringBuilder sbTmp = new StringBuilder();
        for (int i = 0; i < lstSrc.size(); i++) {
            sbTmp.append(lstSrc.get(i));
        }
        sbTmp.append("key=" + key);
        String str = sbTmp.toString();
        str = str.replaceAll(" ", "");
        return ConvertUtil.encrypByMd5(str).toUpperCase();
    }

    private String getSortWorkStr(List<String> lstSrc) {
        return getSort(lstSrc, GConfig.getAppKey());
    }

    private boolean setCommonParam(String imei, String versionName) {
        if (null == imei || imei.length() <= 0) {
            if (null == versionName || versionName.length() <= 0) {
                return false;
            }
        }

        if (null != m_mapCommonParam) {
            m_mapCommonParam.clear();
            m_mapCommonParam = null;
        }
        m_mapCommonParam = new HashMap<>();

        if (null != GConfig.getAppId()) {
            m_mapCommonParam.put("appId", GConfig.getAppId());
        }
        m_mapCommonParam.put("encodeMethod", "des");
        m_mapCommonParam.put("signMethod", "sign");
        m_mapCommonParam.put("deviceId", imei);
        m_mapCommonParam.put("brand", Build.BRAND);
        m_mapCommonParam.put("model", Build.MODEL);
        m_mapCommonParam.put("sdkVersion", versionName);

        return true;
    }

    public String setParam(String token, Object jsonObject) {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        m_mapCommonParam.put("msgId",random());//每次都不一样
        if (null == jsonObject || null == m_mapCommonParam) {
            return null;
        }
        String param = gson.toJson(jsonObject);
        LogUtil.e(TAG, "业务数据：" + param);
        Map<String, String> mapBody = new HashMap<>();
        // body
        String sEncodeBody = new JlEncodingUtil().encodeRequest(GConfig.getAppKey(), param);
        mapBody.put("body", sEncodeBody);

        // token
        if (null != token && token.length() > 0) {
            mapBody.put("authorization", token);
        }

        // common
        mapBody.putAll(m_mapCommonParam);

        // sign
        String sMD5 = getSortWorkStr(sort(mapBody));
        mapBody.put("sign", sMD5);

        return gson.toJson(mapBody);
    }

    public void post(final String param, final String url, final HttpCallback callback) {
        new Thread() {
            @Override
            public void run() {
                String sUrl = m_sHost + url;
                JlOkHttp.getInstance().post(sUrl, param, new HttpReqListener() {
                    @Override
                    public void onStart(String url) {
                    }

                    @Override
                    public void onResponse(String entity) {
                        try {
                            JSONObject objJson = new JSONObject(entity);
                            String returnCode = objJson.getString("returnCode");
                            if (PublicConsts.SUCCESS.equalsIgnoreCase(returnCode)) {
                                String res = objJson.getString("body");
                                String response = new JlEncodingUtil().decodeResponse(GConfig.getAppKey(), res);
                                LogUtil.e(TAG, "返回数据：" + response);
                                if (null != response && response.length() > 0) {
                                    if (callback != null) {
                                        callback.onSuccess(response);
                                    }
                                } else {
                                    if (callback != null) {
                                        callback.onSuccess("");
                                    }
                                }
                            } else {
                                LogUtil.e(TAG,"返回数据:"+entity);
                                String des = objJson.getString("returnDes");
                                if (callback != null) {
                                    callback.onFail(des, returnCode);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (callback != null) {
                                callback.onFail(e.getMessage(), "error");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        String msg = "";
                        if (e instanceof java.net.ConnectException ||
                                e instanceof java.net.UnknownHostException ||
                                e instanceof java.net.SocketException) {
                            msg = "当前网络不可用，请检查你的网络设置";
                        } else if (e instanceof InterruptedException) {
                            msg = "请求超时，请检查网络是否畅通";
                        } else if ("Service Temporarily Unavailable".equals(e.getMessage())) {
                            msg = "服务暂时不可用";
                        } else {
                            msg = "请求超时，请稍候再试";
                        }
                        if (callback != null) {
                            callback.onFail(msg, "-1");
                        }
                    }
                });
            }
        }.start();
    }
}
