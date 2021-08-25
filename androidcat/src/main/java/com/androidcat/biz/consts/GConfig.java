package com.androidcat.biz.consts;

import com.google.gson.Gson;

/**
 * Created by androidcat on 2018/7/3.
 */

public class GConfig {
  public static final String HTTP = "http://";
  public static final String HTTPS = "https://";

  public static final String ENV_DEV = "dev";
  public static final String ENV_TEST = "test";
  public static final String ENV_BETA = "beta";
  public static final String ENV_PROD = "prod";

  private static GConfig config = new GConfig();
  public static GConfig getConfig() {
    return config;
  }
  public static void load(String json){
    config = new Gson().fromJson(json, GConfig.class);
  }
  public static String getDomain(){
    if (config.comm.env.equals(ENV_PROD)){
      return config.comm.protocol + config.comm.prod.host;
    }
    if (config.comm.env.equals(ENV_TEST)){
      return config.comm.protocol + config.comm.test.host;
    }
    if (config.comm.env.equals(ENV_DEV)){
      return config.comm.protocol + config.comm.dev.host;
    }
    if (config.comm.env.equals(ENV_BETA)){
      return config.comm.protocol + config.comm.beta.host;
    }
    return config.comm.protocol + config.comm.prod.host;
  }

  public static String getImgUrl(){
    if (config.comm.env.equals(ENV_PROD)){
      return config.comm.protocol + config.comm.prod.imgUrl;
    }
    if (config.comm.env.equals(ENV_TEST)){
      return config.comm.protocol + config.comm.test.imgUrl;
    }
    if (config.comm.env.equals(ENV_DEV)){
      return config.comm.protocol + config.comm.dev.imgUrl;
    }
    if (config.comm.env.equals(ENV_BETA)){
      return config.comm.protocol + config.comm.beta.imgUrl;
    }
    return config.comm.protocol + config.comm.prod.imgUrl;
  }

  public static String getAppKey(){
    if (config.comm.env.equals(ENV_PROD)){
      return config.comm.prod.appKey;
    }
    if (config.comm.env.equals(ENV_TEST)){
      return config.comm.test.appKey;
    }
    if (config.comm.env.equals(ENV_DEV)){
      return config.comm.dev.appKey;
    }
    if (config.comm.env.equals(ENV_BETA)){
      return config.comm.beta.appKey;
    }
    return config.comm.test.appKey;
  }

  public static String getAppId(){
    if (config.comm.env.equals(ENV_PROD)){
      return config.comm.prod.appId;
    }
    if (config.comm.env.equals(ENV_TEST)){
      return config.comm.test.appId;
    }
    if (config.comm.env.equals(ENV_DEV)){
      return config.comm.dev.appId;
    }
    if (config.comm.env.equals(ENV_BETA)){
      return config.comm.beta.appId;
    }
    return config.comm.test.appId;
  }

  public String versionName = "1.0";
  public int versionCode = 0;

  public Comm comm = new Comm();
  public Android android = new Android();
  public Ios ios = new Ios();


  public class Comm{
    public boolean debug = true;
    public boolean logFile = true;
    public String protocol = HTTP;
    public String env = "test";
    public String pin = "E10ADC3949BA59ABBE56E057F20F883E";

    public Prod prod = new Prod();
    public Test test = new Test();
    public Dev dev = new Dev();
    public Beta beta = new Beta();

    public class Prod{
      public String host = "nfc2go.tyjulink.com/nfc-interface";
      public String imgUrl = "nfc2go.tyjulink.com/";
      public String appKey = "8368c1d9a5404bdeab796c3b18db5ee2";
      public String appId = "0958626e1278400c883a818a0af36ea1";
    }

    public class Test{
      public String host = "yfzx.whty.com.cn/test-nfc/nfc-interface";
      public String imgUrl = "yfzx.whty.com.cn/test-nfc/";
      public String appKey = "15f6811710624899bf04b6db92c697c3";
      public String appId = "8b52238469984da8994d9b888885e929";
    }

    public class Dev{
      public String host = "yfzx.whty.com.cn/dev-nfc/nfc-interface";
      public String imgUrl = "yfzx.whty.com.cn/dev-nfc/";
      public String appKey = "15f6811710624899bf04b6db92c697c3";
      public String appId = "8b52238469984da8994d9b888885e929";
    }

    public class Beta{
      public String host = "yfzx.whty.com.cn/beta-nfc/nfc-interface";
      public String imgUrl = "yfzx.whty.com.cn/beta-nfc/";
      public String appKey = "15f6811710624899bf04b6db92c697c3";
      public String appId = "8b52238469984da8994d9b888885e929";
    }
  }

  public class Android{
    public String os = "01";
  }

  public class Ios{
    public String os = "01";
    public String mode = "";
  }

}
