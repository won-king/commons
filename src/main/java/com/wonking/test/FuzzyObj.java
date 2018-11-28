package com.wonking.test;

/**
 * Created by wangke18 on 2018/11/1.
 */
public class FuzzyObj {
    private boolean signal=false;
    private static final FuzzyObj instance=new FuzzyObj();

    public static FuzzyObj instance(){
        return instance;
    }

    public void signOn(){
        signal=true;
    }

    public void signOff(){
        signal=false;
    }

    public boolean isSignal() {
        return signal;
    }
}
