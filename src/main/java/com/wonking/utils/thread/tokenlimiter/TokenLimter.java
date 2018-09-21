package com.wonking.utils.thread.tokenlimiter;

import com.wonking.utils.thread.ThreadUtil;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wangke18 on 2018/9/20.
 */
public class TokenLimter implements Runnable{
    //令牌桶滴入速度
    private static final int LEAK_RATE=10;
    //滴入速度的时间单位可以定制，这样可以随意控制流控粒度
    private static final TimeUnit LEAK_RATE_TIMEUNIT=TimeUnit.MILLISECONDS;
    private static final int BUCKET_SIZE=100;
    private static volatile AtomicInteger supply=new AtomicInteger(0);
    private AtomicInteger available=new AtomicInteger(BUCKET_SIZE);
    private Semaphore semaphore=new Semaphore(available.get());

    @Override
    public void run() {
        while (true){
            //这里天然不会出现超出桶容量的情况，只会出现补不足的情况，就是应该补的比实际补的量少
            //但实际运行时发现，大约只有1/40的概率发生，而且每次只会少补一个
            //而且对比下面那种思路，这种思路补的速率更加精确
            //这个例子说明，越简单的思路，可能往往越高效
            //把事情复杂化往往会得不偿失，甚至事与愿违

            //还有，这里没必要为了实现完全精确的补充而大费周章的进行加锁等操作(事实上要想精确的补充只有加锁一个方法)
            //这就涉及到性能和准确率的权衡问题，你更想要照顾到哪一方
            //这里我们选择性能，因为我们可以容忍准确率有一些误差，这些误差不是致命的，甚至是无关痛痒的
            int toSupply=BUCKET_SIZE-semaphore.availablePermits();
            if(toSupply>LEAK_RATE){
                //System.out.println("full supply");
                semaphore.release(LEAK_RATE);
            }else if(toSupply>0){
                //System.out.println("supply->"+toSupply);
                semaphore.release(toSupply);
                //System.out.println("available->"+semaphore.availablePermits());
            }
            //这种思路是，每次需要补充的数量不再实时计算，而是用一个volatile变量存储下一次需要补充的数量
            //由其他线程来修改这个变量
            //下一次补充的时候，读取变量，如果超过最大速率，则只补充到最大值，否则补充变量的值
            //实践证明，这种方式补充的精确度比上一种更差
            /*int toSupply=supply.get();
            if(toSupply>LEAK_RATE){
                //System.out.println("full supply");
                semaphore.release(LEAK_RATE);
            }else if(toSupply>0){
                //System.out.println("supply->"+toSupply);
                semaphore.release(toSupply);
                System.out.println("available->"+semaphore.availablePermits());
            }
            supply.getAndSet(0);*/
            ThreadUtil.sleep(LEAK_RATE_TIMEUNIT, 1);
        }
    }

    public boolean acquire(int limits){
        //System.out.println("available->"+semaphore.availablePermits());
        boolean result=semaphore.tryAcquire(limits);
        //supply.addAndGet(limits);
        return result;

    }

    public void release(int limits){
        semaphore.release(limits);
    }
}
