package com.wonking.utils.thread;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wangke18 on 2018/9/21.
 */
public class VolatileTest {
    public static void main(String[] args) {
        Holder holder=new Holder();
        for(int i=0;i<100;++i){
            new Thread(new AddTask(holder)).start();
        }
        ThreadUtil.sleep(3);
        for(int i=1;i<=10000;++i){
            if(!holder.set.contains(i)){
                System.out.println("missing->"+i);
            }
        }
        //System.out.println("set size->"+holder.set.size());
    }


    private static class Holder{
        public volatile AtomicInteger counter=new AtomicInteger(0);
        public Set<Integer> set=new HashSet<>(10000);

        public int increaceAndGet(){
            set.add(counter.incrementAndGet());
            return counter.get();
        }

    }

    private static class AddTask implements Runnable{

        private Holder holder;

        public AddTask(Holder holder) {
            this.holder = holder;
        }

        @Override
        public void run() {
            int t=100;
            while (t-->0){
                System.out.println(holder.increaceAndGet());
            }
        }
    }
}
