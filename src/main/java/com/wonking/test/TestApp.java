package com.wonking.test;

import com.wonking.utils.thread.ThreadUtil;

import java.nio.channels.ServerSocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangke18 on 2018/11/1.
 */
public class TestApp {
    public static final String ARG_STOP="stop";
    public static final String ARG_START="start";

    public static final int SYSTEM_STATUS_STOPPED=0;
    public static final int SYSTEM_STATUS_STARTED=1;
    public static final int SYSTEM_STATUS_STOPPING=2;
    public static final int SYSTEM_STATUS_STARTING=3;

    private static ThreadUtil threadUtil=ThreadUtil.getFixedExecutor(3);
    private static volatile int status=SYSTEM_STATUS_STOPPED;

    public static void main(String[] args) {
        ThreadUtil threadUtil=ThreadUtil.getFixedExecutor(3);
        FuzzyObj obj=FuzzyObj.instance();
        threadUtil.submitTask(new SignOnTask());
        threadUtil.submitTask(new SignOffTask());
        threadUtil.submitTask(new StatusReporter());
        ThreadUtil.sleep(3);
        threadUtil.shutdownNow();
        /*ThreadUtil threadUtil=ThreadUtil.getFixedExecutor(1);
        threadUtil.submitTask(new TimeTicker());
        Thread daemonThread=new Thread(new Server(threadUtil));
        daemonThread.setDaemon(true);
        daemonThread.start();*/
        //double[] ints=new double[]{1,2,3,4,5,6,7};
        /*LinkedList<Double> list=new LinkedList<>();
        list.add(101.0);
        list.add(2567.0);
        list.add(315.0);
        list.add(476.0);
        list.add(585.0);
        list.add(666.0);
        list.add(707.0);
        int time=200;
        testRegress(list, time);*/
        //System.out.println(System.currentTimeMillis());
    }

    //测试给定一组初始值，求其平均值，并将其插入到队尾，挤掉队头，依次循环，
    // 验证最终队列中的所有的值，都趋向于一个固定值，此值与队列的初始值有关
    private static void testRegress(LinkedList<Double> list, int time){
        for(int i=time; i>0; --i){
            double avg=calculate(list);
            System.out.println(avg);
            list.removeFirst();
            list.addLast(avg);
        }
    }

    private static double calculate(LinkedList<Double> list){
        double sum=0;
        int size=list.size();
        Iterator<Double> iterator=list.iterator();
        while (iterator.hasNext()){
            sum+=iterator.next();
        }
        return sum/size;
    }

    private static void start(){
        threadUtil.submitTask(new SignOnTask());
        threadUtil.submitTask(new SignOffTask());
        threadUtil.submitTask(new StatusReporter());
    }

    private static void stop(){
        threadUtil.shutdownNow();
    }
}
