package com.androidcat.catlibs.log;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * ***********************************************************
 * 功能：日志打印的工具类，GLOBAL为true表示全局开启日志打印，false表示全局关闭日志打印<br>
 *     每个级别的日志由该级别的控制变量控制开关
 * 作者：薛龙<br>
 * 时间：2016-1-16<br>
 * ***********************************************************
 */

public final class LogUtil {

    private static final String TAG = "hce_ac_";

    public static boolean GLOBAL = true;
    private static boolean DEBUG = true;
    private static boolean INFO = true;
    private static boolean VERBOSE = true;
    private static boolean ERROR = true;
    private static boolean WARN = true;
    private static boolean FILE = true;

    public static void setDebug(boolean debug){
        DEBUG = debug;
    }

    public static void setLogFile(boolean debug){
        FILE=debug;
    }

    public static void e(String tag, String msg) {
        if (GLOBAL) {
            if (DEBUG) {
                Log.e(TAG+tag,msg);
            }
            if (FILE){
                LogFileTool.getInstance().logAsync(getLogHeader(tag,"E") + msg);
            }
        }
    }

    public static void e(String msg) {
        if (GLOBAL) {
            if (DEBUG) {
                Log.e(TAG,msg);
            }
            if (FILE){
                LogFileTool.getInstance().logAsync(getLogHeader(TAG,"E") + msg);
            }
        }
    }

    public static void e(String tag, String msg, Throwable e) {
        if (!GLOBAL) {
            return;
        }
        if (DEBUG) {
          Log.e(tag,getStackTrace(e));
        }
        if (FILE){
            LogFileTool.getInstance().logAsync(getLogHeader(tag,"E") + msg + "\n" + getStackTrace(e));
        }
    }

    public static void v(String tag, String msg) {
        if (!GLOBAL) {
            return;
        }
        if (DEBUG) {
          Log.v(TAG+tag,msg);
        }
    }

    public static void i(String tag, String msg) {
        if (!GLOBAL) {
            return;
        }
        if (DEBUG) {
          Log.i(TAG+tag,msg);
        }
        if (FILE){
            LogFileTool.getInstance().logAsync(getLogHeader(tag,"I") + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (!GLOBAL) {
            return;
        }
        if (DEBUG) {
          Log.d(TAG+tag,msg);
        }
        if (FILE){
            LogFileTool.getInstance().logAsync(getLogHeader(tag,"D") + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (!GLOBAL) {
            return;
        }
        if (DEBUG) {
          Log.w(TAG+tag,msg);
        }
        if (FILE){
            LogFileTool.getInstance().logAsync(getLogHeader(tag,"W") + msg);
        }
    }

    private static String getLogHeader(String tag,String level){
        return String.format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()))+"/? " + level+"/"+tag+":";
    }

    private static String getStackTrace(Throwable e){
        if (e != null){
            return Log.getStackTraceString(e);
        }
        return "";
    }
}
