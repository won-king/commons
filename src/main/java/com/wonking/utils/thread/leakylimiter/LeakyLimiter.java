package com.wonking.utils.thread.leakylimiter;

import com.wonking.utils.thread.Invocation;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangke18 on 2018/9/20.
 * 漏桶流控算法
 */
public class LeakyLimiter implements Runnable{
    //滴出速度
    private static final int LEAK_RATE=3;
    //滴出速度的时间单位可以定制，这样可以随意控制流控粒度
    private static final TimeUnit LEAK_RATE_TIMEUNIT=TimeUnit.SECONDS;

    public static final long TICK_OF_NANOS=LEAK_RATE_TIMEUNIT.toNanos(1)/LEAK_RATE;
    private static final int BUCKET_SIZE=100;

    //这里队列要用volatile，否则会出现这个滴出线程被阻塞的问题
    //原因是，其他线程虽然在不断的滴入，但是由于此队列不是volatile的，写操作只被写入到线程本地内存中
    //并未刷入主内存，而这个滴出线程也是读取的线程本地内存，队列一直是空，也感知不到其他线程的写入
    //导致饿死的饿死，撑死的撑死。即消费线程一直空转，其他线程想插却又插不进，因为队列已经满了

    //还发现一个更严重的问题，就是有时候队列的size明明大于0，但poll出来的就是空的
    //这说明LinkedList数据结构不适用于并发数据结构，所以改用下面的ConcurrentLinkedQueue
    private volatile LinkedList<Invocation> invocations=new LinkedList<>();
    private volatile ConcurrentLinkedQueue<Invocation> queue=new ConcurrentLinkedQueue<>();
    private volatile BlockingQueue<Invocation> blockingQueue=new LinkedBlockingQueue<>(BUCKET_SIZE);

    @Override
    public void run() {
        taskV3();
        //System.out.println("bucket size->"+invocations.size());
        //System.out.println("bucket size->"+queue.size());
        System.out.println("bucket size->"+blockingQueue.size());
    }

    public int remainingInvokes(){
        return blockingQueue.size();
    }

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
    private void taskV2(){
        System.out.println("---new task started---");
        int count=0;
        while (count<LEAK_RATE){
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

    //V3.这里想到更好的一个思路
    //无论速度是多少，我们一律把他倒过来，最后把速度表示为，t纳秒/个
    //这样的话，这里的逻辑就是每次滴出一个，调度任务是每t纳秒调度一次
    //这样就非常直观了，也非常符合漏桶固定滴出速度这个模型
    //这种做法的唯一缺陷就是，因为1s=10亿ns，所以最大支持10亿/秒的并发量
    //考虑到实际应用，单台机器不可能超过这个限制，所以这个缺陷也算不上真正的缺陷了

    //经实践证明，这种思路的控制非常精确
    //这给我一个启示就是，如果大家对某一个量有各自不同的描述，现要求我们根据不同的输入来精确的控制这个量
    //最好的做法就是，把这个量转化为系统允许的最细粒度的一个统一表示法，在此基础上进行统一处理
    //此结论另一个最好的证据就是电商领域对交易金额的处理，金额一律用人民币的最小单位-分，来表示
    private void taskV3(){
        //Invocation invocation=invocations.pollFirst();
        //Invocation invocation=queue.poll();
        Invocation invocation=blockingQueue.poll();
        if(invocation!=null){
            invocation.invoke();
        }else {
            System.out.println("当前无请求");
        }

    }

    //请求入队列，前后共考虑了三种数据结构，最后采用BlockingQueue
    //其好处是，FIFO特性保证公平，自带容量限制，不需要自己判断有没有超过容量
    public void addInvokes(Invocation invocation){
        addToBlockingQueue(invocation);
    }

    private void addToLinkedList(Invocation invocation){
        if(invocations.size() < BUCKET_SIZE){
            invocations.addLast(invocation);
        }else {
            //实际应用中，这里应该提供一个快速失败响应
            System.out.println("---请求被丢弃---");
        }
    }
    private void addToConcurrentLinkedQueue(Invocation invocation){
        if(queue.size() < BUCKET_SIZE){
            queue.offer(invocation);
        }else {
            //ignore
        }
    }

    private void addToBlockingQueue(Invocation invocation){
        if(!blockingQueue.offer(invocation)){
            System.out.println("---请求被丢弃---");
        }
    }

}
