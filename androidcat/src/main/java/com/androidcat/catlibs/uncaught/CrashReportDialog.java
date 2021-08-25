package com.androidcat.catlibs.uncaught;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;


import com.androidcat.api.CommApi;
import com.androidcat.api.CommApiImpl;
import com.androidcat.catlibs.net.http.response.BaseResponse;
import com.androidcat.catlibs.utils.Res;

import java.util.List;

/**
 * This is the dialog Activity used by ACRA to get authorization from the user
 * to send reports. Requires android:theme="@android:style/Theme.Dialog" and
 * android:launchMode="singleInstance" in your AndroidManifest to work properly.
 * 
 * @author Kevin Gaudin
 * 
 */
public class CrashReportDialog extends Activity {

	private AlertDialog alertDialog;
	private View send;
	private View close;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(Res.layout(this,"whty_traffic_dialog_crash"));

		send = findViewById(Res.id(this,"whty_crash_yesBtn"));
		close = findViewById(Res.id(this,"whty_crash_noBtn"));

		send.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				sendReport();
			}

		});
		close.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				sendReport();
			}

		});
	}

	private void sendReport() {
		final CacheDatabase database = CacheDatabase.getInstance(this);
		final List<CrashEntity> crashEntities = database.getCrashList();
		if (crashEntities != null && crashEntities.size() > 0){
			StringBuilder crashInfo = new StringBuilder();
			for (final CrashEntity crashEntity : crashEntities){
				crashInfo.append(crashEntity.deviceInfo);
				crashInfo.append("\n");
				crashInfo.append(crashEntity.stackTrace);
				crashInfo.append("\n");
			}
			CommApiImpl.getInstance(getBaseContext()).uploadCrash(crashInfo.toString(), new CommApi.CallBack() {
				@Override
				public void onSuccess(int type, BaseResponse entity) {
					dismissLoadingDialog();
					database.deleteCrashList(crashEntities);
					exit();
				}

				@Override
				public void onFail(int type, String error, String code) {
					dismissLoadingDialog();
					exit();
				}
			});
		}

	}
	
	private void exit() {
		finish();
	}

	public void showLoadingDialog() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (alertDialog != null && alertDialog.isShowing()){
					return;
				}
				if(alertDialog==null){
					alertDialog = new AlertDialog.Builder(CrashReportDialog.this).create();
				}
				alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
				alertDialog.setCancelable(false);
				alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK)
							return true;
						return false;
					}
				});
				alertDialog.show();
				alertDialog.setCanceledOnTouchOutside(false);
				alertDialog.setCancelable(false);
				alertDialog.setContentView(Res.layout(CrashReportDialog.this,"whty_traffic_alert"));
			}
		});
	}

	public void dismissLoadingDialog() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (null != alertDialog && alertDialog.isShowing()) {
					alertDialog.dismiss();
				}
			}
		});
	}
}
