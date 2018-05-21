package com.wonking.utils.threadlocal;

/**
 * Created by wangke18 on 2018/5/9.
 */
public class TestThreadLocal {
    private static final ThreadLocal<String> user=new ThreadLocal<>();

    public static String getUser(){
        return user.get();
    }

    public static void addUser(String s){
        user.set(s);
    }
}
