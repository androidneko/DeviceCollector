package com.androidcat.catlibs.update;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidcat.catlibs.R;
import com.androidcat.catlibs.log.LogUtil;
import com.androidcat.catlibs.net.http.response.CheckUpdateResponse;
import com.androidcat.catlibs.view.NumberProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zt on 2015-10-21.
 */
public class UpdateManager {
  private static final String TAG = "UpdateManager";
  private static final String savePath = "/sdcard/updateAPK/"; //apk保存到SD卡的路径
  private static final String saveFileName = savePath + "POS_client.apk"; //完整路径名

  private static final int DOWNLOADING = 1; //表示正在下载
  private static final int DOWNLOADED = 2; //下载完毕
  private static final int DOWNLOAD_FAILED = 3; //下载失败
  private static final int FILE_NOT_EXIST = 4; //下载地址连接不上

  //private static List<String> compatibilityList = new ArrayList<String>();

  private Dialog alertDialog;
  private NumberProgressBar mProgress; //下载进度条控件

  private int progress; //下载进度
  private boolean cancelFlag = false; //取消下载标志位
  private String updateDescription = "本次更新以下内容" + "\n"; //更新内容描述信息

  private Context context;
  private CheckUpdateResponse versionUpdate;

  /*static{
    compatibilityList.add("NX563J&7.1.1");
  }*/

