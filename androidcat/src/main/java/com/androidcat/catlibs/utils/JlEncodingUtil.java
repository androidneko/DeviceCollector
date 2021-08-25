package com.androidcat.catlibs.utils;

import android.util.Log;

/**
 * Created by androidcat on 2017/12/11.
 */

public class JlEncodingUtil {

  //请求加密方法
  public String encodeRequest(String key, String actionInfo){
    try{
      long now = System.currentTimeMillis();
      //1.生成随机数
      String random =  DesTools.generalStringToAscii(8)+DesTools.generalStringToAscii(8);
      //random = "49575151515750564957515151575056";

      //2.生成过程密钥
      String processKey = DesTools.desecb(key,random,0);
      //3. 将actionInfo转换16进制后，补80
      actionInfo = DesTools.padding80(DesTools.bytesToHexString(actionInfo.getBytes("UTF-8")));
      //4. 将字符串编码成16进制数字,适用于所有字符（包括中文）
      actionInfo = DesTools.encodeHexString(actionInfo);
      // 加密
      actionInfo = DesTools.desecb(processKey, actionInfo,0);
      // 最终生成密文
      String end = random + actionInfo;
      return end;
    }catch (Exception e){
      Log.e("encypt","encode request failed");
      return "encode request failed";
    }
  }

  public String decodeResponse(String key, String data){
    try{
//      String key = PublicConsts.APP_KEY;
      long now = System.currentTimeMillis();
      // 获取随机数
      String randData = data.substring(0, 32);
      // 获取应用密文
      String singData = data.substring(32, data.length());
      // 获取过程密钥
      String processKey = DesTools.desecb(key, randData,0);
      //Log.e("processKey","processKey:"+processKey);
      // 解密singData
      String actionInfoString = DesTools.desecb(processKey, singData,1);
      //Log.e("actionInfoString","actionInfoString:"+actionInfoString);
      // 将16进制数字解码成字符串,适用于所有字符（包括中文）
      actionInfoString = DesTools.hexStringToString(actionInfoString);
      // 最后一个'80'出现的位置
      int num = actionInfoString.lastIndexOf("80");
      // 截取actionInfoString
      if (num != -1) {
        actionInfoString = actionInfoString.substring(0, num);
      }
      // actionInfoString转换为字符串
      actionInfoString = new String(DesTools.hexToBytes(actionInfoString), "UTF-8");
      return actionInfoString;
    }catch (Exception e){
      Log.e("decode","decode request failed");
      return "decode request failed";
    }
  }

  //请求加密方法
  public String encodeRequestV2(String key, String actionInfo){
    try{
//      String key = PublicConsts.APP_KEY;
      // 加密
      long now = System.currentTimeMillis();
      actionInfo = DesTools.encrypt(key, actionInfo);
      //Log.e("encypt","encypt data:"+actionInfo);
      //Log.e("encypt","costs:"+(System.currentTimeMillis()-now));
      return actionInfo;
    }catch (Exception e){
      Log.e("err","encode request failed");
      return "encode request failed";
    }
  }

  public String decodeResponseV2(String key, String data){
    try{
      long now = System.currentTimeMillis();
      // 解密singData
      String actionInfoString = DesTools.decrypt(key, data);
      //Log.e("decypt","decode data:"+actionInfoString);
      //Log.e("decypt"," costs:"+(System.currentTimeMillis()-now));
      return actionInfoString;
    }catch (Exception e){
      Log.e("err","decode request failed");
      return "decode request failed";
    }
  }
}
