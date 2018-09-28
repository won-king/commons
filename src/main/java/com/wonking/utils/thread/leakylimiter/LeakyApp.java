package com.wonking.utils.thread.leakylimiter;

import com.wonking.utils.thread.ThreadUtil;

import java.util.concurrent.*;

/**
 * Created by wangke18 on 2018/9/21.
 * 漏桶算法测试
 */
public class LeakyApp {
    public static void main(String[] args) {
        ScheduledExecutorService service= Executors.newScheduledThreadPool(1);
        LeakyLimiter limiter=new LeakyLimiter();
        service.scheduleAtFixedRate(limiter, 0,
                LeakyLimiter.TICK_OF_NANOS, TimeUnit.NANOSECONDS);
        int taskNum=10;
        ThreadUtil threadUtil=ThreadUtil.getFixedExecutor(taskNum);
        for(int i=0;i<taskNum;++i){
            threadUtil.submitTask(new LeakyClient(limiter, i));
        }
        ThreadUtil.sleep(5);
        threadUtil.shutdownNow();
        service.shutdown();
        System.out.println("stop successfully");
        System.out.println("remaining request->"+limiter.remainingInvokes());
    }
}
