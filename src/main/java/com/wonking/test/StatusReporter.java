package com.wonking.test;

import com.wonking.utils.thread.ThreadUtil;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangke18 on 2018/11/1.
 */
public class StatusReporter implements Runnable {
    private FuzzyObj obj;

    /*public StatusReporter(FuzzyObj obj) {
        this.obj = obj;
    }*/

    @Override
    public void run() {
        try{
            while (true){
                if(Thread.interrupted()){
                    break;
                }
                FuzzyObj obj=FuzzyObj.instance();
                if(obj.isSignal()){
                    System.out.println(obj.isSignal()==true);
                }
                ThreadLocalRandom random=ThreadLocalRandom.current();
                TimeUnit.SECONDS.sleep(random.nextInt(5));
            }
        }catch (InterruptedException e){
            //ignore
        }
    }
}
