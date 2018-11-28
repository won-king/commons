package com.wonking.test;

import com.wonking.utils.thread.ThreadUtil;

/**
 * Created by wangke18 on 2018/11/14.
 */
public class ThreadManager {
    private static final ThreadUtil threadUtil=ThreadUtil.getFixedExecutor(1);

    public static void submintTask(Runnable runnable){
        threadUtil.submitTask(runnable);
    }

    public static void shutdown(){
        threadUtil.shutdownNow();
    }
}
