package com.androidcat.catlibs.persistance;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Created by Administrator on 2018/4/18.
 */

public class SharePreferenceUtil {

    private static Context sContext;
    private static final String name="sp";

    public static void init(Context context){
        sContext=context;
    }

    public static String getString(String key){
        SharedPreferences sharedPreferences=sContext.getSharedPreferences(name,Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }

    public static String getString(String key,String url){
        SharedPreferences sharedPreferences=sContext.getSharedPreferences(name,Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,url);
    }


    public static void setString(String key,String value){
        SharedPreferences sharedPreferences=sContext.getSharedPreferences(name,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(key,value);
        editor.commit();
    }


    public static boolean getBoolean(String key){
        SharedPreferences sharedPreferences=sContext.getSharedPreferences(name,Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key,false);
    }

    public static boolean getBoolean(String key,boolean def){
        SharedPreferences sharedPreferences=sContext.getSharedPreferences(name,Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key,def);
    }

    public static void setBoolean(String key,boolean value){
        SharedPreferences sharedPreferences=sContext.getSharedPreferences(name,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(key,value);
        editor.commit();
    }

    public static int getIntValue(String key){
        SharedPreferences sharedPreferences=sContext.getSharedPreferences(name,Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key,0);
    }

    public static void setIntValue(String key,int value){
        SharedPreferences sharedPreferences=sContext.getSharedPreferences(name,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(key,value);
        editor.commit();
    }

    public static long getLongValue(String key){
        SharedPreferences sharedPreferences=sContext.getSharedPreferences(name,Context.MODE_PRIVATE);
        return sharedPreferences.getLong(key,0);
    }

    public static void setLongValue(String key,long value){
        SharedPreferences sharedPreferences=sContext.getSharedPreferences(name,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putLong(key,value);
        editor.commit();
    }


    public static void saveWelfare(String code) {
        if(TextUtils.isEmpty(code)){
            return;
        }
        code=code.substring(0,10);

        setString("freeCode",code);
    }

    public static String getWelfare(){
        return getString("freeCode");
    }
}
