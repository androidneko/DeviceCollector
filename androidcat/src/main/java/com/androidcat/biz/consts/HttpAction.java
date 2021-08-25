package com.androidcat.biz.consts;

public class HttpAction {

    public static final String ACTION_LOGIN="/login";
    public static final String ACTION_GET_SERVER_TIME="/api/canteen/getServerTime";
    public static final String ACTION_GET_WHITELIST="/api/canteen/getWhiteList";
    public static final String ACTION_GET_AD_LIST="/api/canteen/getAdList";
    public static final String ACTION_UPLOAD_JPUSH_INFO="/api/canteen/bindJpush";
    public static final String ACTION_QUERY_DEVICEID="/api/canteen/queryDeviceId";
    public static final String ACTION_BIND_DEVICENAME="/api/canteen/bindDeviceName";
    public static final String ACTION_SET_CANTEEN_SITE="/api/canteen/setCanteenSite";
    public static final String ACTION_SWIPE_CARD="/api/canteen/swipeCard";//2.11.8	刷卡记录上传
    public static final String ACTION_GET_SHORTCUT_INFO="/api/canteen/getShortcutInfo";//2.11.9	快捷消费信息获取
    public static final String ACTION_APP_VERSION="/api/app-version";//获取app版本
    public static final String ACTION_UPLOAD_CRASH="/api/app-crash";
    public static final String ACTION_FEEDBACK="/api/app-proposals";
}
