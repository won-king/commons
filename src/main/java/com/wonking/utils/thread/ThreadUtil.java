package com.wonking.utils.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wangke18 on 2018/5/22.
 */
public class ThreadUtil {
    private static final int MAX_THREAD_CONCURRENCY=10;
    private ExecutorService executor;

    private ThreadUtil(ExecutorService e){
        this.executor=e;
    }

    public static ThreadUtil getSingleExecutor(){
        return new ThreadUtil(Executors.newSingleThreadExecutor());
    }

    public static ThreadUtil getFixedExecutor(){
        return new ThreadUtil(Executors.newFixedThreadPool(MAX_THREAD_CONCURRENCY));
    }

    public static ThreadUtil getFixedExecutor(int threads){
        return new ThreadUtil(Executors.newFixedThreadPool(threads));
    }

    public void submitTask(Runnable r){
        executor.submit(r);
    }

    public void shutdown(){
        executor.shutdown();
    }

    public static void sleep(int second){
        try {
            Thread.sleep(second*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sleepMills(int millisecond){
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
