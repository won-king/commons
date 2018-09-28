package com.wonking.utils.thread.leakylimiter;

import com.wonking.utils.thread.Invocation;
import com.wonking.utils.thread.TimeHandler;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangke18 on 2018/9/21.
 * 模拟客户端请求任务，请求进入漏桶
 */
public class LeakyClient implements Runnable {
    private int id;
    private LeakyLimiter limiter;
    private Invocation invocation=new TimeHandler();

    public LeakyClient(LeakyLimiter limiter, int id){
        this.limiter=limiter;
        this.id=id;
    }

    @Override
    public void run() {
        while (true){
            //设置中断信号时，线程可能正在运行，也可能正在睡眠(每次睡眠时间越短，在运行时被打断的几率越高)
            //如果想让线程在收到中断信号后，执行完最后一次任务，就把下面这段运行时中断的判断注释掉
            if(Thread.interrupted()){
                System.out.println("运行时中断");
                break;
            }
            limiter.addInvokes(invocation);
            try {
                TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(2));
            } catch (InterruptedException e) {
                System.out.println("睡眠中断");
                break;
            }
            //这里不能用这个睡眠工具，它会吃掉中断信号，导致任务无限运行下去
            //ThreadUtil.sleep(ThreadLocalRandom.current().nextInt(10));
        }
        System.out.println("task "+id+" finished");
    }
}
