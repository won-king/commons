package com.wonking.utils.threadlocal;

/**
 * Created by wangke18 on 2018/5/9.
 */
public class Task implements Runnable {
    private static int count=0;
    private static final String base="JDer";

    private String name;

    private Task(){
        name=base+count;
        count++;
    }

    @Override
    public void run() {
        System.out.println("---"+name+"---");
        System.out.println("before add->"+TestThreadLocal.getUser());
        TestThreadLocal.addUser(name);
        for(int i=0;i<2;++i){
            System.out.println(name+"->"+TestThreadLocal.getUser());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Task generateTask(){
        return new Task();
    }

}
