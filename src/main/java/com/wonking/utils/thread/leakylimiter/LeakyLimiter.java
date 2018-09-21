package com.wonking.utils.thread.leakylimiter;

import com.wonking.utils.thread.Invocation;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangke18 on 2018/9/20.
 * 漏桶流控算法
 */
public class LeakyLimiter implements Runnable{
    //滴出速度
    public static final int LEAK_RATE=10;
    //滴出速度的时间单位可以定制，这样可以随意控制流控粒度
    public static final TimeUnit LEAK_RATE_TIMEUNIT=TimeUnit.SECONDS;
    private static final int BUCKET_SIZE=100;

    //这里队列要用volatile，否则会出现这个滴出线程被阻塞的问题
    //原因是，其他线程虽然在不断的滴入，但是由于此队列不是volatile的，写操作只被写入到线程本地内存中
    //并未刷入主内存，而这个滴出线程也是读取的线程本地内存，队列一直是空，也感知不到其他线程的写入
    //导致饿死的饿死，撑死的撑死。即消费线程一直空转，其他线程想插却又插不进，因为队列已经满了
    private volatile LinkedList<Invocation> invocations=new LinkedList<>();

    @Override
    public void run() {
        System.out.println("---new task started---");
        int count=0;
        while (count<LEAK_RATE){
            //这里并不是简单的处理10次(不管实际上有没有poll出来一个请求)就行了，
            //而是要在一个tick时间内不断循环，直到tick结束
            //如果tick还未结束，滴出的请求已经超过最大速率，则进行空转直到tick结束

            //或许思路还可以再换一下
            //整个是一个定时任务，每隔一段时间从队列中取请求滴出，
            // 没取到也不能停止，直到时间超时结束(需要强制shutdown)
            // 这里如果不强制shutdown的话，会出现这样一个问题
            //在某一次tick内，滴出个数一直未超过最大值，但只差一个，就是说这波可能比较闲
            //但是调度任务的调度策略是，如果当前这个任务没有执行完，就不会强制杀死
            //等到某个点，突然一波高峰来了，高峰的第一个请求结束了上一个一直空转的任务
            //但是下一次任务触发必须得等一个tick，这样的话，这波高峰后面的所有请求都被堵在那
            //后面更多的请求会被直接丢弃，这个影响是我们无法接受的
            //这个问题的解决思路有两个
            //1.减小时间粒度，至毫秒甚至纳秒
            //2.每次tick超时必须强制shutdown(这个貌似也不能解决，)
            //如果在一个tick内滴出超过最大速率，则直接退出任务
            //所以这里是写任务逻辑
            //调度由外部实现
            Invocation invocation=invocations.pollFirst();
            if(invocation!=null){
                count++;
                invocation.invoke();
            }else {
                //System.out.println("no invocation in list");
            }
            //ThreadUtil.sleep(LEAK_RATE_TIMEUNIT, 1);
        }
        System.out.println("---out of rate---");
    }

    public void addInvokes(Invocation invocation){
        if(invocations.size() < BUCKET_SIZE){
            invocations.addLast(invocation);
        }else {
            //System.out.println("---请求被丢弃---");
        }
    }

}
