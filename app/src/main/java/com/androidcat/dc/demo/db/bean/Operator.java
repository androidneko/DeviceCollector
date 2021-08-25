package com.androidcat.dc.demo.db.bean;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.NotNull;

public class Operator {

    @Column(column = "account")
    @Id
    @NotNull
    public String account;

    @Column(column = "password")
    @NotNull
    public String password;

    @Column(column = "name")
    @NotNull
    public String name;

    @Column(column = "phoneNum")
    @NotNull
    public String phoneNum;
} 