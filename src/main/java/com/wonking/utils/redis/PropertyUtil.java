package com.wonking.utils.redis;

/**
 * Created by wangke18 on 2018/5/18.
 */
public class PropertyUtil {

    public static int getIntOrDefault(Integer i, int def){
        return i!=null? i:def;
    }

    public static long getLongOrDefault(Long l, long def){
        return l!=null? l:def;
    }
}
