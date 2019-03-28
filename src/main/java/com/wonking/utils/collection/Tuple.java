package com.wonking.utils.collection;

/**
 * Created by wangke18 on 2018/5/25.
 */
public class Tuple<F,S> {
    public final F f;
    public final S s;

    private Tuple(F f, S s){
        this.f=f;
        this.s=s;
    }

    public static <F,S> Tuple<F,S> tuple(F f, S s){
        return new Tuple<>(f,s);
    }
}
