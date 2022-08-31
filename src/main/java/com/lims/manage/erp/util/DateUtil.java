package com.lims.manage.erp.util;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.util
 * @desc
 * @date 2021/9/18 13:02
 * @Copyright © 河南交科院
 */
public class DateUtil {
    private static String ymdhms = "yyyy-MM-dd HH:mm:ss";
    private static String ymd = "yyyy-MM-dd";
    public static long DATEMM = 86400L;

    /**
     * 时间戳转化 yyyy.MM.dd 格式
     * @param time
     * @return
     */
    public static String getY_M_D(Long time){
        SimpleDateFormat yyyy_MM_dd = new SimpleDateFormat("yyyy.MM.dd");
        Date date = new Date(time);
        return yyyy_MM_dd.format(date);
    }



    /**
     * 获得当前时间 格式：2014-12-02 10:38:53
     * @return String
     */
    public static String getCurrentTime() {
        SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat(ymdhms);
        return yyyyMMddHHmmss.format(new Date());
    }

    /**
     * 获取今天0点开始的秒数
     * @return long
     */
    public static synchronized long getTimeNumberToday() {
        SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String str = yyyyMMdd.format(date);
        try {
            date = yyyyMMdd.parse(str);
            return date.getTime() / 1000L;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public static Date getDateFromStr(String date){
        int year = Integer.parseInt(date.substring(0, 4)) - 1900;
        int month = Integer.parseInt(date.substring(4, 6)) - 1;
        int day = Integer.parseInt(date.substring(6, 8));
        return new Date(year, month, day);
    }

    /**
     * 获取今天的日期 格式：20141202
     * @return String
     */
    public static String getTodayString() {
        SimpleDateFormat yyyyMMddHH_NOT_ = new SimpleDateFormat("yyyyMMdd");
        String str = yyyyMMddHH_NOT_.format(new Date());
        return str;
    }

    /**
     * 获取昨天的日期 格式：20141201
     * @return String
     */
    public static String getYesterdayString() {
        SimpleDateFormat yyyyMMddHH_NOT_ = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date(System.currentTimeMillis() - DATEMM * 1000L);
        String str = yyyyMMddHH_NOT_.format(date);
        return str;
    }

    /**
     * 获取之前n天的日期 格式 20141201
     * @param beforeDay
     * @return
     */
    public static String getBeforeDayString(int beforeDay){
        SimpleDateFormat yyyyMMddHH_NOT_ = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date(System.currentTimeMillis() - beforeDay * DATEMM * 1000L);
        String str = yyyyMMddHH_NOT_.format(date);
        return str;
    }

    /**
     * 获取某个时间戳的日期 格式 20141201
     * @param dateTime
     * @return
     */
    public static String getDayString(long dateTime){
        SimpleDateFormat yyyyMMddHH_NOT_ = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date(dateTime);
        String str = yyyyMMddHH_NOT_.format(date);
        return str;
    }
    /**
     * 获得前几天的0点的日期
     * @return Date
     */
    public static Date getBeforeDayZeroHour(int beforeDay) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -beforeDay);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return cal.getTime();
    }

    /**
     * 把long型日期转String ；---OK
     * @param date long型日期；
     * @param format 日期格式；
     * @return
     */
    public static String longToString(long date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        // 前面的lSysTime是秒数，先乘1000得到毫秒数，再转为java.util.Date类型
        java.util.Date dt2 = new Date(date * 1000L);
        String sDateTime = sdf.format(dt2); // 得到精确到秒的表示：08/31/2006 21:08:00
        return sDateTime;
    }

