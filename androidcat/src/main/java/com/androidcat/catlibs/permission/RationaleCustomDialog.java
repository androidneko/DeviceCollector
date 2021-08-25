/*
 * Copyright © Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.androidcat.catlibs.permission;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.androidcat.catlibs.R;
import com.androidcat.catlibs.log.LogUtil;

import androidx.annotation.NonNull;


/**
 * <p>Default Rationale Dialog.</p>
 * Created by Yan Zhenjie on 2016/12/28.
 */
public class RationaleCustomDialog {

    private Dialog mDialog;
    private Rationale mRationale;

    RationaleCustomDialog(@NonNull Context context, @NonNull Rationale rationale) {
        if(context == null){
            return;
        }
        if(mDialog!=null){
            mDialog.cancel();
            mDialog = null;
        }
        mDialog = new Dialog(context, R.style.permissionDialog);
        if (null == mDialog) {
            LogUtil.e("RationaleCustomDialog", "dialog create fail");
            return;
        }

        mDialog.setContentView(R.layout.dialog_permission);

        View yesBtn = mDialog.findViewById(R.id.yesBtn);
        View noBtn = mDialog.findViewById(R.id.noBtn);
        yesBtn.setOnClickListener(yesListener);
        noBtn.setOnClickListener(noListener);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        this.mRationale = rationale;
    }

    private View.OnClickListener yesListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mRationale.resume();
            mDialog.dismiss();
        }
    };

    private View.OnClickListener noListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mRationale.cancel();
            mDialog.dismiss();
        }
    };

    public void show() {
        mDialog.show();
    }
}
