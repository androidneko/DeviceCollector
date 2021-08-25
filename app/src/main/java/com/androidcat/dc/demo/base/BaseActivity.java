package com.androidcat.dc.demo.base;

import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

import com.androidcat.catlibs.utils.SafeHandler;

import androidx.annotation.Nullable;
import me.yokeyword.fragmentation.SupportActivity;

public class BaseActivity extends SupportActivity {
    protected SafeHandler<BaseActivity> baseHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseHandler = new SafeHandler<BaseActivity>(this) {
            @Override
            public void safeHandle(Message msg) {

            }
        };
    }

    protected void childHandle(Message msg){

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void showToast(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseActivity.this,msg,Toast.LENGTH_SHORT).show();
            }
        });
    }

}
