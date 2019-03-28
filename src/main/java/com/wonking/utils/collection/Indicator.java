package com.wonking.utils.collection;

/**
 * Created by wangke18 on 2019/3/1.
 */
public interface Indicator<K,O> {
    K initK();
    boolean isExpected(K k,O o);
    K nextK(K last);
}
