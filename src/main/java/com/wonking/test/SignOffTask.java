package com.wonking.test;

import com.wonking.utils.thread.ThreadUtil;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangke18 on 2018/11/1.
 */
public class SignOffTask implements Runnable{
    private FuzzyObj obj;

    /*public SignOffTask(FuzzyObj obj) {
        this.obj = obj;
    }*/

    @Override
    public void run() {
        try{
            while (true){
                if(Thread.interrupted()){
                    break;
                }
                FuzzyObj.instance().signOff();
                ThreadLocalRandom random=ThreadLocalRandom.current();
                TimeUnit.MILLISECONDS.sleep(random.nextInt(0));
            }
        }catch (InterruptedException e){
            //ignore
        }
    }
}
