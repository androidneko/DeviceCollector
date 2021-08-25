package com.androidcat.catlibs.net.http.response;

/**
 * Created by Administrator on 2015-12-16.
 */
public class CheckUpdateResponse extends BaseResponse{
  public String lastestVersion;
  public String versionName;
  public String versionLog;
  public String serverVersion;
  public String downloadUrl;
  public String isForce;
  public String isJoint;
}
