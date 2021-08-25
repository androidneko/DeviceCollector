package com.androidcat.catlibs.utils;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.math.BigDecimal;
import java.util.UUID;


public class Utils {
    private static String hexStr = "0123456789ABCDEF";
    public static byte[] hexStrToByteArray(String str)
    {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++){
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte)Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String convertHexToBinary(String hexString){
        long l = Long.parseLong(hexString, 16);
        String binaryString = Long.toBinaryString(l);
        int shouldBinaryLen = hexString.length()*4;
        StringBuffer addZero = new StringBuffer();
        int addZeroNum = shouldBinaryLen-binaryString.length();
        for(int i=1;i<=addZeroNum;i++){
            addZero.append("0");
        }
        return addZero.toString()+binaryString;
    }

    public static String xor(String str1, String str2) {
        StringBuffer sb = new StringBuffer();
        int len1 = str1.length(), len2 = str2.length();
        int i = 0, index = 0;
        if(len2 > len1 ) {
            index = len2 - len1;
            while(i ++ < len2 - len1) {
                sb.append(str2.charAt(i-1));
                str1 = "0" + str1;
            }
        } else if(len1 > len2) {
            index = len1 - len2;
            while(i ++ < len1 - len2) {
                sb.append(str1.charAt(i-1));
                str2 = "0" + str2;
            }
        }
        int len = str1.length();
        while(index < len) {
            int j = (Integer.parseInt(str1.charAt(index)+"", 16) ^ Integer.parseInt(str2.charAt(index)+"", 16)) & 0xf;
            sb.append(Integer.toHexString(j));
            index ++;
        }
        System.out.println(str1);
        System.out.println(str2);
        return sb.toString();
    }

    public static String encode(String str) {
        // 根据默认编码获取字节数组
        byte[] bytes = str.getBytes();
        String strs = "";
        // 将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            //取得高四位
            strs += hexStr.charAt((bytes[i] & 0xf0) >> 4);
            // System.out.println(i+"--"+bytes[i]+"----"+(bytes[i] & 0xf0)+"----"+hexString.charAt((bytes[i] & 0xf0) >> 4)+"---"+hexString.charAt((bytes[i] & 0x0f) >> 0));
            //取得低四位
            strs += hexStr.charAt((bytes[i] & 0x0f) >> 0);
        }
        return strs;
    }

    public static double getOneDouble(double f,int num){
        BigDecimal bg = new BigDecimal(f);
        double f1 = bg.setScale(num, BigDecimal.ROUND_HALF_UP).doubleValue();
        return f1;
    }
    public static String getVersionName(Context ctx) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = ctx.getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "1.0.0";
        }
        return packageInfo.versionName;
    }

    public static int getVersionCode(Context ctx) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = ctx.getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return 1;
        }
        return packageInfo.versionCode;
    }

    public static boolean isFlyme() {
        if("Meizu".equals(Build.MANUFACTURER)){
            return true;
        }
        return false;
    }



    /**
     * **********************************************************<br>
     * 方法功能：获得设备唯一标示<br>
     * 参数说明：<br>
     * 作 者：薛龙<br>
     * 开发日期：2013-3-22 下午12:41:31<br>
     * 修改日期：<br>
     * 修改人：<br>
     * 修改说明：<br>
     * **********************************************************<br>
     */
    public static String getDeviceId(Context ctx) {
        String deviceId = Build.SERIAL;
        if (Utils.isNull(deviceId)){
            deviceId = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        if (Utils.isNull(deviceId)){
            TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = telephonyManager.getDeviceId();
        }
        if (Utils.isNull(deviceId)){
            deviceId = UUID.randomUUID().toString();
        }
        return deviceId;
    }

    public static String getDeviceName(Context context){
      String model = Build.MODEL;
      return model;
    }


    public static boolean isNull(String str) {
        return null == str || "".equals(str) ||
                str.length() == 0 ||
                str.trim().length() == 0
                || "null".equals(str);
    }

    public static boolean isNetworkAvailable(Context mContext) {
        boolean isAvailable = false;
        final ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != cm) {
            final NetworkInfo[] netinfo = cm.getAllNetworkInfo();
            if (null != netinfo) {
                for (int i = 0; i < netinfo.length; i++) {
                    if (netinfo[i].isConnected()) {
                        isAvailable = true;
                    }
                }
            }
        }
        Log.e("MainActivity","网络："+isAvailable);
        return isAvailable;
    }



}
