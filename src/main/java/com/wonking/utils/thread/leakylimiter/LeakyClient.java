package com.wonking.utils.thread.leakylimiter;

import com.wonking.utils.thread.Invocation;
import com.wonking.utils.thread.ThreadUtil;
import com.wonking.utils.thread.TimeHandler;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by wangke18 on 2018/9/21.
 */
public class LeakyClient implements Runnable {
    private LeakyLimiter limiter;
    private Invocation invocation=new TimeHandler();

    public LeakyClient(LeakyLimiter limiter){
        this.limiter=limiter;
    }

    @Override
    public void run() {
        while (true){
            limiter.addInvokes(invocation);
            ThreadUtil.sleep(ThreadLocalRandom.current().nextInt(10));
        }
    }
}
