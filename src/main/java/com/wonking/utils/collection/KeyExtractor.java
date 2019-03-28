package com.wonking.utils.collection;

/**
 * Created by wangke18 on 2019/2/26.
 */
public interface KeyExtractor<K,V> {
    V extract(K k);
}
