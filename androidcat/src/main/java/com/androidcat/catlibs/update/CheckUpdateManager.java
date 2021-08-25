package com.androidcat.catlibs.update;

import android.content.Context;

import com.androidcat.api.CommApi;
import com.androidcat.api.CommApiImpl;
import com.androidcat.catlibs.log.LogUtil;
import com.androidcat.catlibs.persistance.SpKey;
import com.androidcat.catlibs.net.http.response.BaseResponse;
import com.androidcat.catlibs.net.http.response.CheckUpdateResponse;
import com.androidcat.catlibs.permission.AndPermission;
import com.androidcat.catlibs.permission.Permission;
import com.androidcat.catlibs.persistance.SharePreferenceUtil;
import com.androidcat.catlibs.utils.Utils;

/**
 * Created by androidcat on 2018/9/18.
 */

public class CheckUpdateManager {
  private static final String TAG = "CheckUpdateManager";
  private Context context;

  public CheckUpdateManager(Context context){
    this.context = context;
  }

  public void checkUpdate(final boolean forceShowUpdate, final UpdateCallback callback) {
    String username = "";
    String baseversion="";
    CommApiImpl.getInstance(context).checkUpdate(username,baseversion, new CommApi.CallBack() {
      @Override
      public void onSuccess(int action, BaseResponse entity) {
        CheckUpdateResponse response = (CheckUpdateResponse) entity;
        try{
          int apkVersionCode = Utils.getVersionCode(context);
          if (response != null && !Utils.isNull(response.lastestVersion)) {
            if (apkVersionCode < Integer.parseInt(response.lastestVersion)) {
              if (!Utils.isNull(response.downloadUrl)) {
                UpdateManager mUpdateManager = new UpdateManager(context, response);
                if (!AndPermission.hasPermission(context, Permission.STORAGE)) {
                  callback.error("请开启应用存储权限!");
                  return;
                } else {
                  if (forceShowUpdate){
                    mUpdateManager.showNoticeDialog("1",callback);
                  }else {
                    if (checkIfShowUpdate(response.lastestVersion)){
                      mUpdateManager.showNoticeDialog("1",callback);
                    }
                  }
                  callback.success("");
                }
                return;
              } else {
                callback.error("新版下载地址有误!");
                return;
              }
            } else if (apkVersionCode >= Integer.parseInt(response.lastestVersion)) {
              callback.success("当前已是最新版本!");
              return;
            }
          }
          callback.success("当前已是最新版本!");
          LogUtil.d(TAG, "version = " + response.lastestVersion  + " downloadUrl = " + response.downloadUrl);
        }catch (Exception e){
          e.printStackTrace();
          callback.error("");
        }
      }

      @Override
      public void onFail(int action, String error, String code) {
        callback.error(error);
      }
    });
  }


  private boolean checkIfShowUpdate(String versionName){
    //定义是否提示更新的策略
    String value = SharePreferenceUtil.getString(SpKey.IS_SKIP_UPDATE + versionName);
    if ("true".equals(value)){
      return false;
    }
    return true;
  }
}
