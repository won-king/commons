package com.wonking.utils.thread.countdownlatch;

import com.wonking.utils.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by wangke18 on 2018/9/28.
 */
public class CountDownApp {
    public static void main(String[] args) {
        int taskNum=5;
        CountDownLatch latch=new CountDownLatch(taskNum);
        CountDownLatch startSignal=new CountDownLatch(1);
        List<CalculateTask> taskList=new ArrayList<>(taskNum);
        for(int i=0;i<taskNum;++i){
            CalculateTask task=new CalculateTask(latch, startSignal, i*1000, i);
            taskList.add(task);
            new Thread(task).start();
        }
        System.out.println("calculator will start after 3 seconds");
        ThreadUtil.sleep(3);
        startSignal.countDown();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int total=0;
        for(CalculateTask task:taskList){
            total+=task.getTotal();
        }
        System.out.println(total);
    }
}
