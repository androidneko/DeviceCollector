package com.androidcat.catlibs.uncaught;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.NotNull;
import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by androidcat on 2019/6/11.
 */

@Table(name = "crash")
public class CrashEntity {
    @Id
    @Column(column = "id")
    @NotNull
    public int id;

    @Column(column = "stackTrace")
    public String stackTrace;

    @Column(column = "deviceInfo")
    public String deviceInfo;

    @Column(column = "state")
    public int state;

    @Column(column = "timeMillions")
    public String timeMillions;
}
