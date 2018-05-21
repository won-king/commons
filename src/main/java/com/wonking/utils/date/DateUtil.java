package com.wonking.utils.date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kewangk on 2017/12/1.
 */
public class DateUtil {
    private static final String DATE_FORMAT_Y   = "yyyy";
    private static final String DATE_FORMAT_YM  = "yyyy-MM";
    private static final String DATE_FORMAT_YMD = "yyyy-MM-dd";
    private static final String DATE_FORMAT_HM  = "yyyy-MM-dd HH:mm";
    private static final String DATE_FORMAT_HMS = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_TIME_UTC   = "yyyy-MM-dd'T'HH:mm:ssZ";

    //由于SimpleDateFormat是非线程安全的，所以这里需要用threadLocal存储本地线程变量，避免多个线程共享一个simpleDateFormat实例
    //可以把simpleDateFormat想象成一个动作缓慢的铸模机，你给他一个模型，等他把模型铸造出来后返回给你
    //但是这个铸模机被很多人共享，你在给他下命令的同时，别人也在给他下命令，可能你给的那个模型还未铸造完
    //别人又给了它另一个模型，你的那个模型就被覆盖了，最后出来的模型就不是你想要的
    //ThreadLocal的原理是，在内部实现了一个Map，维护了Thread与存储值的映射，所以看起来是一个变量，其实不同的线程拿到的是不同的变量
    private static final ThreadLocal<SimpleDateFormat> hmsDateFormat=new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DATE_FORMAT_HMS);
        }
    };

    private DateUtil() {
    }

    public static String getStringDate(String parttern) {
        return dateToStr(new Date(),parttern);
    }

    public static String getDateOfYear() {
        return dateToStr(new Date(),DATE_FORMAT_Y);
    }

    public static String getDateOfYearMonth() {
        return dateToStr(new Date(),DATE_FORMAT_YM);
    }

    public static String getDateOfYMD() {
        return dateToStr(new Date(),DATE_FORMAT_YMD);
    }

    public static String getDateTimeOfShort() {
        return dateToStr(new Date(),DATE_FORMAT_HM);
    }

    public static String getDateTime() {
        return dateToStr(new Date(),DATE_FORMAT_HMS);
    }

    public static String getStringDate(Date date, String parttern) {
        return dateToStr(date,parttern);
    }

    public static String getDateOfYear(Date date) {
        return dateToStr(date,DATE_FORMAT_Y);
    }

    public static String getDateOfYearMonth(Date date) {
        return dateToStr(date,DATE_FORMAT_YM);
    }

    public static String getDateOfYMD(Date date) {
        return dateToStr(date,DATE_FORMAT_YMD);
    }

    public static String getDateTimeOfShort(Date date) {
        return dateToStr(date,DATE_FORMAT_HM);
    }

    public static String getDateTime(Date date) {
        return dateToStr(date,DATE_FORMAT_HMS);
    }

    public static String getUTCTimeNoColon(Date date){
        return FastDateFormat.getInstance(DATE_TIME_UTC).format(date);
    }

    public static long convertToSecond(int day,int hour,int minute,int second){
        long millisOfDay    = day * DateUtils.MILLIS_PER_DAY;
        long millisOfHour   = hour * DateUtils.MILLIS_PER_HOUR;
        long millisOfMinute = minute * DateUtils.MILLIS_PER_MINUTE;
        long millisOfSecond = second * DateUtils.MILLIS_PER_SECOND;
        return (millisOfDay + millisOfHour + millisOfMinute + millisOfSecond)/1000;
    }

    public static Date addMonth(Date date,int amount){
        return DateUtils.addMonths(date, amount);
    }

    public static Date addDays(Date date,int amount){
        return DateUtils.addDays(date, amount);
    }

    public static Date addHours(Date date,int amount){
        return DateUtils.addHours(date, amount);
    }

    public static Date addMinutes(Date date,int amount){
        return DateUtils.addMinutes(date, amount);
    }

    public static Date strToDate(String stringDate){
        try {
            String[] partternArr = new String[]{
                    DATE_FORMAT_Y,
                    DATE_FORMAT_YM,
                    DATE_FORMAT_YMD,
                    DATE_FORMAT_HM,
                    DATE_FORMAT_HMS
            };
            return DateUtils.parseDate(stringDate, partternArr);
        } catch (Exception e) {
            //ignore
        }
        return null;
    }

    public static String dateToStr(Date date,String parttern) {
        if(date==null){
            return null;
        }
        return DateFormatUtils.format(date, parttern);
    }

    public static String dateToStr(Date date){
        return date==null? null: DateFormatUtils.format(date, DATE_FORMAT_HMS);
    }

    //获取当月最后一天的当前时间
    public static String getLastDayOfCurrentMonth(){
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.MONTH,1);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        return hmsDateFormat.get().format(cal.getTime());
    }

    //获取当月第一天的当前时间
    public static String getFirstDayOfCurrentMonth(){
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return hmsDateFormat.get().format(cal.getTime());
    }

    //获取当月最后一天的最后一秒时间
    public static String getNatureLastDayOfCurrentMonth(){
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.MONTH,1);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return hmsDateFormat.get().format(cal.getTime());
    }

    //获取当月第一天的0点0分0秒
    public static String getNatureFirestDayOfCurrentMonth(){
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return hmsDateFormat.get().format(cal.getTime());
    }

    public static void main(String[] args){
        //验证使用threadLocal和不使用threadLocal存储，多线程下生成日期的区别
        ExecutorService service= Executors.newFixedThreadPool(10);
        service.submit(new Task0());
        service.submit(new Task1());
        service.submit(new Task2());
        service.shutdown();
    }

    private static class Task0 implements Runnable{

        @Override
        public void run() {
            System.out.println("---getLastDayOfCurrentMonth---");
            for(int i=0;i<30;++i){
                System.out.println("getLastDayOfCurrentMonth->"+getLastDayOfCurrentMonth());
            }
        }
    }

    private static class Task1 implements Runnable{

        @Override
        public void run() {
            System.out.println("---getFirstDayOfCurrentMonth---");
            for(int i=0;i<30;++i){
                System.out.println("getFirstDayOfCurrentMonth->"+getFirstDayOfCurrentMonth());
            }
        }
    }

    private static class Task2 implements Runnable{

        @Override
        public void run() {
            System.out.println("---getNatureLastDayOfCurrentMonth---");
            for(int i=0;i<30;++i){
                System.out.println("getNatureLastDayOfCurrentMonth->"+getNatureLastDayOfCurrentMonth());
            }
        }
    }

    private static class Task3 implements Runnable{

        @Override
        public void run() {
            System.out.println("---getLastDayOfCurrentMonth---");
            for(int i=0;i<5;++i){
                System.out.println(getLastDayOfCurrentMonth());
            }
        }
    }
}
