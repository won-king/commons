package com.wonking.test;

import com.wonking.utils.thread.ThreadUtil;

import java.nio.channels.ServerSocketChannel;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangke18 on 2018/11/1.
 */
public class TestApp {
    public static final String ARG_STOP="stop";
    public static final String ARG_START="start";

    public static final int SYSTEM_STATUS_STOPPED=0;
    public static final int SYSTEM_STATUS_STARTED=1;
    public static final int SYSTEM_STATUS_STOPPING=2;
    public static final int SYSTEM_STATUS_STARTING=3;

    private static ThreadUtil threadUtil=ThreadUtil.getFixedExecutor(3);
    private static volatile int status=SYSTEM_STATUS_STOPPED;

    public static void main(String[] args) {
        /*ThreadUtil threadUtil=ThreadUtil.getFixedExecutor(3);
        FuzzyObj obj=FuzzyObj.instance();
        threadUtil.submitTask(new SignOnTask());
        threadUtil.submitTask(new SignOffTask());
        threadUtil.submitTask(new StatusReporter());
        ThreadUtil.sleep(3);
        threadUtil.shutdownNow();*/
        ThreadUtil threadUtil=ThreadUtil.getFixedExecutor(1);
        threadUtil.submitTask(new TimeTicker());
        Thread daemonThread=new Thread(new Server(threadUtil));
        daemonThread.setDaemon(true);
        daemonThread.start();
    }

    private static void start(){
        threadUtil.submitTask(new SignOnTask());
        threadUtil.submitTask(new SignOffTask());
        threadUtil.submitTask(new StatusReporter());
    }

    private static void stop(){
        threadUtil.shutdownNow();
    }
}
