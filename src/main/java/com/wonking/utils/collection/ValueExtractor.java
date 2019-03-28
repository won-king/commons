package com.wonking.utils.collection;

import java.util.Map;

/**
 * Created by wangke18 on 2019/2/26.
 */
public interface ValueExtractor<K,V,S> {
    V extract(Map<K,V> map, K k, S s);
}
