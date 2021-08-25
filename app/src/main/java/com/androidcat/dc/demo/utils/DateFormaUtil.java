package com.androidcat.dc.demo.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormaUtil {

    public static String  paramTimeStamp(long timeStamp){
        String strDateFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        String date  = sdf.format(new Date(timeStamp));
       return date;
    }

    public static  String parseDateTime(String agrsDate){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date date = simpleDateFormat.parse(agrsDate);
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return  simpleDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return agrsDate;
    }
    public static  String getCurrentDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        return  simpleDateFormat.format(new Date());
    }
}
