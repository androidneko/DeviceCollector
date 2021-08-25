package com.androidcat.catlibs.permission;

import android.Manifest;
import android.content.Context;
import android.location.LocationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: bletravel_new
 * Author: androidcat
 * Email:androidcat@126.com
 * Created at: 2017-8-18 14:44:53
 * add function description here...
 */
public class PermissionUtils {

    public static final int REQUEST_LOCATION_PERMISSION = 391;

    public static List<String> allPermisssions = new ArrayList<>();
    public static List<String> telephonyPermisssions = new ArrayList<>();
    static {
        allPermisssions.add(Manifest.permission.READ_CONTACTS);
        allPermisssions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        allPermisssions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        allPermisssions.add(Manifest.permission.READ_PHONE_STATE);
        allPermisssions.add(Manifest.permission.CALL_PHONE);
//        allPermisssions.add(Manifest.permission.READ_CALL_LOG);
//        allPermisssions.add(Manifest.permission.RECEIVE_SMS);
//        allPermisssions.add(Manifest.permission.READ_SMS);
        allPermisssions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        allPermisssions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        allPermisssions.add(Manifest.permission.CAMERA);

        telephonyPermisssions.add(Manifest.permission.READ_CONTACTS);
        telephonyPermisssions.add(Manifest.permission.READ_PHONE_STATE);
        telephonyPermisssions.add(Manifest.permission.CALL_PHONE);
        telephonyPermisssions.add(Manifest.permission.READ_CALL_LOG);
        telephonyPermisssions.add(Manifest.permission.RECEIVE_SMS);
        telephonyPermisssions.add(Manifest.permission.READ_SMS);
    }

    public static boolean hasSMSPermission(Context context){
        List<String> list = new ArrayList<>();
        list.add("android.permission.RECEIVE_SMS");
        list.add("android.permission.READ_SMS");
        return !AndPermission.hasAlwaysDeniedPermission(context,list);
    }

    public static boolean hasDeniedPermission(List<String> deniedPers){
        for (String denied : deniedPers){
            for (String per : allPermisssions){
                if (denied.equals(per)){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasDeniedLocPermission(List<String> deniedPers){
        List<String> list = new ArrayList<>();
        list.add(Manifest.permission.ACCESS_FINE_LOCATION);
        list.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        for (String denied : deniedPers){
            for (String per : list){
                if (denied.equals(per)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *判断位置信息是否开启
     * @param context
     * @return
     */
    public static boolean isLocationOpen(final Context context){
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //gps定位
        boolean isGpsProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //网络定位
        boolean isNetWorkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGpsProvider|| isNetWorkProvider;
    }
}
