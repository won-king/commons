package com.wonking.utils.threadlocal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wangke18 on 2018/5/9.
 */
public class ThreadLocalApp {
    public static void main(String[] args) {
        ExecutorService service= Executors.newFixedThreadPool(10);
        final String base="JDer";
        for(int i=0;i<10;++i){
            service.submit(Task.generateTask());
        }
        service.shutdown();
    }

}
