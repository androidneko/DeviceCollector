package com.androidcat.catlibs.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by androidcat on 2019/5/5.
 */

public abstract class SafeHandler<T>  extends Handler{

    protected WeakReference<T> ref;

    public SafeHandler(T cls){
        ref = new WeakReference<T>(cls);
    }

    public T getRef() {
        return ref != null ? ref.get() : null;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        T caller = getRef();
        if (null == caller) {
            return;
        }
        if (caller instanceof Activity){
            if (((Activity) caller).isFinishing() || ((Activity) caller).isDestroyed()){
                return;
            }
        }
        safeHandle(msg);
    }

    public abstract void safeHandle(Message msg);

}
