package com.androidcat.biz.bean;


import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.NotNull;
import com.lidroid.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * Created by androidcat on 2018/9/15.
 */

@Table(name = "CardRecord")
public class CardRecord implements Serializable{

  public static final int NEW_ADDED = 0;
  public static final int TO_BE_DELETED = 1;
  public static final int ROLLED_BACK = 2;

  public static final String MEAL_BREAKFAST = "0";
  public static final String MEAL_LUNCH = "1";
  public static final String MEAL_SUPPER = "2";
  public static final String MEAL_MIDNIGHT_SNACK = "3";

  public static final String CONSUME_TYPE_JICI="0";
  public static final String CONSUME_TYPE_JIFEI="1";

  public static final String ADD="0";
  public static final String DELETE="1";

  @Column(column = "id")
  @Id
  @NotNull
  public int id;

  @Column(column = "cardNo")
  @NotNull
  public String cardNo;

  @Column(column = "staffNo")
  @NotNull
  public String staffNo;

  @Column(column = "deviceId")
  @NotNull
  public String deviceId;

  @Column(column = "mealType")
  public String mealType;

  @Column(column = "transTime")
  public String transTime;

  @Column(column = "transTimeMillions")
  public long transTimeMillions;

  @Column(column = "consumeType")
  public String consumeType;

  //1:new added; 0:to be deleted on tomorrow;2:to be undo
  @Column(column = "state")
  public int state;

  @Column(column = "transMoney")
  public int transMoney;

  @Column(column = "addOrDelete")
  public String addOrDelete;


  @Override
  public String toString() {
    return "CardRecord{" +
      ", cardNo='" + cardNo + '\'' +
      ", mealType='" + mealType + '\'' +
      ", transTime='" + transTime + '\'' +
      ", transMoney='" + transMoney + '\'' +
      ", state='" + state + '\'' +
      '}';
  }
}
