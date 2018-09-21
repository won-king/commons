package com.wonking.utils.thread.tokenlimiter;

import com.wonking.utils.thread.Invocation;
import com.wonking.utils.thread.ThreadUtil;
import com.wonking.utils.thread.TimeHandler;
import com.wonking.utils.thread.counterlimiter.CounterLimiter;
import com.wonking.utils.thread.tokenlimiter.TokenLimter;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by wangke18 on 2018/9/20.
 */
public class GetTimeTask implements Runnable{
    private Invocation invocation=new TimeHandler();
    private CounterLimiter limiter;
    private TokenLimter tokenLimter;
    public GetTimeTask(CounterLimiter limiter){
        //invocation=new TimeHandler();
        this.limiter=limiter;
    }
    public GetTimeTask(TokenLimter limter){
        tokenLimter=limter;
    }

    @Override
    public void run() {
        while(true){
            //ThreadUtil.sleepMills(ThreadLocalRandom.current().nextInt(1));
            if(tokenLimter.acquire(1)){
                invocation.invoke();
                ThreadUtil.sleep(ThreadLocalRandom.current().nextInt(2));
                //tokenLimter.release(1);
            }else {
                System.out.println("---请求被丢弃---");
            }
        }
    }
}
