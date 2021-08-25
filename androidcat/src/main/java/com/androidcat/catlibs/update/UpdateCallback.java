package com.androidcat.catlibs.update;

/**
 * Created by androidcat on 2019/8/27.
 */

public interface UpdateCallback {
    void success(String msg);

    void error(String error);
}