  /**
   * 更新UI的handler
   */
  @SuppressLint("HandlerLeak")
  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case DOWNLOADING:
          mProgress.setProgress(progress);
          break;
        case DOWNLOADED:
          if (alertDialog != null)
            alertDialog.dismiss();
          installAPK();
          break;
        case DOWNLOAD_FAILED:
          if (context != null)
            Toast.makeText(context, "下载失败，请确认网络和权限是否正常。", Toast.LENGTH_LONG).show();
          alertDialog.dismiss();
          break;
        case FILE_NOT_EXIST:
          if (context != null)
            Toast.makeText(context, "连接失败，请确认下载地址是否无误", Toast.LENGTH_LONG).show();
          alertDialog.dismiss();
          break;
        default:
          break;
      }
    }
  };

  public UpdateManager(Context context,final CheckUpdateResponse versionUpdate) {
    this.context = context;
    this.versionUpdate = versionUpdate;
    this.updateDescription += versionUpdate.versionLog;
  }

  /**
   * 显示更新对话框
   */
  public void showNoticeDialog(final String tag, final UpdateCallback updateCallback) {
    try {
      alertDialog = DialogUtils.getUpdateDialog(context, versionUpdate.versionName, "立即升级",
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            final LinearLayout layout_progress = (LinearLayout) alertDialog.findViewById(R.id.layout_progress);
            final LinearLayout layout_btn = (LinearLayout) alertDialog.findViewById(R.id.layout_btn);
            if (null != layout_progress) {
              layout_progress.setVisibility(View.VISIBLE);
            }
            layout_btn.setVisibility(View.GONE);
            showDownloadDialog();
          }
        }, "稍后升级", new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            alertDialog.dismiss();
            logCancel();
          }
        });

      TextView tv = (TextView) alertDialog.findViewById(R.id.tv_text);
      tv.setText(updateDescription);

      //如果下载的安装包，但是未安装，则先检查在判断是否下载更新
      PackageManager pm = context.getPackageManager();
      PackageInfo info = pm.getPackageArchiveInfo(saveFileName,
        PackageManager.GET_ACTIVITIES);
      if (info != null) {
        String version = info.versionName == null ? "0" : info.versionName;
        if (version.equals(versionUpdate.versionName)) {
          File apkFile = new File(saveFileName);
          if (apkFile.exists()) {
            TextView tv_hint = (TextView) alertDialog.findViewById(R.id.tv_hint);
            TextView tv_btn_cancel = (TextView) alertDialog.findViewById(R.id.tv_nfc_cancel);
            TextView tv_btn_ok = (TextView) alertDialog.findViewById(R.id.tv_nfc_confirm);
            tv_btn_ok.setText("立即安装");
            tv_btn_cancel.setText("稍后安装");
            tv_hint.setText("温馨提示：发现本机有最新安装包，点击按钮选择是否安装！");
            tv_hint.setVisibility(View.GONE);
            tv_btn_ok.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                installApkForDifferentVersions();
              }
            });
            tv_btn_cancel.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                if (versionUpdate.isForce.equals("1")) {
                  //强制升级不能记录用户跳过升级，否则就会影响强制升级策略
                  //logCancel();
                  System.exit(0);
                } else {
                  logCancel();
                  alertDialog.dismiss();
                }
              }
            });
          }
        }
      }

      //是否强制更新
      if ("1".equals(versionUpdate.isForce)) {
        TextView tv_btn_cancel = (TextView) alertDialog.findViewById(R.id.tv_nfc_cancel);
        tv_btn_cancel.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            //强制升级不能记录用户跳过升级，否则就会影响强制升级策略
            alertDialog.dismiss();
            updateCallback.error("0");
          }
        });
        //如果是强制更新，在对话框未消失前点击Back直接退出应用
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
          @Override
          public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
              alertDialog.dismiss();
              updateCallback.error("0");
            }
            return false;
          }
        });
      }
      alertDialog.setCanceledOnTouchOutside(false);
      alertDialog.setCancelable(false);
      alertDialog.show();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private void logCancel(){
    LogUtil.d(TAG,"-------用户跳过升级，暂不升级--------");
    //记录用户跳过升级，暂不升级
    // TODO: 2019/9/12
  }

  //显示进度条更新
  public void showDownloadDialog() {
    try{
      mProgress = (NumberProgressBar) alertDialog.findViewById(R.id.progress);
      downloadApkFile(versionUpdate.downloadUrl);
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  private void downloadApkFile(final String downloadUrl) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        File file = new File(savePath);
        if (!file.exists()) {
          file.mkdir();
        }
        File zipFile = new File(saveFileName);
        saveStream(zipFile, downloadUrl);
      }
    }).start();
  }

  private void saveStream(final File hexFile, final String versionPath) {
    android.util.Log.d(TAG, "saveStream");
    final OkHttpClient client = new OkHttpClient();
    try {
      Request request = new Request.Builder()
        .url(versionPath)
        .build();
      client.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          mHandler.sendEmptyMessage(DOWNLOAD_FAILED);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
          if (!response.isSuccessful()){
            mHandler.sendEmptyMessage(FILE_NOT_EXIST);
            return;
          }
          Headers responseHeaders = response.headers();
          for (int i = 0; i < responseHeaders.size(); i++) {
            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
          }
          try {
            FileOutputStream fos = new FileOutputStream(hexFile);
            int count = 0;
            byte buf[] = new byte[1024];
            long length = response.body().contentLength();
            InputStream is = response.body().byteStream();
            do {
              int numread = is.read(buf);
              count += numread;
              progress = (int) (((float) count / length) * 100);
              //更新进度
              mHandler.sendEmptyMessage(DOWNLOADING);
              if (numread <= 0) {
                //下载完成通知安装
                mHandler.sendEmptyMessage(DOWNLOADED);
                break;
              }
              fos.write(buf, 0, numread);
            } while (true);

            fos.close();
            is.close();
          } catch (Exception e) {
            mHandler.sendEmptyMessage(DOWNLOAD_FAILED);
            e.printStackTrace();
          }
        }
      });
    } catch (Exception e) {
      mHandler.sendEmptyMessage(DOWNLOAD_FAILED);
      e.printStackTrace();
    }
  }

  private void installApkForDifferentVersions(){
    boolean haveInstallPermission;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      //先获取是否有安装未知来源应用的权限
      haveInstallPermission = context.getPackageManager().canRequestPackageInstalls();
      if (!haveInstallPermission) {
        //没有权限
        startInstallPermissionSettingActivity();
        return;
      }
    }
    installAPK();
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private void startInstallPermissionSettingActivity() {
    Uri packageURI = Uri.parse("package:" +context.getPackageName());
    //注意这个是8.0新API
    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
    ((Activity)context).startActivityForResult(intent, 10086);
  }

  /**
   * 下载完成后自动安装apk
   */
  public void installAPK() {
    File apkFile = new File(saveFileName);
    if (!apkFile.exists()) {
      return;
    }
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    if (Build.VERSION.SDK_INT >= 24) {
      try{
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".file.provider", apkFile);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//增加读写权限
        intent.setDataAndType(uri, context.getContentResolver().getType(uri));
      }catch (Exception e){
        e.printStackTrace();
      }
    } else {
      intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
    }

    context.startActivity(intent);
    LogUtil.e("installAPK", "----installAPK----");
  }

}
