package com.wonking.utils.thread.leakylimiter;

import java.util.concurrent.*;

/**
 * Created by wangke18 on 2018/9/21.
 */
public class LeakyApp {
    public static void main(String[] args) {
        ScheduledExecutorService service= Executors.newScheduledThreadPool(1);
        LeakyLimiter limiter=new LeakyLimiter();
        service.scheduleAtFixedRate(limiter, 0,
                LeakyLimiter.TICK_OF_NANOS, TimeUnit.NANOSECONDS);
        new Thread(limiter).start();
        for(int i=0;i<10;++i){
            new Thread(new LeakyClient(limiter)).start();
        }
    }
}