    /**
     *
     * 求某一个时间向前多少秒的时间(currentTimeToBefer)---OK
     * @param givedTime 给定的时间
     * @param interval 间隔时间的毫秒数；计算方式 ：n(天)*24(小时)*60(分钟)*60(秒)(类型)
     * @param format_Date_Sign 输出日期的格式；如yyyy-MM-dd、yyyyMMdd等；
     */
    public static String givedTimeToBefore(String givedTime, long interval, String format_Date_Sign) {
        String tomorrow = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format_Date_Sign);
            Date gDate = sdf.parse(givedTime);
            long current = gDate.getTime(); // 将Calendar表示的时间转换成毫秒
            long beforeOrAfter = current - interval * 1000L; // 将Calendar表示的时间转换成毫秒
            Date date = new Date(beforeOrAfter); // 用timeTwo作参数构造date2
            tomorrow = new SimpleDateFormat(format_Date_Sign).format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return tomorrow;
    }

    /**
     * 根据结束时间以及间隔差值，求符合要求的日期集合；
     * @param endTime
     * @param interval
     * @param isEndTime
     * @return
     */
    public static Map<String, String> getDate(String endTime, Integer interval, boolean isEndTime) {
        Map<String, String> result = new HashMap<String, String>();
        if (interval == 0 || isEndTime) {
            if (isEndTime) {
                result.put(endTime, endTime);
            }
        }
        if (interval > 0) {
            int begin = 0;
            for (int i = begin; i < interval; i++) {
                endTime = givedTimeToBefore(endTime, DATEMM, ymd);
                result.put(endTime, endTime);
            }
        }
        return result;
    }

    /**
     * 获取今天的开始时间
     * @return
     */
    public static long getTodayStartMs() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    // 获取本月的开始时间
    public static Date getBeginDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth() - 1, 1);
        return getDayStartTime(calendar.getTime());
    }

    // 获取某个日期的开始时间
    public static Timestamp getDayStartTime(Date d) {
        Calendar calendar = Calendar.getInstance();
        if (null != d)
            calendar.setTime(d);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Timestamp(calendar.getTimeInMillis());
    }

    // 获取今年是哪一年
    public static Integer getNowYear() {
        Date date = new Date();
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        return Integer.valueOf(gc.get(1));
    }
    // 获取本月是哪一月
    public static int getNowMonth() {
        Date date = new Date();
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        return gc.get(2) + 1;
    }

    /**
     * 获取今天的结束时间
     * @return
     */
    public static long getTodayEndMs() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
    /**
     * 将时间戳转为日期
     */
    public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt =new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

    /**
     * 获取给定时间戳当天的结束时间戳
     * @param timeMillis
     * @return
     */
    public static long getDayEndMs(long timeMillis){
        Date date = new Date(timeMillis);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取给定时间戳当天的开始时间戳
     * @param timeMillis
     * @return
     */
    public static long getDayStartMs(long timeMillis){
        Date date = new Date(timeMillis);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取两时间戳经过的天数
     * @return
     */
    public static int getIntervalDays(long formerTime, long latterTime){
        long formerStartMs = getDayStartMs(formerTime);
        long latterStartMs = getDayStartMs(latterTime);
        int days = (int)Math.abs(formerStartMs - latterStartMs) / 1000 / 60 / 60 / 24 + 1;
        return days;
    }

    /**
     * 将指定的时间戳前进或后退N天
     * @param timeMillis
     * @param day 为正数表示+，为负数表示-
     * @return
     */
    public static long getIncreaseDayMs(long timeMillis, int day){
        Date date = new Date(timeMillis);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, day * 24);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取指定时间范围内每隔指定时间段内的所有时间戳
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @param intervalTime 相隔的时间段
     * @return
     */
    public static List<Long> getIntervalTimeStamps(long startTime, long endTime, long intervalTime){
        if(intervalTime == 0){
            return Collections.EMPTY_LIST;
        }
        int count = (int) (Math.abs(endTime - startTime) / intervalTime);
        List<Long> resultList = new ArrayList<>(count);
        long tempTime =startTime;
        //包括起始时间
        resultList.add(startTime);
        for (int i = 0; i < count; i++) {
            tempTime += intervalTime;
            resultList.add(tempTime);
        }
        //包括结束时间
//        resultList.add(dateFormat.format(new Date(tempTime + intervalTime)));
        return resultList;
        //注意24:00的格式化显示
    }

    /**
     * 获取指定时间范围内每隔指定时间段内的所有时间戳
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @param intervalTime 相隔的时间段
     * @return   [yyyy-MM-dd HH:mm:ss,yyyy-MM-dd HH:mm:ss,...,yyyy-MM-dd HH:mm:ss]
     */
    public static Map<String, String> getIntervalFormateDate(long startTime, long endTime, long intervalTime){
        SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat(ymdhms);
        if(intervalTime == 0){
            return Collections.EMPTY_MAP;
        }
        int count = (int) (Math.abs(endTime - startTime) / intervalTime);
        Map<String, String> resultMap = new TreeMap<>();
        long tempTime =startTime;
        //包括起始时间
        String begin = yyyyMMddHHmmss.format(new Date(tempTime));
        for (int i = 0; i < count; i++) {
            resultMap.put(begin, yyyyMMddHHmmss.format(new Date(tempTime + intervalTime)));
            begin = yyyyMMddHHmmss.format(new Date(tempTime + intervalTime));
            tempTime += intervalTime;
        }
        //包括结束时间
        resultMap.put(begin, yyyyMMddHHmmss.format(new Date(tempTime + intervalTime)));
        return resultMap;
    }


    public static final String GENERAL_DATE_FORMAT="yyyy-MM-dd";

    /**
     * @desc 格式化日期
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(GENERAL_DATE_FORMAT);
        return sdf.format(date);
    }

    /**
     * 时间戳转 2021-06-01 26：30：10
     * @param timeStamp
     * @return
     */
    public static String conversionTime(Long timeStamp) {
        //yyyy-MM-dd HH:mm:ss 转换的时间格式  可以自定义
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //转换
        String time = sdf.format(new Date(timeStamp));
        return time;
    }

}
