package com.wonking.utils.thread.counterlimiter;

import com.wonking.utils.thread.Invocation;
import com.wonking.utils.thread.ThreadUtil;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wangke18 on 2018/9/19.
 * 计数器方式的限流器，主要思想如下：
 * 1.用一个变量记住调用次数，每调用一次，counter+1
 * 2.每隔一个tick，将当前的调用次数写入队列
 * 3.写完之后做检查，如果队列长度超过10，则说明时间窗口已满载
 * 4.这时候就要检查窗口内积累的请求量是否超过了阈值
 * 5.计算窗口内积累的请求量的方法就是，用队列的尾部的值-队列的头部的值，就是在时间窗口内的积累量
 * 6.如果超过阈值，则进行限流策略
 *
 * 分析：这种算法存在很大缺陷
 * 无法阻止瞬间高并发的发生，因为它可能在一个tick之后，才能发现高并发
 * 这时候巨量的请求已经涌入系统，等它发现并做出反应为时已晚
 *
 * 律爷说：这个算法可能是配合其他限流算法来进行限流的
 * 我：一开始认为他说的有道理，就下意识的以为，这个算法是用来发现高并发的，发现了之后再采用其他限流算法进行限流
 *    但是仔细一想，不对呀，如果他是用来发现高并发的，那说明高并发实际上还是已经发生了，还是不能阻止他的发生呀
 */
public class CounterLimiter implements Runnable{
    private static final int QPS=100;
    private static final int WINDOW_SIZE=5;
    private AtomicInteger counter=new AtomicInteger(0);
    private LinkedList<Integer> list=new LinkedList<>();
    private Semaphore semaphore=new Semaphore(1);

    @Override
    public void run() {
        while(true){
            list.addLast(counter.get());
            for(Integer i:list){
                System.out.print(i+" | ");
            }
            System.out.println();
            if(list.size()>WINDOW_SIZE){
                list.removeFirst();
            }
            if(list.peekLast().longValue() -list.peekFirst().longValue() > QPS){
                System.out.println("---熔断器被触发---");
            }
            ThreadUtil.sleepMills(1000/WINDOW_SIZE);
        }
    }

    public void invoke(Invocation invocation, Object... args){
        invocation.invoke(args);
        counter.incrementAndGet();
        //System.out.println("invoke time->"+counter.incrementAndGet());
    }

    public boolean acquire(){
        try {
            return semaphore.tryAcquire(10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void release(){
        semaphore.release();
    }
}
