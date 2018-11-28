package com.wonking.test;

import com.wonking.utils.date.DateUtil;

import java.util.concurrent.TimeUnit;

/**
 * Created by wangke18 on 2018/11/15.
 */
public class TimeTicker implements Runnable{

    @Override
    public void run() {
        while (true){
            if(Thread.interrupted()){
                System.out.println("system will stop because interrupted");
                break;
            }
            System.out.println(DateUtil.getDateTime());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                System.out.println("system will stop when sleeping");
                break;
            }
        }
    }
}
