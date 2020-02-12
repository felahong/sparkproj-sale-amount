package com.study.spark.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期格式化类
 */
public class DateFmt {

    public static final String date_long = "yyyy-MM-dd HH:mm:ss";
    public static final String date_short = "yyyy-MM-dd";
    public static final String date_minute = "yyyyMMddHHmm";


    public static SimpleDateFormat sdf = new SimpleDateFormat(date_short);

    public static String getCountDate(String date, String patton) {
        SimpleDateFormat sdf = new SimpleDateFormat(patton);
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            try {
                cal.setTime(sdf.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return sdf.format(cal.getTime());
    }

    public static String getCountDate(String date, String patton, int step) {
        SimpleDateFormat sdf = new SimpleDateFormat(patton);
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            try {
                cal.setTime(sdf.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        cal.add(Calendar.DAY_OF_MONTH, step);   // DAY_OF_MONTH = 5，是对日期操作。参考：https://www.cnblogs.com/yang-hao/p/8454999.html
        return sdf.format(cal.getTime());
    }

    public static Date parseDate(String dateStr) throws Exception {
        return sdf.parse(dateStr);  // 将字符串装换成日期形式
    }

    public static void main(String[] args) throws Exception {
        String date = "2020-12-12";
//		System.out.println(DateFmt.getCountDate("2020-12-12 12:13:14", DateFmt.date_short));
        System.out.println(parseDate(date));    // Sat Dec 12 00:00:00 CST 2020
        System.out.println("判断时间前后"+parseDate(date).after(parseDate("2020-12-11")));

        System.out.println(getCountDate(date, DateFmt.date_short, -1));
        System.out.println(getCountDate(null, DateFmt.date_short, -1));
    }

}
