package com.androidcat.catlibs.update;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidcat.catlibs.R;

public class DialogUtils {

  public static Dialog getUpdateDialog(final Context context, String msg, String confirmBtnTxt, View.OnClickListener onClickListener, String cancelTxt, View.OnClickListener onClickListener1) {
    final Dialog dialog = new Dialog(context, R.style.normalDialog);
    if (null == dialog) {
      android.util.Log.d("DialogUtils", "dialog create fail");
      return null;
    }

    dialog.setContentView(R.layout.update_dialog);
    dialog.setCanceledOnTouchOutside(false);

    TextView tv_dialog_msg = (TextView) dialog.findViewById(R.id.tv_title);
    String temp = "新版本号" + msg;
    tv_dialog_msg.setText(temp);
    TextView tv_nfc_confirm = (TextView) dialog.findViewById(R.id.tv_nfc_confirm);
    tv_nfc_confirm.setText(confirmBtnTxt);
    tv_nfc_confirm.setOnClickListener(onClickListener);
    final LinearLayout m_layout_btn = (LinearLayout) dialog.findViewById(R.id.layout_btn);
    final LinearLayout m_layout_progress = (LinearLayout) dialog.findViewById(R.id.layout_progress);

    TextView tv_nfc_cancel = (TextView) dialog.findViewById(R.id.tv_nfc_cancel);
    if (cancelTxt != null) {
      tv_nfc_cancel.setVisibility(View.VISIBLE);
      tv_nfc_cancel.setOnClickListener(onClickListener1);
      tv_nfc_cancel.setText(cancelTxt);
    } else {
      tv_nfc_cancel.setVisibility(View.GONE);
    }
    return dialog;
  }

}
