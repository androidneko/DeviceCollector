package com.androidcat.catlibs.utils;

import com.androidcat.catlibs.persistance.SharePreferenceUtil;
import com.androidcat.catlibs.persistance.SpKey;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {


    public static String longToString(long timeTemp){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        String time = simpleDateFormat.format(new Date(timeTemp));
        return time;
    }

    public static String longToDate(long timeTemp){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        String time = simpleDateFormat.format(new Date(timeTemp));
        return time;
    }

    public static String longToTime(long timeTemp){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        String time = simpleDateFormat.format(new Date(timeTemp));
        return time;
    }

    public static boolean isMorning(){
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
        Date nowTime =null;
        Date beginTime=null;
        try{
            nowTime=sdf.parse(sdf.format(new Date()));
            String morning = SharePreferenceUtil.getString(SpKey.KEY_MORNING,"10:00");
            beginTime=sdf.parse(morning);
        }catch (Exception e){
            e.printStackTrace();
        }
        boolean flag = beforeCalendar(nowTime, beginTime);
        return flag;
    }

    public static boolean isNoon(){
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
        Date nowTime =null;
        Date beginTime=null;
        Date endTime=null;
        try{
            nowTime=sdf.parse(sdf.format(new Date()));
            String morning = SharePreferenceUtil.getString(SpKey.KEY_MORNING,"10:00");
            String noon = SharePreferenceUtil.getString(SpKey.KEY_NOON,"15:00");

            beginTime=sdf.parse(morning);
            endTime=sdf.parse(noon);
        }catch (Exception e){
            e.printStackTrace();
        }
        boolean flag = belongCalendar(nowTime, beginTime, endTime);
        return flag;
    }

    public static boolean isDinner(){
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
        Date nowTime =null;
        Date beginTime=null;
        Date endTime=null;
        try{
            nowTime=sdf.parse(sdf.format(new Date()));
            String noon = SharePreferenceUtil.getString(SpKey.KEY_NOON,"15:00");
            String evening = SharePreferenceUtil.getString(SpKey.KEY_EVENING,"21:00");
            beginTime=sdf.parse(noon);
            endTime=sdf.parse(evening);
        }catch (Exception e){
            e.printStackTrace();
        }
        boolean flag = belongCalendar(nowTime, beginTime, endTime);
        return flag;
    }

    public static boolean isNight(){
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat sdf1=new SimpleDateFormat("HH:mm");
        Date nowTime =null;
        Date beginTime=null;
        Date endTime=null;
        try{
            nowTime=sdf.parse(sdf.format(new Date()));
            String evening = SharePreferenceUtil.getString(SpKey.KEY_EVENING,"21:00");
            String night = SharePreferenceUtil.getString(SpKey.KEY_NINGHT,"23:59:59");
            beginTime=sdf1.parse(evening);
            endTime=sdf.parse(night);
        }catch (Exception e){
            e.printStackTrace();
        }
        boolean flag = belongCalendar(nowTime, beginTime, endTime);
        return flag;
    }

    public static boolean belongCalendar(Date nowTime,Date beginTime,Date endTime){
        try{
            Calendar date=Calendar.getInstance();
            date.setTime(nowTime);
            Calendar begin = Calendar.getInstance();
            begin.setTime(beginTime);
            Calendar end = Calendar.getInstance();
            end.setTime(endTime);
            if(date.after(begin)&&date.before(end)){
                return true;
            }else{
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean beforeCalendar(Date nowTime,Date beginTime){
        Calendar date=Calendar.getInstance();
        date.setTime(nowTime);
        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);
        if(date.before(begin)){
            return true;
        }else{
            return false;
        }
    }

    public static boolean isTimeRight(String time) {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date endDate= null;
        try {
            endDate = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date nowDate=new Date();
        long nm = 1000 * 60;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少分钟
        long min = diff  / nm;
        if(min>5||min<-5){
            return false;
        }
        return true;
    }
}
