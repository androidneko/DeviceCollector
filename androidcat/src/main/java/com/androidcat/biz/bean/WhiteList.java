package com.androidcat.biz.bean;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

import androidx.annotation.NonNull;

/**
 * Created by androidcat on 2018/9/15.
 */

@Table(name = "whitelist")
public class WhiteList implements Comparable<WhiteList>{

  public static final String ADD="0";
  public static final String DELETE="1";
  public static final String UPDATE="2";

  @Override
  public String toString() {
    return "WhiteList{" +
      "staffNo='" + staffNo + '\'' +
      ", cardType=" + cardType +
      ", cardNo='" + cardNo + '\'' +
      '}';
  }

  @Column(column = "staffNo")
  public String staffNo;

  @Column(column = "password")
  public String password;

  @Column(column = "cardType")
  public String cardType;

  @Column(column = "addOrDelete")
  public String addOrDeleteOrUpdate = ADD;

  @Column(column = "name")
  public String name;

  @Id
  @Column(column = "cardNo")
  public String cardNo;

  public String modifiedTime;

  @Override
  public int compareTo(@NonNull WhiteList o) {
    return modifiedTime.compareTo(o.modifiedTime);
  }
}
