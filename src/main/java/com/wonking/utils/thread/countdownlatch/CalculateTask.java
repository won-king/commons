package com.wonking.utils.thread.countdownlatch;

import com.wonking.utils.thread.ThreadUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by wangke18 on 2018/9/28.
 */
public class CalculateTask implements Runnable {

    private int id;
    private int total;
    private CountDownLatch latch;
    private CountDownLatch startSignal;
    private int start;
    public CalculateTask(CountDownLatch latch,CountDownLatch startSignal, int start, int id){
        this.latch=latch;
        this.startSignal=startSignal;
        this.start=start;
        this.id=id;
    }

    @Override
    public void run() {
        try {
            startSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("task "+id+" started");
        for(int i=start; i<start+1000;++i){
            total+=i;
            ThreadUtil.sleepMills(ThreadLocalRandom.current().nextInt(10));
        }
        latch.countDown();
        System.out.println("task "+id+" completed");
    }

    public int getTotal() {
        return total;
    }
}
