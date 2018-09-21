package com.wonking.utils.thread;

import com.wonking.utils.thread.counterlimiter.CounterLimiter;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by wangke18 on 2018/9/19.
 */
public class SemaphoreDemo extends Thread{
    /*private static final int QPS=100;
    private volatile long lastVisit;
    private Semaphore semaphore=new Semaphore(5);

    public boolean acquire(){
        long now=System.currentTimeMillis();
        if(now-lastVisit >= QPS){
            lastVisit=now;
            return true;
        }
        return false;
    }*/

    public static void main(String[] args) {
        CounterLimiter limiter=new CounterLimiter();
        new Thread(limiter).start();
        for(int i=0;i<10;++i){
            new Thread(new GetTimeTask(limiter)).start();
        }
    }

    private static class GetTimeTask implements Runnable{
        public Invocation invocation;
        private CounterLimiter limiter;
        public GetTimeTask(CounterLimiter limiter){
            invocation=new TimeHandler();
            this.limiter=limiter;
        }

        @Override
        public void run() {
            while(true){
                ThreadUtil.sleepMills(ThreadLocalRandom.current().nextInt(175));
                limiter.invoke(invocation);
            }
        }
    }

}
