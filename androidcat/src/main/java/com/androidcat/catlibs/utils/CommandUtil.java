package com.androidcat.catlibs.utils;


import android.util.Log;

import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Created by coolbear on 15/4/16.
 */
public class CommandUtil {

  private final static String COMMAND_OK = "9000";
  public static String CARD = "";
  private static final String KEY = "6aafd4ec5c848dd9b2e9fc2316afbdfe";
  public static String IMEI;
  public static String lat;
  public static String lng;
  public static String sysVersion;
  public static String androidVersion;
  public static String phoneModel = android.os.Build.MODEL;
  public static String cityCode;
  public static String firmVer;
  public static String userName;
  public static String CARD_ID = null;
  public static String CITY_ID = null;
  public static String appId = "8a88a80f65b2e4b80165b2e7633b0000";
  public static String APPKEY = "c2fdd35572174e369ee9b4dfe3e01482";
  public static String DEVICE_NAME;

  public static String getCommandInfo(String apdu) {
    if (null != apdu) {
      if (COMMAND_OK.equalsIgnoreCase(apdu)) {
        return apdu;
      }

      int nPos = apdu.lastIndexOf(COMMAND_OK);
      if (nPos > 0) {
        apdu = apdu.substring(0, nPos);
      }
    }

    return apdu;
  }

  /**
   * 获取随机数
   *
   * @return 返回四位随机数
   */
  public static String random() {
    Date date = new Date();
    SimpleDateFormat dfFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    String sDate = dfFormat.format(date);
    return sDate + String.valueOf((int) (Math.random() * 9000 + 1000));
  }

  /**
   * 首字母小写
   *
   * @param str
   * @return
   */
  private static String firstLetterToLow(String str) {
    char[] array = str.toCharArray();
    array[0] += 32;
    return String.valueOf(array);
  }

    /*public static List<String> sort(JSONObject jsonObject){
      List<String> lstSort=new ArrayList<>();
      Set<String> sets=jsonObject.keySet();
        Iterator<String> iterator=sets.iterator();
      while (iterator.hasNext()){
        String key=iterator.next();
        String value= null;
        value = jsonObject.getString(key);
        if(!Utils.isNull(value)){
          lstSort.add(key+"="+value+"&");
        }
      }

      Collections.sort(lstSort);
      return lstSort;
    }*/

  public static String getSort(List<String> lstSrc, String key) {
    StringBuilder sbTmp = new StringBuilder();
    for (int i = 0; i < lstSrc.size(); i++) {
      sbTmp.append(lstSrc.get(i));
    }
    sbTmp.append("key=" + key);
    String str = sbTmp.toString();
    str = str.replaceAll(" ", "");
    Log.e("CommandUtil", "签名元数据:" + str);
    return ConvertUtil.encrypByMd5(str).toUpperCase();
  }


  /**
   * 排序
   *
   * @param obj
   * @return
   */
  public static List<String> getSortList(Object obj) {
    String sMethodGet = "get";

    Class clazz = obj.getClass();
    Method[] arrMethod = clazz.getMethods();
    List<String> lstSort = new ArrayList<String>();

    for (int i = 0; i < arrMethod.length; i++) {
      Method method = arrMethod[i];
      if (method.getName().startsWith(sMethodGet)) {
        try {
          Object objResult = method.invoke(obj);
          Class<?> type = method.getReturnType();
          String fieldName = firstLetterToLow(method.getName().substring(sMethodGet.length()));

          if (fieldName.equals("sign") ||
            type == Class.class ||
            ((objResult instanceof String) ? Utils.isNull((String) objResult) : (null == objResult))) {
            continue;
          }
          if (objResult instanceof ArrayList<?>) {
            ArrayList<?> list = (ArrayList<?>) objResult;
            for (int j = 0; j < list.size(); j++) {
              Object obj1 = list.get(j);
              Class clazz1 = obj1.getClass();
              Method[] arrMethod1 = clazz1.getMethods();

              for (int s = 0; s < arrMethod1.length; s++) {
                Method method1 = arrMethod1[s];
                if (method1.getName().startsWith(sMethodGet)) {
                  try {
                    Object objResult1 = method1.invoke(obj1);
                    Class<?> type1 = method1.getReturnType();
                    String fieldName1 = firstLetterToLow(method1.getName().substring(sMethodGet.length()));

                    if (fieldName1.equals("sign") || type1 == Class.class || null == objResult1) {
                      continue;
                    }
                    lstSort.add(fieldName1 + "=" + objResult1 + "&");
                  } catch (IllegalAccessException e) {

                  } catch (InvocationTargetException e) {

                  }
                }
              }
            }
          } else {
            lstSort.add(fieldName + "=" + objResult + "&");
          }
        } catch (IllegalAccessException e) {

        } catch (InvocationTargetException e) {

        }
      }
    }

    Collections.sort(lstSort);

    return lstSort;
  }

  public static String getSortKeyStr(List<String> lstSrc) {
    return getSort(lstSrc, KEY);
  }

  public static String getSortWorkStr(List<String> lstSrc) {
    // return getSort(lstSrc, DesTools.decrypt(SdkConst.MAIN_KEY, SdkConst.WORK_KEY));
    return getSort(lstSrc, CommandUtil.APPKEY);
  }

    /*public static String getSort(List<String> lstSrc, String key) {
        StringBuilder sbTmp = new StringBuilder();
        for (int i = 0; i < lstSrc.size(); i++) {

            sbTmp.append(lstSrc.get(i));
        }
        sbTmp.append("key=" + key);
        Log.e("getSort", "before sign:"+sbTmp.toString());
        return ConvertUtil.encrypByMd5(sbTmp.toString()).toUpperCase();
    }*/


  public static Object Json2Object(Class<?> clazz, String sEntity) {
    Gson gson = new Gson();
    Object objResult = null;
    try {
      //objResult = clazz.newInstance();
      objResult = gson.fromJson(sEntity, (Type) clazz);
    } catch (Exception e) {

    }
    return objResult;
  }

  /**
   * 对象转字符串
   *
   * @param object
   * @return
   */
  public static String Object2Json(Object object) {
    Gson gson = new Gson();
    String objResult = "";
    try {
      objResult = gson.toJson(object);
    } catch (Exception e) {

    }
    return objResult;
  }

}
