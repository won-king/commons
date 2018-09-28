package com.wonking.utils.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangke18 on 2018/5/22.
 */
public class ThreadUtil {
    private static final int MAX_THREAD_CONCURRENCY = 10;
    private ExecutorService executor;

    private ThreadUtil(ExecutorService e) {
        this.executor = e;
    }

    public static ThreadUtil getSingleExecutor() {
        return new ThreadUtil(Executors.newSingleThreadExecutor());
    }

    public static ThreadUtil getFixedExecutor() {
        return new ThreadUtil(Executors.newFixedThreadPool(MAX_THREAD_CONCURRENCY));
    }

    public static ThreadUtil getFixedExecutor(int threads) {
        return new ThreadUtil(Executors.newFixedThreadPool(threads));
    }

    public void submitTask(Runnable r) {
        executor.submit(r);
    }

    public void shutdown() {
        executor.shutdown();
    }
    public void shutdownNow(){
        executor.shutdownNow();
    }

    public static void sleep(int second) {
        try {
            Thread.sleep(second * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sleepMills(int millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //只支持到秒级的睡眠
    public static void sleep(TimeUnit timeUnit, int time) {
        try {
            if (TimeUnit.NANOSECONDS.equals(timeUnit)) {
                TimeUnit.NANOSECONDS.sleep(time);
            }else  if(TimeUnit.MILLISECONDS.equals(timeUnit)){
                TimeUnit.MILLISECONDS.sleep(time);
            }else {
                TimeUnit.SECONDS.sleep(time);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
